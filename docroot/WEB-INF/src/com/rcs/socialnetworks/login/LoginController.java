package com.rcs.socialnetworks.login;

import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.ModelAndView;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.util.PortalUtil;

@Controller(value="LoginController") 
@RequestMapping("VIEW")
public class LoginController {
	
	private static Log log = LogFactoryUtil.getLog(LoginController.class);
	
	@RequestMapping
    public Object view(RenderRequest renderRequest, RenderResponse renderResponse, ModelMap modelMap) {

        HttpServletRequest request = PortalUtil.getHttpServletRequest(renderRequest);
        HttpSession session = request.getSession();
        
        return new ModelAndView("/WEB-INF/views/login/view.jsp", modelMap);
	}

}
