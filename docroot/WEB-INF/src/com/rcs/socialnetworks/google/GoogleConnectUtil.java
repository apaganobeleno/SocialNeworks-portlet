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
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.gdata.client.Service;
import com.google.gdata.client.contacts.ContactsService;
import com.google.gdata.data.Link;
import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.contacts.ContactFeed;
import com.google.gdata.data.contacts.Gender;
import com.google.gdata.data.extensions.Email;
import com.google.gdata.data.extensions.Name;
import com.google.gdata.util.ServiceException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.util.PortalUtil;
import com.rcs.socialnetworks.OAuth20Interface;
import com.rcs.socialnetworks.SocialNetworkOAuth;
import com.rcs.socialnetworks.contact.ContactDTO;
import com.rcs.socialnetworks.contact.SocialNetworkDTO;

public class GoogleConnectUtil extends SocialNetworkOAuth<Credential, ContactEntry> implements OAuth20Interface {
			
	public static final String SOCIAL_NETWORK_NAME = "google";
	
	public static final String ACCESS_TOKEN_FIELD = "googleAccessToken";
	
	public static final String REFRESH_TOKEN_FIELD = "googleRefreshToken";
	
	public static final String EXPIRATION_TIME_FIELD = "googleExpirationTime";
	
	/**
	 * The auth parameter for the oauth URL
	 */
	public static final String AUTH_URL_PARAM = "googleAuthURL";
	
	private Iterable<String> scope = Arrays.asList(getScope());

	private GoogleAuthorizationCodeFlow flow;
	
	private Credential credential;
	  
	private ContactsService service;
	
	/**
	 * Permissions needed to access the user resources
	 */
	public static final String COMMA_SEPARATED_SCOPES = "https://www.google.com/m8/feeds";
	
	public GoogleConnectUtil(PortletRequest portletRequest) {    	
    	super(portletRequest);  
    	this.service = new ContactsService("Google-contactsExampleApp-3"); //TODO @@ change app name
    	this.service.useSsl();
    	this.service.setHeader("GData-Version", "3.0");
//    	GoogleClientSecrets mSecrets;
//        HttpTransport mTransport;
//        JsonFactory mJsonFactory;    	        
//		        
//        //prepare google auth flow 
//        GoogleClientSecrets.Details secretDetails = new GoogleClientSecrets.Details();
//        secretDetails.setClientId(getApiKey());
//        secretDetails.setClientSecret(getApiSecret());
//
//        mSecrets = new GoogleClientSecrets();
//        mSecrets.setInstalled(secretDetails);       
//        
//        mTransport = new ApacheHttpTransport();
//        mJsonFactory = new JacksonFactory();       
//        
//        this.flow = new GoogleAuthorizationCodeFlow.Builder(
//                mTransport,
//                mJsonFactory,
//                mSecrets,
//                scope)
//        .build();
    	this.flow = getFlow();
    }
    
    public GoogleConnectUtil() { 
    	super();    	
    	this.service = new ContactsService("Google-contactsExampleApp-3");
    	this.service.useSsl();
    	this.service.setHeader("GData-Version", "3.0");
//    	GoogleClientSecrets mSecrets;
//        HttpTransport mTransport;
//        JsonFactory mJsonFactory;    	  
//        
//		//prepare google auth flow                
//        GoogleClientSecrets.Details secretDetails = new GoogleClientSecrets.Details();
//        secretDetails.setClientId(getApiKey());
//        secretDetails.setClientSecret(getApiSecret());
//
//        mSecrets = new GoogleClientSecrets();
//        mSecrets.setInstalled(secretDetails);       
//        
//        mTransport = new ApacheHttpTransport();
//        mJsonFactory = new JacksonFactory();       
//        
//        this.flow = new GoogleAuthorizationCodeFlow.Builder(
//                mTransport,
//                mJsonFactory,
//                mSecrets,
//                scope)
//        .build();    	    
    	this.flow = getFlow();
    }
    
