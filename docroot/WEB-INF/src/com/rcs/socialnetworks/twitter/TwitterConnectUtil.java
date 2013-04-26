package com.rcs.socialnetworks.twitter;

import java.util.ArrayList;
import java.util.List;

import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.util.PortalUtil;
import com.rcs.socialnetworks.SocialNetworkOAuthData;
import com.rcs.socialnetworks.SocialNetworkOAuthUtil;
import com.rcs.socialnetworks.contact.ContactDTO;
import com.rcs.socialnetworks.contact.SocialNetworkDTO;

import twitter4j.IDs;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import oauth.signpost.OAuth;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * @author V. Koshelenko
 * @author Florencia Gadea
 */
public class TwitterConnectUtil extends SocialNetworkOAuthUtil implements SocialNetworkOAuthData<RequestToken,AccessToken,User>{
	private static Log log = LogFactoryUtil.getLog(TwitterConnectUtil.class);
	
public static final String socialNetworkName = "linkedin";
	
	public static String apiKey;

	public static String apiSecret;

	public static final String socialNetworkAccessTokenField = "twitterAccessToken";
	
	public static final String socialNetworkTokenSecretField = "twitterTokenSecret";
	
	public static final String socialNetworkRequestTokenField = "twitterRequestToken";
	
    private TwitterConnect twitterConnect;
    private User twitterUser;
    private AccessToken accessToken;
    
    private String redirectURL; //@@ usar la del padre?
    //private Twitter twitter;
    
    public TwitterConnectUtil(PortletRequest request) {
    	super(request);    	
    }
    
    public TwitterConnectUtil() {
    	super();    	
    }
    
    public Twitter getTwitter() {
        Twitter twitter = new TwitterFactory().getInstance();
        twitter.setOAuthConsumer(
                getApiKey(),
                getApiSecret()
        );        
        return twitter;
    }
    
    @Override
	public RequestToken getRequestToken(String redirectURL) {
    	RequestToken requestToken = null;    	     	    	    	    
		try {				    				
			requestToken = getTwitter().getOAuthRequestToken(redirectURL);		
		} catch (Exception e) { //@@ check better way
			// TODO Auto-generated catch block
			e.printStackTrace();
		};    	
        return requestToken;
	}
    
    @Override
    public RequestToken getRequestToken() {
    	RequestToken requestToken = null;
    	if(this.redirectURL == null) {
    		this.redirectURL = this.createRedirectURL();
    	}   
    	requestToken = this.getRequestToken(this.redirectURL);
    	HttpServletRequest servletRequest = PortalUtil.getHttpServletRequest(this.portletRequest);
        HttpSession session = servletRequest.getSession();
        session.setAttribute(getRequestTokenName(), requestToken);
    	return requestToken;
    }
    /*
    public static RequestToken getRequestToken() {
    	
    	String twitterRedirectURL = TwitterConnectUtil.getRedirectURL(portletRequest);

        twitterRedirectURL = HttpUtil.addParameter(twitterRedirectURL, "p_p_id", "LOGIN_PORTLET_WAR_loginportlet");
        twitterRedirectURL = HttpUtil.addParameter(twitterRedirectURL, "p_p_state", "normal");
        twitterRedirectURL = HttpUtil.addParameter(twitterRedirectURL, "p_p_mode", "view");
        twitterRedirectURL = HttpUtil.addParameter(twitterRedirectURL, "socialnetwork", "twitter");
        
        String twitterStrutsActionParameter = "_LOGIN_PORTLET_WAR_loginportlet_action";
        String twitterStrutsActionValue = "twitter";

        twitterRedirectURL = HttpUtil.addParameter(twitterRedirectURL, twitterStrutsActionParameter, twitterStrutsActionValue);

        //Twitter twitter = TwitterConnectUtil.getTwitter(portletRequest);
        return getTwitter().getOAuthRequestToken(twitterRedirectURL);
    }*/
    //@@ sacar
    public RequestToken getTwitterRequestToken(String url) throws SystemException, TwitterException {
        return getTwitter().getOAuthRequestToken(url);
    }
  //@@ sacar
    public static RequestToken getTwitterRequestToken(Twitter twitter, String url) throws SystemException, TwitterException {
        return twitter.getOAuthRequestToken(url);
    }
   
