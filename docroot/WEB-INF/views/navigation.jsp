<%@page import="com.rcs.socialnetworks.linkedin.LinkedInConnectUtil" %>
<div id="login_navigation">
    <ul>
        <%
            Object linkedInAuthUrl = request.getAttribute("linkedInAuthUrl");
            Object twitterAuthUrl = request.getAttribute("twitterAuthUrl");
            Object googleAuthUrl = request.getAttribute("googleAuthUrl");
            Object facebookAuthUrl = request.getAttribute("facebookAuthUrl");
        %>
        <li>
            Enable contacts from:
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
            if (googleAuthUrl != null) {
        %>
        <li>
            <a href="#" onclick="window.location.href = '${googleAuthUrl}'">
                <img src="/${pageContext.servletContext.servletContextName}/img/google-logo.png" alt="Google" />
                Google
            </a>
        </li>
        <%
            }
        %>
        <%
            if (facebookAuthUrl != null) {
        %>
        <li>
            <a href="#" onclick="window.location.href = '${facebookAuthUrl}'">                
            	<img src="/${pageContext.servletContext.servletContextName}/img/facebook-logo.png" alt="Facebook" />
                Facebook
            </a>
        </li>
        <%
            }
        %>
    </ul>
        <ul>        
        <li>
            Disable contacts from:
        </li>
        <%--<c:if test="${!empty linkedInAuthUrl}">--%>
        <%
            if (linkedInAuthUrl == null) {
        %>
        <li>
            <a id="linkedin" class="socialnetwork" href="#" >
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
            if (twitterAuthUrl == null) {
        %>
        <li>
            <a id="twitter" class="socialnetwork" href="#" >
                <img src="/${pageContext.servletContext.servletContextName}/img/twitter-logo.jpg" alt="Twitter" />
                Twitter
            </a>
        </li>
        <%
            }
        %>
        <%--</c:if>--%>
		<%
            if (googleAuthUrl == null) {
        %>
        <li>
            <a id="google" class="socialnetwork" href="#" >
                <img src="/${pageContext.servletContext.servletContextName}/img/google-logo.png" alt="Google" />
                Google
            </a>
        </li>
        <%
            }
        %>
        <%
            if (facebookAuthUrl == null) {
        %>
        <li>
            <a id="facebook" class="socialnetwork" href="#" >
            	<img src="/${pageContext.servletContext.servletContextName}/img/facebook-logo.png" alt="Facebook" />                
                Facebook
            </a>
        </li>
        <%
            }
        %>
    </ul>
</div>
