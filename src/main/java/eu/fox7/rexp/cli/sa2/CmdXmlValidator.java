package eu.fox7.rexp.cli.sa2;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import eu.fox7.rexp.cli.Cmd;
import eu.fox7.rexp.util.FileX;
import eu.fox7.rexp.util.Log;
import eu.fox7.rexp.util.UtilX;
import eu.fox7.rexp.xml.schema.SimpleValidator;
import eu.fox7.rexp.xml.schema.XSchema;
import eu.fox7.rexp.xml.schema.Xsd2XSchema;
import eu.fox7.rexp.xml.util.XmlUtils;
import nu.xom.Element;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

@Parameters(commandDescription = "Validate XML document against a Schema")
public class CmdXmlValidator extends Cmd {
	protected static final String[] CMD_NAMES = {"xv"};

	@Parameter(
		names = {"-d", "--doc"},
		description = "Document",
		required = false
	)
	protected String documentFileName;

	@Parameter(
		names = {"-s", "--schema"},
		description = "Schema",
		required = true
	)
	protected String schemaFileName;

	@Override
	public void init() {
		documentFileName = null;
		schemaFileName = null;
	}

	@Override
	public String[] getCommandNames() {
		return CMD_NAMES;
	}

	@Override
	public void execute() {
		if (documentFileName == null) {
			XSchema schema = readSchema(schemaFileName, true);
			if (schema != null) {
				System.out.println(String.format("%s is a valid schema", schemaFileName));
			}
		} else {
			Element doc = readDocument(documentFileName);
			XSchema schema = readSchema(schemaFileName, false);
			if (doc != null && schema != null) {
				SimpleValidator validator = new SimpleValidator(schema);
				boolean isValid = validator.validate(doc);
				System.out.println(String.format("%s validity for %s: %s", documentFileName, schemaFileName, isValid));
			}
		}
	}

	private Element readDocument(String docFileName) {
		try {
			File file = FileX.newFile(docFileName);
			Element root = XmlUtils.readXml(new FileInputStream(file));
			return root;
		} catch (FileNotFoundException ex) {
			Log.w("%s", ex);
			return null;
		}
	}

	private XSchema readSchema(String schemaFileName, boolean verbose) {
		try {
			Xsd2XSchema.setVerbose(verbose);
			Xsd2XSchema schemaReader = new Xsd2XSchema();
			File file = FileX.newFile(schemaFileName);
			FileInputStream fis = new FileInputStream(file);
			schemaReader.process(fis);
			UtilX.silentClose(fis);
			XSchema schema = schemaReader.getResult();
			return schema;
		} catch (SAXException ex) {
			Log.w("%s", ex);
			return null;
		} catch (IOException ex) {
			Log.w("%s", ex);
			return null;
		} catch (RuntimeException ex) {
			Log.w("%s", ex);
			return null;
		}
	}
}
