package com.rcs.socialnetworks.contact;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The DTO used to represent a contact 
 * from any social network
 * 
 * @author flor
 *
 */
public class ContactDTO implements Serializable {
	
	private long id;
	private String firstName;
	private String middleName;
	private String lastName;
	private String name;
	private String displayName;
	private String screenName;
	private String email;
	private String locale;
	private String pictureURL;
	private String contactURL;
	
	public enum Gender {
		UNDEFINED(0), MALE(1), FEMALE(2);
        public int value;

        private Gender(int value) {
                this.value = value;
        }
	};  

	private int gender = Gender.UNDEFINED.value;
	
	/**
	 * This List carries the information of the 
	 * social networks the contacts belongs to
	 */
	private List <SocialNetworkDTO> socialNetworks = new ArrayList<SocialNetworkDTO>();
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getMiddleName() {
		return middleName;
	}
	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getPictureURL() {
		return pictureURL;
	}
	public void setPictureURL(String pictureURL) {
		this.pictureURL = pictureURL;
	}
	public List <SocialNetworkDTO> getSocialNetworks() {
		return socialNetworks;
	}
	public void setSocialNetworks(List <SocialNetworkDTO> socialNetworks) {
		this.socialNetworks = socialNetworks;
	}	
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public String getLocale() {
		return locale;
	}
	public void setLocale(String locale) {
		this.locale = locale;
	}
	public int getGender() {
		return gender;
	}
	public void setGender(int gender) {
		this.gender = gender;
	}
	public String getScreenName() {
		return screenName;
	}
	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}
	public String getContactURL() {
		return contactURL;
	}
	public void setContactURL(String contactURL) {
		this.contactURL = contactURL;
	}
	
}
