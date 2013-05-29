package com.rcs.socialnetworks;

import java.util.List;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;

import oauth.signpost.OAuth;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.HttpUtil;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.Company;
import com.liferay.portal.model.User;
import com.liferay.portal.model.UserConstants;
import com.liferay.portal.security.auth.CompanyThreadLocal;
import com.liferay.portal.service.persistence.CompanyUtil;
import com.liferay.portal.theme.PortletDisplay;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portlet.PortletURLFactoryUtil;
import com.liferay.portlet.expando.model.ExpandoTableConstants;
import com.liferay.portlet.expando.model.ExpandoValue;
import com.liferay.portlet.expando.service.ExpandoValueLocalServiceUtil;
import com.rcs.socialnetworks.contact.ContactDTO;
import com.rcs.socialnetworks.contact.SocialNetworkDTO;

/**
 * This abstract class is the parent of all social networks used in this portlet, 
 * it defines some common attributes and methods
 * 
 * @author flor - florencia@rotterdam-cs.com
 *
 * @param <AccessToken>
 * @param <Contact>
 */
public abstract class SocialNetworkOAuth<AccessToken,Contact> implements SocialNetworkOAuthInterface<AccessToken,Contact> {		
				
	public final SocialNetworkDTO SOCIAL_NETWORK_DTO = new SocialNetworkDTO(this.getSocialNetworkName());
	
	public static final String OAUTH_CALLBACK_STRUTS_ACTION = "/c/portal/oauthcallback";
	
	public static final String SOCIAL_NETWORK_PARAM = "socialnetwork";
	
	/**
	 * The api key needed by oauth to
	 * authenticate this client
	 */
	protected String apiKey;
	
	/**
	 * The secret string needed by oauth
	 * to authenticate this client
	 */
	protected String apiSecret;
	
	/**
	 * Check if this particular social
	 * network is enabled
	 */	
	public boolean isEnabled;
	
	/**
	 * The max amount of contacts shown 
	 * in this social network graph
	 */
	public int maxContactsShown;
	/**
	 * The current portlet request of 
	 * this portlet
	 */
	protected PortletRequest portletRequest;
	
	/**
	 * The redirect url used by oauth
	 * to send the user to this url
	 */
	protected String redirectURL;						
	
	/**
	 * The token needed by oauth to
	 * access the user private resources 
	 * on behalf of the current user
	 */
	protected AccessToken accessToken;
	
	/**
	 * The current social network user
	 */
	private Contact socialNetworkUser;
	
	/**
	 * The current Liferay user logged in,
	 * this is the resource owner, 
	 * we will access their private
	 * resources.
	 * 
	 */
	protected User user;	
	
	/**
	 * This is the default picture url 
	 * that will be used for contacts 
	 * (Liferay's user male portrait)
	 */
	protected String defaultPictureURL;
	
	public SocialNetworkOAuth(PortletRequest portletRequest) {
		this.portletRequest = portletRequest;
		apiKey = getApiKey();
		apiSecret = getApiSecret();
	}
	
	public SocialNetworkOAuth() {
		apiKey = getApiKey();
		apiSecret = getApiSecret();
	}
		
