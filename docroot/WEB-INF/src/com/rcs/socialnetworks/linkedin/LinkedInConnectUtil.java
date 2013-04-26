package com.rcs.socialnetworks.linkedin;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.google.code.linkedinapi.client.LinkedInApiClient;
import com.google.code.linkedinapi.client.LinkedInApiClientFactory;
import com.google.code.linkedinapi.client.oauth.LinkedInAccessToken;
import com.google.code.linkedinapi.client.oauth.LinkedInOAuthService;
import com.google.code.linkedinapi.client.oauth.LinkedInOAuthServiceFactory;
import com.google.code.linkedinapi.client.oauth.LinkedInRequestToken;
import com.google.code.linkedinapi.schema.Connections;
import com.google.code.linkedinapi.schema.Person;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.HttpUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.User;
import com.liferay.portal.theme.PortletDisplay;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portlet.PortletURLFactoryUtil;
import com.liferay.portlet.expando.model.ExpandoTableConstants;
import com.liferay.portlet.expando.service.ExpandoTableLocalServiceUtil;
import com.liferay.portlet.expando.service.ExpandoValueLocalServiceUtil;
import com.rcs.socialnetworks.SocialNetworkOAuthData;
import com.rcs.socialnetworks.SocialNetworkOAuthUtil;
import com.rcs.socialnetworks.contact.ContactDTO;
import com.rcs.socialnetworks.contact.SocialNetworkDTO;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import oauth.signpost.OAuth;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import sun.util.logging.resources.logging;

/**
 * @author V. Koshelenko
 */
public class LinkedInConnectUtil extends SocialNetworkOAuthUtil implements SocialNetworkOAuthData<LinkedInRequestToken,LinkedInAccessToken,Person> {
	
	public static String apiKey;
	
	public static String apiSecret;
	
	public static final String socialNetworkName = "linkedin";
	
	public static final String socialNetworkAccessTokenField = "linkedinAccessToken";
	
	public static final String socialNetworkTokenSecretField = "linkedinTokenSecret";
	
	public static final String socialNetworkRequestTokenField = "linkedinRequestToken";
	
    private LinkedInOAuthService oAuthService;
    
    private String redirectURL;

    private LinkedInAccessToken accessToken;

    //private LinkedInConnect linkedInConnect;

    private Person linkedInPerson;

    //private PortletRequest portletRequest;
    
    //private User user;
    
    public LinkedInConnectUtil(PortletRequest portletRequest) {    	
    	super(portletRequest);
    	this.oAuthService = LinkedInOAuthServiceFactory.getInstance().createLinkedInOAuthService(getApiKey(), getApiSecret());    	
    }
    
    public LinkedInConnectUtil() { 
    	super();
    	this.oAuthService = LinkedInOAuthServiceFactory.getInstance().createLinkedInOAuthService(getApiKey(), getApiSecret());
    }
   
