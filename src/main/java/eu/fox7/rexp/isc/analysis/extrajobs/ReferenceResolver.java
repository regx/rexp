package eu.fox7.rexp.isc.analysis.extrajobs;

import eu.fox7.rexp.isc.analysis.basejobs.Job;
import eu.fox7.rexp.isc.analysis.util.Downloader;
import eu.fox7.rexp.util.FileX;
import eu.fox7.rexp.util.Log;
import eu.fox7.rexp.xml.schemax.SchemaInfo;
import eu.fox7.rexp.xml.url.UriX;
import eu.fox7.rexp.xml.util.UriHelper;
import eu.fox7.rexp.xml.util.XmlMapUtils;
import eu.fox7.rexp.xml.util.XmlUtils;
import nu.xom.Element;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static eu.fox7.rexp.isc.analysis.corejobs.BatchDownloader.TAG_PATH;
import static eu.fox7.rexp.isc.analysis.corejobs.BatchDownloader.TAG_URL;
import static eu.fox7.rexp.isc.analysis.corejobs.Director.*;

public class ReferenceResolver extends Job {
	private static final boolean SIMULATION = false;

	public static void main(String[] args) {
		setup();
		ReferenceResolver resolver = new ReferenceResolver();
		resolver.run();
	}

	private Map<String, String> url2fileName;
	private String urlMappingFileName;
	private String targetDirectory;

	public ReferenceResolver() {
		urlMappingFileName = resolve(PROP_DOWNLOAD_FILE);
		targetDirectory = resolve(PROP_DOWNLOAD_DIR);
	}

	public void setInOutFileName(String fileName) {
		urlMappingFileName = fileName;
	}

	public void setOutputDirectoryName(String outDirectoryName) {
		targetDirectory = outDirectoryName;
	}

	@Override
	protected void work() {
		init();
		load();
		resolveSchemaReferences();
		save();
	}

	private void init() {
		url2fileName = new LinkedHashMap<String, String>();
	}

	private void load() {
		File urlMappingFile = FileX.newFile(urlMappingFileName);
		Element urlMappingRootElement = XmlUtils.readXml(urlMappingFile);
		XmlMapUtils.readMapFromXmlAttributes(urlMappingRootElement, url2fileName, TAG_URL, TAG_PATH);
	}

	private void save() {
		Element outElement = XmlMapUtils.writeMapToXmlAttributes(url2fileName, TAG_URL, TAG_PATH);
		File urlMappingFile = FileX.newFile(urlMappingFileName);
		XmlUtils.serializeXml(outElement, urlMappingFile);
	}

	public void resolveSchemaReferences() {
		Map<String, String> url2fileNameCopy = new LinkedHashMap<String, String>(url2fileName);
		for (Entry<String, String> entry : url2fileNameCopy.entrySet()) {
			String schemaUrlStr = entry.getKey();
			String schemaFileName = entry.getValue();
			String base = new UriX(schemaUrlStr).base();
			resolveSchemaReferences(schemaFileName, base);
		}
	}

	private void resolveSchemaReferences(String fileName, String base) {
		FileInputStream fis = null;
		try {
			Log.d("Resolving reference: %s, %s", base, fileName);
			String schemaFileName = fileName;
			File schemaFile = FileX.newFile(schemaFileName);
			fis = new FileInputStream(schemaFile);
			SchemaInfo schemaInfo = new SchemaInfo(fis);
			if (!schemaInfo.isValid()) {
				Log.d("Skipping schema with invalid schema info");
				return;
			}
			Set<String> refDocs = schemaInfo.getReferencedDocuments(base);

			for (String refDocUrlStr : refDocs) {
				handleReferencedDocument(refDocUrlStr);
			}
		} catch (FileNotFoundException ex) {
			Log.e(ex.toString());
		} finally {
			try {
				if (fis != null) {
					fis.close();
				}
			} catch (IOException ex) {
				Log.e(ex.toString());
			}
		}
	}

	private void handleReferencedDocument(String refDocUrlStr) {
		if (!url2fileName.containsKey(refDocUrlStr)) {
			Log.d("Found reference %s", refDocUrlStr);
			String expectedFileName = UriHelper.fileNameFromUrlStr(refDocUrlStr);
			String downloadedFileName = expectedFileName;

			File outFileCandidate = FileX.newFile(targetDirectory, expectedFileName);
			if (outFileCandidate.exists()) {
				Log.i("Skipping existing file %s for %s", outFileCandidate, refDocUrlStr);
			} else if (SIMULATION) {
				Log.d("Skipping %s for simulation", refDocUrlStr);
			} else {
				downloadedFileName = Downloader.download(refDocUrlStr, targetDirectory);
			}

			if (!expectedFileName.equals(downloadedFileName)) {
				Log.w("Expected file name %s, got %s", expectedFileName, downloadedFileName);
			}
			if (downloadedFileName != null) {
				String downloadFilePath = String.format("%s/%s", targetDirectory, downloadedFileName);
				url2fileName.put(refDocUrlStr, downloadFilePath);
				Log.i("Downloaded %s to %s", refDocUrlStr, downloadFilePath);

				String basePath = new UriX(refDocUrlStr).base();
				resolveSchemaReferences(downloadedFileName, basePath);
			} else {
				Log.w("Could not donwload %s", refDocUrlStr);
			}
		} else {
			Log.i("%s already downloaded, skipping", refDocUrlStr);
		}
	}
}
