package com.rcs.socialnetworks.contact;

import java.io.Serializable;

/**
 * This class holds the information about the
 * social network the contact belongs to 
 * 
 * @author flor
 *
 */
public class SocialNetworkDTO implements Serializable {
		
	private String socialNetworkName;
	
	public SocialNetworkDTO(String socialNetworkName) {
		this.socialNetworkName = socialNetworkName;
	}

	public String getSocialNetworkName() {
		return socialNetworkName;
	}

	public void setSocialNetworkName(String socialNetworkName) {
		this.socialNetworkName = socialNetworkName;
	}
}
