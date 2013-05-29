package com.rcs.socialnetworks;

/**
 * Interface that defines OAuth 1 
 * constants and methods
 * 
 * @author flor
 *
 * @param <RequestToken>
 * @param <AccessToken>
 */
public interface OAuth10Interface<RequestToken,AccessToken> {

	public AccessToken getAccessToken(RequestToken requestToken, String oAuthVerifier);

	/**
	 * OAuth 1 uses a request token
	 * 
	 * @return
	 */
	public RequestToken getRequestToken();
	
	public RequestToken getRequestToken(String redirectURL);
}
