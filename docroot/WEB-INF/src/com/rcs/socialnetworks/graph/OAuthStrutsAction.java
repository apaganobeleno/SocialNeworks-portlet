package com.rcs.socialnetworks.graph;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.liferay.portal.kernel.struts.BaseStrutsAction;
import com.rcs.socialnetworks.OAuth20Interface;

/**
 * This struts actions was specially created for google oauth, 
 * since the callback url should be a fixed url we needed to 
 * create a hook with a portal struts action
 * 
 * @author flor
 *
 */
public class OAuthStrutsAction extends BaseStrutsAction {

	@Override
	public String execute(
			HttpServletRequest request, HttpServletResponse response)
		throws Exception {

		String code = request.getParameter(OAuth20Interface.CODE);
		if(StringUtils.isBlank(code)) {
			//TODO @@ return error with proper message
		}
		String state = request.getParameter(OAuth20Interface.STATE);
		if(StringUtils.isBlank(state)) {
			//TODO @@ return error with proper message
		}		
		// the actual redirect url is base 64 encoded
		byte[] actualRedirectURL = Base64.decodeBase64(state);				
		// actual redirect
		request.setAttribute(OAuth20Interface.CODE, code);
		response.sendRedirect(new String(actualRedirectURL, "UTF-8") + "&code=" + code);
		return null;
	}
}

