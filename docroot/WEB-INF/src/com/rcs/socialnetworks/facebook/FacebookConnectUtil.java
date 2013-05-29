package com.rcs.socialnetworks.facebook;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.util.PortalUtil;
import com.rcs.socialnetworks.OAuth20Interface;
import com.rcs.socialnetworks.SocialNetworkOAuth;
import com.rcs.socialnetworks.contact.ContactDTO;
import com.rcs.socialnetworks.contact.SocialNetworkDTO;

import facebook4j.Facebook;
import facebook4j.FacebookException;
import facebook4j.FacebookFactory;
import facebook4j.Friend;
import facebook4j.Reading;
import facebook4j.ResponseList;
import facebook4j.User;
import facebook4j.auth.AccessToken;

/**
 * Class that handles the OAuth 2.0 authorization flow
 * for Facebook, it uses the authorization code grant type, 
 * that is used to obtain both access tokens and refresh 
 * tokens and is optimized for confidential clients.
 * 
 *   +----------+
     | Resource |
     |   Owner  |
     |          |
     +----------+
          ^
          |
         (B)
     +----|-----+          Client Identifier      +---------------+
     |         -+----(A)-- & Redirection URI ---->|               |
     |  User-   |                                 | Authorization |
     |  Agent  -+----(B)-- User authenticates --->|     Server    |
     |          |                                 |               |
     |         -+----(C)-- Authorization Code ---<|               |
     +-|----|---+                                 +---------------+
       |    |                                         ^      v
      (A)  (C)                                        |      |
       |    |                                         |      |
       ^    v                                         |      |
     +---------+                                      |      |
     |         |>---(D)-- Authorization Code ---------'      |
     |  Client |          & Redirection URI                  |
     |         |                                             |
     |         |<---(E)----- Access Token -------------------'
     +---------+       (w/ Optional Refresh Token)
 * 
 * The flow illustrated in Figure 3 includes the following steps:

   (A)  The client initiates the flow by directing the resource owner's
        user-agent to the authorization endpoint.  The client includes
        its client identifier, requested scope, local state, and a
        redirection URI to which the authorization server will send the
        user-agent back once access is granted (or denied).

   (B)  The authorization server authenticates the resource owner (via
        the user-agent) and establishes whether the resource owner
        grants or denies the client's access request.

   (C)  Assuming the resource owner grants access, the authorization
        server redirects the user-agent back to the client using the
        redirection URI provided earlier (in the request or during
        client registration).  The redirection URI includes an
        authorization code and any local state provided by the client
        earlier.

   (D)  The client requests an access token from the authorization
        server's token endpoint by including the authorization code
        received in the previous step. When making the request, the
        client authenticates with the authorization server. The client
        includes the redirection URI used to obtain the authorization
        code for verification.

   (E)  The authorization server authenticates the client, validates the
        authorization code, and ensures that the redirection URI
        received matches the URI used to redirect the client in
        step (C).  If valid, the authorization server responds back with
        an access token and, optionally, a refresh token.

 * More info at: http://tools.ietf.org/html/rfc6749#section-4.1
 * 
 * @author flor - florencia@rotterdam-cs.com
 *
 */
public class FacebookConnectUtil extends SocialNetworkOAuth<AccessToken,Friend> implements OAuth20Interface {

	/**
	 * The name of this social network
	 */
	public static final String SOCIAL_NETWORK_NAME = "facebook";
	
	/**
	 * Access token field name that 
	 * is stored in the database
	 */
	public static final String ACCESS_TOKEN_FIELD = "facebookAccessToken";
			
	/**
	 * Expiration time field name that is 
	 * stored in the database, is the date 
	 * where the access token expires
	 */
	public static final String EXPIRATION_TIME_FIELD = "facebookExpirationTime";
	
	/**
	 * The auth parameter for the oauth URL
	 */
	public static final String AUTH_URL_PARAM = "facebookAuthURL";

	/**
	 * This object holds all 
	 * the oauth parameters 
	 * and is neccessary for 
	 * the oauth handshake
	 */
	private Facebook facebook;		
	
