package com.rcs.socialnetworks.portlet;

import java.util.HashMap;
import java.util.Map;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.bind.annotation.RenderMapping;

import com.google.gson.stream.JsonWriter;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

@Controller(value="SocialNetworksController") 
@RequestMapping("VIEW")
public class SocialNetworksController {
	private static Log log = LogFactoryUtil.getLog(SocialNetworksController.class);
		
	@RenderMapping
	public ModelAndView resolveView(PortletRequest request, PortletResponse response) throws PortalException, SystemException {
		Map<String, Object> model = new HashMap<String, Object>();
		String jsonContacts = " {\"person\": {" + 
		   "\"id\": \"1\"," +
		   "\"firstname\": \"Mariano\"," +
		   "\"lastname\": \"Perez\"," +
		   "\"socialnetworks\": {" +
		     "\"socialnetwork\": [" +
		       "{\"value\": \"linkedin\"}," +
		       "{\"value\": \"facebook\"}," +
		       "{\"value\": \"googleplus\"}" +
		     "]" +
		   "}"+
		 "}," +
		   "\"id\": \"2\"," +
		   "\"firstname\": \"Susana\"," +
		   "\"lastname\": \"Hernandez\"," +
		   "\"socialnetworks\": {" +
		     "\"socialnetwork\": [" +
		       "{\"value\": \"linkedin\"}," +
		       "{\"value\": \"facebook\"}" +
		     "]" +
		   "}" +		 
		"}";
		model.put("jsonContacts", jsonContacts);
		log.info("info" + jsonContacts);
		return new ModelAndView("views/view", model);
	}
}
