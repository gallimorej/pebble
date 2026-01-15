
<c:if test="${not empty param.error}"><div class="loginError"><fmt:message key="${fn:escapeXml(param.error)}"/></div></c:if>

<div id="loginPage" class="loginPageForm">
  <div id="loginOptionPasswordArea" class="loginOptionArea">
    <form id="passwordLoginForm" name="passwordLoginForm" method="post"
          action="${url:rewrite(blogUrl)}j_spring_security_check">
      <div class="field">
        <label for="username"><fmt:message key="login.username"/></label>
        <input autocorrect="off" autocapitalize="off" id="username" type="text" name="j_username" />
      </div>
      <div class="field">
        <label for="password"><fmt:message key="login.password"/></label>
        <input id="password" type="password" name="j_password" />
      </div>
      <div class="field">
         <label for="rememberMe"><fmt:message key='login.rememberMe' /></label>
         <input id="rememberMe" type="checkbox" name="_spring_security_remember_me" />
      </div>
      <div class="loginButtons"><input type="submit" value="<fmt:message key='login.button' />" /></div>
    </form>
  </div>
</div>
