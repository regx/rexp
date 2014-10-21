package eu.fox7.rexp.isc.analysis2.gwc;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
public class GoogleWebIterator implements Iterator<String> {
	private final String query;

	private WebClient webClient;
	private HtmlPage page;
	private Queue<String> links;
	private int index;
	private boolean terminationFlag;
	private int loopMax;

	public GoogleWebIterator(String query) {
		terminationFlag = false;
		loopMax = -1;
		this.query = query;
	}

	public void setLoopMax(int loopMax) {
		this.loopMax = loopMax;
	}

	@Override
	public boolean hasNext() {
		if (loopCountTest()) {
			return false;
		}

		if (webClient == null) {
			firstPage(query);
		} else if (links.isEmpty()) {
			nextPage();
		}

		return terminationFlag;
	}

	@Override
	public String next() {
		return links.remove();
	}

	private void firstPage(String query) {
		webClient = new WebClient(BrowserVersion.FIREFOX_3_6);
		page = GoogleWebSearch.getFirstPage(webClient, query);
		links = new LinkedList<String>();
		terminationFlag = page == null;

		GoogleWebSearch.analyzePage(page, links);
		index = GoogleWebSearch.getPageIndex(page);
	}

	private void nextPage() {
		GoogleWebSearch.analyzePage(page, links);
		page = GoogleWebSearch.safeGetNextPage(page);
		terminationFlag = page == null;

		int i = GoogleWebSearch.getPageIndex(page);
		terminationFlag = terminationFlag || (i == index);
		index = i;
	}

	private boolean loopCountTest() {
		if (loopMax >= 0) {
			if (--loopMax == 0) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Not supported.");
	}
}
