<div id="login_navigation">
    <ul>
        <%
            Object linkedInAuthUrl = request.getAttribute("linkedInAuthUrl");
            Object twitterAuthUrl = request.getAttribute("twitterAuthUrl");
            Object googlePlusAuthUrl = request.getAttribute("googlePlusAuthUrl");
        %>
        <li>
            Show contacts from:
        </li>
        <%--<c:if test="${!empty linkedInAuthUrl}">--%>
        <%
            if (linkedInAuthUrl != null) {
        %>
        <li>
            <a href="#" onclick="window.location.href = '${linkedInAuthUrl}'">
                <img src="/${pageContext.servletContext.servletContextName}/img/linkedin-logo.jpg" alt="Linked In" />
                Linked In
            </a>
        </li>
        <%
            }
        %>
        <%--</c:if>--%>
        <%--<c:if test="${!empty twitterAuthUrl}">--%>
        <%
            if (twitterAuthUrl != null) {
        %>
        <li>
            <a href="#" onclick="window.location.href = '${twitterAuthUrl}'">
                <img src="/${pageContext.servletContext.servletContextName}/img/twitter-logo.jpg" alt="Twitter" />
                Twitter
            </a>
        </li>
        <%
            }
        %>
        <%--</c:if>--%>
		<%
            if (googlePlusAuthUrl != null) {
        %>
        <li>
            <a href="#" onclick="window.location.href = '${googlePlusAuthUrl}'">
                <img src="/${pageContext.servletContext.servletContextName}/img/googleplus-logo.jpg" alt="Google Plus" />
                Google Plus
            </a>
        </li>
        <%
            }
        %>
    </ul>
</div>