	@Override
	public List<ContactDTO> addContacts(List<ContactDTO> contacts) {
		URL feedUrl;
		try {
			feedUrl = new URL("https://www.google.com/m8/feeds/contacts/default/full");
			ContactFeed resultFeed = null;
			try {
				resultFeed = this.service.getFeed(feedUrl, ContactFeed.class);
			} catch(Exception e) {
				return contacts;
			}
						
			int i = 0;
			for(ContactEntry contact : resultFeed.getEntries()) {
				contacts = this.addContactAndCheckDuplicated(contacts, contact);
				int maxContactsShown = this.getMaxContactsShown();
				if(i > maxContactsShown)
					break;
				i++;				 			
			}
		} catch (MalformedURLException e) {
			// TODO @@ Show proper error message
			e.printStackTrace();
		}  	
		return contacts;

	}
		
	@Override
	public String getAuthorizationURL() {
		String actualRedirect = this.getRedirectURL();
		byte[] actualRedirectEncoded = Base64.encodeBase64(actualRedirect.getBytes());
		String authorizationURL = flow.newAuthorizationUrl()
				.setState(new String(actualRedirectEncoded))
				.setRedirectUri(this.getOAuthCallbackURL())
				.setAccessType("offline")
				.build();
		return authorizationURL;
	}

