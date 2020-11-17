package com.eulerity.hackathon.webcrawler;

import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashSet;

import javax.naming.directory.InvalidAttributesException;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 * 
 * @author jieli
 */
public class ContentUtil{
	
	/**
	 * verify the input link and split all sub-page link from it.
	 * @param url
	 * @return
	 * @throws Exception 
	 */
	public static ArrayList<String> splitAllUrlsFromSrc(String url) throws Exception {
		if(!isLinkValid(url)) {
			throw new InvalidAttributesException("Invalid Input");
		}
		
		String webContent = "";
		try {
			webContent = getContentFromUrl(url);
		}catch(Exception e) {
			e.printStackTrace();
		}
		if(webContent.length() == 0)
			return null;
		return findAllUrlsFromContent(webContent, url);
	}
	
	/**
	 * Try to connect the link and see if this is a valid link
	 * @param url
	 * @return
	 */
	public static boolean isLinkValid(String url) {
		URL tempurl;
		try {
			tempurl = new URL(url);
			URLConnection co = tempurl.openConnection();
			co.connect();
			return true;
		} catch(Exception e) {
			return false;
		}
	}
	
	/**
	 * Connect to the given link and return a content body as string
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public static String getContentFromUrl(String url) throws Exception{
		HttpClient client = HttpClients.createDefault();
		HttpGet get = new HttpGet(url);
		get.setHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_3) AppleWebKit/537.36 (KHTML); like Gecko) Chrome/35.0.1916.47 Safari/537.36");   
		CloseableHttpResponse response = (CloseableHttpResponse) client.execute(get);
		HttpEntity websiteEntity = response.getEntity();
		return EntityUtils.toString(websiteEntity);
	}
	
	/**
	 * Search all sub-links from given content
	 * @param webContent
	 * @return
	 */
	public static ArrayList<String> findAllUrlsFromContent(String webContent, String url){
		Document doc = Jsoup.parseBodyFragment(webContent);
		Elements es = doc.select("a");
		
		return findAllUrlsFromATags(es, url);
	}
	
	/**
	 * Check each tags and return valid href links;
	 * @param es
	 * @return
	 */
	public static ArrayList<String> findAllUrlsFromATags(Elements es, String url){
		
		ArrayList<String> contentList = new ArrayList<String>();
		HashSet<String> checkList = new HashSet<String>();
//		
//		System.out.println(es.toString());
		int index = 0;
		while(index < es.size()) {
			String foundUrl = es.get(index++).attr("href");
			foundUrl = formatFoundUrl(foundUrl, url);
			foundUrl = cleanFoundUrl(foundUrl);
			
			if(foundUrl != null && !checkList.contains(foundUrl)) {
				
				contentList.add(foundUrl);
				checkList.add(foundUrl);
			}
			if(contentList.size() > 10) {
				break;
			}
		}

		return contentList.size() == 0 ? null : contentList;
	}
	
	/**
	 * check if the url is valid and remove all parameters
	 * @param foundUrl
	 * @return
	 */
	private static String cleanFoundUrl(String foundUrl) {
		if(foundUrl.indexOf('?') != -1) {
			foundUrl = foundUrl.substring(0, foundUrl.indexOf('?'));
		}else if(foundUrl.indexOf('#') != -1) {
			foundUrl = foundUrl.substring(0, foundUrl.indexOf('#'));
		}
		if(foundUrl.charAt(foundUrl.length() - 1) == '/') {
			foundUrl = foundUrl.substring(0, foundUrl.length() - 1);
		}
		return foundUrl;

	}
	
	/**
	 * format the url
	 * @param foundUrl
	 */
	private static String formatFoundUrl(String foundUrl, String url) {
		if(foundUrl.startsWith("https://")) {
			return foundUrl;
		}else {
			String guessUrl_2 = url + foundUrl;
			if(isLinkValid(guessUrl_2)) {
				return guessUrl_2;
			}
		}
		System.out.println(foundUrl);
		return foundUrl;
	}
	
}