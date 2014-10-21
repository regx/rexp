package eu.fox7.rexp.isc.analysis.basejobs;

import eu.fox7.rexp.isc.analysis.util.FileIterator;
import eu.fox7.rexp.util.FileX;
import eu.fox7.rexp.util.Log;
import eu.fox7.rexp.util.StreamX;
import eu.fox7.rexp.util.UtilX;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipVfsFileIterator extends FileIterator {
	private static final String PREFIX = ".rexptemp";
	public static final File TEMP_DIR = new File(System.getProperty("java.io.tmpdir"), PREFIX);

	private ZipInputStream zis;
	private ZipEntry ze;
	private String currentFileName;

	public ZipVfsFileIterator(File file) {
		super(file);
		try {
			FileInputStream fis = new FileInputStream(file);
			zis = new ZipInputStream(fis);
			advance();
		} catch (IOException ex) {
			Log.v("%s", ex);
		}
	}

	public void open() {
		if (!TEMP_DIR.isDirectory()) {
			TEMP_DIR.delete();
			TEMP_DIR.mkdir();
		}
	}

	public void close() {
		UtilX.silentClose(zis);
		FileX.rmDir(TEMP_DIR);
	}

	@Override
	public boolean hasNext() {
		if (ze != null) {
			return true;
		} else {
			close();
			return false;
		}
	}

	@Override
	public File next() {
		File file = null;
		if (ze != null) {
			currentFileName = ze.getName();
			File outFile = new File(TEMP_DIR, currentFileName);
			FileOutputStream fos;
			try {
				FileX.prepareOutFile(outFile);
				fos = new FileOutputStream(outFile);
				StreamX.transfer(zis, fos);
				fos.close();
				file = outFile;
			} catch (IOException ex) {
				Log.v("%s", ex);
			}
		}
		advance();
		return file;
	}

	private void advance() {
		try {
			ze = zis.getNextEntry();
			while (ze != null && ze.isDirectory()) {
				ze = zis.getNextEntry();
			}
		} catch (IOException ex) {
			ze = null;
			Log.v("%s", ex);
		}
	}

	@Override
	public String getPathRelativeToBase(File file) {
		return "./" + currentFileName;
	}
}