	@Override
	public Credential getAccessToken() {
		
		Credential credential = null;
		
		if(this.credential != null) {
			return this.credential;
		}
		    	
    	try {
    		credential = flow.loadCredential(getUser().getUserUuid());    		
    		if(credential != null) {
    			this.setAccessToken(credential);
    			return credential;
    		}
    	} catch (SystemException e) {
			// TODO @@ Show proper error message to user
			e.printStackTrace();
		} catch (IOException e) {
			// TODO @@ Show proper error message to user
			e.printStackTrace();
		}
		
    	// check if it is a code request
		HttpServletRequest originalServletRequest = PortalUtil.getOriginalServletRequest(PortalUtil.getHttpServletRequest(this.portletRequest));		
		if(isSocialNetworkOAuthRequest(originalServletRequest)) {
			String code = originalServletRequest.getParameter(OAuth20Interface.CODE);
			try {			
				GoogleTokenResponse tokenResponse = flow.newTokenRequest(code).setScopes(scope).setRedirectUri(this.getOAuthCallbackURL()).execute();				
				credential = flow.createAndStoreCredential(tokenResponse, getUser().getUserUuid());
				this.credential = credential;
            	this.storeAccessToken(credential);
            	this.service.setOAuth2Credentials(credential);
                return credential;
			} catch (TokenResponseException e) {                                    
				//@@ Show proper error message, this happens if the code is wrong
//    					400 Bad Request
//    					{
//    					  "error" : "invalid_grant"
//    					}
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
		}		

    	// check if the access token is stored
    	String expandoAccessToken = this.getExpandoStringField(ACCESS_TOKEN_FIELD);
    	Long expirationTime = this.getExpandoLongField(EXPIRATION_TIME_FIELD);    	
    	if(StringUtils.isNotBlank(expandoAccessToken)) {
    		if(expirationTime != null && expirationTime.longValue() >= new Date().getTime()) {    			    			
    			credential = new GoogleCredential().setAccessToken(expandoAccessToken);
    			this.setAccessToken(credential);        		
    		} else {
    			// refresh token
    			String expandoRefreshToken = this.getExpandoStringField(REFRESH_TOKEN_FIELD);    			
    			try {
    				TokenResponse restoredResponse =  new GoogleRefreshTokenRequest(new NetHttpTransport(), new JacksonFactory(),
    				expandoRefreshToken, this.getApiKey(), this.getApiSecret()).setScopes(getScope()).setGrantType("refresh_token").execute();
    				credential = this.flow.createAndStoreCredential(restoredResponse, getUser().getUserUuid());    				
					this.storeAccessToken(credential);
				} catch (TokenResponseException e) {
				      if (e.getDetails() != null) {
				          System.err.println("Error: " + e.getDetails().getError());
				          if (e.getDetails().getErrorDescription() != null) {
				            System.err.println(e.getDetails().getErrorDescription());
				          }
				          if (e.getDetails().getErrorUri() != null) {
				            System.err.println(e.getDetails().getErrorUri());
				          }
				        } else {
				          System.err.println(e.getMessage());
				        } 
				      e.printStackTrace();
			      }catch (IOException e) {
					// TODO @@Show better error handling					
					e.printStackTrace();
				} catch (SystemException e) {
					// TODO @@Show better error handling					
					e.printStackTrace();
				}
    			this.setAccessToken(credential);
    		}    		
    		this.service.setOAuth2Credentials(credential);
    		return credential;    		   
    	}		    	
        return credential;	   
	}
		
	@Override
	public void storeAccessToken(Credential credential) {	
		if(StringUtils.isNotBlank(credential.getAccessToken()))
		this.setExpandoField(ACCESS_TOKEN_FIELD, credential.getAccessToken());
		if(StringUtils.isNotBlank(credential.getRefreshToken()))
			this.setExpandoField(REFRESH_TOKEN_FIELD, credential.getRefreshToken());
		if(credential.getExpirationTimeMilliseconds() != null)
			this.setExpandoField(EXPIRATION_TIME_FIELD, credential.getExpirationTimeMilliseconds());		
	}
	        
    @Override
    public String getScope() {    	
    	return COMMA_SEPARATED_SCOPES;
    }

    @Override
	public String getSocialNetworkName() {		
		return SOCIAL_NETWORK_NAME;
	}
	
	@Override
	public ContactEntry getSocialNetworkCurrentUser() {
		// TODO @@ Implement this method
		return null;
	}

	@Override
	public String getPictureURLFromSocialNetworkCurrentUser() {
		// TODO @@ Implement this method
		return null;
	}

	@Override
	public ContactDTO createContactDTO(ContactEntry person) {
		ContactDTO contact = new ContactDTO();						
		// add name
		if(person.hasName()) {
			Name name = person.getName();
			if(name.hasGivenName()) {
				contact.setFirstName(name.getGivenName().getValue());
				contact.setDisplayName(name.getGivenName().getValue());
			}
			if(name.hasFamilyName()) {
				String familyName = name.getFamilyName().getValue();
				contact.setLastName(familyName);
				contact.setDisplayName(contact.getDisplayName()!=null 
								? contact.getDisplayName() + " " + familyName 
								: familyName);
			}												
		} else if(person.hasShortName()){					
			contact.setDisplayName(person.getShortName().getValue());							
		} else if(person.hasNickname()) {				
			contact.setDisplayName(person.getNickname().getValue());
		} else if(person.getTitle() != null && StringUtils.isNotBlank(person.getTitle().getPlainText())) {				
			contact.setDisplayName(person.getTitle().getPlainText());
		} else if(person.getAuthors().size() > 0 && StringUtils.isNotBlank(person.getAuthors().get(0).getName())) {				
			contact.setDisplayName(person.getAuthors().get(0).getName());
		} else if(person.getEmailAddresses().size() > 0) {
			Email email = person.getEmailAddresses().get(0);
			if(StringUtils.isNotBlank(email.getDisplayName())) {					
				contact.setDisplayName(email.getDisplayName());
			} else if(StringUtils.isNotBlank(email.getAddress())) {					
				contact.setDisplayName(email.getAddress());
			} else {					
				contact.setDisplayName("not available");
			}
		}			
		// add gender
		if(person.hasGender()) {
			Gender.Value gender = person.getGender().getValue();
			if(Gender.Value.FEMALE.equals(gender)) {
				contact.setGender(ContactDTO.Gender.FEMALE.value);
			} else if(Gender.Value.MALE.equals(gender)) {
				contact.setGender(ContactDTO.Gender.MALE.value);
			}
		}
		
		// add picture
		// 1. get the real path to store the picture
		HttpServletRequest request = PortalUtil.getHttpServletRequest(this.portletRequest);
		ServletContext servletContext = request.getSession().getServletContext();
		String picturePath = servletContext.getRealPath("/") + "/img/";
		String pictureURL = "";
		Link photoLink = person.getContactPhotoLink();
		// 2. check if the user has a photo
		if (photoLink != null && photoLink.getEtag() != null) {				 
			try {
				// 3. store the photo stream (google does not provide a url to the photo, only the stream)
				Service.GDataRequest gdataRequest = service.createLinkQueryRequest(photoLink);
				gdataRequest.execute();
				InputStream in = gdataRequest.getResponseStream();
		          ByteArrayOutputStream out = new ByteArrayOutputStream();		          
		          RandomAccessFile file = new RandomAccessFile(picturePath + person.hashCode() + "_" + SOCIAL_NETWORK_NAME + ".jpg", "rw");
		          byte[] buffer = new byte[4096];
		          for (int read = 0; (read = in.read(buffer)) != -1; 
		              out.write(buffer, 0, read)) {}
		          file.write(out.toByteArray());
		          file.close();
		          in.close();
		          gdataRequest.end();
		          // 4. provide the path to the photo
		          pictureURL = this.portletRequest.getContextPath() + "/img/" + person.hashCode() + "_" + SOCIAL_NETWORK_NAME + ".jpg";					
			} catch (IOException ignored) { 
				ignored.printStackTrace();
			} catch (ServiceException ignored) { ignored.printStackTrace(); }											  
		}			
		contact.setPictureURL(StringUtils.isNotBlank(pictureURL) ? pictureURL : this.getDefaultPictureURL());			
		contact.setEmail(person.getEmailAddresses().get(0).getAddress());
		List<SocialNetworkDTO> socialNetworks = new ArrayList<SocialNetworkDTO>();
		socialNetworks.add(this.SOCIAL_NETWORK_DTO);
		contact.setSocialNetworks(socialNetworks);
		return contact;
	}

	@Override
	public boolean isDuplicatedContact(ContactDTO contact,
			ContactEntry person) {
		// check if the first and last names are equal
		if(!contact.getSocialNetworks().contains(this.SOCIAL_NETWORK_DTO)) {
			if(person.hasName()) {
				String firstName = "";
				String lastName = "";
				Name name = person.getName();
				if(name.hasGivenName()) {
					firstName = name.getGivenName().getValue();    					
				}
				if(name.hasFamilyName()) {
					lastName = name.getFamilyName().getValue();    					
				}
				if(StringUtils.equalsIgnoreCase(contact.getLastName(), lastName) && StringUtils.equalsIgnoreCase(contact.getFirstName(), firstName)) {    			    			
	    			return true;
	    		}
			}
			// check if the emails are equal
			if(StringUtils.isNotBlank(contact.getEmail()) && StringUtils.equalsIgnoreCase(contact.getEmail(), person.getEmailAddresses().get(0).getAddress())) {			
				return true;
			}
		}
		return false;
	}

	@Override
	public String[] getSocialNetworkExpandoFields() {
		return new String[] {ACCESS_TOKEN_FIELD, EXPIRATION_TIME_FIELD};
	}	
	
	public GoogleAuthorizationCodeFlow getFlow() {
	    if (this.flow == null) {
	        HttpTransport httpTransport = new ApacheHttpTransport();
	        JacksonFactory jsonFactory = new JacksonFactory();
	        GoogleClientSecrets clientSecrets = new GoogleClientSecrets();
	        
	        GoogleClientSecrets.Details secretDetails = new GoogleClientSecrets.Details();
	        secretDetails.setClientId(getApiKey());
	        secretDetails.setClientSecret(getApiSecret());	        
	        clientSecrets.setInstalled(secretDetails);
	        	      
	        this.flow = new GoogleAuthorizationCodeFlow.Builder(
	        		httpTransport,
	        		jsonFactory,
	        		clientSecrets,
	                scope)
	        .setAccessType("offline")
	        .setApprovalPrompt("force")
	        .build();
//	        this.flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, jsonFactory, clientSecrets, getScope())
//	                .setAccessType("offline").setApprovalPrompt("force").build();
	      }
	      return this.flow;
	    }

}
