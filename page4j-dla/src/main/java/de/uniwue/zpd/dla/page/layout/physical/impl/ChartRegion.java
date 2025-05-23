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
package de.uniwue.zpd.dla.page.layout.physical.impl;

import de.uniwue.zpd.dla.page.io.xml.DefaultXmlNames;
import de.uniwue.zpd.dla.page.layout.physical.ContentFactory;
import de.uniwue.zpd.dla.page.layout.physical.RegionContainer;
import de.uniwue.zpd.dla.page.layout.physical.shared.RegionType;
import de.uniwue.zpd.ident.Id;
import de.uniwue.zpd.ident.IdRegister;
import de.uniwue.zpd.maths.geometry.Polygon;
import de.uniwue.zpd.shared.variable.BooleanValue;
import de.uniwue.zpd.shared.variable.DoubleValue;
import de.uniwue.zpd.shared.variable.IntegerValue;
import de.uniwue.zpd.shared.variable.StringValue;
import de.uniwue.zpd.shared.variable.VariableMap;
import de.uniwue.zpd.shared.variable.VariableValue;
import de.uniwue.zpd.shared.variable.Variable.WrongVariableTypeException;

/**
 * Specialised implementation for chart regions.
 * Provides getters and setters for default attributes.
 *
 * @author Christian Clausner
 */
public class ChartRegion extends RegionImpl {

	public ChartRegion(IdRegister idRegister, ContentFactory contentFactory, RegionType type, Id id,
			Polygon coords, VariableMap attributes, RegionContainer parentRegion) {
		super(idRegister, contentFactory, type, id, coords, attributes, parentRegion);
	}

	public String getChartType() {
		return ((StringValue)getAttributes().get(DefaultXmlNames.ATTR_type).getValue()).val;
	}

	public void setChartType(String type) {
		try {
			getAttributes().get(DefaultXmlNames.ATTR_type).setValue(VariableValue.createValueObject(type));
		} catch (WrongVariableTypeException e) {
			e.printStackTrace();
		}
	}

	public int getNumColours() {
		return ((IntegerValue)getAttributes().get(DefaultXmlNames.ATTR_numColours).getValue()).val;
	}

	public void setNumColours(int num) {
		try {
			getAttributes().get(DefaultXmlNames.ATTR_numColours).setValue(VariableValue.createValueObject(num));
		} catch (WrongVariableTypeException e) {
			e.printStackTrace();
		}
	}

	public String getBgColour() {
		return ((StringValue)getAttributes().get(DefaultXmlNames.ATTR_bgColour).getValue()).val;
	}

	public void setBgColour(String colour) {
		try {
			getAttributes().get(DefaultXmlNames.ATTR_bgColour).setValue(VariableValue.createValueObject(colour));
		} catch (WrongVariableTypeException e) {
			e.printStackTrace();
		}
	}

	public double getOrientation() {
		return ((DoubleValue)getAttributes().get(DefaultXmlNames.ATTR_orientation).getValue()).val;
	}

	public void setOrientation(double orientation) {
		try {
			getAttributes().get(DefaultXmlNames.ATTR_orientation).setValue(VariableValue.createValueObject(orientation));
		} catch (WrongVariableTypeException e) {
			e.printStackTrace();
		}
	}

	public boolean hasEmbeddedText() {
		return ((BooleanValue)getAttributes().get(DefaultXmlNames.ATTR_embText).getValue()).val;
	}

	public void setEmbeddedText(boolean hasText) {
		try {
			getAttributes().get(DefaultXmlNames.ATTR_embText).setValue(VariableValue.createValueObject(hasText));
		} catch (WrongVariableTypeException e) {
			e.printStackTrace();
		}
	}

}