	public String getApiKey() {
		if(this.apiKey == null) {
    		this.apiKey = PropsUtil.get(this.getSocialNetworkName() + ".oauth.app.id");
    	}
    	return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getApiSecret() {
		if(apiSecret == null) {
    		apiSecret = PropsUtil.get(this.getSocialNetworkName() + ".oauth.app.secret");
    	}
    	return apiSecret;						
	}

	public void setApiSecret(String apiSecret) {
		this.apiSecret = apiSecret;
	}

	public boolean isEnabled() {
		String enabled = PropsUtil.get(this.getSocialNetworkName() + ".oauth.enabled");
		return StringUtils.equals("true", enabled);
	}	
	
	public PortletRequest getPortletRequest() {
		return this.portletRequest;
	}
	
	public void setPortletRequest(PortletRequest portletRequest) {
		this.portletRequest = portletRequest;
	}
	
	/**
	 * This is the url the user will be redirected to once
	 * it authorizes this portlet to access their private
	 * information on behalf of him/her. It takes the url
	 * where the portlet was installed.
	 * 
	 * @return
	 */
	public String createRedirectURL() {				
		if(this.portletRequest != null) {
	    	ThemeDisplay themeDisplay= (ThemeDisplay) this.portletRequest.getAttribute(WebKeys.THEME_DISPLAY);
	    	PortletDisplay portletDisplay= themeDisplay.getPortletDisplay();
	    	String portletId= portletDisplay.getId();
	    	HttpServletRequest request = PortalUtil.getHttpServletRequest(this.portletRequest);    	
			javax.portlet.PortletURL portletURL = PortletURLFactoryUtil.create(request, portletId, themeDisplay.getPlid(), PortletRequest.RENDER_PHASE);		//		
			String redirectURL = HttpUtil.addParameter(portletURL.toString(), "p_p_state", "normal");
			redirectURL = HttpUtil.addParameter(redirectURL, "p_p_mode", "view");
			redirectURL = HttpUtil.addParameter(redirectURL, SOCIAL_NETWORK_PARAM, this.getSocialNetworkName());
			this.redirectURL = redirectURL;
		}
    	return this.redirectURL;
    }
	
	public String getRedirectURL() {
		if(this.redirectURL == null) {
			this.redirectURL = this.createRedirectURL();			
		}
		return this.redirectURL;		
	}
	
	/**
	 * Get the current 
	 * Liferay User
	 * 
	 * @return
	 */
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
	
	/**
	 * This is a generic way to get the expando
	 * string field needed for every social network,
	 * like access token, token secret, etc.
	 * 
	 * @param fieldName
	 * @return
	 */
	public String getExpandoStringField(String fieldName) {
		String field = null;
		if(this.getUser() != null) {
			try {
				User user = this.getUser();
				field = (String) user.getExpandoBridge().getAttribute(fieldName);		    	
			} catch (Exception ignored) {
				return field;
			}
		}
		return field;
	}
	
	/**
	 * This is a generic way to get the expando
	 * Long field needed for every social network,
	 * like token expiration time, etc.
	 * 
	 * @param fieldName
	 * @return
	 */
	public Long getExpandoLongField(String fieldName) {
		Long field = null;
		if(this.getUser() != null) {
			try {
				User user = this.getUser();
				field = (Long) user.getExpandoBridge().getAttribute(fieldName);		    	
			} catch (Exception ignored) {
				return field;
			}
		}
		return field;
	}
	
	/**
	 * This is a generic way to store the expando
	 * string fields needed for every social network,
	 * like access token, token secret, etc.	
	 */
	public void setExpandoField(String fieldName, String field) {		
		if(this.getUser() != null) {
			try {				
				this.getUser().getExpandoBridge().setAttribute(fieldName, field);																		  
			} catch (Exception ignored) { }
		}		
	}
	
	/**
	 * This is a generic way to store the expando
	 * string fields needed for every social network,
	 * like token expiration time, etc.
	 * 
	 */
	public void setExpandoField(String fieldName, Long field) {		
		if(this.getUser() != null) {
			try {				
				this.getUser().getExpandoBridge().setAttribute(fieldName, field);																		  
			} catch (Exception ignored) { }
		}		
	}
	
	/**
	 * Delete a certain expando field
	 * 
	 * @param fieldName
	 */
	public void deleteExpandoValue(String fieldName) {		
		if(this.getUser() != null) {
			try {
				ExpandoValue expandoValue = ExpandoValueLocalServiceUtil.getValue(this.getUser().getCompanyId(), User.class.getName(), ExpandoTableConstants.DEFAULT_TABLE_NAME, fieldName, this.getUser().getUserId());
				if(expandoValue != null) {
					ExpandoValueLocalServiceUtil.deleteExpandoValue(expandoValue);
				}																								
			} catch (Exception ignored) { 
				ignored.printStackTrace();
			}
		}		
	}
	
	/**
	 * Delete all the expando fields 
	 * for this social network
	 */
	public void deleteExpandoValues() {
		if(this.getUser() != null) {
			try {				
				// get expando column names				
				String [] socialNetworkExpandoFields = this.getSocialNetworkExpandoFields();
				for(String expandoField : socialNetworkExpandoFields) {
					this.deleteExpandoValue(expandoField);
				}																										 
			} catch (Exception ignored) { 
				ignored.printStackTrace();
			}
		}		
	}
	/**
	 * Check if the user is being redirected
	 * by a social network with an oauth token
	 * 
	 * @param request
	 * @return
	 */
	public boolean isSocialNetworkOAuthRequest(HttpServletRequest request) {		
		
		// this is true for a linkedin and twitter oauth access token request
		if(StringUtils.isNotBlank(request.getParameter(SOCIAL_NETWORK_PARAM)) 
				&& StringUtils.equals(getSocialNetworkName(), request.getParameter(SOCIAL_NETWORK_PARAM))
				&& StringUtils.isNotBlank(request.getParameter(OAuth.OAUTH_TOKEN))
				&& StringUtils.isNotBlank(request.getParameter(OAuth.OAUTH_VERIFIER)))
			return true;
		// this is true for a google and facebook oauth access token request
		if(StringUtils.isNotBlank(request.getParameter(SOCIAL_NETWORK_PARAM)) 
				&& StringUtils.equals(getSocialNetworkName(), request.getParameter(SOCIAL_NETWORK_PARAM))
				&& StringUtils.isNotBlank(request.getParameter("code")))
			return true;
		
		return false;
	}
	
	/**
	 * This method checks if the current user has an 
	 * authenticated access token
	 * @return
	 */
	public boolean currentUserHasAccount() {
		AccessToken accessToken = this.getAccessToken();
		return accessToken != null;
	}
	
	public void setAccessToken(AccessToken accessToken) {
		this.accessToken = accessToken;
	}
	
	/**
	 * Add the current contact to the list, but first
	 * check if it is duplicated, and, if so, only add
	 * the social network the contact belongs to
	 */
	public List<ContactDTO> addContactAndCheckDuplicated(
			List<ContactDTO> contacts, Contact person) {
		long contactId = 1;
    	boolean isDuplicated = false;
    	SocialNetworkDTO socialNetworkDTO = new SocialNetworkDTO(getSocialNetworkName());
    	if(!contacts.isEmpty()) {
    		ContactDTO lastContact = contacts.get(contacts.size()-1);
    		contactId = lastContact.getId() + 1;
    		// check if contact is duplicated
    		for(ContactDTO contact : contacts) {
    			isDuplicated = this.isDuplicatedContact(contact, person);
    			if(isDuplicated) {
    				List<SocialNetworkDTO> socialNetworks = contact.getSocialNetworks();
        			socialNetworks.add(socialNetworkDTO);
        			contact.setSocialNetworks(socialNetworks);
        			break;
    			}
        	}
    	}    	    	
    	// if it is not duplicated
    	if(!isDuplicated) {
    		ContactDTO contact = this.createContactDTO(person);
    		contact.setId(contactId);
    		contacts.add(contact);			
			contactId++;    				
    	}
    	return contacts;	
	}
	
	/**
	 * The social network dto for a particular
	 * social network will always be the same
	 * 
	 * @return
	 */
	public SocialNetworkDTO getSocialNetworkDTO() {
		return this.SOCIAL_NETWORK_DTO;
	}
	
	/**
	 * Get the portal URL,
	 * eg: https://localhost:8080
	 * 
	 * @return the portal URL
	 */
	public String getPortalURL() {
		String portalURL = "";
		if(this.portletRequest != null) {			
			portalURL = PortalUtil.getPortalURL(portletRequest);			
		} else {
			Company company;
			try {
				company = CompanyUtil.fetchByPrimaryKey(CompanyThreadLocal.getCompanyId());
				portalURL = PortalUtil.getPortalURL(company.getVirtualHostname(), PortalUtil.getPortalPort(true), true);
			} catch (SystemException ignored) { }						
		}
		return portalURL;
	}
	
	/**
	 * Get the oauth callback URL,
	 * eg: https://localhost:8080/c/portal/oauthcallback
	 * 
	 * @return the oauth callback URL
	 */
	public String getOAuthCallbackURL() {
		return getPortalURL() + OAUTH_CALLBACK_STRUTS_ACTION;
	}
	
	/**
	 * Get the maximum amount of 
	 * contacts shown in the graph 
	 * for this social network
	 * 
	 * @return
	 */
	public int getMaxContactsShown() {
		String maxContactsShown = PropsUtil.get(this.getSocialNetworkName() + ".max.contacts.shown");		
		if(StringUtils.isNotBlank(maxContactsShown)) {
			return Integer.parseInt(maxContactsShown);
		} else {
			return 0;
		}		
	}

	public Contact getSocialNetworkUser() {
		return socialNetworkUser;
	}

	public void setSocialNetworkUser(Contact socialNetworkUser) {
		this.socialNetworkUser = socialNetworkUser;
	}
	
	public String getDefaultPictureURL() {
		if(this.defaultPictureURL == null) {
			ThemeDisplay themeDisplay = (ThemeDisplay) this.portletRequest.getAttribute(WebKeys.THEME_DISPLAY);
			this.defaultPictureURL = UserConstants.getPortraitURL(themeDisplay.getPathImage(), true, 0);
		}		
		return this.defaultPictureURL;
	}
}
