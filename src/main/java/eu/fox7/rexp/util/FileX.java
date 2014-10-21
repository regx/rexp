package eu.fox7.rexp.util;

import java.io.*;

public class FileX {

	public static File newFile(String fileName) {
		return new File(new File(fileName).getAbsolutePath());
	}

	public static File newFile(String parent, String child) {
		File parentFile = new File(new File(parent).getAbsolutePath());
		return new File(parentFile, child);
	}

	public static File newFile(File parentFile, String child) {
		return new File(new File(parentFile.getAbsolutePath()), child);
	}

	public static String toAbsoluteExternal(File file) {
		return file.getAbsolutePath().replaceAll("\\\\", "/");
	}
	
	public static void fileCopy(File inFile, File outFile) {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(inFile);
			FileOutputStream fos = new FileOutputStream(outFile);
			StreamX.transfer(fis, fos);
			fis.close();
			fos.close();
		} catch (IOException ex) {
			Log.e("%s", ex);
		} finally {
			try {
				if (fis != null) {
					fis.close();
				}
			} catch (IOException ex) {
				Log.e("%s", ex);
			}
		}
	}

	public static boolean prepareOutFile(File outFile) {
		if (!outFile.exists()) {
			if (!outFile.getParentFile().exists()) {
				boolean b = outFile.getParentFile().mkdirs();
				if (!b) {
					Log.f("Failed to create dir to %s", outFile);
				}
			}
			try {
				boolean b = outFile.createNewFile();
				if (!b) {
					Log.f("Failed to create %s", outFile);
				}
				return true;
			} catch (IOException ex) {
				return false;
			}
		}
		return false;
	}

	public static boolean streamToFile(InputStream inputStream, File file) {
		boolean notCreatedYet = !file.exists();
		FileOutputStream fos = null;
		try {
			if (notCreatedYet) {
				prepareOutFile(file);
			}
			fos = new FileOutputStream(file, false);
			StreamX.transfer(inputStream, fos);
			fos.close();
			return true;
		} catch (IOException ex) {
			Log.w("%s", ex);
			if (notCreatedYet) {
				boolean b = file.delete();
				if (b) {
					Log.f("Could not delete %s", file);
				}
			}
			UtilX.silentClose(fos);
			return false;
		}
	}
	

	public static String stringFromFile(String path) {
		return stringFromFile(FileX.newFile(path));
	}
	public static String stringFromFile(File file) {
		try {
			FileInputStream fis = new FileInputStream(file);
			String str = StreamX.inputStreamToString(fis);
			fis.close();
			return str;
		} catch (IOException ex) {
			Log.e("%s", ex);
			return null;
		}
	}

	public static void stringToFile(String str, String path) {
		stringToFile(str, FileX.newFile(path));
	}
	public static void stringToFile(String str, File file) {
		try {
			FileOutputStream fos = new FileOutputStream(file);
			byte[] bytes = str.getBytes();
			ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
			streamToFile(bis, file);
			fos.close();
			bis.close();
		} catch (IOException ex) {
			Log.e("%s", ex);
		}
	}
	

	public static FileX create(String filePath) {
		return new FileX(filePath);
	}

	public static FileX create(String parentPath, String childPath) {
		return new FileX(parentPath, childPath);
	}

	private final File file;

	private FileX(String filePath) {
		this.file = newFile(filePath);
	}

	private FileX(String parentPath, String childPath) {
		this.file = newFile(parentPath, childPath);
	}

	public File toFile() {
		return file;
	}

	@Override
	public String toString() {
		return file.toString();
	}
	

	public static void rmDir(File file) {
		if (file.isDirectory()) {
			for (File c : file.listFiles()) {
				rmDir(c);
			}
		}
		if (!file.delete()) {
		}
	}
}