    @Override
    public LinkedInAccessToken getAccessToken(LinkedInRequestToken requestToken, String authVerifier) {
        if (this.accessToken == null) {
        	try {
				this.accessToken = this.getLinkedInOAuthService().getOAuthAccessToken(requestToken, authVerifier);
			} catch (SystemException e) { //@@ check this later
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        return this.accessToken;
    }

    public void setAccessToken(LinkedInAccessToken accessToken) {
        this.accessToken = accessToken;
    }

    public LinkedInOAuthService getLinkedInOAuthService() throws SystemException {
        if (this.oAuthService == null) {
        	this.oAuthService = LinkedInOAuthServiceFactory.getInstance().
                    createLinkedInOAuthService(getApiKey(), getApiSecret());
        }
        return this.oAuthService;
    }

    public Person getLinkedInPerson() {
        return linkedInPerson;
    }

    public void setLinkedInPerson(Person person) {
        this.linkedInPerson = person;
    }

    @Override
    public String getAuthorizationURL() {
    	LinkedInRequestToken requestToken = this.getRequestToken();
    	return requestToken.getAuthorizationUrl();
    }
    
//    public static String getAuthorizationURL(PortletRequest portletRequest) {
//    	LinkedInRequestToken requestToken = getRequestToken(portletRequest);
//    	HttpServletRequest request = PortalUtil.getHttpServletRequest(portletRequest);
//    	HttpSession session = request.getSession();
//    	session.setAttribute("requestToken", requestToken);    	
//    	return requestToken.getAuthorizationUrl();
//    }
//    
//    public static String getRequestToken(PortletRequest portletRequest) {
//    	LinkedInRequestToken requestToken = null;
//    	
//    	String redirectURL = createRedirectURL(portletRequest);    	     	    	    	    
//		try {
//			requestToken = this.getLinkedInOAuthService().getOAuthRequestToken(this.redirectURL);
//		} catch (SystemException e) { //@@ check better way
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		};
//    	
//        return requestToken;
//    }
    /*public static LinkedInRequestToken getLinkedInRequestToken(PortletRequest request) throws SystemException {
        return getLinkedInOAuthService(request).getOAuthRequestToken(getRedirectURL(request));
    }*/

    @Override
    public LinkedInRequestToken getRequestToken() {    	
    	LinkedInRequestToken requestToken = null;
    	if(this.redirectURL == null) {
    		this.redirectURL = this.createRedirectURL();
    	}   
    	requestToken = this.getRequestToken(this.redirectURL);
    	HttpServletRequest servletRequest = PortalUtil.getHttpServletRequest(this.portletRequest);
        HttpSession session = servletRequest.getSession();
        session.setAttribute(getRequestTokenName(), requestToken);
    	return requestToken;
//		try {
//			requestToken = this.getLinkedInOAuthService().getOAuthRequestToken(this.redirectURL);
//		} catch (SystemException e) { //@@ check better way
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		};
//    	
//        return requestToken;
    }
    @Override
    public LinkedInRequestToken getRequestToken(String redirectURL) {
    	LinkedInRequestToken requestToken = null;    	     	    	    	    
		try {
			/*System.out.println("redirectURL1: " + redirectURL);
	    	ThemeDisplay themeDisplay= (ThemeDisplay)this.portletRequest.getAttribute(WebKeys.THEME_DISPLAY);
	    	PortletDisplay portletDisplay= themeDisplay.getPortletDisplay();
	    	String portletId= portletDisplay.getId();
	    	HttpServletRequest request = PortalUtil.getHttpServletRequest(this.portletRequest);    	
			javax.portlet.PortletURL portletURL = PortletURLFactoryUtil.create(request, portletId, themeDisplay.getPlid(), PortletRequest.RENDER_PHASE);	    	
	    	redirectURL = portletURL.toString();*/
	    	System.out.println("redirectURL1: " + redirectURL);
			LinkedInOAuthService service = this.getLinkedInOAuthService();
			System.out.println("service: " + ToStringBuilder.reflectionToString(service));			
			requestToken = service.getOAuthRequestToken(redirectURL);	
		} catch (Exception e) { //@@ check better way
			// TODO Auto-generated catch block
			e.printStackTrace();
		};    	
        return requestToken;
    }
    
//    public LinkedInRequestToken getLinkedInRequestToken(String redirectURL) throws SystemException {
//        return this.getLinkedInOAuthService().getOAuthRequestToken(redirectURL);
//    }
    
//    public static String createRedirectURL(PortletRequest portletRequest) {
//    	ThemeDisplay themeDisplay= (ThemeDisplay)portletRequest.getAttribute(WebKeys.THEME_DISPLAY);
//    	PortletDisplay portletDisplay= themeDisplay.getPortletDisplay();
//    	String portletId= portletDisplay.getId();
//    	HttpServletRequest request = PortalUtil.getHttpServletRequest(portletRequest);    	
//		javax.portlet.PortletURL portletURL = PortletURLFactoryUtil.create(request, portletId, themeDisplay.getPlid(), PortletRequest.RENDER_PHASE);
//    	return portletURL.toString();
//    }
  /*  
    @Override
    public LinkedInRequestToken getRequestToken() {
    	if(this.redirectURL == null) {
    		this.redirectURL = LinkedInConnectUtil.createRedirectURL();
   
            //set URL parameters
    		ThemeDisplay themeDisplay= (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
        	PortletDisplay portletDisplay= themeDisplay.getPortletDisplay();
        	System.out.println("portletid: " + portletDisplay.getId());
        	System.out.println("getPortletName: " + portletDisplay.getPortletName());
        	System.out.println("getNamespace: " + portletDisplay.getNamespace());
    		redirectURL = HttpUtil.addParameter(redirectURL, "p_p_id", portletDisplay.getId()); //@@ change the name of the portlet
    		redirectURL = HttpUtil.addParameter(redirectURL, "p_p_state", "normal");
    		redirectURL = HttpUtil.addParameter(redirectURL, "p_p_mode", "view");
    		redirectURL = HttpUtil.addParameter(redirectURL, "socialnetwork", "linkedin");
            //linkedInRedirectURL = HttpUtil.addParameter(linkedInRedirectURL, "scope", "r_basicprofile&r_emailaddress&r_network");

            String strutsActionParameter = "_LOGIN_PORTLET_WAR_loginportlet_action";
            String strutsActionValue = "linked_in";

            redirectURL = HttpUtil.addParameter(redirectURL, strutsActionParameter, strutsActionValue);
    		
    	 
	    	try {
	    		return getLinkedInOAuthService().getOAuthRequestToken(redirectURL);
	    	} catch(Exception e) {
	    		return null;
	    	}
    	}
        
    }*/
  //@@check override
	/*public String getAppId() {
		return "g0o36a8yuz5s";
	}
	//@@check override
	public String getAppSecret() {
		return "aWXvd3XmboW0rRa1";
	}*/	
	
