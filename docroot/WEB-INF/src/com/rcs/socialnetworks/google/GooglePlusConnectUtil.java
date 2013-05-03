package com.rcs.socialnetworks.google;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import oauth.signpost.OAuth;

import com.rcs.socialnetworks.SocialNetworkOAuthData;
import com.rcs.socialnetworks.SocialNetworkOAuthUtil;
import com.rcs.socialnetworks.contact.ContactDTO;
import com.rcs.socialnetworks.contact.SocialNetworkDTO;
import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;

import com.google.api.services.plus.Plus;
import com.google.api.services.plus.Plus.People;
import com.google.api.services.plus.PlusScopes;
import com.google.code.linkedinapi.client.oauth.LinkedInAccessToken;
import com.google.code.linkedinapi.client.oauth.LinkedInOAuthServiceFactory;
import com.google.gdata.client.Service;
import com.google.gdata.client.contacts.ContactsService;
import com.google.gdata.data.Link;
import com.google.gdata.data.contacts.BasePersonEntry;
import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.contacts.ContactFeed;
import com.google.gdata.data.contacts.ShortName;
import com.google.gdata.data.extensions.Email;
import com.google.gdata.data.extensions.Name;
import com.google.gdata.util.ServiceException;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.auth.oauth2.Credential;
//import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
//import com.google.api.client.extensions.java6.auth.oauth2.FileCredentialStore;
//import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.plus.model.Activity;
import com.google.api.services.plus.model.ActivityFeed;
import com.google.api.services.plus.model.Person;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.util.PortalUtil;

//import com.google.api.services.plus.model.Person;

//import com.google.api.services.plus.Plus;
//import com.google.api.services.plus.PlusScopes;

public class GooglePlusConnectUtil extends SocialNetworkOAuthUtil implements SocialNetworkOAuthData<Credential, Credential, ContactEntry>{

	public static String apiKey;
	
	public static String apiSecret;
			
	public static final String socialNetworkName = "google";
	
	public static final String socialNetworkAccessTokenField = "googleplusAccessToken";
	
	public static final String socialNetworkRefreshTokenField = "googleplusRefreshToken";
	
	public static final String socialNetworkExpirationTimeField = "googleplusExpirationTime";
	
	Iterable<String> scope = Arrays.asList(getScopes());

	public GoogleAuthorizationCodeFlow flow;
	
	public Credential credential;

	/** Global instance of the HTTP transport. */
	private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

	/** Global instance of the JSON factory. */
	private static final JsonFactory JSON_FACTORY = new JacksonFactory();
	  
	private ContactsService service;
	
	public GooglePlusConnectUtil(PortletRequest portletRequest) {    	
    	super(portletRequest);  
    	this.service = new ContactsService("Google-contactsExampleApp-3"); //@@ change app name
    	this.service.useSsl();
    	this.service.setHeader("GData-Version", "3.0");
    	GoogleClientSecrets mSecrets;
        HttpTransport mTransport;
        JsonFactory mJsonFactory;    	
        Credential credential = null;
		//prepare google auth flow
        GoogleAuthorizationCodeFlow flow = null;
        
        GoogleClientSecrets.Details secretDetails = new GoogleClientSecrets.Details();
        secretDetails.setClientId(getApiKey());
        secretDetails.setClientSecret(getApiSecret());

        mSecrets = new GoogleClientSecrets();
        mSecrets.setInstalled(secretDetails);       
        
        mTransport = new ApacheHttpTransport();
        mJsonFactory = new JacksonFactory();       
        
        this.flow = new GoogleAuthorizationCodeFlow.Builder(
                mTransport,
                mJsonFactory,
                mSecrets,
                scope)
        .build(); //@@ set try and catch
    }
    
    public GooglePlusConnectUtil() { 
    	super();    	
    	this.service = new ContactsService("Google-contactsExampleApp-3");
    	this.service.useSsl();
    	this.service.setHeader("GData-Version", "3.0");
    	GoogleClientSecrets mSecrets;
        HttpTransport mTransport;
        JsonFactory mJsonFactory;    	
        Credential credential = null;
		//prepare google auth flow
        GoogleAuthorizationCodeFlow flow = null;
        
        GoogleClientSecrets.Details secretDetails = new GoogleClientSecrets.Details();
        secretDetails.setClientId(getApiKey());
        secretDetails.setClientSecret(getApiSecret());

        mSecrets = new GoogleClientSecrets();
        mSecrets.setInstalled(secretDetails);       
        
        mTransport = new ApacheHttpTransport();
        mJsonFactory = new JacksonFactory();       
        
        this.flow = new GoogleAuthorizationCodeFlow.Builder(
                mTransport,
                mJsonFactory,
                mSecrets,
                scope)
        .build();    	
    }
    
