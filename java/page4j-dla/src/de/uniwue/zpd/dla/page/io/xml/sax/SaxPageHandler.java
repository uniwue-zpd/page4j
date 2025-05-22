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

import de.uniwue.zpd.dla.page.Page;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Abstract base class for SAX handlers intended for PAGE XML.
 *
 * @author Christian Clausner
 *
 */
public abstract class SaxPageHandler extends DefaultHandler {

	/**
	 * Returns the page object that has been created from XML
	 * @return Page object
	 */
	abstract public Page getPageObject();





}
