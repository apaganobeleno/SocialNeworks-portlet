package com.rcs.socialnetworks.twitter;

import com.liferay.portal.kernel.exception.SystemException;
import com.rcs.socialnetworks.SocialNetworkOAuthData;
import com.rcs.socialnetworks.utils.LoginPreferences;
import com.rcs.socialnetworks.utils.LoginPreferencesUtil;

import javax.portlet.PortletRequest;

/**
 * @author V. Koshelenko
 */
public class TwitterConnectImpl /*implements SocialNetworkOAuthData */{

    
    public String getAppId(PortletRequest request) throws SystemException {
        LoginPreferences preferences = LoginPreferencesUtil.getPreferences(request);
        return preferences.getTwitterAppId();
    }

    
    public String getAppSecret(PortletRequest request) throws SystemException {
        LoginPreferences preferences = LoginPreferencesUtil.getPreferences(request);
        return preferences.getTwitterAppSecret();
    }

    
    public String getRedirectURL(PortletRequest request) throws SystemException {
        LoginPreferences preferences = LoginPreferencesUtil.getPreferences(request);
        return preferences.getTwitterRedirectURL();
    }

    
    public boolean isEnabled(PortletRequest request) throws SystemException {
        LoginPreferences preferences = LoginPreferencesUtil.getPreferences(request);
        return preferences.getTwitterIsEnabled();
    }
}
