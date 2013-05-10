package com.rcs.socialnetworks;

import java.util.List;

import javax.portlet.PortletRequest;

import com.rcs.socialnetworks.contact.ContactDTO;

public interface SocialNetworkOAuthData<RequestToken,AccessToken,Contact> {		
		           
    public List<ContactDTO> addContacts(PortletRequest portletRequest, List<ContactDTO> contacts);
    
    public List<ContactDTO> addContacts(List<ContactDTO> contacts);			
	
	public String getAuthorizationURL();
	
	public AccessToken getAccessToken();
	
	public AccessToken getAccessToken(RequestToken requestToken, String oAuthVerifier);
	
	//public void setAccessToken(AccessToken accessToken);
	
	public RequestToken getRequestToken();
	
	public RequestToken getRequestToken(String redirectURL);
	
	public List<ContactDTO> addContactAndCheckDuplicated(List<ContactDTO> contacts, Contact contact);
	
	public void storeAccessToken(AccessToken accessToken);
	
	public Contact getSocialNetworkCurrentUser();
	
	public String getPictureURLFromSocialNetworkCurrentUser();

}
