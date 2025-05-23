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
package de.uniwue.zpd.dla.page.layout.converter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uniwue.zpd.dla.page.Page;
import de.uniwue.zpd.dla.page.layout.PageLayout;
import de.uniwue.zpd.dla.page.layout.physical.AttributeContainer;
import de.uniwue.zpd.dla.page.layout.physical.Region;
import de.uniwue.zpd.dla.page.layout.physical.RegionContainer;
import de.uniwue.zpd.dla.page.layout.physical.text.LowLevelTextContainer;
import de.uniwue.zpd.dla.page.layout.physical.text.LowLevelTextObject;
import de.uniwue.zpd.io.FormatModel;
import de.uniwue.zpd.io.FormatVersion;
import de.uniwue.zpd.shared.variable.Variable;
import de.uniwue.zpd.shared.variable.VariableMap;

/**
 * Central access point for converting page objects to specific format versions.
 *
 * @author Christian Clausner
 *
 */
public class ConverterHub {

	/** Singleton instance */
	private static ConverterHub instance = null;


	/** Registered converters */
	private Map<FormatVersion,Map<FormatVersion, LayoutConverter>> layoutConverters = new HashMap<FormatVersion, Map<FormatVersion,LayoutConverter>>();

	/**
	 * Private constructor (Singleton)
	 */
	private ConverterHub() {
		//Register layout converters
		// New to old
		addConverter(new Converter_2010_03_19_to_2010_01_12());
		addConverter(new Converter_2010_01_12_to_2009_03_16());
		addConverter(new Converter_2013_07_15_to_2010_03_19());
		addConverter(new Converter_2016_07_15_to_2013_07_15());
		addConverter(new Converter_2017_07_15_to_2016_07_15());
		addConverter(new Converter_2018_07_15_to_2017_07_15());
		addConverter(new Converter_2019_07_15_to_2018_07_15());
		// Old to new
		addConverter(new Converter_2018_07_15_to_2019_07_15());
		addConverter(new Converter_2017_07_15_to_2018_07_15());
		addConverter(new Converter_2016_07_15_to_2017_07_15());
		addConverter(new Converter_2013_07_15_to_2016_07_15());
		addConverter(new Converter_2009_03_16_to_2016_07_15());


		//TODO If more schemas are added, we could dynamically create chain converters.

		//2009-03-16 to 2019-07-15
		ChainConverter chain_2009_03_16_to_2019_07_15 = new ChainConverter();
		chain_2009_03_16_to_2019_07_15.addConverter(new Converter_2009_03_16_to_2016_07_15()); //This goes from medium to old to new (not great!)
		chain_2009_03_16_to_2019_07_15.addConverter(new Converter_2016_07_15_to_2017_07_15());
		chain_2009_03_16_to_2019_07_15.addConverter(new Converter_2017_07_15_to_2018_07_15());
		chain_2009_03_16_to_2019_07_15.addConverter(new Converter_2018_07_15_to_2019_07_15());
		addConverter(chain_2009_03_16_to_2019_07_15);

		//2009-03-16 to 2018-07-15
		ChainConverter chain_2009_03_16_to_2018_07_15 = new ChainConverter();
		chain_2009_03_16_to_2018_07_15.addConverter(new Converter_2009_03_16_to_2016_07_15()); //This goes from medium to old to new (not great!)
		chain_2009_03_16_to_2018_07_15.addConverter(new Converter_2016_07_15_to_2017_07_15());
		chain_2009_03_16_to_2018_07_15.addConverter(new Converter_2017_07_15_to_2018_07_15());
		addConverter(chain_2009_03_16_to_2018_07_15);

		//2010-03-19 to 2019-07-15
		ChainConverter chain_2010_03_19_to_2019_07_15 = new ChainConverter();
		chain_2010_03_19_to_2019_07_15.addConverter(new Converter_2010_03_19_to_2010_01_12()); //This goes from medium to old to new (not great!)
		chain_2010_03_19_to_2019_07_15.addConverter(new Converter_2010_01_12_to_2009_03_16());
		chain_2010_03_19_to_2019_07_15.addConverter(new Converter_2009_03_16_to_2016_07_15());
		chain_2010_03_19_to_2019_07_15.addConverter(new Converter_2016_07_15_to_2017_07_15());
		chain_2010_03_19_to_2019_07_15.addConverter(new Converter_2017_07_15_to_2018_07_15());
		chain_2010_03_19_to_2019_07_15.addConverter(new Converter_2018_07_15_to_2019_07_15());
		addConverter(chain_2010_03_19_to_2019_07_15);

		//2010-03-19 to 2018-07-15
		ChainConverter chain_2010_03_19_to_2018_07_15 = new ChainConverter();
		chain_2010_03_19_to_2018_07_15.addConverter(new Converter_2010_03_19_to_2010_01_12()); //This goes from medium to old to new (not great!)
		chain_2010_03_19_to_2018_07_15.addConverter(new Converter_2010_01_12_to_2009_03_16());
		chain_2010_03_19_to_2018_07_15.addConverter(new Converter_2009_03_16_to_2016_07_15());
		chain_2010_03_19_to_2018_07_15.addConverter(new Converter_2016_07_15_to_2017_07_15());
		chain_2010_03_19_to_2018_07_15.addConverter(new Converter_2017_07_15_to_2018_07_15());
		addConverter(chain_2010_03_19_to_2018_07_15);

		//2010-03-19 to 2017-07-15
		ChainConverter chain_2010_03_19_to_2017_07_15 = new ChainConverter();
		chain_2010_03_19_to_2017_07_15.addConverter(new Converter_2010_03_19_to_2010_01_12()); //This goes from medium to old to new (not great!)
		chain_2010_03_19_to_2017_07_15.addConverter(new Converter_2010_01_12_to_2009_03_16());
		chain_2010_03_19_to_2017_07_15.addConverter(new Converter_2009_03_16_to_2016_07_15());
		chain_2010_03_19_to_2017_07_15.addConverter(new Converter_2016_07_15_to_2017_07_15());
		addConverter(chain_2010_03_19_to_2017_07_15);

		//2010-03-19 to 2016-07-15
		ChainConverter chain_2010_03_19_to_2016_07_15 = new ChainConverter();
		chain_2010_03_19_to_2016_07_15.addConverter(new Converter_2010_03_19_to_2010_01_12()); //This goes from medium to old to new (not great!)
		chain_2010_03_19_to_2016_07_15.addConverter(new Converter_2010_01_12_to_2009_03_16());
		chain_2010_03_19_to_2016_07_15.addConverter(new Converter_2009_03_16_to_2016_07_15());
		addConverter(chain_2010_03_19_to_2016_07_15);

		//2010-03-19 to 2009-03-16
		ChainConverter chain_2010_03_19_to_2009_03_16 = new ChainConverter();
		chain_2010_03_19_to_2009_03_16.addConverter(new Converter_2010_03_19_to_2010_01_12());
		chain_2010_03_19_to_2009_03_16.addConverter(new Converter_2010_01_12_to_2009_03_16());
		addConverter(chain_2010_03_19_to_2009_03_16);

		//2013-07-15 to 2010-01-12
		ChainConverter chain_2013_07_15_to_2010_01_12 = new ChainConverter();
		chain_2013_07_15_to_2010_01_12.addConverter(new Converter_2013_07_15_to_2010_03_19());
		chain_2013_07_15_to_2010_01_12.addConverter(new Converter_2010_03_19_to_2010_01_12());
		addConverter(chain_2013_07_15_to_2010_01_12);

		//2013-07-15 to 2009-03-16
		ChainConverter chain_2013_07_15_to_2009_03_16 = new ChainConverter();
		chain_2013_07_15_to_2009_03_16.addConverter(new Converter_2013_07_15_to_2010_03_19());
		chain_2013_07_15_to_2009_03_16.addConverter(new Converter_2010_03_19_to_2010_01_12());
		chain_2013_07_15_to_2009_03_16.addConverter(new Converter_2010_01_12_to_2009_03_16());
		addConverter(chain_2013_07_15_to_2009_03_16);

		//2013-07-15 to 2019-07-15
		ChainConverter chain_2013_07_15_to_2019_07_15 = new ChainConverter();
		chain_2013_07_15_to_2019_07_15.addConverter(new Converter_2013_07_15_to_2016_07_15());
		chain_2013_07_15_to_2019_07_15.addConverter(new Converter_2016_07_15_to_2017_07_15());
		chain_2013_07_15_to_2019_07_15.addConverter(new Converter_2017_07_15_to_2018_07_15());
		chain_2013_07_15_to_2019_07_15.addConverter(new Converter_2018_07_15_to_2019_07_15());
		addConverter(chain_2013_07_15_to_2019_07_15);

		//2013-07-15 to 2018-07-15
		ChainConverter chain_2013_07_15_to_2018_07_15 = new ChainConverter();
		chain_2013_07_15_to_2018_07_15.addConverter(new Converter_2013_07_15_to_2016_07_15());
		chain_2013_07_15_to_2018_07_15.addConverter(new Converter_2016_07_15_to_2017_07_15());
		chain_2013_07_15_to_2018_07_15.addConverter(new Converter_2017_07_15_to_2018_07_15());
		addConverter(chain_2013_07_15_to_2018_07_15);

		//2013-07-15 to 2017-07-15
		ChainConverter chain_2013_07_15_to_2017_07_15 = new ChainConverter();
		chain_2013_07_15_to_2017_07_15.addConverter(new Converter_2013_07_15_to_2016_07_15());
		chain_2013_07_15_to_2017_07_15.addConverter(new Converter_2016_07_15_to_2017_07_15());
		addConverter(chain_2013_07_15_to_2017_07_15);

		//2016-07-15 to 2019-07-15
		ChainConverter chain_2016_07_15_to_2019_07_15 = new ChainConverter();
		chain_2016_07_15_to_2019_07_15.addConverter(new Converter_2016_07_15_to_2017_07_15());
		chain_2016_07_15_to_2019_07_15.addConverter(new Converter_2017_07_15_to_2018_07_15());
		chain_2016_07_15_to_2019_07_15.addConverter(new Converter_2018_07_15_to_2019_07_15());
		addConverter(chain_2016_07_15_to_2019_07_15);

		//2016-07-15 to 2018-07-15
		ChainConverter chain_2016_07_15_to_2018_07_15 = new ChainConverter();
		chain_2016_07_15_to_2018_07_15.addConverter(new Converter_2016_07_15_to_2017_07_15());
		chain_2016_07_15_to_2018_07_15.addConverter(new Converter_2017_07_15_to_2018_07_15());
		addConverter(chain_2016_07_15_to_2018_07_15);

		//2016-07-15 to 2009-03-16
		ChainConverter chain_2016_07_15_to_2009_03_16 = new ChainConverter();
		chain_2016_07_15_to_2009_03_16.addConverter(new Converter_2016_07_15_to_2013_07_15());
		chain_2016_07_15_to_2009_03_16.addConverter(new Converter_2013_07_15_to_2010_03_19());
		chain_2016_07_15_to_2009_03_16.addConverter(new Converter_2010_03_19_to_2010_01_12());
		chain_2016_07_15_to_2009_03_16.addConverter(new Converter_2010_01_12_to_2009_03_16());
		addConverter(chain_2016_07_15_to_2009_03_16);

		//2016-07-15 to 2010-01-12
		ChainConverter chain_2016_07_15_to_2010_01_12 = new ChainConverter();
		chain_2016_07_15_to_2010_01_12.addConverter(new Converter_2016_07_15_to_2013_07_15());
		chain_2016_07_15_to_2010_01_12.addConverter(new Converter_2013_07_15_to_2010_03_19());
		chain_2016_07_15_to_2010_01_12.addConverter(new Converter_2010_03_19_to_2010_01_12());
		addConverter(chain_2016_07_15_to_2010_01_12);

		//2016-07-15 to 2010-03-19
		ChainConverter chain_2016_07_15_to_2010_03_19 = new ChainConverter();
		chain_2016_07_15_to_2010_03_19.addConverter(new Converter_2016_07_15_to_2013_07_15());
		chain_2016_07_15_to_2010_03_19.addConverter(new Converter_2013_07_15_to_2010_03_19());
		addConverter(chain_2016_07_15_to_2010_03_19);

		//2017-07-15 to 2019-07-15
		ChainConverter chain_2017_07_15_to_2019_07_15 = new ChainConverter();
		chain_2017_07_15_to_2019_07_15.addConverter(new Converter_2017_07_15_to_2018_07_15());
		chain_2017_07_15_to_2019_07_15.addConverter(new Converter_2018_07_15_to_2019_07_15());
		addConverter(chain_2017_07_15_to_2019_07_15);

		//2017-07-15 to 2009-03-16
		ChainConverter chain_2017_07_15_to_2009_03_16 = new ChainConverter();
		chain_2017_07_15_to_2009_03_16.addConverter(new Converter_2017_07_15_to_2016_07_15());
		chain_2017_07_15_to_2009_03_16.addConverter(new Converter_2016_07_15_to_2013_07_15());
		chain_2017_07_15_to_2009_03_16.addConverter(new Converter_2013_07_15_to_2010_03_19());
		chain_2017_07_15_to_2009_03_16.addConverter(new Converter_2010_03_19_to_2010_01_12());
		chain_2017_07_15_to_2009_03_16.addConverter(new Converter_2010_01_12_to_2009_03_16());
		addConverter(chain_2017_07_15_to_2009_03_16);

		//2017-07-15 to 2010-01-12
		ChainConverter chain_2017_07_15_to_2010_01_12 = new ChainConverter();
		chain_2017_07_15_to_2010_01_12.addConverter(new Converter_2017_07_15_to_2016_07_15());
		chain_2017_07_15_to_2010_01_12.addConverter(new Converter_2016_07_15_to_2013_07_15());
		chain_2017_07_15_to_2010_01_12.addConverter(new Converter_2013_07_15_to_2010_03_19());
		chain_2017_07_15_to_2010_01_12.addConverter(new Converter_2010_03_19_to_2010_01_12());
		addConverter(chain_2017_07_15_to_2010_01_12);

		//2017-07-15 to 2010-03-19
		ChainConverter chain_2017_07_15_to_2010_03_19 = new ChainConverter();
		chain_2017_07_15_to_2010_03_19.addConverter(new Converter_2017_07_15_to_2016_07_15());
		chain_2017_07_15_to_2010_03_19.addConverter(new Converter_2016_07_15_to_2013_07_15());
		chain_2017_07_15_to_2010_03_19.addConverter(new Converter_2013_07_15_to_2010_03_19());
		addConverter(chain_2017_07_15_to_2010_03_19);

		//2017-07-15 to 2013-07-15
		ChainConverter chain_2017_07_15_to_2013_07_15 = new ChainConverter();
		chain_2017_07_15_to_2013_07_15.addConverter(new Converter_2017_07_15_to_2016_07_15());
		chain_2017_07_15_to_2013_07_15.addConverter(new Converter_2016_07_15_to_2013_07_15());
		addConverter(chain_2017_07_15_to_2013_07_15);

		//2018-07-15 to 2009-03-16
		ChainConverter chain_2018_07_15_to_2009_03_16 = new ChainConverter();
		chain_2018_07_15_to_2009_03_16.addConverter(new Converter_2018_07_15_to_2017_07_15());
		chain_2018_07_15_to_2009_03_16.addConverter(new Converter_2017_07_15_to_2016_07_15());
		chain_2018_07_15_to_2009_03_16.addConverter(new Converter_2016_07_15_to_2013_07_15());
		chain_2018_07_15_to_2009_03_16.addConverter(new Converter_2013_07_15_to_2010_03_19());
		chain_2018_07_15_to_2009_03_16.addConverter(new Converter_2010_03_19_to_2010_01_12());
		chain_2018_07_15_to_2009_03_16.addConverter(new Converter_2010_01_12_to_2009_03_16());
		addConverter(chain_2018_07_15_to_2009_03_16);

		//2018-07-15 to 2010-01-12
		ChainConverter chain_2018_07_15_to_2010_01_12 = new ChainConverter();
		chain_2018_07_15_to_2010_01_12.addConverter(new Converter_2018_07_15_to_2017_07_15());
		chain_2018_07_15_to_2010_01_12.addConverter(new Converter_2017_07_15_to_2016_07_15());
		chain_2018_07_15_to_2010_01_12.addConverter(new Converter_2016_07_15_to_2013_07_15());
		chain_2018_07_15_to_2010_01_12.addConverter(new Converter_2013_07_15_to_2010_03_19());
		chain_2018_07_15_to_2010_01_12.addConverter(new Converter_2010_03_19_to_2010_01_12());
		addConverter(chain_2018_07_15_to_2010_01_12);

		//2018-07-15 to 2010-03-19
		ChainConverter chain_2018_07_15_to_2010_03_19 = new ChainConverter();
		chain_2018_07_15_to_2010_03_19.addConverter(new Converter_2018_07_15_to_2017_07_15());
		chain_2018_07_15_to_2010_03_19.addConverter(new Converter_2017_07_15_to_2016_07_15());
		chain_2018_07_15_to_2010_03_19.addConverter(new Converter_2016_07_15_to_2013_07_15());
		chain_2018_07_15_to_2010_03_19.addConverter(new Converter_2013_07_15_to_2010_03_19());
		addConverter(chain_2018_07_15_to_2010_03_19);

		//2018-07-15 to 2013-07-15
		ChainConverter chain_2018_07_15_to_2013_07_15 = new ChainConverter();
		chain_2018_07_15_to_2013_07_15.addConverter(new Converter_2018_07_15_to_2017_07_15());
		chain_2018_07_15_to_2013_07_15.addConverter(new Converter_2017_07_15_to_2016_07_15());
		chain_2018_07_15_to_2013_07_15.addConverter(new Converter_2016_07_15_to_2013_07_15());
		addConverter(chain_2018_07_15_to_2013_07_15);

		//2018-07-15 to 2016-07-15
		ChainConverter chain_2018_07_15_to_2016_07_15 = new ChainConverter();
		chain_2018_07_15_to_2016_07_15.addConverter(new Converter_2018_07_15_to_2017_07_15());
		chain_2018_07_15_to_2016_07_15.addConverter(new Converter_2017_07_15_to_2016_07_15());
		addConverter(chain_2018_07_15_to_2016_07_15);

		//2019-07-15 to 2009-03-16
		ChainConverter chain_2019_07_15_to_2009_03_16 = new ChainConverter();
		chain_2019_07_15_to_2009_03_16.addConverter(new Converter_2019_07_15_to_2018_07_15());
		chain_2019_07_15_to_2009_03_16.addConverter(new Converter_2018_07_15_to_2017_07_15());
		chain_2019_07_15_to_2009_03_16.addConverter(new Converter_2017_07_15_to_2016_07_15());
		chain_2019_07_15_to_2009_03_16.addConverter(new Converter_2016_07_15_to_2013_07_15());
		chain_2019_07_15_to_2009_03_16.addConverter(new Converter_2013_07_15_to_2010_03_19());
		chain_2019_07_15_to_2009_03_16.addConverter(new Converter_2010_03_19_to_2010_01_12());
		chain_2019_07_15_to_2009_03_16.addConverter(new Converter_2010_01_12_to_2009_03_16());
		addConverter(chain_2019_07_15_to_2009_03_16);

		//2019-07-15 to 2010-01-12
		ChainConverter chain_2019_07_15_to_2010_01_12 = new ChainConverter();
		chain_2019_07_15_to_2010_01_12.addConverter(new Converter_2019_07_15_to_2018_07_15());
		chain_2019_07_15_to_2010_01_12.addConverter(new Converter_2018_07_15_to_2017_07_15());
		chain_2019_07_15_to_2010_01_12.addConverter(new Converter_2017_07_15_to_2016_07_15());
		chain_2019_07_15_to_2010_01_12.addConverter(new Converter_2016_07_15_to_2013_07_15());
		chain_2019_07_15_to_2010_01_12.addConverter(new Converter_2013_07_15_to_2010_03_19());
		chain_2019_07_15_to_2010_01_12.addConverter(new Converter_2010_03_19_to_2010_01_12());
		addConverter(chain_2019_07_15_to_2010_01_12);

		//2019-07-15 to 2010-03-19
		ChainConverter chain_2019_07_15_to_2010_03_19 = new ChainConverter();
		chain_2019_07_15_to_2010_03_19.addConverter(new Converter_2019_07_15_to_2018_07_15());
		chain_2019_07_15_to_2010_03_19.addConverter(new Converter_2018_07_15_to_2017_07_15());
		chain_2019_07_15_to_2010_03_19.addConverter(new Converter_2017_07_15_to_2016_07_15());
		chain_2019_07_15_to_2010_03_19.addConverter(new Converter_2016_07_15_to_2013_07_15());
		chain_2019_07_15_to_2010_03_19.addConverter(new Converter_2013_07_15_to_2010_03_19());
		addConverter(chain_2019_07_15_to_2010_03_19);

		//2019-07-15 to 2013-07-15
		ChainConverter chain_2019_07_15_to_2013_07_15 = new ChainConverter();
		chain_2019_07_15_to_2013_07_15.addConverter(new Converter_2019_07_15_to_2018_07_15());
		chain_2019_07_15_to_2013_07_15.addConverter(new Converter_2018_07_15_to_2017_07_15());
		chain_2019_07_15_to_2013_07_15.addConverter(new Converter_2017_07_15_to_2016_07_15());
		chain_2019_07_15_to_2013_07_15.addConverter(new Converter_2016_07_15_to_2013_07_15());
		addConverter(chain_2019_07_15_to_2013_07_15);

		//2019-07-15 to 2016-07-15
		ChainConverter chain_2019_07_15_to_2016_07_15 = new ChainConverter();
		chain_2019_07_15_to_2016_07_15.addConverter(new Converter_2019_07_15_to_2018_07_15());
		chain_2019_07_15_to_2016_07_15.addConverter(new Converter_2018_07_15_to_2017_07_15());
		chain_2019_07_15_to_2016_07_15.addConverter(new Converter_2017_07_15_to_2016_07_15());
		addConverter(chain_2019_07_15_to_2016_07_15);

		//2019-07-15 to 2017-07-15
		ChainConverter chain_2019_07_15_to_2017_07_15 = new ChainConverter();
		chain_2019_07_15_to_2017_07_15.addConverter(new Converter_2019_07_15_to_2018_07_15());
		chain_2019_07_15_to_2017_07_15.addConverter(new Converter_2018_07_15_to_2017_07_15());
		addConverter(chain_2019_07_15_to_2017_07_15);

	}

