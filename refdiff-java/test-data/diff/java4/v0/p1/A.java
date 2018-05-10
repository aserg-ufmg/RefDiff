package p1;

public class A {
	
	public byte[] fetch(Feed feed) {
		String url = feed.getLink() != null ? feed.getLink() : feed.getUrl();
		
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