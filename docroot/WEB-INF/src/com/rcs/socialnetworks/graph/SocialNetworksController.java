package com.rcs.socialnetworks.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.bind.annotation.RenderMapping;
import org.springframework.web.portlet.bind.annotation.ResourceMapping;

import com.google.code.linkedinapi.client.LinkedInApiClientException;
import com.google.gson.Gson;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.Portlet;
import com.liferay.portal.model.User;
import com.liferay.portal.service.PortletLocalServiceUtil;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portal.util.comparator.UserLastNameComparator;
import com.rcs.socialnetworks.contact.ContactDTO;
import com.rcs.socialnetworks.facebook.FacebookConnectUtil;
import com.rcs.socialnetworks.google.GoogleConnectUtil;
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
        String errorMessage = "";
		// check if user is logged in
		User user = PortalUtil.getUser(request);		
		if(user == null) {
			model.put("errorMessage", LanguageUtil.get(LocaleUtil.fromLanguageId(LanguageUtil.getLanguageId(request)), "rcs.socialnetowork.error.loggedin"));
			return new ModelAndView("/WEB-INF/views/error.jsp", model);
		}		
				
		
		ContactDTO currentUser = new ContactDTO();
		currentUser.setFirstName(user.getFirstName());
		currentUser.setLastName(user.getLastName());
		currentUser.setEmail(user.getEmailAddress());
		currentUser.setGender(user.isFemale() ? ContactDTO.Gender.FEMALE.value : ContactDTO.Gender.MALE.value);
		if(user.getPortraitId() != 0) {
			currentUser.setPictureURL(user.getPortraitURL(themeDisplay));
		}
		List<ContactDTO> contacts = new ArrayList<ContactDTO>();
		
		try {
			LinkedInConnectUtil linkedin;
			if(session.getAttribute(LinkedInConnectUtil.SOCIAL_NETWORK_NAME) != null) {
				linkedin = (LinkedInConnectUtil) session.getAttribute(LinkedInConnectUtil.SOCIAL_NETWORK_NAME);
				linkedin.setPortletRequest(portletRequest);
			} else {
				linkedin = new LinkedInConnectUtil(portletRequest);
			}
			// Check if linkedin is enabled, and the api key and secret added							
	        if (linkedin.isEnabled()) {        	  	
	        	if(linkedin.currentUserHasAccount()) {        		
	        		// if we finally have the linkedin access token, then get the linkedin connections            		    			
	    			//transform connections to ContactDTO
	    			contacts = linkedin.addContacts(contacts);    			
	    			if(StringUtils.isBlank(currentUser.getPictureURL())) {
	    				currentUser.setPictureURL(linkedin.getPictureURLFromSocialNetworkCurrentUser());
	    			}
	        	} else {
	        		String linkedInAuthUrl = linkedin.getAuthorizationURL();
	                model.put("linkedInAuthUrl", linkedInAuthUrl);
	        	}        	        	        	
	        }
	        session.setAttribute(LinkedInConnectUtil.SOCIAL_NETWORK_NAME, linkedin);
		} catch(LinkedInApiClientException e) {
			log.error("The LinkedIn API key or secret are wrong.");
			errorMessage = errorMessage.concat("LinkedIn Error: Wrong Api Key or secret.");
			log.error(errorMessage);
			model.put("errorMessage", errorMessage);
		} catch(Exception e) {			
			errorMessage = errorMessage.concat("LinkedIn Error: Wrong Api Key or secret.");
			log.error(errorMessage);
			model.put("errorMessage", errorMessage);
		}
        
		TwitterConnectUtil twitter;
		if(session.getAttribute(TwitterConnectUtil.SOCIAL_NETWORK_NAME) != null) {
			twitter = (TwitterConnectUtil) session.getAttribute(TwitterConnectUtil.SOCIAL_NETWORK_NAME);
			twitter.setPortletRequest(portletRequest);
		} else {
			twitter = new TwitterConnectUtil(portletRequest);
		}
		 if (twitter.isEnabled()) {	        		        		        	
	        	if(twitter.currentUserHasAccount()) {        		
	        		// if we finally have the linkedin access token, then get the linkedin connections            		    			
	    			//transform connections to ContactDTO
	    			contacts = twitter.addContacts(contacts);
	    			if(StringUtils.isBlank(currentUser.getPictureURL())) {
	    				currentUser.setPictureURL(twitter.getPictureURLFromSocialNetworkCurrentUser());
	    			}
	        	} else {
	        		try {
	        			String twitterAuthUrl = twitter.getAuthorizationURL(); //@@ add this in the class	        		
	        			model.put("twitterAuthUrl", twitterAuthUrl);
	        		} catch(Exception e) {	        			
	        			errorMessage = errorMessage.concat("<br/>Twitter Error: Wrong Api Key or secret.");
	        			log.error(errorMessage);
	        			model.put("errorMessage", errorMessage);
	        		}
	        	}      
		 }
		 session.setAttribute(TwitterConnectUtil.SOCIAL_NETWORK_NAME, twitter);
		 
		GoogleConnectUtil google;
		if(session.getAttribute(GoogleConnectUtil.SOCIAL_NETWORK_NAME) != null) {
			google = (GoogleConnectUtil) session.getAttribute(GoogleConnectUtil.SOCIAL_NETWORK_NAME);
			google.setPortletRequest(portletRequest);
		} else {
			google = new GoogleConnectUtil(portletRequest);
		}		
		if(google.isEnabled()) {						
        	if(google.currentUserHasAccount()) {        		
        		// if we finally have the google access token, then get the google connections            		    			
    			//transform connections to ContactDTO
    			contacts = google.addContacts(contacts);
        	} else {
        		String googlePlusAuthUrl = google.getAuthorizationURL();
                model.put("googleAuthUrl", googlePlusAuthUrl);
        	}      
			
		}
		session.setAttribute(GoogleConnectUtil.SOCIAL_NETWORK_NAME, google);
		
		FacebookConnectUtil facebook;
		if(session.getAttribute(FacebookConnectUtil.SOCIAL_NETWORK_NAME) != null) {
			facebook = (FacebookConnectUtil) session.getAttribute(FacebookConnectUtil.SOCIAL_NETWORK_NAME);
			facebook.setPortletRequest(portletRequest);
		} else {
			facebook = new FacebookConnectUtil(portletRequest);
		}		
		if(facebook.isEnabled()) {

			if(facebook.currentUserHasAccount()) {				
				// if we finally have the facebook access token, then get the facebook connections            		    			
    			//transform connections to ContactDTO
    			contacts = facebook.addContacts(contacts);
			} else {
				String facebookAuthUrl = facebook.getAuthorizationURL();
                model.put("facebookAuthUrl", facebookAuthUrl);
			}
		}
		session.setAttribute(FacebookConnectUtil.SOCIAL_NETWORK_NAME, facebook);
		
		// check if Liferay's social networking portlet is installed
		boolean isFriendsPortletInstalled = false;
		List<Portlet> portlets = PortletLocalServiceUtil.getPortlets();
		for(Portlet portlet : portlets) {
			if("Social Networking Portlet".equals(portlet.getPluginPackage().getName())) { //TODO @@ add constant name
				isFriendsPortletInstalled = true;
			}		
		}
		
		// check if user has Liferay friends
		long currentUserId = user.getUserId();	
		int socialUsersCount = UserLocalServiceUtil.getSocialUsersCount(currentUserId);		
		if(isFriendsPortletInstalled || socialUsersCount > 0) { // add liferay friends
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
	
	@ResourceMapping(value = "revokeSocialNetwork")
    public ModelAndView revokeSocialNetworkController(
    		String socialNetworkName
    		,ResourceRequest request
            ,ResourceResponse response
            ) throws Exception {
				
		User user = PortalUtil.getUser(request);
		HttpServletRequest servletRequest = PortalUtil.getHttpServletRequest(request);
        HttpSession session = servletRequest.getSession();
		if(GoogleConnectUtil.SOCIAL_NETWORK_NAME.equals(socialNetworkName)) {
			GoogleConnectUtil google;
			if(session.getAttribute(GoogleConnectUtil.SOCIAL_NETWORK_NAME) != null) {
				google = (GoogleConnectUtil) session.getAttribute(GoogleConnectUtil.SOCIAL_NETWORK_NAME);				
			} else {
				google = new GoogleConnectUtil();
				google.setUser(user);
			}					
			session.setAttribute(GoogleConnectUtil.SOCIAL_NETWORK_NAME, google);						
			google.deleteExpandoValues();
		}
		
		if(FacebookConnectUtil.SOCIAL_NETWORK_NAME.equals(socialNetworkName)) {
			FacebookConnectUtil facebook;
			if(session.getAttribute(FacebookConnectUtil.SOCIAL_NETWORK_NAME) != null) {
				facebook = (FacebookConnectUtil) session.getAttribute(FacebookConnectUtil.SOCIAL_NETWORK_NAME);				
			} else {
				facebook = new FacebookConnectUtil();
				facebook.setUser(user);
			}					
			facebook.deleteExpandoValues();
		}
		
		if(TwitterConnectUtil.SOCIAL_NETWORK_NAME.equals(socialNetworkName)) {
			TwitterConnectUtil twitter;
			if(session.getAttribute(TwitterConnectUtil.SOCIAL_NETWORK_NAME) != null) {
				twitter = (TwitterConnectUtil) session.getAttribute(TwitterConnectUtil.SOCIAL_NETWORK_NAME);				
			} else {
				twitter = new TwitterConnectUtil();
				twitter.setUser(user);
			}			
			twitter.deleteExpandoValues();
		}
		
		if(LinkedInConnectUtil.SOCIAL_NETWORK_NAME.equals(socialNetworkName)) {
			LinkedInConnectUtil linkedIn;
			if(session.getAttribute(LinkedInConnectUtil.SOCIAL_NETWORK_NAME) != null) {
				linkedIn = (LinkedInConnectUtil) session.getAttribute(LinkedInConnectUtil.SOCIAL_NETWORK_NAME);				
			} else {
				linkedIn = new LinkedInConnectUtil();
				linkedIn.setUser(user);
			}			
			linkedIn.deleteExpandoValues();
		}				
		return null;
	}
    		
//	public static BufferedImage scaleImage(BufferedImage image, int imageType,
//            int newWidth, int newHeight) {
//        // Make sure the aspect ratio is maintained, so the image is not distorted
//        double thumbRatio = (double) newWidth / (double) newHeight;
//        int imageWidth = image.getWidth(null);
//        int imageHeight = image.getHeight(null);
//        double aspectRatio = (double) imageWidth / (double) imageHeight;
//
//        if (thumbRatio < aspectRatio) {
//            newHeight = (int) (newWidth / aspectRatio);
//        } else {
//            newWidth = (int) (newHeight * aspectRatio);
//        }
//
//        // Draw the scaled image
//        BufferedImage newImage = new BufferedImage(newWidth, newHeight,
//                imageType);
//        Graphics2D graphics2D = newImage.createGraphics();
//        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
//                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
//        graphics2D.drawImage(image, 0, 0, newWidth, newHeight, null);
//
//        return newImage;
//    }		

}
