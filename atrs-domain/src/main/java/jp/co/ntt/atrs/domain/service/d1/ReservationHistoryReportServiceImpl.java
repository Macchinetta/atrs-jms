/*
 * Copyright(c) 2014-2017 NTT Corporation.
 */
package jp.co.ntt.atrs.domain.service.d1;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import jp.co.ntt.atrs.domain.common.logging.LogMessages;
import jp.co.ntt.atrs.domain.repository.reservation.ReservationHistoryDto;
import jp.co.ntt.atrs.domain.repository.reservation.ReservationRepository;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.terasoluna.gfw.common.date.jodatime.JodaTimeDateFactory;
import org.terasoluna.gfw.common.exception.SystemException;

/**
 * 履歴レポート作成サービス
 * @author NTT 電電次郎
 */
@Service
public class ReservationHistoryReportServiceImpl implements
                                                ReservationHistoryReportService {

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(ReservationHistoryReportServiceImpl.class);

    /**
     * レポート名接頭文字列
     */
    private static final String REPORT_NAME_PREFIX = "atrs_report_";

    /**
     * 出力中のファイル名に使用する接頭文字列
     */
    private static final String TMP_REPORT_NAME_PREFIX = "tmp_";

    /**
     * 改行文字列
     */
    private static final String LINE_SEPARATOR = "\r\n";

    /**
     * レポートの見出し文字列
     */
    private static final String REPORT_HEADER = String.format(
            "\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"", "Reserve No",
            "Reservation Date", "Total Fare", "Reservation Flight Date",
            "Reservation Flight No", "Reservation Flight Name");

    /**
     * JmsMessageTemplate
     */
    @Inject
    JmsMessagingTemplate jmsMessagingTemplate;

    /**
     * 予約リポジトリ
     */
    @Inject
    ReservationRepository reservationRepository;

    /**
     * レポートを格納するディレクトリパス
     */
    @Value("${report.dir}")
    Path reportDirPath;

    /**
     * 作成中のレポートを暫定的に格納するディレクトリパス
     */
    @Value("${report.dir}/tmp")
    Path tmpReportDirPath;

    /**
     * システム日付取得用インターフェース。
     */
    @Inject
    JodaTimeDateFactory dateFactory;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional("jmsSendTransactionManager")
    public void sendRequest(String customerNo) {
        this.jmsMessagingTemplate.convertAndSend(
                "jms/queue/ReservationHistoryReportRequestQueue", customerNo);
    }

    /**
     * レポートを作成する。
     * <p>
     * 予約履歴を取得し、それを暫定CSVファイルに出力する。出力完了後にCSVファイルを正式名称に改名する。 ファイル名は、特定の接頭文字列、顧客番号、作成時刻をタイムスタンプ(現年月日時分秒)で連結した形式である。
     * </p>
     * @param criteria レポート作成条件保持オブジェクト
     */
    @Override
    public void createReport(ReservationHistoryReportCriteriaDto criteria) {

        // 予約履歴情報の取得
        List<ReservationHistoryDto> reservationHistoryList = this.reservationRepository
                .findAllByCustomerNoForReport(criteria.getCustomerNo());
        // レポート出力
        generateReport(criteria.getCustomerNo(), reservationHistoryList);

    }

    /**
     * 当該顧客のレポート名一覧を降順で取得する。
     * @param customerNo 顧客番号
     * @return レポート名リスト
     */
    @Override
    public List<String> getExistingReportNameList(String customerNo) {

        Path customerDirPath = this.reportDirPath.resolve(customerNo);

        if (!customerDirPath.toFile().exists()) {
            return Collections.emptyList();
        }

        List<String> existingReportNameList = new ArrayList<>();

        try (DirectoryStream<Path> dirStream = Files
                .newDirectoryStream(customerDirPath)) {

            for (Path path : dirStream) {
                existingReportNameList.add(path.getFileName().toString());
            }

        } catch (IOException e) {
            throw new SystemException(LogMessages.E_AR_D1_L0001.getCode(), LogMessages.E_AR_D1_L0001
                    .getMessage(customerNo), e);
        }

        if (existingReportNameList.isEmpty()) {
            return existingReportNameList;
        }

        existingReportNameList.sort(Collections.reverseOrder());

        return existingReportNameList;

    }

    /**
     * 指定されたファイルが既存するかチェックする。
     * @param customerNo 顧客番号
     * @param reportName チェック対象のファイル名
     * @return ファイルが既存する場合に <code>true</code>、既存しない場合<code>false</<code>
     */
    private boolean checkReportExistence(String customerNo, String reportName) {

        if (!StringUtils.hasText(reportName)) {
            return false;
        }

        Path customerDirPath = this.reportDirPath.resolve(customerNo);

        if (!customerDirPath.toFile().exists()) {
            return false;
        }

        try (DirectoryStream<Path> dirStream = Files
                .newDirectoryStream(customerDirPath)) {
            for (Path path : dirStream) {
                if (reportName.equals(path.getFileName().toString())) {
                    return true;
                }
            }
        } catch (IOException e) {
            throw new SystemException(LogMessages.E_AR_D1_L0001.getCode(), LogMessages.E_AR_D1_L0001
                    .getMessage(customerNo), e);
        }
        return false;
    }

    /**
     * レポートを生成する。
     * @param customerNo 顧客番号
     * @param reservationHistoryList 予約履歴リスト
     */
    private void generateReport(String customerNo,
            List<ReservationHistoryDto> reservationHistoryList) {
        // 正式レポートファイル名
        String finalReportFileName = generateReportFileName(customerNo);

        // システム共通の暫定レポートファイル名
        String tmpReportFileName = TMP_REPORT_NAME_PREFIX + finalReportFileName;
        Path tmpReportFilePath = this.tmpReportDirPath
                .resolve(tmpReportFileName);

        try {
            // 暫定レポートファイルへの出力
            try (Writer csvWriter = Files.newBufferedWriter(tmpReportFilePath)) {
                csvWriter.write(REPORT_HEADER);
                csvWriter.write(LINE_SEPARATOR);
                for (ReservationHistoryDto reservationHistory : reservationHistoryList) {
                    csvWriter.write(reservationHistory.toCsvLineString());
                    csvWriter.write(LINE_SEPARATOR);
                }
            }

            // ディレクトリは顧客別に設ける
            Path finalReportDirPath = this.reportDirPath.resolve(customerNo);
            createDirectories(finalReportDirPath);
            Path finalReportFilePath = finalReportDirPath
                    .resolve(finalReportFileName);
            // 暫定ファイルから正式ファイルへの変換移動
            Files.move(tmpReportFilePath, finalReportFilePath);

        } catch (IOException e) {
            throw new SystemException(LogMessages.E_AR_D1_L0002.getCode(), LogMessages.E_AR_D1_L0002
                    .getMessage(customerNo), e);
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("report created : " + finalReportFileName);
        }

    }

    /**
     * レポート名を生成する。
     * @param customerNo レポート作成情報保持オブジェクト
     * @return レポート名
     */
    private String generateReportFileName(String customerNo) {
        DateTime dateTime = this.dateFactory.newDateTime();
        return REPORT_NAME_PREFIX + customerNo + "_"
                + dateTime.toString("yyyyMMddHHmmss") + ".csv";
    }

    /**
     * 作成した予約履歴レポートを格納するディレクトリを準備する。
     */
    @PostConstruct
    public void createReportDirectories() {
        // ディレクトリ準備
        createDirectories(this.reportDirPath);
        createDirectories(this.tmpReportDirPath);
    }

    /**
     * ディレクトリを用意する。
     * @param dirPath 対象のディレクトリパス
     */
    private void createDirectories(Path dirPath) {
        if (Files.isDirectory(dirPath)) {
            return;
        }
        try {
            Files.createDirectories(dirPath);
        } catch (IOException e) {
            throw new SystemException(LogMessages.E_AR_D1_L0003.getCode(), LogMessages.E_AR_D1_L0003
                    .getMessage(dirPath), e);
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("directory created : " + dirPath);
        }
    }

    /**
     * 顧客番号に紐づくファイルのパスを取得する。
     * @param customerNo 顧客番号
     * @param reportName ファイル名
     * @return ファイルパス
     */
    public Path getReportFilePath(String customerNo, String reportName) {
        if (!checkReportExistence(customerNo, reportName)) {
            return null;
        }
        return this.reportDirPath.resolve(customerNo).resolve(reportName);
    }

}