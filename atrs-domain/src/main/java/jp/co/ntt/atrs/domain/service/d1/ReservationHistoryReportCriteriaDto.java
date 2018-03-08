/*
 * Copyright 2014-2018 NTT Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
