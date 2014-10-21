package eu.fox7.rexp.isc.experiment2;

import eu.fox7.rexp.isc.analysis.corejobs.Director;
import eu.fox7.rexp.isc.experiment2.extra.SystemX;
import eu.fox7.rexp.util.FileX;
import eu.fox7.rexp.util.Log;
import eu.fox7.rexp.xml.url.UriX;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class IscOutput {
	public static final String DEFAULT_OUTPUT_DIRECTORY = "./analysis/isb_out";
	public static final String DEFAULT_TEX_TEMPLATE_RESOURCE = "tex/isc-bench.tex";
	public static final String DEFAULT_TEX_DATA_SUBDIR = "data";
	public static final String PDF_FILE_BASENAME = "isc-bench.pdf";
	private static final String CMD_LATEXMK_PDF = "latexmk -pdf";
	private static final String CMD_LATEXMK_CLEAN = "latexmk -c -pdf";

	private boolean simulation = false;
	private String outputDir = DEFAULT_OUTPUT_DIRECTORY;
	private String texTemplateResource = DEFAULT_TEX_TEMPLATE_RESOURCE;
	private String texDataSubDir = DEFAULT_TEX_DATA_SUBDIR;
	private String dataOutputSemiPattern = IscResults.ISC_RESULTS_OUTPUT;

	public static void main(String[] args) throws IOException {
		Director.setup();
		IscOutput iop = new IscOutput();
		iop.process();
	}

	public IscOutput() {
	}

	public String process() {
		try {
			File dataOutDir = FileX.newFile(outputDir, texDataSubDir);
			copyIscResults(dataOutputSemiPattern, dataOutDir);
			SystemX.copyResourceToDir(texTemplateResource, outputDir);

			if (!simulation) {
				SystemX.system(CMD_LATEXMK_CLEAN, outputDir);
				SystemX.system(CMD_LATEXMK_PDF, outputDir);
			}

			return FileX.newFile(outputDir, PDF_FILE_BASENAME).getAbsolutePath();
		} catch (IOException ex) {
			Log.e("%s", ex);
			return null;
		}
	}

	static List<File> copyIscResults(String dataOutputSemiPattern, File copyTgtDir) throws IOException {
		List<File> dataFiles = getDataFiles(dataOutputSemiPattern);
		for (File file : dataFiles) {
			SystemX.copyFileToDir(file, copyTgtDir);
		}
		return dataFiles;
	}

	static List<File> getDataFiles(String patternString) {
		List<File> result = new LinkedList<File>();
		UriX u = new UriX(patternString);
		String dir = u.subPath();
		String baseNamePattern = u.fullName().replaceAll("%s", "[^/,\\\\\\\\]*");
		for (File f : FileX.newFile(dir).listFiles()) {
			if (f.isFile() && f.getName().matches(baseNamePattern)) {
				result.add(f);
			}
		}
		return result;
	}
}
