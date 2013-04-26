package com.rcs.socialnetworks;

import java.util.Date;
import java.util.List;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;

import oauth.signpost.OAuth;

import org.apache.commons.lang.StringUtils;

import com.google.code.linkedinapi.client.oauth.LinkedInAccessToken;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.HttpUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.User;
import com.liferay.portal.theme.PortletDisplay;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portlet.PortletURLFactoryUtil;
import com.rcs.socialnetworks.contact.ContactDTO;

public abstract class SocialNetworkOAuthUtil /*implements SocialNetworkOAuthData */{		
			
	public static String apiKey;
	
	public static String apiSecret;
	
	public static boolean isEnabled;
	
	protected PortletRequest portletRequest;
	
	protected String redirectURL;		
	
	//public final String socialNetworkName = "socialNetworkName";
	
	public static final String socialNetworkAccessTokenField = "socialNetworkAccessTokenField";//@@ check this later
	
	public static final String socialNetworkTokenSecretField = "socialNetworkTokenSecretField";
	
	//@@ add expiration field?
	
	private User user;
	
	public SocialNetworkOAuthUtil(PortletRequest portletRequest) {
		this.portletRequest = portletRequest;
		apiKey = getApiKey();
		apiSecret = getApiSecret();
	}
	
	public SocialNetworkOAuthUtil() {
		apiKey = getApiKey();
		apiSecret = getApiSecret();
	}
		
	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		SocialNetworkOAuthUtil.apiKey = apiKey;
	}

	public String getApiSecret() {
		//if api-secret is null, then find the value
		return apiSecret;
	}

	public void setApiSecret(String apiSecret) {
		SocialNetworkOAuthUtil.apiSecret = apiSecret;
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public void setEnabled(boolean isEnabled) {
		SocialNetworkOAuthUtil.isEnabled = isEnabled;
	}	
	
	public static String createRedirectURL(PortletRequest portletRequest) {
    	ThemeDisplay themeDisplay= (ThemeDisplay) portletRequest.getAttribute(WebKeys.THEME_DISPLAY);
    	PortletDisplay portletDisplay= themeDisplay.getPortletDisplay();
    	String portletId= portletDisplay.getId();
    	HttpServletRequest request = PortalUtil.getHttpServletRequest(portletRequest);    	
		javax.portlet.PortletURL portletURL = PortletURLFactoryUtil.create(request, portletId, themeDisplay.getPlid(), PortletRequest.RENDER_PHASE);
    	return portletURL.toString();
    }
	
	public String createRedirectURL() {
    	ThemeDisplay themeDisplay= (ThemeDisplay) this.portletRequest.getAttribute(WebKeys.THEME_DISPLAY);
    	PortletDisplay portletDisplay= themeDisplay.getPortletDisplay();
    	String portletId= portletDisplay.getId();
    	HttpServletRequest request = PortalUtil.getHttpServletRequest(portletRequest);    	
		javax.portlet.PortletURL portletURL = PortletURLFactoryUtil.create(request, portletId, themeDisplay.getPlid(), PortletRequest.RENDER_PHASE);
		//String redirectURL = HttpUtil.addParameter(portletURL.toString(), "p_p_id", portletDisplay.getId()); //@@ change the name of the portlet
		String redirectURL = HttpUtil.addParameter(portletURL.toString(), "p_p_state", "normal"); //@@ change the name of the portlet
		//redirectURL = HttpUtil.addParameter(redirectURL, "p_p_state", "normal");
		redirectURL = HttpUtil.addParameter(redirectURL, "p_p_mode", "view");
		redirectURL = HttpUtil.addParameter(redirectURL, "socialnetwork", this.getSocialNetworkName());
    	return redirectURL;
    }
	
	public String geRedirectURL() {
		if(this.redirectURL == null) {
			this.redirectURL = this.createRedirectURL();
			/*
			ThemeDisplay themeDisplay= (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
	    	PortletDisplay portletDisplay= themeDisplay.getPortletDisplay();
	    	System.out.println("portletid: " + portletDisplay.getId());
	    	System.out.println("getPortletName: " + portletDisplay.getPortletName());
	    	System.out.println("getNamespace: " + portletDisplay.getNamespace());
	    	 = re*/
		}
		return this.redirectURL;		
	}

/*	public static String geRedirectURL(PortletRequest portletRequest) {
		String redirectURL = SocialNetworkOAuthUtil.createRedirectURL(portletRequest);
		
	}*/
	public User getUser() {
		if(this.user == null && this.portletRequest != null) {
			try {
				this.user = PortalUtil.getUser(this.portletRequest);
			} catch(Exception ignored) { }			
		}
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getSocialNetworkName() {
		return "socialNetworkName";
	}

	public String getAccessTokenFieldName() {
		return "accessTokenFieldName";
	}
	
	public String getTokenSecrtFieldName() {
		return "tokenSecretFieldName";
	}
	
	public String getExpandoAccessToken() {
		return getExpandoField(getAccessTokenFieldName());
	}
	
	public String getExpandoTokenSecret() {		
		return getExpandoField(getTokenSecrtFieldName());
	}
	
	public String getExpandoField(String fieldName) {
		String field = null;
		if(this.getUser() != null) {
			try {
				//User user = PortalUtil.getUser(servletRequest);
				User user = this.getUser();
				//getExpandoAccessToken
				//getExpandoTokenSecret
				field = (String) user.getExpandoBridge().getAttribute(fieldName);		    	
			} catch (Exception ignored) {
				return field;
			}
		}
		return field;
	}
	
	public void setExpandoField(String fieldName, String field) {		
		if(this.getUser() != null) {
			try {
				//User user = PortalUtil.getUser(servletRequest);
				//User user = this.getUser();
				//getExpandoAccessToken
				//getExpandoTokenSecret
				this.getUser().getExpandoBridge().setAttribute(fieldName, field);						    
			} catch (Exception ignored) {				
			}
		}		
	}
	
	public String getRequestTokenName() {
		return "requestTokenName";
	}
	
	public boolean isSocialNetworkOAuthRequest(HttpServletRequest request) {		
		return StringUtils.isNotBlank(request.getParameter("socialnetwork")) 
				&& StringUtils.equals(getSocialNetworkName(), request.getParameter("socialnetwork"))
				&& StringUtils.isNotBlank(request.getParameter(OAuth.OAUTH_TOKEN)) 
				&& request.getSession().getAttribute(getRequestTokenName()) != null;					
	}
	
	public void storeAccessToken(String accessToken, String tokenSecret) {
		setExpandoField(getAccessTokenFieldName(), accessToken);
		setExpandoField(getTokenSecrtFieldName(), tokenSecret);
//		if(this.getUser() != null) {
//			this.getUser().getExpandoBridge().setAttribute("linkedinAccessToken", accessToken);
//			user.getExpandoBridge().setAttribute("linkedinTokenSecret", tokenSecret);
////  		user.getExpandoBridge().setAttribute("linkedinExpirationTime", linkedinAccessToken.getExpirationTime().getTime());
//		}
	}
	//public String getUserAccessToken
}
