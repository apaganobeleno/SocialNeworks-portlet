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

<portlet:defineObjects />

<script type="text/javascript">
      var nodesTable = null;
      var linksTable = null;
      var packagesTable = null;
      var network = null;      
      
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
        
        // Create a data table with links.
        linksTable = new google.visualization.DataTable();
        linksTable.addColumn('number', 'from');
        linksTable.addColumn('number', 'to');		
        //add the central node, it's me!        
        nodesTable.addRow([0, 'Me']);
        console.log(objContacts);
		for (var key in objContacts) {
    	   	var obj = objContacts[key];    	   	    		
	 	   	// iterat through contacts
	 	   	for (var prop in obj) {	 		   	
 	       		//console.log(prop + " = " + obj[prop]);
 	       		var data = obj[prop];	 	       	
				nodesTable.addRow([data['id'], data['firstname'] + ' ' + data['lastname']]);
				linksTable.addRow([0, data['id']]);
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
          'stabilize': false   // stabilize positions before displaying
        };
          	
        // Instantiate our graph object.
        network = new links.Network(document.getElementById('mynetwork'));

        // Draw our graph with the created data and options 
        network.draw(nodesTable, linksTable, packagesTable, options);

      }
      
</script>
This is the <b>SocialNeworks</b> portlet.

<div id="mynetwork"></div>