	/**
	 * Returns singleton instance
	 */
	public static ConverterHub getInstance() {
		if (instance == null)
			instance = new ConverterHub();
		return instance;
	}

	/**
	 * Registers a converter.
	 */
	private void addConverter(LayoutConverter converter) {
		Map<FormatVersion, LayoutConverter> targets = layoutConverters.get(converter.getSourceVersion());
		if (targets == null) {
			targets = new HashMap<FormatVersion, LayoutConverter>();
			layoutConverters.put(converter.getSourceVersion(), targets);
		}
		targets.put(converter.getTargetVersion(), converter);
	}

	/**
	 * Converts the given page (layout) to the specified target format (might change attributes, attribute values and attribute constraints).
	 *
	 * @param page Page object containing the layout
	 * @param targetModel Target model for a specific format version
	 * @return A list of conversion messages or null.
	 */
	public static List<ConversionMessage> convert(Page page, FormatModel targetModel) {
		FormatVersion sourceVersion = page.getFormatVersion();
		if (sourceVersion == null || sourceVersion.equals(targetModel.getVersion()))
			return null;

		//Layout conversion
		List<ConversionMessage> messages = null;
		ConverterHub instance = getInstance();

		LayoutConverter layoutConverter = instance.findConverter(page.getFormatVersion(), targetModel.getVersion());

		if (layoutConverter != null)
			messages = layoutConverter.convert(page.getLayout());

		//Adapt existing attributes and constraints
		adaptAttributes(page, targetModel.getTypeAttributeTemplates());
		adaptAttributes(page.getLayout(), targetModel);

		page.setFormatVersion(targetModel, false);

		return messages;
	}

