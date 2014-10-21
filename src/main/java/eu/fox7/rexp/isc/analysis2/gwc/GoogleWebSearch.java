package eu.fox7.rexp.isc.analysis2.gwc;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import eu.fox7.rexp.util.Log;
import eu.fox7.rexp.util.UtilX;
import eu.fox7.rexp.xml.util.UriHelper;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class GoogleWebSearch {
	private static final String BASE_URL = "http://www.google.de";
	private static final String URL_EX = "/search?hl=de&q=";
	private static final String TEST_QUERY = "filetype:xsd maxoccurs=20..500";
	private static final String RESULT_DIV_XPATH = "//div[@class='vsc']";
	private static final String HREF = "href";
	private static final String ELEMENT_NEXT_ID = "pnnext";

	private static final int SLEEP_MILLIS = 2000;

	public static void main(String[] args) throws IOException {
		new GoogleWebSearch().crawl(TEST_QUERY);
	}

	private List<String> links;
	private boolean printPage;
	private int loopMax = -1;

	public GoogleWebSearch() {
		links = new LinkedList<String>();
		printPage = false;
	}

	public void setLoopMax(int loopMax) {
		this.loopMax = loopMax;
	}

	public void crawl(String query) throws IOException {
		WebClient webClient = new WebClient(BrowserVersion.FIREFOX_3_6);
		HtmlPage page = getFirstPage(webClient, query);

		int currentPageIndex = -1;
		while (page != null) {
			if (printPage) {
				Log.v(page.asText());
			}
			if (loopMax >= 0) {
				if (--loopMax == 0) {
					break;
				}
			}
			int pageIndex = getPageIndex(page);
			Log.d("INDEX: " + pageIndex);
			if (pageIndex > 0) {
				if (pageIndex == currentPageIndex) {
					break;
				} else {
					currentPageIndex = pageIndex;
				}
			}

			analyzePage(page, links);
			page = getNextPage(page);
		}

		webClient.closeAllWindows();
	}

	public static HtmlPage getFirstPage(WebClient webClient, String query) {
		try {
			String surl = BASE_URL + URL_EX + UriHelper.encode(query);
			Log.d("Opening %s", surl);
			HtmlPage page = webClient.getPage(surl);
			return page;
		} catch (IOException ex) {
			Log.w("%s", ex);
			return null;
		}
	}

	public static void analyzePage(HtmlPage page, Collection<String> links) {
		List<?> divs = page.getByXPath(RESULT_DIV_XPATH);
		for (Object o : divs) {
			HtmlElement e = (HtmlElement) o;
			traverseChildren(e, links);
		}
	}

	public static HtmlPage safeGetNextPage(HtmlPage page) {
		try {
			return getNextPage(page);
		} catch (IOException ex) {
			Log.w("%s", ex);
			return null;
		}
	}

	private static HtmlPage getNextPage(HtmlPage page) throws IOException {
		HtmlElement nextElement = page.getElementById(ELEMENT_NEXT_ID);
		if (nextElement != null && nextElement.hasAttribute(HREF)) {
			String nextLink = nextElement.getAttribute(HREF);
			Log.d("NEXT: " + nextLink);
			Log.v("SLEEP: " + SLEEP_MILLIS);
			UtilX.silentSleep(SLEEP_MILLIS);
			Log.v(("SLEEP done"));
			page = nextElement.click();
			return page;
		} else {
			return null;
		}
	}

	private static void traverseChildren(HtmlElement e, Collection<String> links) {
		String href = e.getAttribute(HREF);
		if (href.length() > 1) {
			if (!href.contains("webcache.google")) {
				Log.i("Found link: %s", href);
				links.add(href);
			}
		}
		for (HtmlElement c : e.getChildElements()) {
			traverseChildren(c, links);
		}
	}

	public static int getPageIndex(HtmlPage page) {
		List<?> es = page.getByXPath("//td[@class='cur']");
		if (es.size() > 0) {
			HtmlElement c = (HtmlElement) es.get(0);
			try {
				return Integer.parseInt(c.getTextContent());
			} catch (NumberFormatException ignored) {
			}
		}
		return -1;
	}
}
