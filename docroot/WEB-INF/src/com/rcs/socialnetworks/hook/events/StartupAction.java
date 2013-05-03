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
import org.apache.log4j.Logger;

/**
 * Create the expando attributes on startup: the twitter, linkedin and google access tokens. 
 * @author flor
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
		try {						
			ExpandoColumn expandoColumn = ExpandoColumnLocalServiceUtil.addColumn(
					expandoTable.getTableId(), "googleplusAccessToken",
					ExpandoColumnConstants.STRING);			
			ResourcePermissionLocalServiceUtil.setResourcePermissions(companyId, ExpandoColumn.class.getName(), ResourceConstants.SCOPE_GROUP, String.valueOf(expandoColumn.getColumnId()), user.getRoleId(), new String[] { ActionKeys.VIEW, ActionKeys.UPDATE, ActionKeys.ACCESS });									
		} catch (Exception ignored) { }
		
		try {						
			ExpandoColumn expandoColumn = ExpandoColumnLocalServiceUtil.addColumn(
					expandoTable.getTableId(), "googleplusRefreshToken",
					ExpandoColumnConstants.STRING);			
			ResourcePermissionLocalServiceUtil.setResourcePermissions(companyId, ExpandoColumn.class.getName(), ResourceConstants.SCOPE_GROUP, String.valueOf(expandoColumn.getColumnId()), user.getRoleId(), new String[] { ActionKeys.VIEW, ActionKeys.UPDATE, ActionKeys.ACCESS });									
		} catch (Exception ignored) { }
		
		try {						
			ExpandoColumn expandoColumn = ExpandoColumnLocalServiceUtil.addColumn(
					expandoTable.getTableId(), "googleplusExpirationTime",
					ExpandoColumnConstants.LONG);			
			ResourcePermissionLocalServiceUtil.setResourcePermissions(companyId, ExpandoColumn.class.getName(), ResourceConstants.SCOPE_GROUP, String.valueOf(expandoColumn.getColumnId()), user.getRoleId(), new String[] { ActionKeys.VIEW, ActionKeys.UPDATE, ActionKeys.ACCESS });									
		} catch (Exception ignored) { }
		
		try {
			ExpandoColumn expandoColumn = ExpandoColumnLocalServiceUtil.addColumn(
				expandoTable.getTableId(), "twitterAccessToken",
				ExpandoColumnConstants.STRING);
			ResourcePermissionLocalServiceUtil.setResourcePermissions(companyId, ExpandoColumn.class.getName(), ResourceConstants.SCOPE_GROUP, String.valueOf(expandoColumn.getColumnId()), user.getRoleId(), new String[] { ActionKeys.VIEW, ActionKeys.UPDATE, ActionKeys.ACCESS });
		} catch (Exception ignored) { }
		
		try {
			ExpandoColumn expandoColumn = ExpandoColumnLocalServiceUtil.addColumn(
				expandoTable.getTableId(), "twitterTokenSecret",
				ExpandoColumnConstants.STRING);
			ResourcePermissionLocalServiceUtil.setResourcePermissions(companyId, ExpandoColumn.class.getName(), ResourceConstants.SCOPE_GROUP, String.valueOf(expandoColumn.getColumnId()), user.getRoleId(), new String[] { ActionKeys.VIEW, ActionKeys.UPDATE, ActionKeys.ACCESS });
		} catch (Exception ignored) { }
		
		try {
			ExpandoColumn expandoColumn = ExpandoColumnLocalServiceUtil.addColumn(
				expandoTable.getTableId(), "twitterExpirationTime",
				ExpandoColumnConstants.LONG);
			ResourcePermissionLocalServiceUtil.setResourcePermissions(companyId, ExpandoColumn.class.getName(), ResourceConstants.SCOPE_GROUP, String.valueOf(expandoColumn.getColumnId()), user.getRoleId(), new String[] { ActionKeys.VIEW, ActionKeys.UPDATE, ActionKeys.ACCESS });
		} catch (Exception ignored) { }
		
		try {					
			ExpandoColumn expandoColumn = ExpandoColumnLocalServiceUtil.addColumn(
					expandoTable.getTableId(), "linkedinAccessToken",
					ExpandoColumnConstants.STRING);				
			ResourcePermissionLocalServiceUtil.setResourcePermissions(companyId, ExpandoColumn.class.getName(), ResourceConstants.SCOPE_GROUP, String.valueOf(expandoColumn.getColumnId()), user.getRoleId(), new String[] { ActionKeys.VIEW, ActionKeys.UPDATE, ActionKeys.ACCESS });
		} catch (Exception ignored) { }
		
		try {					
			ExpandoColumn expandoColumn = ExpandoColumnLocalServiceUtil.addColumn(
					expandoTable.getTableId(), "linkedinTokenSecret",
					ExpandoColumnConstants.STRING);		
			ResourcePermissionLocalServiceUtil.setResourcePermissions(companyId, ExpandoColumn.class.getName(), ResourceConstants.SCOPE_GROUP, String.valueOf(expandoColumn.getColumnId()), user.getRoleId(), new String[] { ActionKeys.VIEW, ActionKeys.UPDATE, ActionKeys.ACCESS });
		} catch (Exception ignored) { }
		
		try {					
			ExpandoColumn expandoColumn = ExpandoColumnLocalServiceUtil.addColumn(
					expandoTable.getTableId(), "linkedinExpirationTime",
					ExpandoColumnConstants.LONG);
			ResourcePermissionLocalServiceUtil.setResourcePermissions(companyId, ExpandoColumn.class.getName(), ResourceConstants.SCOPE_GROUP, String.valueOf(expandoColumn.getColumnId()), user.getRoleId(), new String[] { ActionKeys.VIEW, ActionKeys.UPDATE, ActionKeys.ACCESS });
		} catch (Exception ignored) { }
	}
}
