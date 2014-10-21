package eu.fox7.rexp.isc.analysis.extrajobs;

import eu.fox7.rexp.isc.analysis.basejobs.BatchJob;
import eu.fox7.rexp.isc.analysis.basejobs.XmlJob;
import eu.fox7.rexp.isc.analysis.corejobs.BatchDownloader;
import eu.fox7.rexp.isc.analysis.corejobs.Director;
import eu.fox7.rexp.isc.experiment2.extra.SystemX;
import eu.fox7.rexp.util.FileX;
import eu.fox7.rexp.util.Log;

import java.io.File;
import java.io.IOException;

public class DataMerger {
	public static void main(String[] args) {
		Director.setup();
		DataMerger d = new DataMerger();
		d.process();
	}

	private String chrootPath = "../.rexp2";
	private String targetPath = "../.rexp3";

	private File chroot;
	private File target;

	public DataMerger() {
	}

	public void setChroot(String chroot) {
		this.chrootPath = chroot;
	}

	public void setTarget(String target) {
		this.targetPath = target;
	}

	private void init() {
		chroot = FileX.newFile(chrootPath);
		target = FileX.newFile(targetPath);
	}

	public void process() {
		init();
		copyRawXsds();
		mergeMetaData();
	}

	protected void copyRawXsds() {
		for (File root : chroot.listFiles()) {
			copyRawXsds(root);
		}
	}

	protected void copyRawXsds(File root) {
		try {
			File xsdSrcDir = FileX.newFile(root, "xsd/raw");
			File xsdTgtDir = FileX.newFile(target, "xsd/raw");
			SystemX.copyDirToDir(xsdSrcDir, xsdTgtDir);
		} catch (IOException ex) {
			Log.w("%s", ex);
		}
	}

	private void mergeMetaData() {
		String metaFileName = "meta/download.xml";
		BatchDownloader bd = new BatchDownloader();
		mergeXmlJob(bd, metaFileName);
	}

	private <T extends BatchJob.Item<?>> void mergeXmlJob(XmlJob<T> job, String metaFileName) {
		File meta;
		for (File root : chroot.listFiles()) {
			meta = FileX.newFile(root, metaFileName);
			job.setMetaFileName(meta.getAbsolutePath());
			job.load();
		}
		meta = FileX.newFile(target, metaFileName);
		job.setMetaFileName(meta.getAbsolutePath());
		job.save();
	}
}
