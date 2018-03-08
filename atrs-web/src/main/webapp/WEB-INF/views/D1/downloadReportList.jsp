<div class="navi-forward">
  <form:form method="get" action="${pageContext.request.contextPath}/HistoryReport/download">
    <div class="info">
      <p>以下の中からダウンロードするレポートを選択して下さい。</p>
    </div>

    <table>
      <tr>
        <th>レポート名</th>
        <th>
          <!-- DLボタン配置カラム -->
        </th>
      </tr>
      <c:forEach items="${reportNameList}" var="reportName">
        <tr>
          <td>${f:h(reportName)}</td>
          <td>
            <button name="reportName" value="${f:h(reportName)}">ダウンロード</button>
          </td>
        </tr>
      </c:forEach>
    </table>

    <button type="button" name="forwardTop" class="forward" onclick="atrs.moveTo('/')">TOPへ戻る</button>
  </form:form>
</div>
