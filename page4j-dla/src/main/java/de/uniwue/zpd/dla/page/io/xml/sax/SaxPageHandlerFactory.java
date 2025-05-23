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

import de.uniwue.zpd.io.xml.XmlFormatVersion;
import de.uniwue.zpd.io.xml.XmlModelAndValidatorProvider;

/**
 * Creates SAX handlers for PAGE XML.
 * 
 * @author Christian Clausner
 *
 */
public class SaxPageHandlerFactory {

	/**
	 * Creates a handler for the given format
	 * @param validatorProvider Provider for XML validators
	 * @param schemaVersion XML schema version for the format
	 * @return New handler object
	 */
	public static SaxPageHandler createHandler(XmlModelAndValidatorProvider validatorProvider, XmlFormatVersion schemaVersion) {
		
		if (schemaVersion != null) {
			
			if (schemaVersion instanceof XmlFormatVersion) {
				//Abbyy
				if (((XmlFormatVersion)schemaVersion).toString().equals("http://www.abbyy.com/FineReader_xml/FineReader10-schema-v1.xml"))
					return new SaxPageHandler_AbbyyFineReader10(validatorProvider, schemaVersion);
				//HOCR
				else if (((XmlFormatVersion)schemaVersion).toString().equals("HOCR"))
					return new SaxPageHandler_Hocr();
				//ALTO
				else if (((XmlFormatVersion)schemaVersion).toString().equals("http://www.loc.gov/standards/alto/ns-v2#")
						|| ((XmlFormatVersion)schemaVersion).toString().equals("http://www.loc.gov/standards/alto/ns-v3#")
						|| ((XmlFormatVersion)schemaVersion).toString().equals("http://www.loc.gov/standards/alto/ns-v4#")
						|| ((XmlFormatVersion)schemaVersion).toString().equals("http://schema.ccs-gmbh.com/ALTO")) //1.1
					return new SaxPageHandler_Alto_2_1(validatorProvider, schemaVersion);
			}
			
			//Old PAGE schemas
			if (schemaVersion.isOlderThan(new XmlFormatVersion("2010-03-19")))
				return new SaxPageHandlerLegacy(validatorProvider, schemaVersion);
			else if (schemaVersion.isOlderThan(new XmlFormatVersion("2013-07-15")))
				return new SaxPageHandler_2010_03_19(validatorProvider, schemaVersion);
			else if (schemaVersion.isOlderThan(new XmlFormatVersion("2016-07-15")))
				return new SaxPageHandler_2013_07_15(validatorProvider, schemaVersion);
			else if (schemaVersion.isOlderThan(new XmlFormatVersion("2017-07-15")))
				return new SaxPageHandler_2016_07_15(validatorProvider, schemaVersion);
			else if (schemaVersion.isOlderThan(new XmlFormatVersion("2018-07-15")))
				return new SaxPageHandler_2017_07_15(validatorProvider, schemaVersion);
			else if (schemaVersion.isOlderThan(new XmlFormatVersion("2019-07-15")))
				return new SaxPageHandler_2018_07_15(validatorProvider, schemaVersion);
		}
		
		//Latest schema
		return new SaxPageHandler_2019_07_15(validatorProvider, schemaVersion);
	}
}
