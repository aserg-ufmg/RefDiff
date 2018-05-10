package p1;

public class A {
	
	public byte[] fetch(Feed feed) {
		byte[] icon = fetch(feed.getLink());
		if (icon == null) {
			icon = fetch(feed.getUrl());
		}
		return icon;
	}
	
	private byte[] fetch(String url) {
		if (url == null) {
			log.debug("url is null");
			return null;
		}
		
		int doubleSlash = url.indexOf("//");
		if (doubleSlash == -1) {
			doubleSlash = 0;
		} else {
			doubleSlash += 2;
		}
		int firstSlash = url.indexOf('/', doubleSlash);
		if (firstSlash != -1) {
			url = url.substring(0, firstSlash);
		}
		
		byte[] icon = getIconAtRoot(url);
		
		if (icon == null) {
			icon = getIconInPage(url);
		}
		
		return icon;
	}
	
}