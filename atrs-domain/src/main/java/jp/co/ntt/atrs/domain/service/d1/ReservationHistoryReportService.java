/*
 * Copyright(c) 2014-2017 NTT Corporation.
 */
package jp.co.ntt.atrs.domain.service.d1;

import java.nio.file.Path;
import java.util.List;

/**
 * 履歴レポート作成サービス
 * @author NTT 電電次郎
 */
public interface ReservationHistoryReportService {

    /**
     * 指定の顧客の予約履歴のレポート作成を電文送付して要求する。
     * @param customerNo 対象の顧客番号
     */
    void sendRequest(String customerNo);

    /**
     * レポートを作成する。
     * @param criteria レポート作成条件保持オブジェクト
     */
    void createReport(ReservationHistoryReportCriteriaDto criteria);

    /**
     * 当該顧客のレポート名一覧を取得する。
     * @param customerNo 顧客番号
     * @return レポート名リスト
     */
    List<String> getExistingReportNameList(String customerNo);

    /**
     * 顧客番号に紐づくファイルのパスを取得する。
     * @param customerNo 顧客番号
     * @param reportName ファイル名
     * @return ファイルパス
     */
    public Path getReportFilePath(String customerNo, String reportName);

}
