package com.rcs.socialnetworks.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.portlet.PortletRequest;

import org.apache.commons.lang.StringUtils;

import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.User;
import com.liferay.portal.theme.ThemeDisplay;
import com.rcs.socialnetworks.contact.ContactDTO;
import com.rcs.socialnetworks.contact.SocialNetworkDTO;

public class LiferayUtil {

	public static List<ContactDTO> addContacts(PortletRequest request, List<ContactDTO> contacts, Collection<?> liferayContacts) throws SystemException {    	
    	if(liferayContacts != null && !liferayContacts.isEmpty()) {    		
			List<User> users = (List<User>) liferayContacts; //@@ ver cómo chequear esto					
			for(User user : users) {
				contacts = LiferayUtil.addContactAndCheckDuplicated(request, contacts, user);
			}			
		}
    	return contacts;    	
    }
    
    public static List<ContactDTO> addContactAndCheckDuplicated(PortletRequest request, List<ContactDTO> contacts, User user) throws SystemException {
    	long contactId = 1;
    	boolean isDuplicated = false;
    	if(!contacts.isEmpty()) {
    		ContactDTO lastContact = contacts.get(contacts.size()-1);
    		contactId = lastContact.getId() + 1;
    		// check if contact is duplicated
    		for(ContactDTO contact : contacts) {
        		if(StringUtils.equalsIgnoreCase(contact.getLastName(), user.getLastName()) && StringUtils.equalsIgnoreCase(contact.getFirstName(), user.getFirstName())) {    			
        			List<SocialNetworkDTO> socialNetworks = contact.getSocialNetworks();
        			socialNetworks.add(new SocialNetworkDTO("liferay")); //@@ add constant
        			contact.setSocialNetworks(socialNetworks);
        			isDuplicated = true;
        		}
        	}
    	}    	    	
    	// if it is not duplicated
    	if(!isDuplicated) {
    		ContactDTO contact = new ContactDTO();				
			contact.setId(contactId);
			contact.setFirstName(user.getFirstName());
			//contact.setMiddleName(person.getMiddleName());
			contact.setLastName(user.getLastName());
			ThemeDisplay themeDisplay = (ThemeDisplay)request.getAttribute(WebKeys.THEME_DISPLAY);
			try {
				contact.setPictureURL("http://" + request.getServerName() + ":" + request.getServerPort() + user.getPortraitURL(themeDisplay));
			} catch(Exception e) {
				contact.setPictureURL(""); //@@ add another static image
			}					
			List<SocialNetworkDTO> socialNetworks = new ArrayList<SocialNetworkDTO>();
			socialNetworks.add(new SocialNetworkDTO("liferay"));
			contact.setSocialNetworks(socialNetworks);
			contacts.add(contact);
			contactId++;    				
    	}
    	return contacts;
    }
}
