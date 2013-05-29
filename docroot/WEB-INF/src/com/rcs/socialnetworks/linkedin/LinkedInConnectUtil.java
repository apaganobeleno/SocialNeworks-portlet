package com.rcs.socialnetworks.linkedin;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import oauth.signpost.OAuth;

import org.apache.commons.lang.StringUtils;

import com.google.code.linkedinapi.client.LinkedInApiClient;
import com.google.code.linkedinapi.client.LinkedInApiClientFactory;
import com.google.code.linkedinapi.client.oauth.LinkedInAccessToken;
import com.google.code.linkedinapi.client.oauth.LinkedInOAuthService;
import com.google.code.linkedinapi.client.oauth.LinkedInOAuthServiceFactory;
import com.google.code.linkedinapi.client.oauth.LinkedInRequestToken;
import com.google.code.linkedinapi.schema.Connections;
import com.google.code.linkedinapi.schema.Person;
import com.google.code.linkedinapi.schema.TwitterAccount;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.util.PortalUtil;
import com.rcs.socialnetworks.OAuth10Interface;
import com.rcs.socialnetworks.SocialNetworkOAuth;
import com.rcs.socialnetworks.contact.ContactDTO;
import com.rcs.socialnetworks.contact.SocialNetworkDTO;
import com.rcs.socialnetworks.twitter.TwitterConnectUtil;

/**
 * This is the class that handles the LinkedIn OAuth 1.0a 
 * connection to access the user protected resources in LinkedIn
 * 
 * @author flor
 *
 */
public class LinkedInConnectUtil extends SocialNetworkOAuth<LinkedInAccessToken,Person> implements OAuth10Interface<LinkedInRequestToken, LinkedInAccessToken> {
	
	public static final String SOCIAL_NETWORK_NAME = "linkedin";
		
	public static final String ACCESS_TOKEN_FIELD = "linkedinAccessToken";
	
	public static final String TOKEN_SECRET_FIELD = "linkedinTokenSecret";
	
	public static final String REQUEST_TOKEN_FIELD = "linkedinRequestToken";
	
	public static final String EXPIRATION_TIME_FIELD = "linkedinExpirationTime";	
		
	/**
	 * The auth parameter for the oauth URL
	 */
	public static final String AUTH_URL_PARAM = "linkedinAuthURL";
	
    private LinkedInOAuthService oAuthService;
    
    private LinkedInAccessToken accessToken;

    private Person linkedInPerson;

