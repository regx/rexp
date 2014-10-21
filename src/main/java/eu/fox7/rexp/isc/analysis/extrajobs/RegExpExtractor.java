package eu.fox7.rexp.isc.analysis.extrajobs;

import eu.fox7.rexp.isc.analysis.basejobs.FileJob;
import eu.fox7.rexp.isc.analysis.corejobs.Director;
import eu.fox7.rexp.util.FileX;
import eu.fox7.rexp.util.Log;
import eu.fox7.rexp.util.UtilX;
import eu.fox7.rexp.xml.XSchemaSerialiser;
import eu.fox7.rexp.xml.schema.XSchema;
import eu.fox7.rexp.xml.schema.Xsd2XSchema;
import eu.fox7.rexp.xml.util.XmlUtils;
import nu.xom.Element;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class RegExpExtractor extends FileJob {
	private String outputDiretoryPath;
	private boolean skipExisting;

	public RegExpExtractor() {
		outputDiretoryPath = Director.resolve(Director.PROP_REGEXP_DIR);
		skipExisting = true;
	}

	public void setSkipExisting(boolean skipExisting) {
		this.skipExisting = skipExisting;
	}

	public void setOutputDiretoryPath(String outputDiretoryPath) {
		this.outputDiretoryPath = outputDiretoryPath;
	}

	@Override
	protected void process(File file, String relativeFileName) {
		try {
			String outFileName = UtilX.replaceLast(relativeFileName, ".xsd", ".xml");
			File outFile = FileX.newFile(outputDiretoryPath, outFileName);
			if (outFile.exists() && skipExisting) {
				Log.w("%s already exists, skipping", relativeFileName);
				return;
			}

			Xsd2XSchema schemaReader = new Xsd2XSchema();
			schemaReader.process(file);
			XSchema schema = schemaReader.getResult();
			Element element = XSchemaSerialiser.schemaToXml(schema);

			FileX.prepareOutFile(outFile);
			FileOutputStream fos = new FileOutputStream(outFile);
			XmlUtils.serializeXml(element, fos);
			fos.close();
		} catch (SAXException ex) {
			Log.w("Could not parse %s to read regular expressions", relativeFileName);
		} catch (IOException ex) {
			Log.w("Could not process files for regular expressions extraction: %s", relativeFileName);
		} catch (RuntimeException ex) {
			Log.e("Could not extract from schema %s: %s", relativeFileName, ex);
		}
	}

	@Override
	protected String getJobDirectoryProperty() {
		return Director.PROP_XSD_FILTERED_DIR;
	}
}