	//@@check override
	@Override
    public boolean isEnabled() {
        return true;
    }
/*
    public static LinkedInConnect getLinkedInConnect() {
        if (LinkedInConnectUtil.linkedInConnect == null) {
            LinkedInConnectUtil.linkedInConnect = new LinkedInConnectImpl();
        }
        return LinkedInConnectUtil.linkedInConnect;
    }

    public static void setLinkedInConnect(LinkedInConnect linkedInConnect) {
        LinkedInConnectUtil.linkedInConnect = linkedInConnect;
    }
    
    
    
    public boolean getLinkedInAccessToken(User user) throws SystemException {
    	boolean userHasLinkedinAccount = false;
    	LinkedInAccessToken linkedinAccessToken = this.getLinkedInAccessToken(portletRequest);        	
    	if(linkedinAccessToken != null) {
    		userHasLinkedinAccount = true;
    	}
    	return userHasLinkedinAccount;
    }
 */   
    // in a future, return a LocalResponse object: issuccess, message, LinkedInAccessToken
	@Override
    public LinkedInAccessToken getAccessToken() {
    	if(this.accessToken != null)
    		return this.accessToken;
    	
    	LinkedInAccessToken accessToken = null;
    	
    	// check if the access token is in the session
    	HttpServletRequest servletRequest = PortalUtil.getHttpServletRequest(this.portletRequest);
        HttpSession session = servletRequest.getSession();
    	if(session.getAttribute("linkedinAccessToken") != null) {
    		accessToken = (LinkedInAccessToken) session.getAttribute("linkedinAccessToken");
    		this.setAccessToken(accessToken);
    		return accessToken;
    	}
    	 
    	
    	// check if the access token is stored
    	String expandoAccessToken = getExpandoAccessToken();
    	Long expirationTime = (Long) this.getUser().getExpandoBridge().getAttribute("linkedinExpirationTime");
    	if(StringUtils.isNotBlank(expandoAccessToken)) {
    		if(expirationTime != null && expirationTime.longValue() >= new Date().getTime()) {
    			//String tokenSecretStr = (String)user.getExpandoBridge().getAttribute("linkedinTokenSecret");        		
    			accessToken = new LinkedInAccessToken(expandoAccessToken, getExpandoTokenSecret());
    			this.setAccessToken(accessToken);        		
    		} else {
    			//@@ do something else here!! delete this?
    			accessToken = new LinkedInAccessToken(expandoAccessToken, getExpandoTokenSecret());
    			this.setAccessToken(accessToken);
    		}
			//@@ refresh token? TODO
    		return accessToken;    		   
    	}
//		try {
//			//User user = PortalUtil.getUser(servletRequest);
////			User user = this.getUser();
////			//getExpandoAccessToken
////			//getExpandoTokenSecret
////			String accessTokenStr = (String) user.getExpandoBridge().getAttribute("linkedinAccessToken");
////	    	Long expirationTime = (Long) user.getExpandoBridge().getAttribute("linkedinExpirationTime");
////	    	if(StringUtils.isNotBlank(accessTokenStr) && expirationTime != null) {
////	    		if(expirationTime.longValue() >= new Date().getTime()) {
////	    			String tokenSecretStr = (String)user.getExpandoBridge().getAttribute("linkedinTokenSecret");        		
////	    			linkedinAccessToken = new LinkedInAccessToken(accessTokenStr, tokenSecretStr);
////	    			this.setAccessToken(linkedinAccessToken);
////	        		return linkedinAccessToken;
////	    		} else {
////	    			//@@ refresh token? TODO
////	    			return null;
////	    		}    	        	
////	    	}
//		} catch (Exception ignored) {
//			return null;
//		}
    	
		// check if access token has to be requested
    	HttpServletRequest originalServletRequest = PortalUtil.getOriginalServletRequest(PortalUtil.getHttpServletRequest(this.portletRequest));
    	if(isSocialNetworkOAuthRequest(originalServletRequest)) {
    		String oauthVerifier = originalServletRequest.getParameter(OAuth.OAUTH_VERIFIER);	        
	        LinkedInRequestToken requestToken = (LinkedInRequestToken) session.getAttribute(getRequestTokenName());
	        //LinkedInOAuthService oAuthService = this.getLinkedInOAuthService();
	        //linkedinAccessToken = oAuthService.getOAuthAccessToken(requestToken, oauthVerifier);
	        accessToken = this.getAccessToken(requestToken, oauthVerifier);
	        this.storeAccessToken(accessToken);
	        //parseAndStoreAccessToken
//          if (linkedinAccessToken != null) {                	
//              this.setAccessToken(linkedinAccessToken);                    
//              User user = PortalUtil.getUser(servletRequest);
//      		user.getExpandoBridge().setAttribute("linkedinAccessToken", linkedinAccessToken.getToken().toString());
//      		user.getExpandoBridge().setAttribute("linkedinTokenSecret", linkedinAccessToken.getTokenSecret().toString());
//      		user.getExpandoBridge().setAttribute("linkedinExpirationTime", linkedinAccessToken.getExpirationTime().getTime());
//      		return linkedinAccessToken;
//          }	
    	}
//    	HttpServletRequest originalServletRequest = PortalUtil.getOriginalServletRequest(PortalUtil.getHttpServletRequest(portletRequest));
//    	//boolean isLinkedinOAuthRequest = originalServletRequest.getParameter("socialnetwork") != null && StringUtils.equals("linkedin", originalServletRequest.getParameter("socialnetwork"));
//    	if(isSocialNetworkOAuthRequest(originalServletRequest) && originalServletRequest.getParameter(OAuth.OAUTH_TOKEN) != null && session.getAttribute("requestToken") != null) {
//    		//@@ que el session requestToken sea igual al que viene en la url
//			String oauthVerifier = originalServletRequest.getParameter(OAuth.OAUTH_VERIFIER);	        
//	        LinkedInRequestToken requestToken = (LinkedInRequestToken) session.getAttribute("requestToken");	        	        
//	        try {	            
//                LinkedInOAuthService oAuthService = this.getLinkedInOAuthService();
//                linkedinAccessToken = oAuthService.getOAuthAccessToken(requestToken, oauthVerifier);
//                if (linkedinAccessToken != null) {                	
//                    this.setAccessToken(linkedinAccessToken);                    
//                    User user = PortalUtil.getUser(servletRequest);
//            		user.getExpandoBridge().setAttribute("linkedinAccessToken", linkedinAccessToken.getToken().toString());
//            		user.getExpandoBridge().setAttribute("linkedinTokenSecret", linkedinAccessToken.getTokenSecret().toString());
//            		user.getExpandoBridge().setAttribute("linkedinExpirationTime", linkedinAccessToken.getExpirationTime().getTime());
//            		return linkedinAccessToken;
//                }	           
//	        } catch (Exception ignored) {
//	            return null;
//	        }
//		}
    	return accessToken;
    }
    
