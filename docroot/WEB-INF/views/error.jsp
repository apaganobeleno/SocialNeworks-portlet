<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="portlet" uri="http://java.sun.com/portlet" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>
<%@ taglib uri="http://liferay.com/tld/theme" prefix="liferay-theme" %>
<%@ taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui" %>
<liferay-theme:defineObjects /> 
<portlet:defineObjects />
<c:if test="${errorMessage != null}" >
	<c:out value="${errorMessage}" escapeXml="false" />
</c:if>