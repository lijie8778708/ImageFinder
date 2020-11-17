package com.eulerity.hackathon.webcrawler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * 
 * @author jieli
 *
 */
public class ImageUtil {
	
	/**
	 * srcset not only contains images link, but also other trash information. 
	 * This method will filter all trash method and return all image links
	 * @param srcSet
	 * @return
	 */
	public static HashMap<String, String> splitImageUrlForSrcSet(String srcSet) {
		String[] allImageUrls = srcSet.split(" ");
        HashMap<String, String> imageMap = new HashMap<>();
        
        for(String curImg : allImageUrls) {
        	
        	// check if current string is a image link
        	if(curImg.startsWith("https://")) {
        		if(curImg.indexOf('?') != -1)
        			curImg = curImg.substring(0, curImg.indexOf('?'));
        		String key = curImg.substring(curImg.lastIndexOf("/"));
	        	imageMap.put(key, curImg);
        	}
        }
		return imageMap;
	}
	
	/**
	 * This method will handle normal src and return a valid image link
	 * @param src
	 * @return
	 */
	public static String splitImageUrlForSrc(String src) {
		if(src.startsWith("https://")) {
			return src;
		}else {
			src = "https:" + src;
			if(src.startsWith("https://")) {
				return src;
			}
		}
		return null;
	}
	
	/**
	 * Get content from web site and find all images from the content
	 * @param src
	 * @return
	 */
	public static String[] splitAllImageFromUrl(String src) {
		String webContent = null;
		try {
			webContent = ContentUtil.getContentFromUrl(src);
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return allImageFromUrl(webContent);
	}
	
	/**
	 * Search all image link from given content
	 * @param webContent
	 * @return
	 */
	private static String[] allImageFromUrl(String webContent) {
		if(webContent == null) {
			return new String[] {};
		}
		Document doc = Jsoup.parseBodyFragment(webContent);
		Elements imgElements = doc.select("img");
		ArrayList<String> ret_list = new ArrayList<String>();
		
		for(Element e : imgElements){
            String srcSet=e.attr("srcset");
            String src = e.attr("src");
            
            // Some web site use srcset to contains a group of image
            // handle both src or srcset
            Map<String, String> mapForSrcSet = ImageUtil.splitImageUrlForSrcSet(srcSet);
            String strForSrc = ImageUtil.splitImageUrlForSrc(src);
            
            for(String keyForImage : mapForSrcSet.keySet()) {
            	ret_list.add(mapForSrcSet.get(keyForImage));
	        }
            if(strForSrc != null) {
            	ret_list.add(strForSrc);
            }
            
            // Big amount of image will low of the speed, i only want no more than 20 images in a single thread
            if(ret_list.size() > 20) {
            	break;
            }
	    }
		return ret_list.size() == 0 ? null : convertArrayListToStringList(ret_list);
	}
	
	private static String[] convertArrayListToStringList(ArrayList<String> ret_list) {
		String[] imgUrls = new String[ret_list.size()];
		
		int index = 0;
		for(String s : ret_list) {
			imgUrls[index++] = s;
		}
		return imgUrls;
	}
}