package com.eulerity.hackathon;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.eulerity.hackathon.webcrawler.WebCrawler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@WebServlet(
    name = "ImageFinder",
    urlPatterns = {"/"}
)
public class ImageFinder extends HttpServlet{
	private static final long serialVersionUID = 1L;

	protected static final Gson GSON = new GsonBuilder().create();

	//This is just a test array
	public static final String[] testImages = {"https://images.pexels.com/photos/545063/pexels-photo-545063.jpeg?auto=compress&format=tiny"
			,"https://images.pexels.com/photos/464664/pexels-photo-464664.jpeg?auto=compress&format=tiny"
			, "https://images.pexels.com/photos/406014/pexels-photo-406014.jpeg?auto=compress&format=tiny"
			, "https://images.pexels.com/photos/1108099/pexels-photo-1108099.jpeg?auto=compress&format=tiny"
			};

	@Override
	protected final void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/json");
		String path = req.getServletPath();
		String url = req.getParameter("url");
		url = formatUrl(url);
		
		System.out.println("Got request of:" + path + " with query param:" + url);
		
		System.out.println("===============search more sub-page================");
		
		WebCrawler webCrawler = new WebCrawler();
		try {
			webCrawler.findUrlsFromSrc(url);
			
			System.out.println("==============Found sub-page===============");
			webCrawler.printAllUrls();
			
			System.out.println("==============Search Images=================");
			webCrawler.begin();
			
			// Make sure all threads finished job
			while(true) {
				if(!webCrawler.isEmpty() && webCrawler.isFinish()) {
					break;
				}
			}
			String[] retImgs = webCrawler.getImgUrls();
			
			resp.getWriter().print(GSON.toJson(retImgs));
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			resp.getWriter().print(GSON.toJson(testImages));
		}
		
	}
	
	/**
	 * We will allow user to put multiply time of url
	 * @param url
	 * @return
	 */
	private String formatUrl(String url) {
		if(url == null || url.length() == 0) {
			return url;
		}
		url = url.toLowerCase();
		if(url.charAt(url.length() - 1) == '/') {
			url = url.substring(0, url.length() - 1);
		}
		// https://www.xx.com
		if(url.startsWith("https://www") || url.startsWith("http://www")) {
			return url;
		}
		// www.xx.com or xx.com
		if(url.endsWith(".com")) {
			if(url.startsWith("www")) {
				return "https://" + url;
			}else{
				return "https://www."+url;
			}
		}
		return "https://www." + url + ".com";
	}
}
