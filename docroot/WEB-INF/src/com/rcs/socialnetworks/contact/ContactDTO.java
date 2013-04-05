package com.rcs.socialnetworks.contact;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ContactDTO implements Serializable {
	
	private long id;
	private String firstName;
	private String middleName;
	private String lastName;
	private String pictureURL;
	private int pictureHeight; //@@ set default value?
	private int pictureWidth;
	
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
	public int getPictureHeight() {
		return pictureHeight;
	}
	public void setPictureHeight(int pictureHeight) {
		this.pictureHeight = pictureHeight;
	}
	public int getPictureWidth() {
		return pictureWidth;
	}
	public void setPictureWidth(int pictureWidth) {
		this.pictureWidth = pictureWidth;
	}
	
}