	/**
	 * Permissions needed to access the user resources
	 */
	public static final String COMMA_SEPARATED_PERMISSIONS = "email,read_friendlists";
	
	public FacebookConnectUtil() {
		super();
		this.facebook = new FacebookFactory().getInstance();
		this.facebook.setOAuthAppId(getApiKey(), getApiSecret());
		this.facebook.setOAuthPermissions(getScope());	
	}
	
	public FacebookConnectUtil(PortletRequest portletRequest) {
		super(portletRequest);
		this.facebook = new FacebookFactory().getInstance();
		this.facebook.setOAuthAppId(getApiKey(), getApiSecret());
		this.facebook.setOAuthPermissions(getScope());
	}		
	
	@Override
	public List<ContactDTO> addContacts(List<ContactDTO> contacts) {
		try {
			ResponseList<Friend> friends = this.facebook.getFriends(new Reading().fields("id","name","first_name","last_name","link","username","gender","locale","picture"));
			int i = 0;
			for(Friend friend : friends) {	
				contacts = this.addContactAndCheckDuplicated(contacts, friend);
				int maxContactsShown = this.getMaxContactsShown();
				if(i > maxContactsShown)
					break;
				i++;	
			}
		} catch (FacebookException e) {
			// TODO @@ Show proper error message
			e.printStackTrace();
		}
		return contacts;
	}

	@Override
	public String getAuthorizationURL() {		
		try {
			String callbackURLEncoded = java.net.URLEncoder.encode(this.getOAuthCallbackURL(), "UTF-8");
			String actualRedirect = this.getRedirectURL();
			byte[] actualRedirectEncoded = Base64.encodeBase64(actualRedirect.getBytes());
			String accessTokenURL = this.facebook.getOAuthAuthorizationURL(callbackURLEncoded, new String(actualRedirectEncoded));			
			return accessTokenURL;
		} catch(UnsupportedEncodingException e) {
			e.printStackTrace();
			return "";
		}
	}
	
	@Override
	public AccessToken getAccessToken() {		       
        AccessToken accessToken = null;
        
        if(this.accessToken != null) {
        	return this.accessToken;
        }            
    	
    	// check if the access token is stored in database
    	String expandoAccessToken = this.getExpandoStringField(ACCESS_TOKEN_FIELD);
    	Long expirationTime = this.getExpandoLongField(EXPIRATION_TIME_FIELD);
    	if(StringUtils.isNotBlank(expandoAccessToken)) {
    		if(expirationTime != null && expirationTime.longValue() >= new Date().getTime()) {    			
				accessToken = new AccessToken(expandoAccessToken);
				this.setAccessToken(accessToken);
				this.facebook.setOAuthAccessToken(accessToken);    		
	    		return accessToken;    		   
	    	} else {
	    		//TODO @@ Not sure if I should do something here
	    		accessToken = new AccessToken(expandoAccessToken);
				this.setAccessToken(accessToken);
				this.facebook.setOAuthAccessToken(accessToken);
				return accessToken;
	    	}
    	}

    	// check if this is the answer to a code token request
		HttpServletRequest originalServletRequest = PortalUtil.getOriginalServletRequest(PortalUtil.getHttpServletRequest(this.portletRequest));		
		if(isSocialNetworkOAuthRequest(originalServletRequest)) {
			String code = originalServletRequest.getParameter(OAuth20Interface.CODE);			
			try {				
		        facebook.setOAuthPermissions(getScope());		        
		        accessToken = facebook.getOAuthAccessToken(code);		        		       
		        this.setAccessToken(accessToken);
		        this.storeAccessToken(accessToken);
		        this.facebook.setOAuthAccessToken(accessToken);		        
		        return accessToken;		        
			} catch (Exception e) {
				//possible exception:
				// FacebookException [statusCode=400, response=HttpResponse{statusCode=400, responseAsString='{"error":{"message":"redirect_uri isn't an absolute URI. Check RFC 3986.","type":"OAuthException","code":191}}
		        		//', is=sun.net.www.protocol.http.HttpURLConnection$HttpInputStream@a4f1d0, streamConsumed=true}, errorType=OAuthException, errorMessage=redirect_uri isn't an absolute URI. Check RFC 3986., errorCode=191]
				e.printStackTrace();
			}
		}    	
		return accessToken;
	}
		
