<%--
/**
* Copyright (c) 2000-2010 Liferay, Inc. All rights reserved.
*
* This library is free software; you can redistribute it and/or modify it under
* the terms of the GNU Lesser General Public License as published by the Free
* Software Foundation; either version 2.1 of the License, or (at your option)
* any later version.
*
* This library is distributed in the hope that it will be useful, but WITHOUT
* ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
* FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
* details.
*/
--%>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>

<div>
    
        <div class="rowInput">
            <label for="login">@@Username</label>
            <div id="loginInputField" class="inputField">
                <input type="text" name="login" id="login"/>
                <input type="hidden" name="_58_login" id="_58_login"/>
            </div>
        </div>
        <div class="rowInput">
            <label for="password">@@Password</label>
            <div id="passwordInputField" class="inputField">
                <input type="password" name="password" id="password"/>
                <input type="hidden" name="_58_password" id="_58_password"/>
            </div>
            <input type="hidden" name="_58_redirect" value="${defRedirect}">
            
        </div>
        <div class="aui-button-holder">
			<span class="aui-button aui-button-submit">
				<span class="aui-button-content">
					<input class="aui-button-input aui-button-input-submit" type="submit" value="Sign In">
				</span>
			</span>
		</div>	    
</div>
