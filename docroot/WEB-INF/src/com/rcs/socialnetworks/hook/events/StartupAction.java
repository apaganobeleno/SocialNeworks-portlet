package com.rcs.socialnetworks.hook.events;

import org.apache.log4j.Logger;

import com.liferay.portal.kernel.events.ActionException;
import com.liferay.portal.kernel.events.SimpleAction;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.model.ResourceConstants;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.RoleConstants;
import com.liferay.portal.model.User;
import com.liferay.portal.security.permission.ActionKeys;
import com.liferay.portal.service.ResourcePermissionLocalServiceUtil;
import com.liferay.portal.service.RoleLocalServiceUtil;
import com.liferay.portlet.expando.model.ExpandoColumn;
import com.liferay.portlet.expando.model.ExpandoColumnConstants;
import com.liferay.portlet.expando.model.ExpandoTable;
import com.liferay.portlet.expando.model.ExpandoTableConstants;
import com.liferay.portlet.expando.service.ExpandoColumnLocalServiceUtil;
import com.liferay.portlet.expando.service.ExpandoTableLocalServiceUtil;
import com.rcs.socialnetworks.facebook.FacebookConnectUtil;
import com.rcs.socialnetworks.google.GoogleConnectUtil;
import com.rcs.socialnetworks.linkedin.LinkedInConnectUtil;
import com.rcs.socialnetworks.twitter.TwitterConnectUtil;

import org.apache.log4j.Logger;

/**
 * Create on startup the expando attributes needed for OAuth: 
 * the twitter, linkedin, google  and facebook access tokens.
 * 
 * After creating the expando attributes, the admin user has
 * to provide the user VIEW and UPDATE permissions manually
 * from the control panel.
 *  
 * @author flor - florencia.gadea@rotterdam-cs.com
 *
 */
public class StartupAction extends SimpleAction {
	
	protected static final Logger logger = Logger.getLogger(StartupAction.class);
	
	@Override
	public void run(String[] ids) throws ActionException {
		try {			
			doRun(GetterUtil.getLong(ids[0]));
		}
		catch (Exception e) {
			throw new ActionException(e);
		}
	}