    @Override
    public boolean currentUserHasAccount() {
    	LinkedInAccessToken accessToken = this.getAccessToken();
    	return accessToken != null;
    }
        
    //contacts = LinkedInConnectUtil.addContacts(portletRequest, contacts);
    
    @Override
    public List<ContactDTO> addContacts(List<ContactDTO> contacts) {
    	LinkedInAccessToken linkedinAccessToken = this.getAccessToken();
    	//@@ obtener directamente de getLinkedin
    	String consumerKey = this.getApiKey();
        String consumerSecret = this.getApiSecret();        
        LinkedInApiClientFactory clientFactory = LinkedInApiClientFactory.newInstance(consumerKey, consumerSecret);                
        LinkedInApiClient client = clientFactory.createLinkedInApiClient(linkedinAccessToken);                    			    			    	
		Connections connections = client.getConnectionsForCurrentUser();
    	if(connections != null) {    		
			List<Person> persons = connections.getPersonList(); //@@ ver cómo chequear esto
			int i = 0;
			for(Person person : persons) {
				contacts = this.addContactAndCheckDuplicated(contacts, person);
				if(i > 10)
					break;//@@ remove this
				i++;
			}			
		}
    	return contacts;    	
    }
    //@@override después
    public List<ContactDTO> addContacts(PortletRequest portletRequest, List<ContactDTO> contacts, Collection<?> linkedinContacts) throws SystemException {
    	/*String consumerKey = LinkedInConnectUtil.getAppId(portletRequest);
        String consumerSecret = LinkedInConnectUtil.getAppSecret(portletRequest);
        LinkedInApiClientFactory clientFactory = LinkedInApiClientFactory.newInstance(consumerKey, consumerSecret);                
        LinkedInApiClient client = clientFactory.createLinkedInApiClient(linkedinAccessToken);                    			    			    	
		Connections connections = client.getConnectionsForCurrentUser();*/
    	if(linkedinContacts != null) {    		
			List<Person> persons = (List<Person>) linkedinContacts; //@@ ver cómo chequear esto
			int i = 0;
			for(Person person : persons) {
				contacts = this.addContactAndCheckDuplicated(contacts, person);
				if(i > 20)
					break; //@@ remove this
				i++;
			}			
		}
    	return contacts;    	
    }
    
