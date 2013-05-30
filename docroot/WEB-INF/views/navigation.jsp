<%@page 
		import="com.rcs.socialnetworks.linkedin.LinkedInConnectUtil"
		import="com.rcs.socialnetworks.google.GoogleConnectUtil"
		import="com.rcs.socialnetworks.facebook.FacebookConnectUtil"	 
		import="com.rcs.socialnetworks.twitter.TwitterConnectUtil"
		import="org.apache.commons.lang.StringUtils" %>
<div id="login_navigation">
    <ul>
        <%
            Object linkedInAuthUrl = request.getAttribute("linkedInAuthUrl");
            Object twitterAuthUrl = request.getAttribute("twitterAuthUrl");
            Object googleAuthUrl = request.getAttribute("googleAuthUrl");
            Object facebookAuthUrl = request.getAttribute("facebookAuthUrl");
            boolean twitterIsErrorFree = true;
            boolean linkedInIsErrorFree = true;
            boolean googleIsErrorFree = true;
            boolean facebookIsErrorFree = true;            
            if(request.getAttribute("errorMessage") != null) {
            	String errorMessage = (String) request.getAttribute("errorMessage");
            	twitterIsErrorFree = !StringUtils.containsIgnoreCase(errorMessage, "twitter");
            	linkedInIsErrorFree = !StringUtils.containsIgnoreCase(errorMessage, "linkedin");
            	googleIsErrorFree = !StringUtils.containsIgnoreCase(errorMessage, "google");
            	facebookIsErrorFree = !StringUtils.containsIgnoreCase(errorMessage, "facebook");
            }
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
            if (new LinkedInConnectUtil().isEnabled() && linkedInAuthUrl == null && linkedInIsErrorFree) {
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
            if (new TwitterConnectUtil().isEnabled() && twitterAuthUrl == null && twitterIsErrorFree) {
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
            if (new GoogleConnectUtil().isEnabled() && googleAuthUrl == null && googleIsErrorFree) {
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
            if (new FacebookConnectUtil().isEnabled()&& facebookAuthUrl == null && facebookIsErrorFree) {
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
