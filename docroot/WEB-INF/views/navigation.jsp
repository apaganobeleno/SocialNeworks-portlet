<div id="login_navigation">
    <ul>
        <%
            Object linkedInAuthUrl = request.getAttribute("linkedInAuthUrl");
            Object twitterAuthUrl = request.getAttribute("twitterAuthUrl");
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

    </ul>
</div>
