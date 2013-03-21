package com.rcs.socialnetworks.portlet;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.portlet.PortletContext;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.bind.annotation.RenderMapping;

import com.google.gson.stream.JsonWriter;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

@Controller(value="SocialNetworksController") 
@RequestMapping("VIEW")
public class SocialNetworksController {
	private static Log log = LogFactoryUtil.getLog(SocialNetworksController.class);
		
	@RenderMapping
	public ModelAndView resolveView(PortletRequest request, PortletResponse response) throws PortalException, SystemException {
		 
		Map<String, Object> model = new HashMap<String, Object>();
		
		
		try {
			BufferedImage in = javax.imageio.ImageIO.read(new URL("http://m4.licdn.com/media/p/1/000/129/371/02397eb.jpg"));
			BufferedImage out = scaleImage(in, BufferedImage.TYPE_INT_RGB, 60, 120);
			PortletContext portletContext = request.getPortletSession().getPortletContext();
			
			File file = new java.io.File(portletContext.getRealPath("/") + "img/temp/1.jpg");
			log.debug("file: " + file.exists());
	        javax.imageio.ImageIO.write(out, "JPG", file);
	        
	        in = javax.imageio.ImageIO.read(new URL("http://m3.licdn.com/media/p/3/000/11f/073/2e02e59.jpg"));
			out = scaleImage(in, BufferedImage.TYPE_INT_RGB, 60, 120);			
			
			file = new java.io.File(portletContext.getRealPath("/") + "img/temp/2.jpg");
			log.debug("file: " + file.exists());
	        javax.imageio.ImageIO.write(out, "JPG", file);
	        
	        in = javax.imageio.ImageIO.read(new URL("http://m4.licdn.com/media/p/4/000/150/223/1c87138.jpg"));
			out = scaleImage(in, BufferedImage.TYPE_INT_RGB, 60, 120);			
			
			file = new java.io.File(portletContext.getRealPath("/") + "img/temp/3.jpg");
			log.debug("file: " + file.exists());
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
		model.put("jsonContacts", jsonContacts);
		log.info("info" + jsonContacts);
		return new ModelAndView("views/view", model);
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
