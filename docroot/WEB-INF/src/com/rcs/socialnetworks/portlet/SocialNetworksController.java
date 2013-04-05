package com.rcs.socialnetworks.portlet;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletContext;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.bind.annotation.RenderMapping;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.Image;
import com.liferay.portal.model.User;
import com.liferay.portal.service.ImageLocalServiceUtil;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.comparator.UserLastNameComparator;
import com.liferay.portlet.social.model.SocialRelationConstants;
import com.rcs.socialnetworks.contact.ContactDTO;
import com.rcs.socialnetworks.contact.SocialNetworkDTO;

@Controller(value="SocialNetworksController") 
@RequestMapping("VIEW")
public class SocialNetworksController {
	private static Log log = LogFactoryUtil.getLog(SocialNetworksController.class);
		
	@RenderMapping
	public ModelAndView resolveView(PortletRequest request, PortletResponse response) throws PortalException, SystemException {
		 
		Map<String, Object> model = new HashMap<String, Object>();
		String contactsJSON = "";		
		try {
			//request.getUserId();
			PortletContext portletContext = request.getPortletSession().getPortletContext();
			String realPath = portletContext.getRealPath("/");
			
			ThemeDisplay themeDisplay = (ThemeDisplay)request.getAttribute(WebKeys.THEME_DISPLAY);
			long currentUserId = themeDisplay.getUserId();
			int socialUsersCount = UserLocalServiceUtil.getSocialUsersCount(currentUserId, SocialRelationConstants.TYPE_BI_FRIEND);
			log.error("socialUsersCount: " + socialUsersCount);
			List<User> friends = UserLocalServiceUtil.getSocialUsers(currentUserId, SocialRelationConstants.TYPE_BI_FRIEND, 0, socialUsersCount, new UserLastNameComparator(true));
			List<ContactDTO> contacts = new ArrayList<ContactDTO>();
			//@@add it in the constructor
			SocialNetworkDTO socialNetworkDTO = new SocialNetworkDTO();
			socialNetworkDTO.setSocialNetworkName("liferay");
			List<SocialNetworkDTO> socialNetworks = new ArrayList<SocialNetworkDTO>();
			socialNetworks.add(socialNetworkDTO);			
			long id = 1;
			for(User friend : friends )  {
				log.error("friend: " + friend.toString());
				//@@do this in a utils class
				ContactDTO contact = new ContactDTO();
				contact.setId(id);
				contact.setFirstName(friend.getFirstName());
				contact.setMiddleName(friend.getMiddleName());
				contact.setLastName(friend.getLastName());				
				//@@ change later				
				//contact.setPictureURL(request.getServerName() + ":" + request.getServerPort() + friend.getPortraitURL(themeDisplay));				
				contact.setPictureURL(request.getServerName() + ":" + request.getServerPort() + friend.getPortraitURL(themeDisplay));
				Image image = ImageLocalServiceUtil.getImage(friend.getPortraitId());
				if(image != null) {
					contact.setPictureHeight(image.getHeight());
					contact.setPictureWidth(image.getWidth());
				}								
				contact.setSocialNetworks(socialNetworks);
				contacts.add(contact);
				id++;
			}
			
			Gson gson = new Gson();
	        contactsJSON = gson.toJson(contacts);
	        log.error("contacts: " + contactsJSON);
	        
			BufferedImage in = javax.imageio.ImageIO.read(new URL("http://m4.licdn.com/media/p/1/000/129/371/02397eb.jpg"));			
			BufferedImage out = scaleImage(in, BufferedImage.TYPE_INT_RGB, 60, 120);			
			
			log.error("url: " + portletContext.getRealPath("/") + "img/temp/1.jpg");
			File file = new java.io.File(realPath + "img/temp/1.jpg");
			
	        javax.imageio.ImageIO.write(out, "JPG", file);
	        
	        in = javax.imageio.ImageIO.read(new URL("http://m3.licdn.com/media/p/3/000/11f/073/2e02e59.jpg"));
			out = scaleImage(in, BufferedImage.TYPE_INT_RGB, 60, 120);			
			
			file = new java.io.File(portletContext.getRealPath("/") + "img/temp/2.jpg");
			log.error("file: " + file.exists());
	        javax.imageio.ImageIO.write(out, "JPG", file);
	        
	        in = javax.imageio.ImageIO.read(new URL("http://m4.licdn.com/media/p/4/000/150/223/1c87138.jpg"));
			out = scaleImage(in, BufferedImage.TYPE_INT_RGB, 60, 120);			
			
			file = new java.io.File(portletContext.getRealPath("/") + "img/temp/3.jpg");			
	        javax.imageio.ImageIO.write(out, "JPG", file);
	        
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        				
		String jsonContacts = " {\"person\": [{" + 
		   "\"id\": 1," +
		   "\"firstname\": \"Gustavo\"," +
		   "\"lastname\": \"Genovese\"," +
		   "\"picture\": \"temp/1.jpg\"," + 
		   "\"socialnetworks\": {" +
		     "\"socialnetwork\": [" +
		       "{\"value\": \"linkedin\"}," +
		       "{\"value\": \"facebook\"}," +
		       "{\"value\": \"googleplus\"}" +
		     "]" +
		   "}"+
		 "},{" +
		   "\"id\": 2," +
		   "\"firstname\": \"Marcelo\"," +
		   "\"lastname\": \"Aberastain\"," +
		   "\"picture\": \"temp/2.jpg\"," +
		   "\"socialnetworks\": {" +
		     "\"socialnetwork\": [" +
		       "{\"value\": \"linkedin\"}," +
		       "{\"value\": \"facebook\"}" +
		     "]" +
		   "}" +		 
		  "},{" +
		   "\"id\": 3," +
		   "\"firstname\": \"Maarten\"," +
		   "\"lastname\": \"Jongmans\"," +
		   "\"picture\": \"temp/3.jpg\"," +
		   "\"socialnetworks\": {" +
		     "\"socialnetwork\": [" +
		       "{\"value\": \"linkedin\"}" +		       
		     "]" +
		   "}" +		 
		  "}]" +
		"}";	
		model.put("jsonContacts", " {\"person\": " + contactsJSON + "}");
		//log.info("info" + jsonContacts);
		return new ModelAndView("/WEB-INF/views/view.jsp", model);
	}
	
	public static BufferedImage scaleImage(BufferedImage image, int imageType,
            int newWidth, int newHeight) {
        // Make sure the aspect ratio is maintained, so the image is not distorted
        double thumbRatio = (double) newWidth / (double) newHeight;
        int imageWidth = image.getWidth(null);
        int imageHeight = image.getHeight(null);
        double aspectRatio = (double) imageWidth / (double) imageHeight;

        if (thumbRatio < aspectRatio) {
            newHeight = (int) (newWidth / aspectRatio);
        } else {
            newWidth = (int) (newHeight * aspectRatio);
        }

        // Draw the scaled image
        BufferedImage newImage = new BufferedImage(newWidth, newHeight,
                imageType);
        Graphics2D graphics2D = newImage.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.drawImage(image, 0, 0, newWidth, newHeight, null);

        return newImage;
    }
}
