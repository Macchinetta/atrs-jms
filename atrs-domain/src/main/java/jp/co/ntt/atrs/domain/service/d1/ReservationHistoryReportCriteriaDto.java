/*
 * Copyright(c) 2014-2017 NTT Corporation.
 */
package jp.co.ntt.atrs.domain.service.d1;

import java.io.Serializable;

/**
 * レポート作成情報保持クラス
 * @author NTT 電電次郎
 */
public class ReservationHistoryReportCriteriaDto implements Serializable {

    /**
     * シリアルバージョンUID。
     */
    private static final long serialVersionUID = 7094850548311586132L;

    /**
     * 顧客番号。
     */
    private String customerNo;

    /**
     * 顧客番号を取得する。
     * @return 顧客番号
     */
    public String getCustomerNo() {
        return customerNo;
    }

    /**
     * 顧客番号を設定する。
     * @param customerNo 番号
     */
    public void setCustomerNo(String customerNo) {
        this.customerNo = customerNo;
    }

}