	private static void adaptAttributes(PageLayout layout, FormatModel model) {
		Map<String, VariableMap> templates = model.getTypeAttributeTemplates();

		for (int i=0; i<layout.getRegionCount(); i++) {
			Region reg = layout.getRegion(i);
			adaptAttributes(reg, templates);

			if (reg instanceof LowLevelTextContainer)
				adaptAttributesOfChildren((LowLevelTextContainer)reg, templates);
			if (reg instanceof RegionContainer)
				adaptAttributesOfChildren((RegionContainer)reg, templates);
		}
	}

	private static void adaptAttributesOfChildren(LowLevelTextContainer container, Map<String, VariableMap> templates) {
		for (int i=0; i<container.getTextObjectCount(); i++) {
			LowLevelTextObject obj = container.getTextObject(i);
			adaptAttributes(obj, templates);
			if (obj instanceof LowLevelTextContainer)
				adaptAttributesOfChildren((LowLevelTextContainer)obj, templates);
		}
	}

	private static void adaptAttributesOfChildren(RegionContainer container, Map<String, VariableMap> templates) {
		for (int i=0; i<container.getRegionCount(); i++) {
			Region obj = container.getRegion(i);
			adaptAttributes(obj, templates);
			if (obj instanceof RegionContainer)
				adaptAttributesOfChildren((RegionContainer)obj, templates);
		}
	}

