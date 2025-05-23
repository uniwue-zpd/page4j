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
package de.uniwue.zpd.dla.page.io;

import de.uniwue.zpd.dla.page.Page;
import de.uniwue.zpd.io.UnsupportedFormatVersionException;
import de.uniwue.zpd.io.xml.XmlModelAndValidatorProvider.UnsupportedSchemaVersionException;

/**
 * Reader interface for PAGE.
 *
 * @author Christian Clausner
 *
 */
public interface PageReader {

	/**
	 * Reads a PAGE input source and returns a Page object.
	 * @param source Input source of some kind (e.g. FileInput).
	 * @return Page object
	 * @throws UnsupportedSchemaVersionException
	 */
	public Page read(InputSource source) throws UnsupportedFormatVersionException;

}
