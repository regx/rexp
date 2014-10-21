package eu.fox7.rexp.isc.analysis2.mvn;

import eu.fox7.rexp.isc.analysis2.mvn.MvnRepo.Doc;
import eu.fox7.rexp.util.Log;
import eu.fox7.rexp.xml.url.UriX;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MvnJarScanner {
	public static interface ScanAction {
		void onScan(String jarPath, String innerPath, InputStream inputStream);
	}

	private ScanAction action;
	private boolean debug = false;

	public MvnJarScanner() {
	}

	public MvnJarScanner(ScanAction action) {
		this.action = action;
	}

	public void setCrawlAction(ScanAction action) {
		this.action = action;
	}
	public void scan(Doc doc) {
		try {
			final String jarPath = doc.path;
			InputStream inputStream = doc.open();

			scanZip(inputStream, new ZipEvent() {
				@Override
				public void onZipFile(String path, InputStream inputStream) {
					if (debug) {
						onMatchDebugOutput(jarPath, path, inputStream);
					}
					fireAction(jarPath, path, inputStream);
				}
			});
		} catch (IOException ex) {
			Log.w("Could not scan %s", doc);
		}
	}

	private void fireAction(String jarPath, String path, InputStream inputStream) {
		if (action != null) {
			action.onScan(jarPath, path, inputStream);
		}
	}

	private void onMatchDebugOutput(String jarPath, String innerPath, InputStream inputStream) {
		UriX uri = new UriX(UriX.concatBaseWithSub(jarPath, innerPath));
		String dirPath = uri.subPath();
		String fileName = uri.fullName();
		Log.d("%s, %s, %s, %s", jarPath, innerPath, dirPath, fileName);
	}
	

	public static interface ZipEvent {
		void onZipFile(String path, InputStream inputStream);
	}

	public static void scanZip(InputStream inputStream, ZipEvent callback) throws IOException {
		ZipInputStream zis = new ZipInputStream(inputStream);

		ZipEntry ze = zis.getNextEntry();
		while (ze != null) {
			if (!ze.isDirectory()) {
				String path = ze.getName();
				callback.onZipFile(path, zis);
			}
			ze = zis.getNextEntry();
		}
		zis.close();
	}
}
