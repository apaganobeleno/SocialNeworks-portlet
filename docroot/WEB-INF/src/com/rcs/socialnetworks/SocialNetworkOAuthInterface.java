package com.rcs.socialnetworks;

import java.util.List;

import javax.portlet.PortletRequest;

import com.rcs.socialnetworks.contact.ContactDTO;

/**
 * This is the interfaces that handles the OAuth
 * flow for the different social networks
 * 
 * @author flor
 *
 * @param <RequestToken>
 * @param <AccessToken>
 * @param <Contact>
 */
public interface SocialNetworkOAuthInterface<AccessToken,Contact> {		
	
	/**
	 * Add the user friends to 
	 * the list of ContactDTO
	 * 
	 * @param contacts
	 * @return
	 */
    public List<ContactDTO> addContacts(List<ContactDTO> contacts);			
	
    /**
     * The url that the user will be sent 
     * to authorize this portlet to access 
     * their private resources
     * 
     * @return
     */
	public String getAuthorizationURL();
	
	/**
	 * This method gets the access token 
	 * that will allow this portlet to 
	 * access the user private resources
	 * @return
	 */
	public AccessToken getAccessToken();		
	
	public void setAccessToken(AccessToken accessToken);		
	
	/**
	 * Add this user connection 
	 * to the list of ContactDTO
	 * 
	 * @param contacts
	 * @param contact
	 * @return
	 */
	public List<ContactDTO> addContactAndCheckDuplicated(List<ContactDTO> contacts, Contact contact);
	
	/**
	 * Store the access token granted 
	 * to access the user resources
	 * 
	 * @param accessToken
	 */
	public void storeAccessToken(AccessToken accessToken);
	
	/**
	 * Get the current user of this social network
	 * 
	 * @return
	 */
	public Contact getSocialNetworkCurrentUser();
	
	/**
	 * Get the picture url of the current 
	 * user of this social network
	 * 
	 * @return
	 */
	public String getPictureURLFromSocialNetworkCurrentUser();
	
	/**
	 * Create a contactDTO 
	 * from a user connection
	 * 
	 * @param contact
	 * @return
	 */
	public ContactDTO createContactDTO(Contact contact);
	
	/**
	 * Check if these two contacts from different 
	 * social networks are the same person
	 * 
	 * @param contactDTO
	 * @param contact
	 * @return
	 */
	public boolean isDuplicatedContact(ContactDTO contactDTO, Contact contact);
	
	/**
	 * Get the same of the current social 
	 * network (eg: facebook, linkedin, etc.)
	 * 
	 * @return
	 */
	public String getSocialNetworkName();
	
	/**
	 * Get all the expando fields 
	 * for this social network
	 * 
	 * @return the expando fields
	 */
	public String [] getSocialNetworkExpandoFields();
}