    @Override
    public List<ContactDTO> addContactAndCheckDuplicated(List<ContactDTO> contacts, Person person) {
    	long contactId = 1;
    	boolean isDuplicated = false;
    	if(!contacts.isEmpty()) {
    		ContactDTO lastContact = contacts.get(contacts.size()-1);
    		contactId = lastContact.getId() + 1;
    		// check if contact is duplicated
    		for(ContactDTO contact : contacts) {
        		if(StringUtils.equalsIgnoreCase(contact.getLastName(), person.getLastName()) && StringUtils.equalsIgnoreCase(contact.getFirstName(), person.getFirstName())) {    			
        			List<SocialNetworkDTO> socialNetworks = contact.getSocialNetworks();
        			socialNetworks.add(new SocialNetworkDTO("linkedin")); //@@ add constant
        			contact.setSocialNetworks(socialNetworks);
        			isDuplicated = true;
        		}
        	}
    	}    	    	
    	// if it is not duplicated
    	if(!isDuplicated) {
    		ContactDTO contact = new ContactDTO();				
			contact.setId(contactId);
			contact.setFirstName(person.getFirstName());
			//contact.setMiddleName(person.getMiddleName());
			contact.setLastName(person.getLastName());
			String pictureURL = StringUtils.isNotBlank(person.getPictureUrl()) ? person.getPictureUrl() : "http://s.c.lnkd.licdn.com/scds/common/u/images/themes/katy/ghosts/person/ghost_person_200x200_v1.png"; //@@ change this, add constant
			contact.setPictureURL(pictureURL);			
			List<SocialNetworkDTO> socialNetworks = new ArrayList<SocialNetworkDTO>();
			socialNetworks.add(new SocialNetworkDTO("linkedin"));
			contact.setSocialNetworks(socialNetworks);
			contacts.add(contact);			
			contactId++;    				
    	}
    	return contacts;
    }