	@Override
	public List<ContactDTO> addContacts(PortletRequest portletRequest,
			List<ContactDTO> contacts) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ContactDTO> addContacts(List<ContactDTO> contacts) {
		URL feedUrl;
		try {
			feedUrl = new URL("https://www.google.com/m8/feeds/contacts/default/full");
			//feedUrl = new URL("https://www.google.com/m8/feeds/contacts/florenciagadea@gmail.com/full");
			ContactFeed resultFeed = null;
			try {
				resultFeed = this.service.getFeed(feedUrl, ContactFeed.class);
			} catch(Exception e) {
				return contacts;
			}
						
			int i = 0;
			for(ContactEntry contact : resultFeed.getEntries()) {
				contacts = this.addContactAndCheckDuplicated(contacts, contact);
				if(i > 10)
					break;//@@ remove this
				i++;				 			
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 	
		// TODO Auto-generated method stub
		return contacts;

	}
	
	
	@Override
	public boolean currentUserHasAccount() {
		Credential accessToken = this.getAccessToken();
    	return accessToken != null;
	}

	@Override
	public String getAuthorizationURL() {
		String authorizationURL = new GoogleAuthorizationCodeRequestUrl(
				 getApiKey()
				,createRedirectURL()
				,scope)
		.setAccessType("offline")
		.build();
		//String url = flow.newAuthorizationUrl().setState("xyz").setRedirectUri("https://client.example.com/rd").build();
		return authorizationURL;
	}

	@Override
	public Credential getAccessToken() {
		
		Credential credential = null;
		
		HttpServletRequest servletRequest = PortalUtil.getHttpServletRequest(this.portletRequest);
        HttpSession session = servletRequest.getSession();
    	if(session.getAttribute("googleplusAccessToken") != null) {
    		//(String)session.getAttribute("googlePlusAccessToken")
    		String accessToken = (String)session.getAttribute("googleplusAccessToken");
    		credential = new GoogleCredential().setAccessToken(accessToken);
    		
//    		try {
//				credential.refreshToken();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
    		try {
    			GoogleTokenResponse restoredResponse = new GoogleTokenResponse();
    			String refreshToken = getExpandoField(socialNetworkRefreshTokenField);
	        	restoredResponse.setRefreshToken(refreshToken);
		        credential = flow.createAndStoreCredential(restoredResponse, null);    			
				credential.refreshToken();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		this.setAccessToken(credential);
    		this.service.setOAuth2Credentials(credential);
    		return credential;
    	}
		
    	// check if the access token is stored
    	String expandoAccessToken = getExpandoAccessToken();
    	Long expirationTime = (Long) this.getUser().getExpandoBridge().getAttribute("googleplusExpirationTime");
    	if(StringUtils.isNotBlank(expandoAccessToken)) {
    		if(expirationTime != null && expirationTime.longValue() >= new Date().getTime()) {
    			//String tokenSecretStr = (String)user.getExpandoBridge().getAttribute("linkedinTokenSecret");
    			
    			credential = new GoogleCredential().setAccessToken(expandoAccessToken);
    			try {
    				GoogleTokenResponse restoredResponse = new GoogleTokenResponse();
        			String refreshToken = getExpandoField(socialNetworkRefreshTokenField);
    	        	restoredResponse.setRefreshToken(refreshToken);
    		        credential = flow.createAndStoreCredential(restoredResponse, null);    			
    				credential.refreshToken();					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    			this.setAccessToken(credential);        		
    		} else {
    			//@@ do something else here!! delete this?
    			credential = new GoogleCredential().setAccessToken(expandoAccessToken);
    			try {
    				GoogleTokenResponse restoredResponse = new GoogleTokenResponse();
        			String refreshToken = getExpandoField(socialNetworkRefreshTokenField);
    	        	restoredResponse.setRefreshToken(refreshToken);
    		        credential = flow.createAndStoreCredential(restoredResponse, null);    			
    				credential.refreshToken();
    				
    				//this.storeAccessToken(credential); @@ check what happens if the credential is refreshed but not stored 
    				//this.setAccessToken(credential);    				
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}    			
    		}
			//@@ refresh token? TODO
    		this.service.setOAuth2Credentials(credential);
    		return credential;    		   
    	}
		
		HttpServletRequest originalServletRequest = PortalUtil.getOriginalServletRequest(PortalUtil.getHttpServletRequest(this.portletRequest));
		if(isSocialNetworkOAuthRequest(originalServletRequest)) {
			String code = originalServletRequest.getParameter("code");
			try {
			GoogleTokenResponse tokenResponse = flow.newTokenRequest(code).setRedirectUri(this.createRedirectURL()).execute();
            	this.credential = flow.createAndStoreCredential(tokenResponse, null);
            	this.storeAccessToken(this.credential);
            	this.service.setOAuth2Credentials(credential);
			} catch (TokenResponseException e) {                    
                //log.info("TokenResponseException in GoogleTokenExpert in getToken nr 2");
                e.printStackTrace();
                //throw e;
            } catch (Exception e) {
                e.printStackTrace();
                //throw e;
            }
		}						
		
        return credential;	   
	}

	@Override
	public Credential getAccessToken(Credential requestToken,
			String oAuthVerifier) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Credential getRequestToken() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Credential getRequestToken(String redirectURL) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ContactDTO> addContactAndCheckDuplicated(
			List<ContactDTO> contacts, ContactEntry person) {
		long contactId = 1;
    	boolean isDuplicated = false;
    	if(!contacts.isEmpty()) {
    		ContactDTO lastContact = contacts.get(contacts.size()-1);
    		contactId = lastContact.getId() + 1;
    		// check if contact is duplicated
    		for(ContactDTO contact : contacts) {
        		if(StringUtils.equalsIgnoreCase(contact.getFirstName() + " " + contact.getLastName(), person.getTitle().getPlainText()) || StringUtils.equalsIgnoreCase(contact.getName(), person.getTitle().getPlainText())) {    			
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
			System.out.println("contactId: " + contactId);
			if(person.hasName()) {
				Name name = person.getName();				
				//System.out.println("Name: " + ToStringBuilder.reflectionToString(name));
				if(name.hasFamilyName()) {
					contact.setLastName(name.getFamilyName().getValue());
					System.out.println("getFamilyName: " + name.getFamilyName().getValue());
				}
				if(name.hasGivenName()) {
					contact.setFirstName(name.getGivenName().getValue());
					System.out.println("getGivenName: " + name.getGivenName().getValue());
				}									
			} else if(person.hasShortName()){	
				System.out.println("person.getShortName(): " + ToStringBuilder.reflectionToString(person.getShortName()));
				contact.setName(person.getShortName().getValue());							
			} else if(person.hasNickname()) {
				System.out.println("person.getNickname(): " + ToStringBuilder.reflectionToString(person.getNickname()));
				contact.setName(person.getNickname().getValue());
			} else if(person.getTitle() != null && StringUtils.isNotBlank(person.getTitle().getPlainText())) {
				System.out.println("getTitle: " + person.getTitle().getPlainText());
				contact.setName(person.getTitle().getPlainText());
			} else if(person.getAuthors().size() > 0 && StringUtils.isNotBlank(person.getAuthors().get(0).getName())) {
				System.out.println("getAuthors: " + person.getAuthors().get(0).getName());
				contact.setName(person.getAuthors().get(0).getName());
			} else if(person.getEmailAddresses().size() > 0) {
				Email email = person.getEmailAddresses().get(0);
				if(StringUtils.isNotBlank(email.getDisplayName())) {
					System.out.println("getDisplayName: " + email.getDisplayName());
					contact.setName(email.getDisplayName());
				} else if(StringUtils.isNotBlank(email.getAddress())) {
					System.out.println("getAddress:" + email.getAddress());
					contact.setName(email.getAddress());
				} else {
					System.out.println("not available");
					contact.setName("not available");
				}
			}			
			
			// add picture
			String pictureURL = "";
			Link photoLink = person.getContactPhotoLink();
			System.out.println("photoLink: " + photoLink.getHref() + " - " + ToStringBuilder.reflectionToString(photoLink));			
			if (photoLink != null && StringUtils.isNotBlank(photoLink.getEtag())) {				 
				try {
					System.out.println("photoLink.getHref(): " + photoLink.getHref());
					 InputStream in = service.getStreamFromLink(photoLink);
					    ByteArrayOutputStream out = new ByteArrayOutputStream();
					    RandomAccessFile file = new RandomAccessFile(contactId+"test.jpg", "rw");
					    byte[] buffer = new byte[4096];
					    int read;
					    while (true) {
					      if ((read = in.read(buffer)) != -1) {
					        out.write(buffer, 0, read);
					      } else {
					        break;
					      }
					    }
					    file.write(out.toByteArray());					    
					    file.close();

					//Service.GDataRequest gdataRequest = this.service.createLinkQueryRequest(photoLink);
					//pictureURL = gdataRequest.getRequestUrl().getRef();
				} catch (IOException ignored) { 
					ignored.printStackTrace();
				} catch (ServiceException ignored) { ignored.printStackTrace(); }											  
			}
			
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
	public void storeAccessToken(Credential credential) {		
		this.setExpandoField(socialNetworkAccessTokenField, credential.getAccessToken());
		this.setExpandoField(socialNetworkRefreshTokenField, credential.getRefreshToken());
		this.setExpandoField(socialNetworkExpirationTimeField, credential.getExpirationTimeMilliseconds());		
	}
	
	@Override
	public String getApiKey() {    	
		if(apiKey == null) { //@@ hacerlo de manera más automática
    		apiKey = PropsUtil.get("google.default.connect.app.id");
    	}
    	return apiKey;		
	}	

    @Override
	public String getApiSecret() {    	
    	if(apiSecret == null) {
    		apiSecret = PropsUtil.get("google.default.connect.app.secret");
    	}
    	return apiSecret;
	}    	
    
    //@@create later
    public String getScopes() {
    	
    	return "https://www.google.com/m8/feeds"; //@@add static attribute
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
	
	
	@Override
    public boolean isEnabled() { //@@change later
        return true;
    }

	@Override
	public void setAccessToken(Credential accessToken) {
		this.credential = accessToken;		
	}

	@Override
	public ContactEntry getSocialNetworkCurrentUser() {
		// TODO Auto-generated method stub @@
		return null;
	}

	@Override
	public String getPictureURLFromSocialNetworkCurrentUser() {
		// TODO Auto-generated method stub @@
		return null;
	}
}