	@Override
	public Friend getSocialNetworkCurrentUser() {
		if(this.getSocialNetworkUser() == null) {		
			try {
				User facebookUser = this.facebook.getMe();
				this.setSocialNetworkUser((Friend)facebookUser);
			} catch (FacebookException e) {
				// TODO @@ Show proper error message
				e.printStackTrace();
			}		
		} 
		return this.getSocialNetworkUser();
	}

	@Override
	public String getPictureURLFromSocialNetworkCurrentUser() {
		String pictureURL = "";
		try {
			User facebookUser = this.getSocialNetworkCurrentUser();
			facebookUser.getPicture().getURL().toString();
		} catch (Exception e) {
			// TODO @@ Show proper error message
			e.printStackTrace();
		}
		return pictureURL;
	}	
		
	@Override
	public String getSocialNetworkName() {		
		return SOCIAL_NETWORK_NAME;
	}	
		
	@Override
	public void storeAccessToken(AccessToken accessToken) {
		this.setExpandoField(ACCESS_TOKEN_FIELD, accessToken.getToken());		
		this.setExpandoField(EXPIRATION_TIME_FIELD, new Date().getTime() + accessToken.getExpires());
	}	

	@Override
	public ContactDTO createContactDTO(Friend user) {
		ContactDTO contact = new ContactDTO();								
		contact.setDisplayName(user.getFirstName() + " " + user.getLastName());
		contact.setFirstName(user.getFirstName());
		contact.setLastName(user.getLastName());
		contact.setName(user.getName());
		contact.setEmail(user.getEmail());
		contact.setGender(StringUtils.equals("male", user.getGender()) 
					? ContactDTO.Gender.MALE.value 
					: ContactDTO.Gender.FEMALE.value);			
		contact.setMiddleName(user.getMiddleName());
		contact.setScreenName(user.getUsername());
		// add picture						
		String pictureURL = "";
		try {
			pictureURL = user.getPicture().getURL().toString();
		} catch(Exception ignored) {}								
		contact.setPictureURL(StringUtils.isNotBlank(pictureURL) ? pictureURL : this.getDefaultPictureURL());			
		List<SocialNetworkDTO> socialNetworks = new ArrayList<SocialNetworkDTO>();
		socialNetworks.add(this.SOCIAL_NETWORK_DTO);
		contact.setSocialNetworks(socialNetworks);
		return contact;
	}

	@Override
	public boolean isDuplicatedContact(ContactDTO contact, Friend user) {
		if(!contact.getSocialNetworks().contains(this.SOCIAL_NETWORK_DTO)) {
			// check if first and last name are equal
    		if(StringUtils.equalsIgnoreCase(contact.getLastName(), user.getLastName()) && StringUtils.equalsIgnoreCase(contact.getFirstName(), user.getFirstName())) {    			
    			return true;
    		} 
    		// check if emails are equal
    		if(StringUtils.isNotBlank(contact.getEmail()) && StringUtils.equalsIgnoreCase(contact.getEmail(), user.getEmail())) {
    			return true;
    		}
    		// check if screennames are equal
    		if(StringUtils.isNotBlank(contact.getScreenName()) && StringUtils.equalsIgnoreCase(contact.getScreenName(), user.getUsername())) {
    			return true;
    		}
		}
		return false;
	}

	@Override
	public String getScope() {
		return COMMA_SEPARATED_PERMISSIONS;
	}
	
	@Override
	public String[] getSocialNetworkExpandoFields() {
		return new String[] {ACCESS_TOKEN_FIELD, EXPIRATION_TIME_FIELD};		
	}
}
