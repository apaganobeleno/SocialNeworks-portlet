<%
/**
 * Copyright (c) 2000-2012 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="portlet" uri="http://java.sun.com/portlet" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>
<%@ taglib uri="http://liferay.com/tld/theme" prefix="liferay-theme" %>
<%@ taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui" %>
<liferay-theme:defineObjects /> 
<portlet:defineObjects />
<portlet:resourceURL var="revokeSocialNetworkURL" id="revokeSocialNetwork" />
<script type="text/javascript">
      var nodesTable = null;
      var linksTable = null;
      var packagesTable = null;
      var network = null;      
      var imageDIR = "<%=request.getContextPath()%>/img/";
      var jsonContacts = '${jsonContacts}';
      var jsonCurrentUser = '${jsonCurrentUser}';      
      var objContacts = JSON.parse(jsonContacts);
      var objCurrentUser = JSON.parse(jsonCurrentUser);
      
      google.load('visualization', '1');
      
      // Set callback to run when API is loaded
      google.setOnLoadCallback(drawVisualization); 
		
      function isBlank(str) {
    	    return (!str || /^\s*$/.test(str));
      }
      // Called when the Visualization API is loaded.
      function drawVisualization() {
        // Create a data table with nodes.
        nodesTable = new google.visualization.DataTable();
        nodesTable.addColumn('number', 'id');
        nodesTable.addColumn('string', 'text');   // optional
        nodesTable.addColumn('string', 'image');  // optional
        nodesTable.addColumn('string', 'style');   // optional
        
        // Create a data table with links.
        linksTable = new google.visualization.DataTable();
        linksTable.addColumn('number', 'from');
        linksTable.addColumn('number', 'to');
        
        // Create a data table with packages.
    	packagesTable = new google.visualization.DataTable();
        packagesTable.addColumn('number', 'from');
        packagesTable.addColumn('number', 'to');
        packagesTable.addColumn('number', 'progress');  // optional
        packagesTable.addColumn('string', 'image');  // optional
        packagesTable.addColumn('string', 'style');  // optional
		
        //add the central node, it's me!        
        nodesTable.addRow([0, objCurrentUser['firstName'] + ' ' + objCurrentUser['lastName'], objCurrentUser['pictureURL'] ? objCurrentUser['pictureURL'] : '<%=user.getPortraitURL(themeDisplay) %>' , 'image']);        
		for (var key in objContacts) {
    	   	var obj = objContacts[key];    	   	    		
	 	   	// iterate through contacts
	 	   	for (var prop in obj) {	 		   	 	       		
 	       		var data = obj[prop];
 	       		var name = '';  	       		
 	       		if(!isBlank(data['displayName'])) {
 	       			name = data['displayName']; 	       			
 	       		}        		
				nodesTable.addRow([data['id'], name, data['pictureURL'], 'image']);
				linksTable.addRow([0, data['id']]);								
				var socialnetworks = data['socialNetworks'];
				var distance = 0.3;
								
				for(var socialnetwork in socialnetworks) {
					var socialnetworkData = socialnetworks[socialnetwork];					
					for(var value in socialnetworkData) {
						var img = '';						
						var dataValue = socialnetworkData[value];						
						if(dataValue == 'google') {
							img = imageDIR + 'google-logo.png';						
						}
						if(dataValue == 'linkedin') {
							img = imageDIR + 'linkedin-logo.jpg';
						}
						if(dataValue == 'facebook') {
							img = imageDIR + 'facebook-logo.png';
						}
						if(dataValue == 'liferay') {
							img = imageDIR + 'liferay-logo.png';
						}
						if(dataValue == 'twitter') {
							img = imageDIR + 'twitter-logo.jpg';
						}											
						packagesTable.addRow([0, data['id'], distance, img, 'image']);
						distance = distance + 0.2;						
					}
					
					
				}
				
	 	   	}
    	}
        
     	// specify options
        var options = {
          'width': '900px', 
          'height': '700px',
          'stabilize': false,   // stabilize positions before displaying
          'nodes': {
              // default for all nodes        	  
        	  'radius': 50
        	  ,'distance': 200
        	  ,'style': 'image'
            }
        };
          	
        // Instantiate our graph object.
        network = new links.Network(document.getElementById('mynetwork'));

        // Draw our graph with the created data and options 
        network.draw(nodesTable, linksTable, packagesTable, options);

      }
      
      jQuery(document).ready(function(){
    	  <%--//Configuration Form Button Listener --%>
          jQuery(".socialnetwork").click(function() {
        	  jQuery("#socialnetwork-portlet").mask('Loading...');
              console.log("click");
        	  var socialnetworkName = jQuery(this).attr('id');                         	              
              jQuery.ajax({
                  url : '${revokeSocialNetworkURL}'
                      ,type : 'POST'
                   	  ,data: { 
                   	        'socialNetworkName': socialnetworkName               	        
                   	  }
                      ,success : function(data) { location.reload(); }
                  });
              
          });
      });
      
      
</script>
<div id="socialnetwork-portlet">
	<jsp:include page="navigation.jsp" />
	<div id="mynetwork"></div>
</div>