	private static void adaptAttributes(AttributeContainer obj, Map<String, VariableMap> templates) {
		VariableMap attributes = obj.getAttributes();
		if (attributes == null || attributes.getType() == null)
			return;
		VariableMap template = templates.get(attributes.getType());
		if (template != null) {
			//Remove not supported attributes and update constraints
			for (int i=0; i<attributes.getSize(); i++) {
				Variable attr = attributes.get(i);
				Variable templateVar = template.get(attr.getName());
				if (templateVar == null) {
					attributes.remove(i);
					i--;
				} else {
					//Handle constraint
					if (templateVar.getConstraint() == null)
						attr.setConstraint(null);
					else
						attr.setConstraint(templateVar.getConstraint().clone());
				}
			}
			//Add new attributes
			for (int i=0; i<template.getSize(); i++) {
				Variable templateVar = template.get(i);
				Variable attr = attributes.get(templateVar.getName());
				if (attr == null) {
					attributes.add(templateVar.clone());
				}
			}
		}
	}

	/**
	 * Checks if the given page (layout) complies with specified target format.
	 * @param page Page object containing the layout
	 * @param targetVersion Target format version
	 * @return A list of messages or null.
	 */
	public static List<ConversionMessage> checkForCompliance(Page page, FormatVersion targetVersion) {
		FormatVersion sourceVersion = page.getFormatVersion();
		if (sourceVersion == null || sourceVersion.equals(targetVersion))
			return null;

		List<ConversionMessage> messages = null;
		ConverterHub instance = getInstance();

		LayoutConverter converter = instance.findConverter(page.getFormatVersion(), targetVersion);

		if (converter != null)
			messages = converter.checkForCompliance(page.getLayout());

		return messages;
	}

	//TODO If more schemas are added, we could dynamically create chain converters.
	/**
	 * Tries to find a converter matching the given source and target versions.
	 * @return Converter object or null
	 */
	private LayoutConverter findConverter(FormatVersion source, FormatVersion target) {
		Map<FormatVersion, LayoutConverter> targets = layoutConverters.get(source);
		if (targets == null)
			return null;
		LayoutConverter conv = targets.get(target);
		return conv;
	}
}
