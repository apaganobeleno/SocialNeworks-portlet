package com.rcs.socialnetworks.utils;

import java.util.ArrayList;
import java.util.List;

import javax.portlet.PortletRequest;

import org.apache.commons.lang.StringUtils;

import com.google.code.linkedinapi.schema.Person;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.User;
import com.liferay.portal.theme.ThemeDisplay;
import com.rcs.socialnetworks.contact.ContactDTO;
import com.rcs.socialnetworks.contact.SocialNetworkDTO;

/**
 * Util class to draw the liferay 
 * contacts in the graphic
 * 
 * @author flor
 *
 */
public class LiferayUtil {

	public static final String SOCIAL_NETWORK_NAME = "liferay";
	
	public static final SocialNetworkDTO SOCIAL_NETWORK_DTO = new SocialNetworkDTO(SOCIAL_NETWORK_NAME);
	
	/**
	 * Add the user friends to 
	 * the list of ContactDTO
	 * 
	 * @param request
	 * @param contacts
	 * @param users
	 * @return
	 * @throws SystemException
	 */
	public static List<ContactDTO> addContacts(PortletRequest request, List<ContactDTO> contacts, List<User> users) throws SystemException {    	
    	if(users != null && !users.isEmpty()) {    							
			for(User user : users) {
				contacts = LiferayUtil.addContactAndCheckDuplicated(request, contacts, user);
			}			
		}
    	return contacts;    	
    }
  
	/**
	 * Add the current contact to the list, but first
	 * check if it is duplicated, and, if so, only add
	 * the social network the contact belongs to
	 */
    public static List<ContactDTO> addContactAndCheckDuplicated(PortletRequest request, List<ContactDTO> contacts, User user) throws SystemException {
    	long contactId = 1;
    	boolean isDuplicated = false;
    	if(!contacts.isEmpty()) {
    		ContactDTO lastContact = contacts.get(contacts.size()-1);
    		contactId = lastContact.getId() + 1;
    		// check if contact is duplicated
    		for(ContactDTO contact : contacts) {
    			isDuplicated = isDuplicatedContact(contact, user);
    			if(isDuplicated) {
    				List<SocialNetworkDTO> socialNetworks = contact.getSocialNetworks();
        			socialNetworks.add(SOCIAL_NETWORK_DTO);
        			contact.setSocialNetworks(socialNetworks);
        			break;
    			}
        	}
    	}    	    	
    	// if it is not duplicated
    	if(!isDuplicated) {
    		ContactDTO contact = new ContactDTO();				
			contact.setId(contactId);
			contact.setFirstName(user.getFirstName());
			contact.setMiddleName(user.getMiddleName());			
			contact.setLastName(user.getLastName());
			try {
				contact.setGender(user.isFemale() ? ContactDTO.Gender.FEMALE.value : ContactDTO.Gender.MALE.value);
			} catch (PortalException e1) {
				contact.setGender(ContactDTO.Gender.MALE.value);
			}
			contact.setDisplayName(user.getFirstName() + " " + user.getLastName());
			contact.setScreenName(user.getScreenName());
			ThemeDisplay themeDisplay = (ThemeDisplay)request.getAttribute(WebKeys.THEME_DISPLAY);
			try {
				contact.setPictureURL("http://" + request.getServerName() + ":" + request.getServerPort() + user.getPortraitURL(themeDisplay));
			} catch(Exception e) {
				contact.setPictureURL("");
			}					
			List<SocialNetworkDTO> socialNetworks = new ArrayList<SocialNetworkDTO>();
			socialNetworks.add(SOCIAL_NETWORK_DTO);
			contact.setSocialNetworks(socialNetworks);
			contacts.add(contact);
			contactId++;    				
    	}
    	return contacts;
    }
    
    /**
     * 
	 * Check if these two contacts from different 
	 * social networks are the same person
	 * 
     * @param contact
     * @param user
     * @return
     */
    public static boolean isDuplicatedContact(ContactDTO contact, User user) {
    	if(!contact.getSocialNetworks().contains(SOCIAL_NETWORK_DTO)) {
    		if(StringUtils.equalsIgnoreCase(contact.getLastName(), user.getLastName()) && StringUtils.equalsIgnoreCase(contact.getFirstName(), user.getFirstName())) {
    			return true;
    		}
    		if(StringUtils.isNotBlank(contact.getEmail()) && StringUtils.equalsIgnoreCase(contact.getEmail(), user.getEmailAddress())) {
    			return true;
    		}
    	}
    	return false;
    }
}
