package de.uniwue.zpd.dla.page.io.xml;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import de.uniwue.zpd.dla.page.Page;
import de.uniwue.zpd.dla.page.io.FileInput;
import de.uniwue.zpd.dla.page.io.FileTarget;
import de.uniwue.zpd.dla.page.io.PageReader;
import de.uniwue.zpd.dla.page.io.PageWriter;
import de.uniwue.zpd.io.UnsupportedFormatVersionException;
import de.uniwue.zpd.io.xml.XmlFormatVersion;
import de.uniwue.zpd.io.xml.XmlModelAndValidatorProvider;
import de.uniwue.zpd.io.xml.XmlValidator;

public class XmlPageWriter_AltoTest {

	private static File xmlAllFeaturesFile2019 = null;
	private static Page pageAllFeatures2019 = null;
	private static File outputFile = null;

	@Before
	public void setUp() throws Exception {

		outputFile = new File("c:/junit/altoOutput.xml");
		if (outputFile.exists()) {
			if (!outputFile.delete())
				throw new Exception("Old output file could not be deleted: "+ outputFile.getPath());
		}

		xmlAllFeaturesFile2019 = new File("c:/junit/allFeatures2019.xml");
		if (!xmlAllFeaturesFile2019.exists())
			throw new Exception("Page XML file not found: "+ xmlAllFeaturesFile2019.getPath());

		PageReader reader = PageXmlInputOutput.getReader();
		pageAllFeatures2019 = reader.read(new FileInput(xmlAllFeaturesFile2019));
		if (pageAllFeatures2019 == null)
			throw new Exception("Page XML could not be opened: "+ xmlAllFeaturesFile2019.getPath());
	}

	@Test
	public void test() {
		try {
			XmlModelAndValidatorProvider validatorProvider = new PageXmlModelAndValidatorProvider();
			XmlValidator validator = null;
			if (validatorProvider != null) {
				validator = validatorProvider.getValidator(new XmlFormatVersion("http://www.loc.gov/standards/alto/ns-v4#"));
			}

			//Valid
			PageWriter writer = new XmlPageWriter_Alto(validator);

			try {
				writer.write(pageAllFeatures2019, new FileTarget(outputFile));
			} catch (UnsupportedFormatVersionException e) {
				e.printStackTrace();
			}
			assertTrue(outputFile.exists());


		} catch(Exception exc) {
			exc.printStackTrace();
		}
	}

}
