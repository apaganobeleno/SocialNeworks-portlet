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
<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="portlet" uri="http://java.sun.com/portlet" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>
<%@ taglib uri="http://liferay.com/tld/theme" prefix="liferay-theme" %>
<%@ taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui" %>
<liferay-theme:defineObjects /> 
<portlet:defineObjects />

<script type="text/javascript">
      var nodesTable = null;
      var linksTable = null;
      var packagesTable = null;
      var network = null;      
      var imageDIR = "<%=request.getContextPath()%>/img/";
      var jsonContacts = '${jsonContacts}';
      //console.log(jsonContacts);
      var objContacts = JSON.parse(jsonContacts);
      //console.log(objContacts);
      google.load('visualization', '1');
      
      // Set callback to run when API is loaded
      google.setOnLoadCallback(drawVisualization); 

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
        nodesTable.addRow([0, 'Me', imageDIR + 'user_portrait.png', 'image']);        
		for (var key in objContacts) {
    	   	var obj = objContacts[key];    	   	    		
	 	   	// iterat through contacts
	 	   	for (var prop in obj) {	 		   	
 	       		//console.log(prop + " = " + obj[prop]);
 	       		var data = obj[prop];	 	       	
				nodesTable.addRow([data['id'], data['firstname'] + ' ' + data['lastname'], imageDIR + data['picture'], 'image']);
				linksTable.addRow([0, data['id']]);				
				console.log(data['id']);
				console.log(data['socialnetworks']);
				var socialnetworks = data['socialnetworks'];
				var distance = 0.3;
				//@@ count the amount of social networks
				for(var socialnetwork in socialnetworks) {
					var socialnetworkData = socialnetworks[socialnetwork];
					console.log(socialnetworkData);
					for(var value in socialnetworkData) {
						var img;
						console.log(socialnetworkData[value]);
						var dataValue = socialnetworkData[value];
						if(dataValue['value'] == 'googleplus') {
							img = imageDIR + 'googleplus-logo.png';						
						}
						if(dataValue['value'] == 'linkedin') {
							img = imageDIR + 'linkedin-logo.png';
						}
						if(dataValue['value'] == 'facebook') {
							img = imageDIR + 'facebook-logo.png';
						}
						packagesTable.addRow([0, data['id'], distance, img, 'image']);
						distance = distance + 0.2;
						console.log(img);
					}
					
					
				}
				
	 	   	}
    	}
        
        //nodesTable.addRow([2]);
        //nodesTable.addRow([3]);
        
        //linksTable.addRow([1, 2]);
        //linksTable.addRow([3, 2]);
        
     	// specify options
        var options = {
          'width': '600px', 
          'height': '600px',
          'stabilize': false,   // stabilize positions before displaying
          'nodes': {
              // default for all nodes
        	  "widthMax": 30
        	  ,"distance": 200
            }
        };
          	
        // Instantiate our graph object.
        network = new links.Network(document.getElementById('mynetwork'));

        // Draw our graph with the created data and options 
        network.draw(nodesTable, linksTable, packagesTable, options);

      }
      
</script>
This is the <b>SocialNeworks</b> portlet.

<div id="mynetwork"></div>