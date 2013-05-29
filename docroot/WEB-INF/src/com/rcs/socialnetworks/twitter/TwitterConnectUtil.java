package com.rcs.socialnetworks.twitter;

import java.util.ArrayList;
import java.util.List;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;

import oauth.signpost.OAuth;

import org.apache.commons.lang.StringUtils;

import twitter4j.IDs;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.util.PortalUtil;
import com.rcs.socialnetworks.OAuth10Interface;
import com.rcs.socialnetworks.SocialNetworkOAuth;
import com.rcs.socialnetworks.contact.ContactDTO;
import com.rcs.socialnetworks.contact.SocialNetworkDTO;

/**
 * This is the class that handles the Twitter OAuth 1.0a 
 * connection to access the user protected resources in Twitter
 * 
 * @author Florencia Gadea
 */
public class TwitterConnectUtil extends SocialNetworkOAuth<AccessToken,User> implements OAuth10Interface<RequestToken,AccessToken>{
	private static Log log = LogFactoryUtil.getLog(TwitterConnectUtil.class);
		
	public static final String SOCIAL_NETWORK_NAME = "twitter";		

	/**
	 * The name of the field where the access token will 
	 * be stored in the user expando bridge field
	 */
	public static final String ACCESS_TOKEN_FIELD = "twitterAccessToken";
	
	/**
	 * The name of the field where the token secret 
	 * will be stored in the user expando bridge field
	 */
	public static final String TOKEN_SECRET_FIELD = "twitterTokenSecret";
		
	public static final String REQUEST_TOKEN_FIELD = "twitterRequestToken";	    
    
	/**
	 * The auth parameter for the oauth URL
	 */
	public static final String AUTH_URL_PARAM = "twitterAuthURL";
	
    private User twitterUser;
    
    private RequestToken requestToken;
    
    private AccessToken accessToken;
       
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
		} catch (Exception e) {
			// TODO @@ Return a proper error message to user
			e.printStackTrace();
		};    	
        return requestToken;
	}
    
    @Override
    public RequestToken getRequestToken() {    	
    	RequestToken requestToken = this.getRequestToken(this.getRedirectURL());
    	this.requestToken = requestToken;    	
    	return this.requestToken;
    }
   
    public void setTwitterUser(User user){
        this.twitterUser = user;
    }

    public User getTwitterUser() {
        return this.twitterUser;
    }
          
    @Override
    public AccessToken getAccessToken() {
    	if(this.accessToken != null)
    		return this.accessToken;
    	
    	AccessToken accessToken = null;
    	
    	if(this.accessToken != null) {
    		return this.accessToken;
    	}
    	    	
    	// check if the access token is stored
    	String expandoAccessToken = getExpandoStringField(ACCESS_TOKEN_FIELD);
    	if(StringUtils.isNotBlank(expandoAccessToken)) {	    		       		
			accessToken = new AccessToken(expandoAccessToken, getExpandoStringField(TOKEN_SECRET_FIELD));
			this.setAccessToken(accessToken);
    		return accessToken;	    		    	        	
    	}		

    	HttpServletRequest originalServletRequest = PortalUtil.getOriginalServletRequest(PortalUtil.getHttpServletRequest(this.portletRequest));    	
    	if(isSocialNetworkOAuthRequest(originalServletRequest) && this.requestToken != null) {
    		String oauthVerifier = originalServletRequest.getParameter(OAuth.OAUTH_VERIFIER);
	        accessToken = this.getAccessToken(requestToken, oauthVerifier);
	        this.storeAccessToken(accessToken);	       
    	}		    	    	
    	return accessToken;
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
			//TODO @@Return proper error message
			log.error("TwitterException: ", e);
		}
		
    	if(friendIds != null && friendIds.getIDs().length > 0) {    		
			int i = 0;						
			for(long id : friendIds.getIDs()) {
				User friend = null;
				try {					
					friend = twitter.showUser(id);
					contacts = this.addContactAndCheckDuplicated(contacts, friend);					
					int maxContactsShown = this.getMaxContactsShown();
					if(i > maxContactsShown)
						break;
					i++;
				} catch (Exception ignored) {
					//TODO @@Show proper error message to user
				}												
			}			
		}
    	return contacts;
	}
	
	@Override
	public String getAuthorizationURL() {
		RequestToken requestToken = this.getRequestToken();
    	return requestToken.getAuthenticationURL();
	}

	@Override
	public AccessToken getAccessToken(RequestToken requestToken,
			String oAuthVerifier) {
		AccessToken accessToken = null;
		try {
			Twitter twitter = this.getTwitter();
			accessToken = twitter.getOAuthAccessToken(requestToken, oAuthVerifier);
		} catch (TwitterException e) {
			// TODO @@Show proper error message to user
			e.printStackTrace();
		}		
		return accessToken;
	}	
	    
	@Override
	public void storeAccessToken(AccessToken accessToken) {
		setExpandoField(ACCESS_TOKEN_FIELD, accessToken.getToken());
		setExpandoField(TOKEN_SECRET_FIELD, accessToken.getTokenSecret());				
	}

	@Override
	public User getSocialNetworkCurrentUser() {
		if(this.getSocialNetworkUser() == null) { 			
			AccessToken accessToken = this.getAccessToken();		
			Twitter twitter = this.getTwitter();
			twitter.setOAuthAccessToken(accessToken);
			try {
				User user = twitter.showUser(twitter.getScreenName());
				this.setSocialNetworkUser(user);
			} catch (IllegalStateException ignored) { // TODO @@ Show proper error message 
			} catch (TwitterException ignored) { // TODO @@ Show proper error message 
			}									
		}
		return getSocialNetworkUser();
	}

	@Override
	public String getPictureURLFromSocialNetworkCurrentUser() {
		if(this.twitterUser == null) {
			this.twitterUser = getSocialNetworkCurrentUser();
		}		
		return this.twitterUser.getProfileImageURL().toString();
	}
	
	@Override
	public String getSocialNetworkName() {		
		return SOCIAL_NETWORK_NAME;
	}

	@Override
	public ContactDTO createContactDTO(User friend) {
		ContactDTO contact = new ContactDTO();								
		contact.setName(friend.getName());
		contact.setDisplayName(friend.getName());
		contact.setScreenName(friend.getScreenName());		
		String pictureURL = StringUtils.isNotBlank(friend.getProfileImageURL().toString()) ? friend.getProfileImageURL().toString() : this.getDefaultPictureURL();
		contact.setPictureURL(pictureURL);			
		List<SocialNetworkDTO> socialNetworks = new ArrayList<SocialNetworkDTO>();
		socialNetworks.add(this.SOCIAL_NETWORK_DTO);
		contact.setSocialNetworks(socialNetworks);
		return contact;
	}

	@Override
	public boolean isDuplicatedContact(ContactDTO contact, User friend) {
		if(!contact.getSocialNetworks().contains(this.SOCIAL_NETWORK_DTO)) {
			if(StringUtils.equalsIgnoreCase(contact.getDisplayName(), friend.getName())) {
				return true;
			}
			if(StringUtils.isNotBlank(contact.getScreenName()) && StringUtils.equalsIgnoreCase(contact.getScreenName(), friend.getScreenName())) {
				return true;
			}
		}
		return false;
	}

	public void setRequestToken(RequestToken requestToken) {
		this.requestToken = requestToken;
	}
	
	@Override
	public String[] getSocialNetworkExpandoFields() {
		return new String[] {ACCESS_TOKEN_FIELD, TOKEN_SECRET_FIELD};		
	}
}