    public void setTwitterUser(User user){
        this.twitterUser = user;
    }

    public User getTwitterUser() {
        return this.twitterUser;
    }

    private TwitterConnect getTwitterConnect(){
        if (this.twitterConnect == null) {
            //this.twitterConnect = new TwitterConnectImpl(); //@@change this 
        }
        return this.twitterConnect;
    }        
    @Override
    public AccessToken getAccessToken() {
    	if(this.accessToken != null)
    		return this.accessToken;
    	
    	AccessToken accessToken = null;
    	// check if the access token is in the session
    	HttpServletRequest servletRequest = PortalUtil.getHttpServletRequest(this.portletRequest);
        HttpSession session = servletRequest.getSession();
    	if(session.getAttribute(getAccessTokenFieldName()) != null) {
    		accessToken = (AccessToken) session.getAttribute(getAccessTokenFieldName()); //@@ hasta aca
    		this.setAccessToken(accessToken);
    		return accessToken;
    	}
    	
    	// check if the access token is stored
    	String expandoAccessToken = getExpandoAccessToken();    		
		//String accessTokenStr = (String) user.getExpandoBridge().getAttribute("twitterAccessToken");
    	//Long expirationTime = (Long) user.getExpandoBridge().getAttribute("twitterExpirationTime");
    	if(StringUtils.isNotBlank(expandoAccessToken)) {	    		
			//String tokenSecretStr = (String)user.getExpandoBridge().getAttribute("twitterTokenSecret");        		
			accessToken = new AccessToken(expandoAccessToken, getExpandoTokenSecret());
			this.setAccessToken(accessToken);
    		return accessToken;	    		    	        	
    	}		

    	HttpServletRequest originalServletRequest = PortalUtil.getOriginalServletRequest(PortalUtil.getHttpServletRequest(this.portletRequest));
    	if(isSocialNetworkOAuthRequest(originalServletRequest)) {
    		String oauthVerifier = originalServletRequest.getParameter(OAuth.OAUTH_VERIFIER);	        
	        RequestToken requestToken = (RequestToken) session.getAttribute(getRequestTokenName());
	        //LinkedInOAuthService oAuthService = this.getLinkedInOAuthService();
	        //linkedinAccessToken = oAuthService.getOAuthAccessToken(requestToken, oauthVerifier);
	        accessToken = this.getAccessToken(requestToken, oauthVerifier);
	        this.storeAccessToken(accessToken);	       
    	}
		    	    	
    	return accessToken;
    }
    
//    public static boolean currentUserHasTwitterAccount(PortletRequest portletRequest) throws SystemException {
//    	AccessToken accessToken = TwitterConnectUtil.getTwitterAccessToken();
//    	return accessToken != null;
//    }    	    	    

    //@@override a futuro
	public void setAccessToken(AccessToken accessToken) {
		this.accessToken = accessToken;
	}
	
	@Override
	public List<ContactDTO> addContacts(List<ContactDTO> contacts) {
		AccessToken accessToken = this.getAccessToken();		
		Twitter twitter = this.getTwitter();
		twitter.setOAuthAccessToken(accessToken);
				
		IDs friendIds = null;
		try {
			friendIds = twitter.getFriendsIDs(-1);															    
		} catch(TwitterException e) {
			//@@@ log here
			log.error("TwitterException: ", e);
		}
		
    	if(friendIds != null && friendIds.getIDs().length > 0) {    		
			int i = 0;						
			for(long id : friendIds.getIDs()) {
				User friend = null;
				try {					
					friend = twitter.showUser(id);
					contacts = this.addContactAndCheckDuplicated(contacts, friend);					
					if(i > 10)
						break;//@@ remove this
					i++;
				} catch (Exception ex) {
					//@@ignore?
				}												
			}			
		}
    	return contacts;
	}
	