	protected void doRun(long companyId) throws Exception {
		
		logger.error("StartupAction: start");
		ExpandoTable expandoTable = null;

		try {			
			expandoTable = ExpandoTableLocalServiceUtil.addTable(companyId, User.class.getName(), ExpandoTableConstants.DEFAULT_TABLE_NAME);			
		} catch (Exception e) {			
			expandoTable = ExpandoTableLocalServiceUtil.getTable(companyId, User.class.getName(), ExpandoTableConstants.DEFAULT_TABLE_NAME);					
		}

		Role user = RoleLocalServiceUtil.getRole(companyId, RoleConstants.USER);
		String[] userPermissions = new String[] { ActionKeys.VIEW, ActionKeys.UPDATE, ActionKeys.ACCESS, ActionKeys.DELETE };
		try {						
			ExpandoColumn expandoColumn = ExpandoColumnLocalServiceUtil.addColumn(
					expandoTable.getTableId(), GoogleConnectUtil.ACCESS_TOKEN_FIELD,
					ExpandoColumnConstants.STRING);			
			ResourcePermissionLocalServiceUtil.setResourcePermissions(companyId, ExpandoColumn.class.getName(), ResourceConstants.SCOPE_INDIVIDUAL, String.valueOf(expandoColumn.getColumnId()), user.getRoleId(), userPermissions);									
		} catch (Exception ignored) { }
		
		try {						
			ExpandoColumn expandoColumn = ExpandoColumnLocalServiceUtil.addColumn(
					expandoTable.getTableId(), GoogleConnectUtil.REFRESH_TOKEN_FIELD,
					ExpandoColumnConstants.STRING);			
			ResourcePermissionLocalServiceUtil.setResourcePermissions(companyId, ExpandoColumn.class.getName(), ResourceConstants.SCOPE_INDIVIDUAL, String.valueOf(expandoColumn.getColumnId()), user.getRoleId(), userPermissions);									
		} catch (Exception ignored) { }
		
		try {						
			ExpandoColumn expandoColumn = ExpandoColumnLocalServiceUtil.addColumn(
					expandoTable.getTableId(), GoogleConnectUtil.EXPIRATION_TIME_FIELD,
					ExpandoColumnConstants.LONG);			
			ResourcePermissionLocalServiceUtil.setResourcePermissions(companyId, ExpandoColumn.class.getName(), ResourceConstants.SCOPE_COMPANY, String.valueOf(expandoColumn.getColumnId()), user.getRoleId(), userPermissions);									
		} catch (Exception ignored) { }
		
		try {
			ExpandoColumn expandoColumn = ExpandoColumnLocalServiceUtil.addColumn(
				expandoTable.getTableId(), TwitterConnectUtil.ACCESS_TOKEN_FIELD,
				ExpandoColumnConstants.STRING);
			ResourcePermissionLocalServiceUtil.setResourcePermissions(companyId, ExpandoColumn.class.getName(), ResourceConstants.SCOPE_GROUP, String.valueOf(expandoColumn.getColumnId()), user.getRoleId(), userPermissions);
		} catch (Exception ignored) { }
		
		try {
			ExpandoColumn expandoColumn = ExpandoColumnLocalServiceUtil.addColumn(
				expandoTable.getTableId(), TwitterConnectUtil.TOKEN_SECRET_FIELD,
				ExpandoColumnConstants.STRING);
			ResourcePermissionLocalServiceUtil.setResourcePermissions(companyId, ExpandoColumn.class.getName(), ResourceConstants.SCOPE_GROUP, String.valueOf(expandoColumn.getColumnId()), user.getRoleId(), userPermissions);
		} catch (Exception ignored) { }
				
		try {					
			ExpandoColumn expandoColumn = ExpandoColumnLocalServiceUtil.addColumn(
					expandoTable.getTableId(), LinkedInConnectUtil.ACCESS_TOKEN_FIELD,
					ExpandoColumnConstants.STRING);				
			ResourcePermissionLocalServiceUtil.setResourcePermissions(companyId, ExpandoColumn.class.getName(), ResourceConstants.SCOPE_GROUP, String.valueOf(expandoColumn.getColumnId()), user.getRoleId(), userPermissions);
		} catch (Exception ignored) { }
		
		try {					
			ExpandoColumn expandoColumn = ExpandoColumnLocalServiceUtil.addColumn(
					expandoTable.getTableId(), LinkedInConnectUtil.TOKEN_SECRET_FIELD,
					ExpandoColumnConstants.STRING);		
			ResourcePermissionLocalServiceUtil.setResourcePermissions(companyId, ExpandoColumn.class.getName(), ResourceConstants.SCOPE_GROUP, String.valueOf(expandoColumn.getColumnId()), user.getRoleId(), userPermissions);
		} catch (Exception ignored) { }
		
		try {					
			ExpandoColumn expandoColumn = ExpandoColumnLocalServiceUtil.addColumn(
					expandoTable.getTableId(), LinkedInConnectUtil.EXPIRATION_TIME_FIELD,
					ExpandoColumnConstants.LONG);
			ResourcePermissionLocalServiceUtil.setResourcePermissions(companyId, ExpandoColumn.class.getName(), ResourceConstants.SCOPE_GROUP, String.valueOf(expandoColumn.getColumnId()), user.getRoleId(), userPermissions);
		} catch (Exception ignored) { }
		
		try {					
			ExpandoColumn expandoColumn = ExpandoColumnLocalServiceUtil.addColumn(
					expandoTable.getTableId(), FacebookConnectUtil.ACCESS_TOKEN_FIELD,
					ExpandoColumnConstants.STRING);				
			ResourcePermissionLocalServiceUtil.setResourcePermissions(companyId, ExpandoColumn.class.getName(), ResourceConstants.SCOPE_GROUP, String.valueOf(expandoColumn.getColumnId()), user.getRoleId(), userPermissions);
		} catch (Exception ignored) { }
		
		try {					
			ExpandoColumn expandoColumn = ExpandoColumnLocalServiceUtil.addColumn(
					expandoTable.getTableId(), FacebookConnectUtil.EXPIRATION_TIME_FIELD,
					ExpandoColumnConstants.LONG);		
			ResourcePermissionLocalServiceUtil.setResourcePermissions(companyId, ExpandoColumn.class.getName(), ResourceConstants.SCOPE_GROUP, String.valueOf(expandoColumn.getColumnId()), user.getRoleId(), userPermissions);
		} catch (Exception ignored) { }
	}
}
