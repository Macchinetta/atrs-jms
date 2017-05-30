<security:authentication property="principal.member" var="member" scope="page"/>

<div class="info">
  <p>
      ${f:h(member.customerNo)} の予約履歴レポートを作成します。よろしいですか？<br>
  </p>
</div>

<div class="navi-forward">
  <form:form method="post" 
    action="${pageContext.request.contextPath}/HistoryReport/create">

    <button type="submit" name="yes" class="forward">はい</button>
    <button type="button" name="no" name="forwardTop" class="forward" 
      onclick="atrs.moveTo('/')">いいえ</button>

  </form:form>
</div>

