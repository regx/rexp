package eu.fox7.rexp.isc.experiment2.extra;

import eu.fox7.rexp.isc.analysis.util.FileIterator;
import eu.fox7.rexp.isc.analysis.util.PropertiesManager;
import eu.fox7.rexp.util.FileX;
import eu.fox7.rexp.util.Log;
import eu.fox7.rexp.util.StreamX;
import eu.fox7.rexp.util.UtilX;
import eu.fox7.rexp.xml.url.UriX;

import java.io.*;

public class SystemX {
	public static File copyResourceToDir(String srcResourceName, String tgtDirStr) throws IOException {
		InputStream ins = PropertiesManager.getResourceStream(srcResourceName);
		String baseName = new UriX(srcResourceName).fullName();
		File outFile = FileX.newFile(tgtDirStr, baseName);
		FileX.prepareOutFile(outFile);
		FileOutputStream fos = new FileOutputStream(outFile);
		StreamX.transfer(ins, fos);
		UtilX.silentClose(ins);
		UtilX.silentClose(fos);
		return outFile;
	}

	public static File copyFileToDir(String srcFileStr, String tgtDirStr) throws IOException {
		File srcFile = FileX.newFile(srcFileStr);
		File tgtDir = FileX.newFile(tgtDirStr);
		return copyFileToDir(srcFile, tgtDir);
	}

	public static File copyFileToDir(File srcFile, File tgtDir) throws IOException {
		String tgtBaseFileName = srcFile.getName();
		File outFile = FileX.newFile(tgtDir, tgtBaseFileName);
		FileX.prepareOutFile(outFile);
		FileX.fileCopy(srcFile, outFile);
		return outFile;
	}

	public static void copyDirToDir(File srcDir, File tgtDir) throws IOException {
		FileIterator it = new FileIterator(srcDir);
		for (File file : UtilX.iterate(it)) {
			String relative = it.getPathRelativeToBase(file);
			File outFile = FileX.newFile(tgtDir, relative);
			Log.v("Copying: %s -> %s", file, outFile);
			FileX.prepareOutFile(outFile);
			FileX.fileCopy(file, outFile);
		}
	}

	public static void system(final String strCmd, final String workDir) throws IOException {
		Runtime rt = Runtime.getRuntime();
		Process p = rt.exec(strCmd, null, workDir != null ? FileX.newFile(workDir) : null);
		p.getOutputStream().close();
		InputStream ins = p.getInputStream();
		InputStreamReader isr = new InputStreamReader(ins);
		BufferedReader br = new BufferedReader(isr);
		String line = br.readLine();
		while (line != null) {
			System.out.println(line);
			line = br.readLine();
		}
	}

	public static void openFileInSystemViewer(String fileName) {
		try {
			if (System.getProperty("os.name").startsWith("Windows")) {
				system(String.format("cmd /c start %s", fileName), null);
			} else {
				system(String.format("open %s &", fileName), null);
			}
		} catch (IOException ex) {
			Log.e("Could not open file %s", fileName);
		}
	}
}
