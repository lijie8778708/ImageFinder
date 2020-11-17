package com.eulerity.hackathon.webcrawler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * 
 * @author jieli
 *
 */
public class WebCrawler{
	
	private List<String> allUrls;
	private List<String> allImages;
	private boolean isFinish;
	
	public WebCrawler() {
	}
	
	public void findUrlsFromSrc(String src) throws Exception{
		if(src == null)
			return;
		this.allUrls = Collections.synchronizedList(ContentUtil.splitAllUrlsFromSrc(src));
		this.allImages = Collections.synchronizedList(new ArrayList<String>());
		this.isFinish = false;
	}
	
	public void printAllUrls() {
		for(String s : this.allUrls) {
			System.out.println(s);
		}
	}
	
	public String[] getImgUrls() {
		String[] imgUrls = new String[this.allImages.size()];
		int index = 0;
		for(String s : this.allImages) {
			imgUrls[index++] = s;
		}
		return imgUrls;
	}
	
	public boolean isEmpty() {
		return this.allImages.isEmpty();
	}
	
	public boolean isFinish() {
		return this.isFinish;
	}
	
	public void begin() throws InterruptedException{
		int threads = this.allUrls.size();
		
		CountDownLatch latch = new CountDownLatch(threads);
		for(int i = 0; i < threads; i++) {
			String src = this.allUrls.get(i);
			HandleThread thread = new HandleThread(src, latch, i);
			thread.start();
		}
	
		latch.await();
		this.isFinish = true;
	}
	
	class HandleThread extends Thread {
		private String src;
		private CountDownLatch latch;
		private int threadNumber;
		
		public HandleThread(String src, CountDownLatch latch, int threadNumber) {
			this.src = src;
			this.latch = latch;
			this.threadNumber = threadNumber;
		}
		public void run() {
			
			// 
			String[] foundImgs = ImageUtil.splitAllImageFromUrl(this.src);
			if(foundImgs == null) {
				return;
			}else {
				for(String img : foundImgs) {
					// A lot web site contains same images for multiply size, make sure only save it once
					if(img.indexOf('?') != -1) {
						img = img.substring(0, img.indexOf('?'));
					}
					if(allImages.contains(img))
						continue;
					allImages.add(img);
					System.out.println("Thread " + this.threadNumber + " found image " + img);
				}
			}
			latch.countDown();
		}
	}
}