    /*
	@Override
	public boolean currentUserHasAccount(PortletRequest portletRequest)
			throws SystemException {
		// TODO Auto-generated method stub
		return false;
	}*/
    @Override
	public String getApiKey() {
    	return "g0o36a8yuz5s";
		/*if(LinkedInConnectUtil.apiKey == null) {
			LinkedInConnectUtil.apiKey = "g0o36a8yuz5s"; //@@change later
		}
		return LinkedInConnectUtil.apiKey;*/
	}	
	//@@ override or not?
    @Override
	public String getApiSecret() {
    	return "aWXvd3XmboW0rRa1";
		/*if(LinkedInConnectUtil.apiSecret == null) {
			LinkedInConnectUtil.apiSecret = "aWXvd3XmboW0rRa1"; //@@change later, and change to appSecret
		}
		return LinkedInConnectUtil.apiSecret;*/
	}

	//@@ storeapisecret & api key?
/*
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
	}*/
/*
	@Override
	public List<ContactDTO> addContacts(PortletRequest portletRequest, List<ContactDTO> contacts) {
		// TODO Auto-generated method stub
		return null;
	}*/

//	@Override
//	public boolean currentUserHasAccount(PortletRequest portletRequest) {
//		// TODO Auto-generated method stub
//		return false;
//	}
//
//	@Override
//	public boolean currentUserHasAccount() {
//		// TODO Auto-generated method stub
//		return false;
//	}
/*
	@Override
	public Object getAccessToken(Object requestToken, String oAuthVerifier) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getRequestToken() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getRequestToken(String redirectURL) {
		// TODO Auto-generated method stub
		return null;
	}
*/
//	@Override
//	public List addContactAndCheckDuplicated(List contacts, Object contact) {
//		// TODO Auto-generated method stub
//		return null;
//	}
/*
	@Override
	public List addContacts(List contacts) {
		// TODO Auto-generated method stub
		return null;
	}*/

	@Override
	public List<ContactDTO> addContacts(PortletRequest portletRequest,
			List<ContactDTO> contacts) {
		// TODO Auto-generated method stub
		return null;
	}
	
	//override?
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
	
	@Override
	public String getRequestTokenName() {
		return socialNetworkRequestTokenField;
	}
	
	@Override
	public void storeAccessToken(LinkedInAccessToken accessToken) {
		this.storeAccessToken(accessToken.getToken(), accessToken.getTokenSecret());
		//store expiration time
	}
}
