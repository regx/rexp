package eu.fox7.rexp.isc.analysis2.mvn;

import com.google.gson.Gson;
import eu.fox7.rexp.isc.analysis.util.Downloader;
import eu.fox7.rexp.util.Log;
import eu.fox7.rexp.util.StreamX;
import eu.fox7.rexp.xml.util.HttpArgs;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public class MvnRepo {

	public static void main(String[] args) {
		MvnRepo c = getRoot();
		System.out.println(c.response.docs.size());
	}
	

	public static class ResponseHeader {
		public int status;
		public int QTime;
		public Params params;
	}

	public static class Params {
		public String fl;
		public String sort;
		public String indent;
		public String q;
		public String core;
		public String wt;
		public int rows;
		public String version;
	}

	public static class Response {
		public int numFound;
		public int start;
		public Collection<Doc> docs;
	}

	public static class Doc {
		public int id;
		public String path;
		public String name;
		public int type;
		public String lastModified;
		public String repositoryId;
		public String fileSize;
		public int parentId;

		public boolean isFile() {
			return type == 1;
		}

		@Override
		public String toString() {
			Gson gson = new Gson();
			return gson.toJson(this);
		}

		public InputStream open() throws IOException {
			if (this.isFile()) {
				String urlStr = buildRetrievalUrlStr(this);
				InputStream inputStream = Downloader.openHttpInputStream(urlStr);
				return inputStream;
			} else {
				throw new IOException("Mvn central document is not retrievable");
			}
		}
	}
	

	public ResponseHeader responseHeader;
	public Response response;

	public static MvnRepo getRoot() {
		return get(CENTRAL_ID);
	}

	public static MvnRepo get(int id) {
		String rootUrlStr = buildRequestListByIdUrlStr(id);
		InputStream is = Downloader.openHttpInputStream(rootUrlStr);
		MvnRepo c = MvnRepo.create(is);
		return c;
	}

	public static MvnRepo create(InputStream inputStream) {
		try {
			Gson gson = new Gson();
			MvnRepo object = gson.fromJson(StreamX.wrapStream(inputStream), MvnRepo.class);
			inputStream.close();
			return object;
		} catch (IOException ex) {
			Log.e("%s", ex);
			return null;
		}
	}

	public Collection<Doc> getDocs() {
		if (response != null) {
			return response.docs;
		} else {
			return null;
		}
	}
	

	protected static int CENTRAL_ID = 47;
	protected static final String URL_STR_LIST_BASE = "http://search.maven.org/solrsearch/select?";
	protected static final String URL_STR_RETRIEVE_BASE = "http://search.maven.org/remotecontent?filepath=";

	protected static String buildRequestListByIdUrlStr(int id) {
		String query = String.format("parentId:\"%s\"", id);
		return buildRequestListUrlStr(query);
	}

	protected static String buildRequestListByIdUrlStr(String path) {
		String query = String.format("path:\"%s\"", path);
		return buildRequestListUrlStr(query);
	}

	protected static String buildRequestListUrlStr(String query) {
		HttpArgs httpArgs = new HttpArgs();
		httpArgs.put("q", query);
		httpArgs.put("rows", "100000");
		httpArgs.put("core", "filelisting");
		httpArgs.put("wt", "json");
		String urlStr = URL_STR_LIST_BASE + httpArgs.toString();
		return urlStr;
	}
	
	protected static String buildRetrievalUrlStr(Doc doc) {
		if (doc != null) {
			return URL_STR_RETRIEVE_BASE + doc.path;
		} else {
			Log.w("Cannot build retrieve url for null document");
			return null;
		}
	}
}
