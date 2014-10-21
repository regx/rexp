package eu.fox7.rexp.isc.analysis.util;

import eu.fox7.rexp.isc.analysis.corejobs.Director;
import eu.fox7.rexp.util.FileX;
import eu.fox7.rexp.util.Log;
import eu.fox7.rexp.util.UtilX;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;

public class FileIterator implements Iterator<File> {
	public static void main(String[] args) {
		Director.setup();
		File b = FileX.newFile("./xsd/normalized");
		for (File f : new FileIterator(b).iterable()) {
			System.out.println(relativePath(b, f.getAbsoluteFile()));
		}
	}

	public static String relativePath(File ancestor, File descendant) {
		return descendant.getAbsolutePath().replace(ancestor.getAbsolutePath(), ".").replaceAll("\\\\", "/");
	}

	public static String rebase(String base, String path) {
		if (new File(path).isAbsolute()) {
			return path;
		} else {
			if (path.startsWith("./")) {
				path = path.replaceFirst("\\./", "");
			}
			if (!base.endsWith("/")) {
				base = base + "/";
			}
			return base + path;
		}
	}

	private File baseDir;
	private Iterator<File> files;
	private FileIterator sub;
	File current;
	public FileIterator(File file) {
		baseDir = file;
		if (file != null && file.exists() && file.isDirectory()) {
			files = Arrays.asList(file.listFiles()).iterator();
		} else {
			files = null;
			Log.v("File %s does not exist or is not a directory", file);
		}
	}

	public File getBaseDir() {
		return baseDir;
	}

	public String getPathRelativeToBase(File file) {
		return relativePath(baseDir, file);
	}

	@Override
	public boolean hasNext() {
		if (files == null) {
			return false;
		} else if (sub != null && sub.hasNext()) {
			return true;
		} else {
			sub = null;

			if (files.hasNext()) {
				if (current == null) {
					current = files.next();
				}

				if (current.isFile()) {
					return true;
				} else {
					sub = new FileIterator(current);
					if (sub.hasNext()) {
						return true;
					} else {
						current = null;
						return hasNext();
					}
				}
			} else {
				return false;
			}
		}
	}

	@Override
	public File next() {
		if (sub != null) {
			current = sub.next();
		}
		File last = current;
		current = null;
		return last;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Not supported");
	}

	public Iterable<File> iterable() {
		return UtilX.iterate(this);
	}
}
