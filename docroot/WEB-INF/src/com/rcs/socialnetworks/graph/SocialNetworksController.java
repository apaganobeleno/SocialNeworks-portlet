package com.rcs.socialnetworks.graph;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletContext;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import oauth.signpost.OAuth;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.bind.annotation.RenderMapping;

import sun.util.logging.resources.logging;
import twitter4j.auth.RequestToken;

import com.google.code.linkedinapi.client.LinkedInApiClient;
import com.google.code.linkedinapi.client.LinkedInApiClientFactory;
import com.google.code.linkedinapi.client.oauth.LinkedInAccessToken;
import com.google.code.linkedinapi.client.oauth.LinkedInOAuthService;
import com.google.code.linkedinapi.client.oauth.LinkedInRequestToken;
import com.google.code.linkedinapi.schema.Connections;
import com.google.code.linkedinapi.schema.Person;
import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.HttpUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.Image;
import com.liferay.portal.model.Portlet;
import com.liferay.portal.model.User;
import com.liferay.portal.service.ImageLocalServiceUtil;
import com.liferay.portal.service.PortletLocalServiceUtil;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portal.util.comparator.UserLastNameComparator;
import com.liferay.portlet.expando.model.ExpandoTable;
import com.liferay.portlet.expando.model.ExpandoTableConstants;
import com.liferay.portlet.expando.service.ExpandoTableLocalServiceUtil;
import com.liferay.portlet.social.model.SocialRelationConstants;
import com.rcs.socialnetworks.contact.ContactDTO;
import com.rcs.socialnetworks.contact.SocialNetworkDTO;
import com.rcs.socialnetworks.google.GooglePlusConnectUtil;
import com.rcs.socialnetworks.linkedin.LinkedInConnectUtil;
import com.rcs.socialnetworks.twitter.TwitterConnectUtil;
import com.rcs.socialnetworks.utils.LiferayUtil;

/**
 * This controlles get the users friends from Liferay
 * and then shows them in a Graph.
 * 
 * @author flor
 *
 */
@Controller(value="SocialNetworksController") 
@RequestMapping("VIEW")
public class SocialNetworksController {
	private static Log log = LogFactoryUtil.getLog(SocialNetworksController.class);
		