    private LinkedInApiClient linkedInClient;
    
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
			} catch (SystemException e) {
				//TODO @@ Show proper error to user
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

    @Override
    public String getAuthorizationURL() {
    	LinkedInRequestToken requestToken = this.getRequestToken();
    	return requestToken.getAuthorizationUrl();
    }    

    @Override
    public LinkedInRequestToken getRequestToken() {    	
    	LinkedInRequestToken requestToken = null;    	
    	requestToken = this.getRequestToken(this.getRedirectURL());
    	HttpServletRequest servletRequest = PortalUtil.getHttpServletRequest(this.portletRequest);
        HttpSession session = servletRequest.getSession();
        session.setAttribute(REQUEST_TOKEN_FIELD, requestToken);
    	return requestToken;
    }
    
    @Override
    public LinkedInRequestToken getRequestToken(String redirectURL) {
    	LinkedInRequestToken requestToken = null;    	     	    	    	    
		try {
			LinkedInOAuthService service = this.getLinkedInOAuthService();					
			requestToken = service.getOAuthRequestToken(redirectURL);	
		} catch (Exception e) { 
			// TODO @@Show proper error message to user
			e.printStackTrace();
		};    	
        return requestToken;
    }
    
    //TODO in a future, return a LocalResponse object: issuccess, message, LinkedInAccessToken
	@Override
    public LinkedInAccessToken getAccessToken() {
    	if(this.accessToken != null)
    		return this.accessToken;
    	
    	LinkedInAccessToken accessToken = null;
    	
    	// check if the access token is in the session
    	HttpServletRequest servletRequest = PortalUtil.getHttpServletRequest(this.portletRequest);
        HttpSession session = servletRequest.getSession();
    	if(session.getAttribute(ACCESS_TOKEN_FIELD) != null) {
    		accessToken = (LinkedInAccessToken) session.getAttribute(ACCESS_TOKEN_FIELD);
    		this.setAccessToken(accessToken);
    		return accessToken;
    	}
    	     	
    	// check if the access token is stored
    	String expandoAccessToken = getExpandoStringField(ACCESS_TOKEN_FIELD);
    	Long expirationTime = getExpandoLongField(EXPIRATION_TIME_FIELD);
    	if(StringUtils.isNotBlank(expandoAccessToken)) {
    		if(expirationTime != null && expirationTime.longValue() >= new Date().getTime()) {       		
    			accessToken = new LinkedInAccessToken(expandoAccessToken, getExpandoStringField(TOKEN_SECRET_FIELD));
    			this.setAccessToken(accessToken);        		
    		} else {
    			//TODO @@ Should I refresh the access token?
    			accessToken = new LinkedInAccessToken(expandoAccessToken, getExpandoStringField(TOKEN_SECRET_FIELD));
    			this.setAccessToken(accessToken);
    		}			
    		return accessToken;    		   
    	}
    	
		// check if access token has to be requested
    	HttpServletRequest originalServletRequest = PortalUtil.getOriginalServletRequest(PortalUtil.getHttpServletRequest(this.portletRequest));
    	if(isSocialNetworkOAuthRequest(originalServletRequest)) {
    		String oauthVerifier = originalServletRequest.getParameter(OAuth.OAUTH_VERIFIER);	        
	        LinkedInRequestToken requestToken = (LinkedInRequestToken) session.getAttribute(REQUEST_TOKEN_FIELD);
	        accessToken = this.getAccessToken(requestToken, oauthVerifier);
	        this.storeAccessToken(accessToken);	        
    	}
    	return accessToken;
    }
    
    
    @Override
    public List<ContactDTO> addContacts(List<ContactDTO> contacts) {
    	LinkedInAccessToken linkedinAccessToken = this.getAccessToken();
    	String consumerKey = this.getApiKey();
        String consumerSecret = this.getApiSecret();        
        LinkedInApiClientFactory clientFactory = LinkedInApiClientFactory.newInstance(consumerKey, consumerSecret);                
        LinkedInApiClient client = clientFactory.createLinkedInApiClient(linkedinAccessToken);
        this.linkedInClient = client;
		Connections connections = client.getConnectionsForCurrentUser();
    	if(connections != null) {    		
			List<Person> persons = connections.getPersonList();
			int i = 0;
			for(Person person : persons) {
				contacts = this.addContactAndCheckDuplicated(contacts, person);
				int maxContactsShown = this.getMaxContactsShown();
				if(i > maxContactsShown)
					break;
				i++;
			}			
		}
    	return contacts;    	
    }

	@Override
	public String getSocialNetworkName() {		
		return SOCIAL_NETWORK_NAME;
	}			
	
	@Override
	public void storeAccessToken(LinkedInAccessToken accessToken) {		
		setExpandoField(ACCESS_TOKEN_FIELD, accessToken.getToken());
		setExpandoField(TOKEN_SECRET_FIELD, accessToken.getTokenSecret());
		this.setExpandoField(EXPIRATION_TIME_FIELD, new Long(accessToken.getExpirationTime().getTime()));
	}
	
	@Override
	public Person getSocialNetworkCurrentUser() {
		if(this.linkedInPerson != null) {
			return linkedInPerson;
		} else if(this.linkedInClient != null){
			this.linkedInPerson = this.linkedInClient.getProfileForCurrentUser();	        
		} else {
			LinkedInApiClientFactory clientFactory = LinkedInApiClientFactory.newInstance(getApiKey(), getApiSecret());                
	        LinkedInApiClient client = clientFactory.createLinkedInApiClient(this.getAccessToken());
	        this.linkedInClient = client;
	        this.linkedInPerson = this.linkedInClient.getProfileForCurrentUser();
		}
		return linkedInPerson;
	}
	
	public String getPictureURLFromSocialNetworkCurrentUser() {
		String pictureURL = "";
		if(this.linkedInPerson != null) {
			pictureURL = this.linkedInPerson.getPictureUrl();		
		} else {
			Person person = this.getSocialNetworkCurrentUser();
			this.linkedInPerson = person;
			pictureURL = person.getPictureUrl();
		}
		return pictureURL;
	}

	@Override
	public ContactDTO createContactDTO(Person person) {
		ContactDTO contact = new ContactDTO();						
		contact.setFirstName(person.getFirstName());		
		contact.setLastName(person.getLastName());
		contact.setDisplayName(person.getFirstName() + " " + person.getLastName());
		String pictureURL = StringUtils.isNotBlank(person.getPictureUrl()) ? person.getPictureUrl() : this.getDefaultPictureURL();
		contact.setPictureURL(pictureURL);			
		List<SocialNetworkDTO> socialNetworks = new ArrayList<SocialNetworkDTO>();
		socialNetworks.add(this.SOCIAL_NETWORK_DTO);
		contact.setSocialNetworks(socialNetworks);
		return contact;
	}

	@Override
	public boolean isDuplicatedContact(ContactDTO contact, Person person) {
		if(!contact.getSocialNetworks().contains(this.SOCIAL_NETWORK_DTO)) {
			if(StringUtils.equalsIgnoreCase(contact.getLastName(), person.getLastName()) && StringUtils.equalsIgnoreCase(contact.getFirstName(), person.getFirstName())) {    			
				return true;
			}
			if(contact.getSocialNetworks().contains(new TwitterConnectUtil().getSocialNetworkDTO())) {
				List<TwitterAccount> twitterAccounts = person.getTwitterAccounts().getTwitterAccountList();
				for(TwitterAccount twitterAccount : twitterAccounts) {
					if(StringUtils.equals(twitterAccount.getProviderAccountName(), contact.getScreenName())) {
						return true;
					}
				}
			}			
		}
		return false;
	}

	@Override
	public String[] getSocialNetworkExpandoFields() {
		return new String[] {ACCESS_TOKEN_FIELD, TOKEN_SECRET_FIELD, EXPIRATION_TIME_FIELD};		
	}
}
