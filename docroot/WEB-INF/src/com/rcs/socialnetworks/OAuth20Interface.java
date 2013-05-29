package com.rcs.socialnetworks;

/**
 * Interface that defines OAuth 2 
 * constants and methods
 * 
 * @author flor
 *
 */
public interface OAuth20Interface {	
	
	/**
	 * The OAuth 2 parameter code
	 */
	public static final String CODE = "code";
	
	/**
	 * The OAuth 2 parameter state
	 */
	public static final String STATE = "state";
	
	/**
	 * Specifies the resources 
	 * the portlet will access
	 *  
	 * @return
	 */
	public String getScope();
}
