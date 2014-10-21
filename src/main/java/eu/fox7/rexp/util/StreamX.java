package eu.fox7.rexp.util;

import java.io.*;
public class StreamX {
	public static Reader wrapStream(InputStream inputStream) {
		InputStreamReader isr = new InputStreamReader(inputStream);
		BufferedReader br = new BufferedReader(isr);
		return br;
	}
	public static byte[] toBytes(InputStream inStream) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			transfer(inStream, bos);
			inStream.close();
			bos.close();
		} catch (IOException ex) {
			Log.e("%s", ex);
		}
		return bos.toByteArray();
	}

	private static int BUF_SIZE = 4096;
	public static void transfer(InputStream inStream, OutputStream outStream) throws IOException {
		byte[] b = new byte[BUF_SIZE];
		int i = inStream.read(b);
		while (i > 0) {
			outStream.write(b, 0, i);
			i = inStream.read(b);
		}
	}
	public static String inputStreamToString(InputStream inStream) {
		StringBuilder sb = new StringBuilder();
		try {
			InputStreamReader isr = new InputStreamReader(inStream);
			BufferedReader br = new BufferedReader(isr);
			String line = br.readLine();
			boolean touched = false;
			while (line != null) {
				if (touched) {
					sb.append("\n");
				}
				sb.append(line);
				touched = true;
				line = br.readLine();
			}
		} catch (IOException ex) {
			Log.e(ex.toString());
		}
		return sb.toString();
	}
}