	@RenderMapping
	public ModelAndView resolveView(PortletRequest portletRequest, PortletResponse response) throws PortalException, SystemException {
		 		
		Map<String, Object> model = new HashMap<String, Object>();
		HttpServletRequest request = PortalUtil.getHttpServletRequest(portletRequest);
        HttpSession session = request.getSession();        
        ThemeDisplay themeDisplay= (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
		// check if user is logged in
		User user = PortalUtil.getUser(request);		
		if(user == null) {
			model.put("errorMessage", "@@ You need to be logged in to see your contacts."); //@@ add to language.properties
			return new ModelAndView("/WEB-INF/views/error.jsp", model);
		}		
		
		ContactDTO currentUser = new ContactDTO();
		currentUser.setFirstName(user.getFirstName());
		currentUser.setLastName(user.getLastName());
		currentUser.setEmail(user.getEmailAddress());		
		if(user.getPortraitId() != 0) {
			currentUser.setPictureURL(user.getPortraitURL(themeDisplay));
		}
		//currentUser.setGender()
		List<ContactDTO> contacts = new ArrayList<ContactDTO>();
		
		LinkedInConnectUtil linkedin = new LinkedInConnectUtil(portletRequest);
		// Check if linkedin is enabled, and the api key and secret added		
		boolean linkedInEnabled = linkedin.isEnabled();
		//List<ContactDTO> linkedinContacts = new ArrayList<ContactDTO>();		
        if (linkedInEnabled) {
        	  	
        	if(linkedin.currentUserHasAccount()) {        		
        		// if we finally have the linkedin access token, then get the linkedin connections            		    			
    			//transform connections to ContactDTO
    			contacts = linkedin.addContacts(contacts);    			
    			if(StringUtils.isBlank(currentUser.getPictureURL())) {
    				currentUser.setPictureURL(linkedin.getPictureURLFromSocialNetworkCurrentUser());
    			}
        	} else {
        		String linkedInAuthUrl = linkedin.getAuthorizationURL(); //@@ add this in the class
                model.put("linkedInAuthUrl", linkedInAuthUrl);
        	}        	        	        	
        }
		
		TwitterConnectUtil twitter = new TwitterConnectUtil(portletRequest);
		 if (twitter.isEnabled()) {	        		        		        	
	        	if(twitter.currentUserHasAccount()) {        		
	        		// if we finally have the linkedin access token, then get the linkedin connections            		    			
	    			//transform connections to ContactDTO
	    			contacts = twitter.addContacts(contacts);
	    			if(StringUtils.isBlank(currentUser.getPictureURL())) {
	    				currentUser.setPictureURL(twitter.getPictureURLFromSocialNetworkCurrentUser());
	    			}
	        	} else {
	        		String twitterAuthUrl = twitter.getAuthorizationURL(); //@@ add this in the class
	                model.put("twitterAuthUrl", twitterAuthUrl);
	        	}      
		 }
		 
		GooglePlusConnectUtil googlePlus = new GooglePlusConnectUtil(portletRequest);
		if(googlePlus.isEnabled()) {
			log.error("googlePlus.currentUserHasAccount(): " + googlePlus.currentUserHasAccount());
        	if(googlePlus.currentUserHasAccount()) {    //@@ seguir acá!!        		
        		// if we finally have the google access token, then get the google connections            		    			
    			//transform connections to ContactDTO
    			contacts = googlePlus.addContacts(contacts);
        	} else {
        		String googlePlusAuthUrl = googlePlus.getAuthorizationURL();
                model.put("googlePlusAuthUrl", googlePlusAuthUrl);
        	}      
			
		}
		// check if Liferay's social networking portlet is installed
		boolean isFriendsPortletInstalled = false;
		List<Portlet> portlets = PortletLocalServiceUtil.getPortlets();
		for(Portlet portlet : portlets) {
			if("Social Networking Portlet".equals(portlet.getPluginPackage().getName())) { //@@ add constant
				isFriendsPortletInstalled = true;
			}		
		}
		
		// check if user has friends
		long currentUserId = user.getUserId();
		//int socialUsersCount = UserLocalServiceUtil.getSocialUsersCount(currentUserId, SocialRelationConstants.TYPE_BI_FRIEND);		
		int socialUsersCount = UserLocalServiceUtil.getSocialUsersCount(currentUserId);		
		if(isFriendsPortletInstalled || socialUsersCount > 0) { // add liferay friends
			//List<User> friends = UserLocalServiceUtil.getSocialUsers(currentUserId, SocialRelationConstants.TYPE_BI_FRIEND, 0, socialUsersCount, new UserLastNameComparator(true));
			List<User> friends = UserLocalServiceUtil.getSocialUsers(currentUserId, 0, socialUsersCount, new UserLastNameComparator(true));
			contacts = LiferayUtil.addContacts(portletRequest, contacts, friends);
		}		
		
		String contactsJSON = "";
		Gson gson = new Gson();
		contactsJSON = gson.toJson(contacts);		        							
		model.put("jsonContacts", " {\"person\": " + contactsJSON + "}");
		model.put("jsonCurrentUser", gson.toJson(currentUser));
		return new ModelAndView("/WEB-INF/views/view.jsp", model);	
	}
	
	public static BufferedImage scaleImage(BufferedImage image, int imageType,
            int newWidth, int newHeight) {
        // Make sure the aspect ratio is maintained, so the image is not distorted
        double thumbRatio = (double) newWidth / (double) newHeight;
        int imageWidth = image.getWidth(null);
        int imageHeight = image.getHeight(null);
        double aspectRatio = (double) imageWidth / (double) imageHeight;

        if (thumbRatio < aspectRatio) {
            newHeight = (int) (newWidth / aspectRatio);
        } else {
            newWidth = (int) (newHeight * aspectRatio);
        }

        // Draw the scaled image
        BufferedImage newImage = new BufferedImage(newWidth, newHeight,
                imageType);
        Graphics2D graphics2D = newImage.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.drawImage(image, 0, 0, newWidth, newHeight, null);

        return newImage;
    }		

}
