package eu.fox7.rexp.xml.util;

import eu.fox7.rexp.util.Log;
import eu.fox7.rexp.util.UtilX;
import nu.xom.*;
import org.apache.xerces.parsers.SAXParser;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
public class XmlUtils {
	public static final String ROOT_ELEMENT_NAME = "root";
	public static final String ITEM_ELEMENT_NAME = "item";
	private static final String NO_DTD_LOAD = "http://apache.org/xml/features/nonvalidating/load-external-dtd";

	private static Builder xomParser;

	public static SAXParserFactory newSAXParserFactory() {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			factory.setFeature(NO_DTD_LOAD, false);
		} catch (ParserConfigurationException ex) {
			Log.w(ex.toString());
			Log.w("Could not initialize specialized XML parser");
		} catch (SAXNotRecognizedException ex) {
			Log.w(ex.toString());
			Log.w("Could not initialize specialized XML parser");
		} catch (SAXNotSupportedException ex) {
			Log.w(ex.toString());
			Log.w("Could not initialize specialized XML parser");
		}
		return factory;
	}

	private static XMLReader newXmlReader() {
		XMLReader xmlReader = new SAXParser();
		try {
			xmlReader.setFeature(NO_DTD_LOAD, false);
		} catch (SAXNotRecognizedException ex) {
			Log.w(ex.toString());
			Log.w("Could not initialize specialized XML parser");
		} catch (SAXNotSupportedException ex) {
			Log.w(ex.toString());
			Log.w("Could not initialize specialized XML parser");
		}
		return xmlReader;
	}

	private static Builder getDocumentBuilder() {
		synchronized (XmlUtils.class) {
			if (xomParser == null) {
				XMLReader xmlReader = newXmlReader();
				xomParser = new Builder(xmlReader);
			}
			return xomParser;
		}
	}
	

	public static Element readXml(InputStream inStream) {
		try {
			Builder parser = getDocumentBuilder();
			Document doc = parser.build(inStream);
			return doc.getRootElement();
		} catch (ParsingException ex) {
			Log.e(ex.toString());
		} catch (IOException ex) {
			Log.e(ex.toString());
		}
		return null;
	}

	public static Element readXml(File file) {
		Element element = null;
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			element = readXml(fis);
			fis.close();
		} catch (IOException ex) {
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
		return element;
	}

	private static Builder htmlParser;

	private static Builder getHtmlDocumentBuilder() {
		synchronized (XmlUtils.class) {
			if (htmlParser == null) {
				try {
					XMLReader tagsoup = XMLReaderFactory.createXMLReader("org.ccil.cowan.tagsoup.Parser");
					htmlParser = new Builder(tagsoup);
				} catch (SAXException ex) {
					Log.w(ex.getMessage());
					Log.w("Could not load a tag cleaning HTML parser");
					htmlParser = new Builder();
				}
			}
			return htmlParser;
		}
	}

	public static Element readHtml(InputStream inStream) {
		try {
			Builder parser = getHtmlDocumentBuilder();
			Document doc = parser.build(inStream);
			return doc.getRootElement();
		} catch (ParsingException ex) {
			Log.e(ex.toString());
		} catch (IOException ex) {
			Log.e(ex.toString());
		}
		return null;
	}

	public static Element readHtml(File file) {
		Element element = null;
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			element = readHtml(fis);
			fis.close();
		} catch (IOException ex) {
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
		return element;
	}
	

	public static boolean serializeXml(Element element, OutputStream outStream) {
		if (element == null) {
			Log.w("Could not serialize document");
			return false;
		}
		element = new Element(element);
		Document doc = new Document(element);
		try {
			Serializer serializer = new Serializer(outStream, "UTF-8");
			serializer.setIndent(4);
			serializer.setUnicodeNormalizationFormC(true);
			serializer.write(doc);
		} catch (IOException ex) {
			Log.e("Could not serialize XML");
			Log.e("%s", ex);
			return false;
		}
		return true;
	}

	public static boolean serializeXml(Element element, File file) {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			boolean b = serializeXml(element, fos);
			return b;
		} catch (IOException ex) {
			Log.e("%s", ex);
		} finally {
			try {
				if (fos != null) {
					fos.close();
				}
			} catch (IOException ex) {
				Log.e("%s", ex);
			}
		}
		return false;
	}

	public static boolean normalizeXml(InputStream inStream, OutputStream outStream) {
		return serializeXml(readXml(inStream), outStream);
	}

	public static String elementToXmlString(Element e) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		XmlUtils.serializeXml(e, baos);
		String s = new String(baos.toByteArray());
		UtilX.silentClose(baos);
		return s;
	}
}
