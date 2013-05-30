SocialNeworks-portlet
=====================

The Social Networking Integration portlet represents a user social graph, showing all the user contacts from different social networks (Facebook, LinkedIn, Twitter and Google) and takes care of the user authentication through OAuth.

Installation and configuration:

1) To deploy the portlet, you can just throw it in the deploy folder as you usually do.

2) To let the user authenticate with the different social networks, you need register your site with the social networks you need. Once you register the app, you will be provided an application id and an application secret. You will be requested to provide a callback url (the is the url that the social network will redirect the user to once a successful authentication is finished). The callback urls for google and facebook are fixed. You should set it to http://<your-domain>/c/portal/oauthcallback. For Twitter and LinkedIn it will be enough to specify the domain only.

3) You have to fill the proper portal properties in portal.properties with the given api key and secret of the desired social networks.

5) And then you have to set the <socialnetwork>.oauth.enabled property to true.

6) Restart the portlet, so the new properties are taken and the necessary expando fields are created.

7) After deploying the portlet, you need to restart the server. The new expando fields for the user table will be created. You'll have to set manually the user view and update permissions of these fields. Go to the Control Panel, Custom Fields (under Portal), click on User resource, and then you'll see all the fields just created. Click on Actions -> Permissions, and set the Update and View for role User.

8) Now you can put the portlet in any page you want and test. Click on the desired social network link and see what happens!
