package com.rcs.socialnetworks.contact;

import java.io.Serializable;

public class SocialNetworkDTO implements Serializable {
	
	private String socialNetworkName;

	public String getSocialNetworkName() {
		return socialNetworkName;
	}

	public void setSocialNetworkName(String socialNetworkName) {
		this.socialNetworkName = socialNetworkName;
	}
}
