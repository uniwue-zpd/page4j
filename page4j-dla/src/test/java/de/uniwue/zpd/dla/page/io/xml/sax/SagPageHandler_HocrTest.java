/*
 * Copyright 2019 PRImA Research Lab, University of Salford, United Kingdom
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.uniwue.zpd.dla.page.io.xml.sax;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import de.uniwue.zpd.dla.page.Page;
import de.uniwue.zpd.dla.page.io.FileInput;
import de.uniwue.zpd.dla.page.io.FileTarget;
import de.uniwue.zpd.dla.page.io.xml.PageXmlInputOutput;
import de.uniwue.zpd.dla.page.io.xml.XmlPageReader;
import de.uniwue.zpd.dla.page.io.xml.XmlPageWriter;
import de.uniwue.zpd.io.UnsupportedFormatVersionException;
import de.uniwue.zpd.io.xml.XmlModelAndValidatorProvider.UnsupportedSchemaVersionException;

public class SagPageHandler_HocrTest {

	File hocrFile;

	@Before
	public void setUp() throws Exception {
		hocrFile = new File("c:/junit/00075765_hocr.html");
		if (!hocrFile.exists())
			throw new Exception("HOCR file not found: "+ hocrFile.getPath());
	}

	@Test
	public void test() {
		XmlPageReader reader = PageXmlInputOutput.getReader();

		Page page = null;
		try {
			page = reader.read(new FileInput(hocrFile));
		} catch (UnsupportedFormatVersionException e) {
			e.printStackTrace();
		}
		assertNotNull(page);

		//Save as PAGE XML
		try {
			XmlPageWriter writer = PageXmlInputOutput.getWriterForLastestXmlFormat();
			boolean success = writer.write(page, new FileTarget(new File("c:/junit/hocrConvertedToPAGE.xml")));
			assertTrue(success);
		} catch (UnsupportedSchemaVersionException e) {
			e.printStackTrace();
		} catch (UnsupportedFormatVersionException e) {
			e.printStackTrace();
		}
	}

}