	@Override
	public List<ContactDTO> addContactAndCheckDuplicated(List<ContactDTO> contacts, User friend) {
		long contactId = 1;
    	boolean isDuplicated = false;
    	if(!contacts.isEmpty()) {
    		ContactDTO lastContact = contacts.get(contacts.size()-1);
    		contactId = lastContact.getId() + 1;
    		// check if contact is duplicated
    		for(ContactDTO contact : contacts) {
        		//if(StringUtils.equalsIgnoreCase(contact.getLastName(), person.getLastName()) && StringUtils.equalsIgnoreCase(contact.getFirstName(), person.getFirstName())) {
    			if(StringUtils.equalsIgnoreCase(contact.getFirstName() + " " + contact.getLastName(), friend.getName())) {
        			List<SocialNetworkDTO> socialNetworks = contact.getSocialNetworks();
        			socialNetworks.add(new SocialNetworkDTO("twitter")); //@@ add constant
        			contact.setSocialNetworks(socialNetworks);
        			isDuplicated = true;
        		}
        	}
    	}    	    	    	
    	// if it is not duplicated
    	if(!isDuplicated) {
    		ContactDTO contact = new ContactDTO();				
			contact.setId(contactId);
			//contact.setFirstName(person.getFirstName());			
			contact.setName(friend.getName());
			//contact.setLastName(person.getLastName());
			String pictureURL = StringUtils.isNotBlank(friend.getProfileImageURL().toString()) ? friend.getProfileImageURL().toString() : "http://s.c.lnkd.licdn.com/scds/common/u/images/themes/katy/ghosts/person/ghost_person_200x200_v1.png"; //@@ change this, add constant
			contact.setPictureURL(pictureURL);			
			List<SocialNetworkDTO> socialNetworks = new ArrayList<SocialNetworkDTO>();
			socialNetworks.add(new SocialNetworkDTO("twitter"));
			contact.setSocialNetworks(socialNetworks);
			contacts.add(contact);			
			contactId++;    				
    	}
    	return contacts;
	}

	@Override
	public List<ContactDTO> addContacts(PortletRequest portletRequest,
			List<ContactDTO> contacts) {
		// TODO Auto-generated method stub
		return null;
	}	

	@Override
	public boolean currentUserHasAccount() {
		AccessToken accessToken = this.getAccessToken();
    	return accessToken != null;
	}

	@Override
	public String getAuthorizationURL() {
		RequestToken requestToken = this.getRequestToken();
    	return requestToken.getAuthenticationURL();
	}

//	@Override
//	public AccessToken getAccessToken() {
//		// TODO Auto-generated method stub
//		return null;
//	}

	@Override
	public AccessToken getAccessToken(RequestToken requestToken,
			String oAuthVerifier) {
		AccessToken accessToken = null;
		try {
			Twitter twitter = this.getTwitter();
			accessToken = twitter.getOAuthAccessToken(requestToken, oAuthVerifier);
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return accessToken;
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
	public String getApiKey() {    	
		if(TwitterConnectUtil.apiKey == null) {
			TwitterConnectUtil.apiKey = "sm8ZdXdRwtIuW9UWkXxA"; //@@change later
		}
		return TwitterConnectUtil.apiKey;
	}	

    @Override
	public String getApiSecret() {    	
		if(TwitterConnectUtil.apiSecret == null) {
			TwitterConnectUtil.apiSecret = "LBzvfCIso0hFmHte7lCTmG9iTrCwAEEE4xxjg8w"; //@@change later, and change to appSecret
		}
		return TwitterConnectUtil.apiSecret;
	}
    
	@Override
	public void storeAccessToken(AccessToken accessToken) {
		this.storeAccessToken(accessToken.getToken(), accessToken.getTokenSecret());
		//store expiration time
	}

	@Override
    public boolean isEnabled() { //@@change later
        return true;
    }
}
