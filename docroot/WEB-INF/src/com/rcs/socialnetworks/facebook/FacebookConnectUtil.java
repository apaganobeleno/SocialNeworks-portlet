package com.rcs.socialnetworks.facebook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.http.client.utils.URLEncodedUtils;

import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.gdata.data.Link;
import com.google.gdata.data.extensions.Email;
import com.google.gdata.data.extensions.Name;
import com.google.gdata.util.ServiceException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.facebook.FacebookConnect;
import com.liferay.portal.kernel.security.pacl.permission.PortalRuntimePermission;
import com.liferay.portal.kernel.servlet.URLEncoder;
import com.liferay.portal.kernel.util.CharPool;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.kernel.util.HttpUtil;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.util.PortalUtil;
import com.rcs.socialnetworks.SocialNetworkOAuthUtil;
import com.rcs.socialnetworks.contact.ContactDTO;
import com.rcs.socialnetworks.contact.SocialNetworkDTO;

import facebook4j.Facebook;
import facebook4j.FacebookException;
import facebook4j.FacebookFactory;
import facebook4j.Friend;
import facebook4j.ResponseList;
import facebook4j.User;
import facebook4j.auth.AccessToken;

public class FacebookConnectUtil extends SocialNetworkOAuthUtil<String,AccessToken,User> {

	public static final String socialNetworkName = "facebook";
	
	public static String apiKey;

	public static String apiSecret;

	public static final String socialNetworkAccessTokenField = "facebookAccessToken";
	
	public static final String socialNetworkTokenSecretField = "facebookTokenSecret";
	
	public static final String socialNetworkRequestTokenField = "facebookRequestToken";
	
	public static final String socialNetworkExpirationTimeField = "facebookExpirationTime";

	private String code;

	private Facebook facebook;
	
	public long companyId;
	
	public static final String commaSeparetedPermissions = "email,read_friendlists";
	
	public FacebookConnectUtil() {
		super();
		this.companyId = getUser().getCompanyId();
		this.facebook = new FacebookFactory().getInstance();
		this.facebook.setOAuthAppId(getApiKey(), getApiSecret());
		this.facebook.setOAuthPermissions(commaSeparetedPermissions);	
	}
	
	public FacebookConnectUtil(PortletRequest portletRequest) {
		super(portletRequest);
		this.companyId = getUser().getCompanyId();
		this.facebook = new FacebookFactory().getInstance();
		this.facebook.setOAuthAppId(getApiKey(), getApiSecret());
		this.facebook.setOAuthPermissions(commaSeparetedPermissions);
	}		
	
