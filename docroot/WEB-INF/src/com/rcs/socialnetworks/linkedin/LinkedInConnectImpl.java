package com.rcs.socialnetworks.linkedin;

//import com.aimprosoft.portlet.login.model.LoginPreferences;
//import com.aimprosoft.portlet.login.util.LoginPreferencesUtil;
import com.liferay.portal.kernel.exception.SystemException;
import com.rcs.socialnetworks.SocialNetworkOAuthData;
import com.rcs.socialnetworks.utils.LoginPreferences;
import com.rcs.socialnetworks.utils.LoginPreferencesUtil;

import javax.portlet.PortletRequest;

/**
 * @author Florencia Gadea
 */
public class LinkedInConnectImpl /*implements SocialNetworkOAuthData */{


    public String getAppId(PortletRequest request) throws SystemException {
        LoginPreferences preferences = LoginPreferencesUtil.getPreferences(request);
        return preferences.getLinkedInAppId();
    }


    public String getAppSecret(PortletRequest request) throws SystemException {
        LoginPreferences preferences = LoginPreferencesUtil.getPreferences(request);
        return preferences.getLinkedInAppSecret();
    }


    public String getRedirectURL(PortletRequest request) throws SystemException {
        LoginPreferences preferences = LoginPreferencesUtil.getPreferences(request);
        return preferences.getLinkedInRedirectURL();
    }

    public boolean isEnabled(PortletRequest request) throws SystemException {
        LoginPreferences preferences = LoginPreferencesUtil.getPreferences(request);
        return preferences.getLinkedInIsEnabled();
    }
}