	@Override
	public List<ContactDTO> addContacts(PortletRequest portletRequest,
			List<ContactDTO> contacts) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ContactDTO> addContacts(List<ContactDTO> contacts) {
		try {
			ResponseList<Friend> friends = this.facebook.getFriends();
			int i = 0;
			for(Friend friend : friends) {				
				User user = facebook.getUser(friend.getId());
				System.out.println("user1: " + user.getName());
				contacts = this.addContactAndCheckDuplicated(contacts, user);				
				if(i > 10)
					break;//@@ remove this
				i++;	
				//friend.getMetadata().
			}
		} catch (FacebookException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// TODO Auto-generated method stub
		return contacts;
	}

	@Override
	public String getAuthorizationURL() {		
		try {
			//return com.liferay.portal.kernel.facebook.FacebookConnectUtil.getAuthURL(this.companyId);
			//System.out.println("getAuthURL: " + com.liferay.portal.kernel.facebook.FacebookConnectUtil.getAuthURL(this.companyId));
			String accessTokenURL = com.liferay.portal.kernel.facebook.FacebookConnectUtil.getAuthURL(this.companyId);
			accessTokenURL = HttpUtil.addParameter(
					accessTokenURL, "client_id", getApiKey());
			String redirect_uri = this.createRedirectURL();
			System.out.println("redirect_uri: " + redirect_uri);
			accessTokenURL = HttpUtil.addParameter(
					accessTokenURL, "redirect_uri", redirect_uri);

			accessTokenURL = HttpUtil.addParameter(
					accessTokenURL, "scope", "read_friendlists"); //@@ may be add more permissions
			String encodedURL = java.net.URLEncoder.encode(this.createRedirectURL(), "UTF-8");
			System.out.println("encodedURL: " + encodedURL);
			accessTokenURL = this.facebook.getOAuthAuthorizationURL(encodedURL);
			System.out.println("getAuthorizationURL: " + accessTokenURL);
			return accessTokenURL;
		} catch (SystemException e) {
			// TODO Auto-generated catch block
			return null;
		} catch(UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getAccessTokenURL() {
		//HttpServletRequest originalServletRequest = PortalUtil.getOriginalServletRequest(PortalUtil.getHttpServletRequest(this.portletRequest));
		String url = "";
		try {
			url = HttpUtil.addParameter(com.liferay.portal.kernel.facebook.FacebookConnectUtil.getAccessTokenURL(companyId), "client_id", getApiKey());			
			String redirect_uri = this.createRedirectURL();
			System.out.println("redirect_uri: " + redirect_uri);
			url = HttpUtil.addParameter(url, "redirect_uri", this.createRedirectURL());
			//url = HttpUtil.addParameter(url, "client_secret", this.getApiSecret());
			url = HttpUtil.addParameter(url, "code", this.getCode());
		} catch (SystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return url;

	}
	
	@Override
	public AccessToken getAccessToken() {
		HttpServletRequest servletRequest = PortalUtil.getHttpServletRequest(this.portletRequest);
        HttpSession session = servletRequest.getSession();
        AccessToken accessToken = null;
    	if(session.getAttribute("facebookAccessToken") != null) {    		
    		accessToken = (AccessToken) session.getAttribute("facebookAccessToken");
    		//accessToken = new AccessToken(accessTokenStr);
    		//accessToken = new GoogleCredential().setAccessToken(accessTokenStr);
    		//@@ see what happens if the credential expired
//    		try {
//				credential.refreshToken();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
    		this.facebook.setOAuthAccessToken(accessToken);
    		this.setAccessToken(accessToken);
    		
    		return accessToken;
    	}
    	
    	// check if the access token is stored
    	String expandoAccessToken = getExpandoAccessToken();
    	System.out.println("expandoAccessToken: " + expandoAccessToken);
    	Long expirationTime = (Long) this.getUser().getExpandoBridge().getAttribute("facebookExpirationTime");
    	if(StringUtils.isNotBlank(expandoAccessToken)) {
    		if(expirationTime != null && expirationTime.longValue() >= new Date().getTime()) {
    			//String tokenSecretStr = (String)user.getExpandoBridge().getAttribute("linkedinTokenSecret");
    			
    			accessToken = new AccessToken(expandoAccessToken);
    			this.setAccessToken(accessToken);
    			this.facebook.setOAuthAccessToken(accessToken);
//    		} else {
    			//@@ do something else here!! delete this?
//    			credential = new GoogleCredential().setAccessToken(expandoAccessToken);
//    			try {
//    				GoogleTokenResponse restoredResponse = new GoogleTokenResponse();
//        			String refreshToken = getExpandoField(socialNetworkRefreshTokenField);
//    	        	restoredResponse.setRefreshToken(refreshToken);
//    		        credential = flow.createAndStoreCredential(restoredResponse, null);    			
//    				credential.refreshToken();
//    				
//    				//this.storeAccessToken(credential); @@ check what happens if the credential is refreshed but not stored 
//    				//this.setAccessToken(credential);    				
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}    			
//    		}
			//@@ refresh token? TODO
    		
    		return accessToken;    		   
    	} else {
    		accessToken = new AccessToken(expandoAccessToken);
			this.setAccessToken(accessToken);
			this.facebook.setOAuthAccessToken(accessToken);
    	}

		//com.liferay.portal.kernel.facebook.FacebookConnectUtil.getAccessToken(companyId, redirect, code);
		HttpServletRequest originalServletRequest = PortalUtil.getOriginalServletRequest(PortalUtil.getHttpServletRequest(this.portletRequest));
		System.out.println("originalServletRequest: " + ToStringBuilder.reflectionToString(originalServletRequest));
		if(isSocialNetworkOAuthRequest(originalServletRequest)) {
			String code = originalServletRequest.getParameter("code");
			this.code = code;
			System.out.println("code: " + code);
			try {
				
		        facebook.setOAuthPermissions(commaSeparetedPermissions);		        		        
		        //String authURL = facebook.getOAuthAuthorizationURL(this.createRedirectURL());
		        //System.out.println("authURL: " + authURL);
		        //facebook.getOAuthAuthorizationURL(arg0)	@@ seguir acá!		        
		        //AccessToken accessToken = facebook.getOAuthAccessToken(code);
		        //AccessToken accessToken = new AccessToken(code);
		        //facebook.setOAuthAccessToken(accessToken);		        
		        accessToken = facebook.getOAuthAccessToken(code);
		        System.out.println("accessToken: " + ToStringBuilder.reflectionToString(accessToken));		        
		        this.setAccessToken(accessToken);
		        this.storeAccessToken(accessToken);
		        session.setAttribute("facebookAccessToken", accessToken);
		        return accessToken;
		        //On the facebook docs it tells you to send it back to facebook:

		        	//http://developers.facebook.com/docs/authentication/

		        	//Basically you do this:

//		        	   https://graph.facebook.com/oauth/access_token?
//		        	   client_id=YOUR_APP_ID&redirect_uri=YOUR_URL&
//		        	   client_secret=YOUR_APP_SECRET&code=THE_CODE_FROM_ABOVE
//				String redirect = ParamUtil.getString(request, "redirect");
//
//				String code = ParamUtil.getString(request, "code");
//
//				String token = FacebookConnectUtil.getAccessToken(
//					themeDisplay.getCompanyId(), redirect, code);

//				if (Validator.isNotNull(token)) {
//					session.setAttribute(WebKeys.FACEBOOK_ACCESS_TOKEN, token);
//
//					setFacebookCredentials(session, themeDisplay.getCompanyId(), token);
//				}
//				else {
//					return mapping.findForward(ActionConstants.COMMON_REFERER_JSP);
//				}
//
//				response.sendRedirect(redirect);
				
				
				
//				String accessToken = com.liferay.portal.kernel.facebook.FacebookConnectUtil.getFacebookConnect().getAccessToken(this.companyId, this.createRedirectURL(), code);
//				System.out.println("accessToken1: " + accessToken);
//				String url = HttpUtil.addParameter(com.liferay.portal.kernel.facebook.FacebookConnectUtil.getAccessTokenURL(companyId), "client_id", getApiKey());
//				url = HttpUtil.addParameter(url, "redirect_uri", this.createRedirectURL());								
//				url = HttpUtil.addParameter(url, "client_secret", getApiSecret());
//				url = HttpUtil.addParameter(url, "code", code);
//
//				Http.Options options = new Http.Options();
//				options.setLocation(url);
//				options.setPost(true);
//				
//
//				try {
//					String content = HttpUtil.URLtoString(options);
//
//					if (Validator.isNotNull(content)) {
//						int x = content.indexOf("access_token=");
//
//						if (x >= 0) {
//							int y = content.indexOf(CharPool.AMPERSAND, x);
//
//							if (y < x) {
//								y = content.length();
//							}
//
//							accessToken = content.substring(x + 13, y);
//						}
//					}
//				}
//				catch (Exception e) {
//					throw new SystemException(
//						"Unable to retrieve Facebook access token", e);
//				}
//				System.out.println("accessToken2: " + accessToken);
				//this.setExpandoField(socialNetworkAccessTokenField, accessToken);
				//this.storeAccessToken(accessToken);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    	}
		return accessToken;
	}

	
	
	@Override
	public List<ContactDTO> addContactAndCheckDuplicated(
			List<ContactDTO> contacts, User user) {
		long contactId = 1;
    	boolean isDuplicated = false;
    	if(!contacts.isEmpty()) {
    		ContactDTO lastContact = contacts.get(contacts.size()-1);
    		contactId = lastContact.getId() + 1;
    		// check if contact is duplicated
    		for(ContactDTO contact : contacts) {    			
        		if(StringUtils.equalsIgnoreCase(contact.getLastName(), user.getLastName()) && StringUtils.equalsIgnoreCase(contact.getFirstName(), user.getFirstName())) {    			
        			List<SocialNetworkDTO> socialNetworks = contact.getSocialNetworks();
        			socialNetworks.add(new SocialNetworkDTO(getSocialNetworkName()));
        			contact.setSocialNetworks(socialNetworks);
        			isDuplicated = true;
        		} 
        		if(StringUtils.isNotBlank(contact.getEmail()) && StringUtils.equalsIgnoreCase(contact.getEmail(), user.getEmail()) && !isDuplicated) { //agregar !isDuplicated para todas las redes sociales
        			List<SocialNetworkDTO> socialNetworks = contact.getSocialNetworks();
        			socialNetworks.add(new SocialNetworkDTO(getSocialNetworkName()));
        			contact.setSocialNetworks(socialNetworks);
        			isDuplicated = true;
        		}
        	}
    	}    	    	
    	// if it is not duplicated    	    
    	if(!isDuplicated) {
    		ContactDTO contact = new ContactDTO();				
			contact.setId(contactId);
			
			contact.setDisplayName(user.getFirstName() + " " + user.getLastName());
			contact.setFirstName(user.getFirstName());
			contact.setLastName(user.getLastName());
			contact.setName(user.getName());
			contact.setEmail(user.getEmail());
			contact.setGender(StringUtils.equals("male", user.getGender()) ? 1 : 2); //@@ change this
			contact.setMiddleName(user.getMiddleName());
			
			// add picture
			
			System.out.println("user.getPicture(): " + user.toString());
			String pictureURL = "";
			try {
				pictureURL = user.getPicture().getURL().toString();
			} catch(Exception ignored) {}								
			contact.setPictureURL(StringUtils.isNotBlank(pictureURL) ? pictureURL : "http://s.c.lnkd.licdn.com/scds/common/u/images/themes/katy/ghosts/person/ghost_person_200x200_v1.png");			
			//contact.setEmail(person.getEmailAddresses()); @@check duplicity with email addresses
			List<SocialNetworkDTO> socialNetworks = new ArrayList<SocialNetworkDTO>();
			socialNetworks.add(new SocialNetworkDTO(getSocialNetworkName()));
			contact.setSocialNetworks(socialNetworks);
			contacts.add(contact);			
			contactId++;
    	}    	
		return contacts;
	}
	

	@Override
	public User getSocialNetworkCurrentUser() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPictureURLFromSocialNetworkCurrentUser() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public boolean isEnabled() {
		try {
			return com.liferay.portal.kernel.facebook.FacebookConnectUtil.isEnabled(this.companyId);
		} catch (SystemException ignored) {
			return false;
		}
	}
	
	@Override
	public String getApiKey() {    	
		if(apiKey == null) { //@@ hacerlo de manera más automática
    		try {
				apiKey = com.liferay.portal.kernel.facebook.FacebookConnectUtil.getAppId(this.companyId); //@@ ver por qué no anda esto
				apiKey = "509822345747697";
				System.out.println("apiKey: " + apiKey);
			} catch (SystemException e) {
				e.printStackTrace();
				return null;
			}
    	}
    	return apiKey;		
	}
	
	@Override
	public String getApiSecret() {    	
    	if(apiSecret == null) {
    		try {
				apiSecret = com.liferay.portal.kernel.facebook.FacebookConnectUtil.getAppSecret(this.companyId);
				System.out.println("apiSecret1: " + apiSecret);
				apiSecret = "ab70a65ff59b5c7d26782139ea7ee6ff";
			} catch (SystemException e) {
				// @@ change this?
				return null;
			}
    	}
    	return apiSecret;
	}
	
	@Override
	public String getRequestToken() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRequestToken(String redirectURL) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String getSocialNetworkName() {		
		return socialNetworkName;
	}
	
	@Override
	public String getAccessTokenFieldName() {
		return socialNetworkAccessTokenField;
	}
	
	@Override
	public String getTokenSecrtFieldName() {
		return socialNetworkTokenSecretField;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	@Override
	public void storeAccessToken(AccessToken accessToken) {
		this.setExpandoField(getAccessTokenFieldName(), accessToken.getToken());
		this.setExpandoField(socialNetworkExpirationTimeField, accessToken.getExpires());
	}

	@Override
	public AccessToken getAccessToken(String requestToken, String oAuthVerifier) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
