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
package de.uniwue.zpd.dla.page.layout.physical.text.impl;

import java.util.List;

import de.uniwue.zpd.dla.page.io.xml.DefaultXmlNames;
import de.uniwue.zpd.dla.page.layout.physical.ContentFactory;
import de.uniwue.zpd.dla.page.layout.physical.RegionContainer;
import de.uniwue.zpd.dla.page.layout.physical.impl.RegionImpl;
import de.uniwue.zpd.dla.page.layout.physical.shared.ContentType;
import de.uniwue.zpd.dla.page.layout.physical.shared.LowLevelTextType;
import de.uniwue.zpd.dla.page.layout.physical.shared.RegionType;
import de.uniwue.zpd.dla.page.layout.physical.text.LowLevelTextContainer;
import de.uniwue.zpd.dla.page.layout.physical.text.LowLevelTextContainerImpl;
import de.uniwue.zpd.dla.page.layout.physical.text.LowLevelTextObject;
import de.uniwue.zpd.dla.page.layout.physical.text.TextContent;
import de.uniwue.zpd.dla.page.layout.physical.text.TextObject;
import de.uniwue.zpd.ident.Id;
import de.uniwue.zpd.ident.IdRegister;
import de.uniwue.zpd.ident.IdRegister.InvalidIdException;
import de.uniwue.zpd.maths.geometry.Polygon;
import de.uniwue.zpd.shared.variable.BooleanValue;
import de.uniwue.zpd.shared.variable.DoubleValue;
import de.uniwue.zpd.shared.variable.IntegerValue;
import de.uniwue.zpd.shared.variable.StringValue;
import de.uniwue.zpd.shared.variable.Variable;
import de.uniwue.zpd.shared.variable.Variable.WrongVariableTypeException;
import de.uniwue.zpd.shared.variable.VariableMap;
import de.uniwue.zpd.shared.variable.VariableValue;

/**
 * Specialised layout region for text.
 *
 * @author Christian Clausner
 *
 */
public class TextRegion extends RegionImpl implements TextObject, LowLevelTextContainer {


	private LowLevelTextContainerImpl textLines = new LowLevelTextContainerImpl();

	private TextContentVariants textContentVariants;

	private ContentFactory contentFactory;


	protected TextRegion(ContentFactory contentFactory, IdRegister idRegister, Id id, Polygon coords,
						VariableMap attributes, //VariableMap textStyle,
						RegionContainer parentRegion) {
		super(idRegister, contentFactory, RegionType.TextRegion, id, coords, attributes, parentRegion);
		this.contentFactory = contentFactory;
		textContentVariants = new TextContentVariants(contentFactory.getAttributeFactory());
	}

	@Override
	public ContentType getType() {
		return RegionType.TextRegion;
	}

	@Override
	public String getText() {
		return textContentVariants.getText();
	}

	@Override
	public String getPlainText() {
		return textContentVariants.getPlainText();
	}

	@Override
	public void setText(String text) {
		textContentVariants.setText(text);
	}

	@Override
	public void setPlainText(String text) {
		textContentVariants.setPlainText(text);
	}

	@Override
	public boolean hasTextObjects() {
		return textLines.hasTextObjects();
	}

	@Override
	public int getTextObjectCount() {
		return textLines.getTextObjectCount();
	}

	@Override
	public LowLevelTextObject getTextObject(int index) {
		return textLines.getTextObject(index);
	}

	/**
	 * Creates a child text line
	 * @return The new text line object
	 */
	public TextLine createTextLine() {
		return createTextLine(null);
	}

	/**
	 * Creates a new text line object and adds it to this text region
	 * @param id Preferred ID for the text line (not guaranteed, check the returned line for the actual ID)
	 * @return New text line
	 */
	public TextLine createTextLine(String id) {
		TextLine line = (TextLine)contentFactory.createContent(LowLevelTextType.TextLine);
		line.setParent(this);
		if (id != null) {
			try {
				line.setId(id);
			} catch (InvalidIdException e) {
				e.printStackTrace();
			}
		}
		addTextObject(line);
		return line;
	}

	@Override
	public void addTextObject(LowLevelTextObject textObj) {
		textLines.addTextObject(textObj);
	}

	@Override
	public LowLevelTextObject getTextObject(Id id) {
		return textLines.getTextObject(id);
	}

	@Override
	public void removeTextObject(int index) throws IndexOutOfBoundsException {
		textLines.removeTextObject(index);
	}

	@Override
	public void removeTextObject(Id id) {
		textLines.removeTextObject(id);
	}

	@Override
	public boolean isTemporary() {
		return this.getId().toString().equals(TEMP_ID_SUFFIX);
	}

	public String getTextType() {
		Variable v = getAttributes().get(DefaultXmlNames.ATTR_type);
		return v != null && v.getValue() != null ? ((StringValue)v.getValue()).val : null;
	}

	public void setTextType(String type) {
		try {
			getAttributes().get(DefaultXmlNames.ATTR_type).setValue(VariableValue.createValueObject(type));
		} catch (WrongVariableTypeException e) {
			e.printStackTrace();
		}
	}

	public double getOrientation() {
		Variable v = getAttributes().get(DefaultXmlNames.ATTR_orientation);
		return v != null && v.getValue() != null ? ((DoubleValue)v.getValue()).val : 0.0;
	}

	public void setOrientation(double orientation) {
		try {
			getAttributes().get(DefaultXmlNames.ATTR_orientation).setValue(VariableValue.createValueObject(orientation));
		} catch (WrongVariableTypeException e) {
			e.printStackTrace();
		}
	}

	public String getTextColour() {
		if (getAttributes().get(DefaultXmlNames.ATTR_textColour) == null || getAttributes().get(DefaultXmlNames.ATTR_textColour).getValue() == null)
			return null;
		return ((StringValue)getAttributes().get(DefaultXmlNames.ATTR_textColour).getValue()).val;
	}

	public void setTextColour(String colour) {
		try {
			getAttributes().get(DefaultXmlNames.ATTR_textColour).setValue(VariableValue.createValueObject(colour));
		} catch (WrongVariableTypeException e) {
			e.printStackTrace();
		}
	}

	public String getBgColour() {
		if (getAttributes().get(DefaultXmlNames.ATTR_bgColour) == null || getAttributes().get(DefaultXmlNames.ATTR_bgColour).getValue() == null)
			return null;
		return ((StringValue)getAttributes().get(DefaultXmlNames.ATTR_bgColour).getValue()).val;
	}

	public void setBgColour(String colour) {
		try {
			getAttributes().get(DefaultXmlNames.ATTR_bgColour).setValue(VariableValue.createValueObject(colour));
		} catch (WrongVariableTypeException e) {
			e.printStackTrace();
		}
	}

	public Boolean isReverseVideo() {
		if (getAttributes().get(DefaultXmlNames.ATTR_reverseVideo) == null || getAttributes().get(DefaultXmlNames.ATTR_reverseVideo).getValue() == null)
			return null;
		return ((BooleanValue)getAttributes().get(DefaultXmlNames.ATTR_reverseVideo).getValue()).val;
	}

	public void setReverseVideo(boolean reverseVideo) {
		try {
			getAttributes().get(DefaultXmlNames.ATTR_reverseVideo).setValue(VariableValue.createValueObject(reverseVideo));
		} catch (WrongVariableTypeException e) {
			e.printStackTrace();
		}
	}

	public Double getFontSize() {
		if (getAttributes().get(DefaultXmlNames.ATTR_fontSize) == null || getAttributes().get(DefaultXmlNames.ATTR_fontSize).getValue() == null)
			return null;
		return ((DoubleValue)getAttributes().get(DefaultXmlNames.ATTR_fontSize).getValue()).val;
	}

	public void setFontSize(double fontSize) {
		try {
			getAttributes().get(DefaultXmlNames.ATTR_fontSize).setValue(VariableValue.createValueObject(fontSize));
		} catch (WrongVariableTypeException e) {
			e.printStackTrace();
		}
	}

	public Integer getLeading() {
		if (getAttributes().get(DefaultXmlNames.ATTR_leading) == null || getAttributes().get(DefaultXmlNames.ATTR_leading).getValue() == null)
			return null;
		return ((IntegerValue)getAttributes().get(DefaultXmlNames.ATTR_leading).getValue()).val;
	}

	public void setLeading(int leading) {
		try {
			getAttributes().get(DefaultXmlNames.ATTR_leading).setValue(VariableValue.createValueObject(leading));
		} catch (WrongVariableTypeException e) {
			e.printStackTrace();
		}
	}

	public Integer getKerning() {
		if (getAttributes().get(DefaultXmlNames.ATTR_kerning) == null || getAttributes().get(DefaultXmlNames.ATTR_kerning).getValue() == null)
			return null;
		return ((IntegerValue)getAttributes().get(DefaultXmlNames.ATTR_kerning).getValue()).val;
	}

	public void setKerning(int kerning) {
		try {
			getAttributes().get(DefaultXmlNames.ATTR_kerning).setValue(VariableValue.createValueObject(kerning));
		} catch (WrongVariableTypeException e) {
			e.printStackTrace();
		}
	}

	public String getReadingDirection() {
		if (getAttributes().get(DefaultXmlNames.ATTR_readingDirection) == null || getAttributes().get(DefaultXmlNames.ATTR_readingDirection).getValue() == null)
			return null;
		return ((StringValue)getAttributes().get(DefaultXmlNames.ATTR_readingDirection).getValue()).val;
	}

	public void setReadingDirection(String direction) {
		try {
			getAttributes().get(DefaultXmlNames.ATTR_readingDirection).setValue(VariableValue.createValueObject(direction));
		} catch (WrongVariableTypeException e) {
			e.printStackTrace();
		}
	}

	public Double getReadingOrientation() {
		if (getAttributes().get(DefaultXmlNames.ATTR_readingOrientation) == null || getAttributes().get(DefaultXmlNames.ATTR_readingOrientation).getValue() == null)
			return null;
		return ((DoubleValue)getAttributes().get(DefaultXmlNames.ATTR_readingOrientation).getValue()).val;
	}

	public void setReadingOrientation(double orientation) {
		try {
			getAttributes().get(DefaultXmlNames.ATTR_readingOrientation).setValue(VariableValue.createValueObject(orientation));
		} catch (WrongVariableTypeException e) {
			e.printStackTrace();
		}
	}

	public Boolean isIndented() {
		if (getAttributes().get(DefaultXmlNames.ATTR_indented) == null || getAttributes().get(DefaultXmlNames.ATTR_indented).getValue() == null)
			return null;
		return ((BooleanValue)getAttributes().get(DefaultXmlNames.ATTR_indented).getValue()).val;
	}

	public void setIndented(boolean indented) {
		try {
			getAttributes().get(DefaultXmlNames.ATTR_indented).setValue(VariableValue.createValueObject(indented));
		} catch (WrongVariableTypeException e) {
			e.printStackTrace();
		}
	}

	public String getPrimaryLanguage() {
		return ((StringValue)getAttributes().get(DefaultXmlNames.ATTR_primaryLanguage).getValue()).val;
	}

	public void setPrimaryLanguage(String lang) {
		try {
			getAttributes().get(DefaultXmlNames.ATTR_primaryLanguage).setValue(VariableValue.createValueObject(lang));
		} catch (WrongVariableTypeException e) {
			e.printStackTrace();
		}
	}

	public String getSecondaryLanguage() {
		return ((StringValue)getAttributes().get(DefaultXmlNames.ATTR_secondaryLanguage).getValue()).val;
	}

	public void setSecondaryLanguage(String lang) {
		try {
			getAttributes().get(DefaultXmlNames.ATTR_secondaryLanguage).setValue(VariableValue.createValueObject(lang));
		} catch (WrongVariableTypeException e) {
			e.printStackTrace();
		}
	}

	public String getPrimaryScript() {
		return ((StringValue)getAttributes().get(DefaultXmlNames.ATTR_primaryScript).getValue()).val;
	}

	public void setPrimarySkript(String skript) {
		try {
			getAttributes().get(DefaultXmlNames.ATTR_primaryScript).setValue(VariableValue.createValueObject(skript));
		} catch (WrongVariableTypeException e) {
			e.printStackTrace();
		}
	}

	public String getSecondaryScript() {
		return ((StringValue)getAttributes().get(DefaultXmlNames.ATTR_secondaryScript).getValue()).val;
	}

	public void setSecondaryScript(String skript) {
		try {
			getAttributes().get(DefaultXmlNames.ATTR_secondaryScript).setValue(VariableValue.createValueObject(skript));
		} catch (WrongVariableTypeException e) {
			e.printStackTrace();
		}
	}

	@Override
	public List<LowLevelTextObject> getTextObjectsSorted() {
		return textLines.getTextObjectsSorted(false);
	}

	@Override
	public Double getConfidence() {
		return textContentVariants.getConfidence();
	}

	@Override
	public void setConfidence(Double confidence) {
		textContentVariants.setConfidence(confidence);
	}

	@Override
	public Boolean isBold() {
		if (getAttributes().get(DefaultXmlNames.ATTR_bold) == null || getAttributes().get(DefaultXmlNames.ATTR_bold).getValue() == null)
			return null;
		return ((BooleanValue)getAttributes().get(DefaultXmlNames.ATTR_bold).getValue()).val;
	}

	@Override
	public void setBold(Boolean bold) {
		try {
			getAttributes().get(DefaultXmlNames.ATTR_bold).setValue(VariableValue.createValueObject(bold));
		} catch (WrongVariableTypeException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Boolean isItalic() {
		if (getAttributes().get(DefaultXmlNames.ATTR_italic) == null || getAttributes().get(DefaultXmlNames.ATTR_italic).getValue() == null)
			return null;
		return ((BooleanValue)getAttributes().get(DefaultXmlNames.ATTR_italic).getValue()).val;
	}

	@Override
	public void setItalic(Boolean italic) {
		try {
			getAttributes().get(DefaultXmlNames.ATTR_italic).setValue(VariableValue.createValueObject(italic));
		} catch (WrongVariableTypeException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Boolean isUnderlined() {
		if (getAttributes().get(DefaultXmlNames.ATTR_underlined) == null || getAttributes().get(DefaultXmlNames.ATTR_underlined).getValue() == null)
			return null;
		return ((BooleanValue)getAttributes().get(DefaultXmlNames.ATTR_underlined).getValue()).val;
	}

	@Override
	public void setUnderlined(Boolean underlined) {
		try {
			getAttributes().get(DefaultXmlNames.ATTR_underlined).setValue(VariableValue.createValueObject(underlined));
		} catch (WrongVariableTypeException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getUnderlineStyle() {
		if (getAttributes().get(DefaultXmlNames.ATTR_underlineStyle) == null || getAttributes().get(DefaultXmlNames.ATTR_underlineStyle).getValue() == null)
			return null;
		return ((StringValue)getAttributes().get(DefaultXmlNames.ATTR_underlineStyle).getValue()).val;
	}

	@Override
	public void setUnderlineStyle(String style) {
		try {
			getAttributes().get(DefaultXmlNames.ATTR_underlineStyle).setValue(VariableValue.createValueObject(style));
		} catch (WrongVariableTypeException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Boolean isSubscript() {
		if (getAttributes().get(DefaultXmlNames.ATTR_subscript) == null || getAttributes().get(DefaultXmlNames.ATTR_subscript).getValue() == null)
			return null;
		return ((BooleanValue)getAttributes().get(DefaultXmlNames.ATTR_subscript).getValue()).val;
	}

	@Override
	public void setSubscript(Boolean subscript) {
		try {
			getAttributes().get(DefaultXmlNames.ATTR_subscript).setValue(VariableValue.createValueObject(subscript));
		} catch (WrongVariableTypeException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Boolean isSuperscript() {
		if (getAttributes().get(DefaultXmlNames.ATTR_superscript) == null || getAttributes().get(DefaultXmlNames.ATTR_superscript).getValue() == null)
			return null;
		return ((BooleanValue)getAttributes().get(DefaultXmlNames.ATTR_superscript).getValue()).val;
	}

	@Override
	public void setSuperscript(Boolean superscript) {
		try {
			getAttributes().get(DefaultXmlNames.ATTR_superscript).setValue(VariableValue.createValueObject(superscript));
		} catch (WrongVariableTypeException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Boolean isStrikethrough() {
		if (getAttributes().get(DefaultXmlNames.ATTR_strikethrough) == null || getAttributes().get(DefaultXmlNames.ATTR_strikethrough).getValue() == null)
			return null;
		return ((BooleanValue)getAttributes().get(DefaultXmlNames.ATTR_strikethrough).getValue()).val;
	}

	@Override
	public void setStrikethrough(Boolean strikethrough) {
		try {
			getAttributes().get(DefaultXmlNames.ATTR_strikethrough).setValue(VariableValue.createValueObject(strikethrough));
		} catch (WrongVariableTypeException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Boolean isSmallCaps() {
		if (getAttributes().get(DefaultXmlNames.ATTR_smallCaps) == null || getAttributes().get(DefaultXmlNames.ATTR_smallCaps).getValue() == null)
			return null;
		return ((BooleanValue)getAttributes().get(DefaultXmlNames.ATTR_smallCaps).getValue()).val;
	}

	@Override
	public void setSmallCaps(Boolean smallCaps) {
		try {
			getAttributes().get(DefaultXmlNames.ATTR_smallCaps).setValue(VariableValue.createValueObject(smallCaps));
		} catch (WrongVariableTypeException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Boolean isLetterSpaced() {
		if (getAttributes().get(DefaultXmlNames.ATTR_letterSpaced) == null || getAttributes().get(DefaultXmlNames.ATTR_letterSpaced).getValue() == null)
			return null;
		return ((BooleanValue)getAttributes().get(DefaultXmlNames.ATTR_letterSpaced).getValue()).val;
	}

	@Override
	public void setLetterSpaced(Boolean letterSpaced) {
		try {
			getAttributes().get(DefaultXmlNames.ATTR_letterSpaced).setValue(VariableValue.createValueObject(letterSpaced));
		} catch (WrongVariableTypeException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String composeText(boolean replaceTextContent, boolean recursive) {
		String composed = "";
		//Compose from text lines
		for (int i=0; i<textLines.getTextObjectCount(); i++) {
			if (recursive)
				((LowLevelTextContainer)textLines.getTextObject(i)).composeText(replaceTextContent, recursive);
			if (i > 0)
				composed += "\n";
			composed += textLines.getTextObject(i).getText();
		}
		if (replaceTextContent && !composed.isEmpty())
			setText(composed);
		return composed;
	}

	@Override
	public String getComments() {
		return textContentVariants.getComments();
	}

	@Override
	public void setComments(String comments) {
		textContentVariants.setComments(comments);
	}

	@Override
	public String getDataType() {
		return textContentVariants.getDataType();
	}

	@Override
	public void setDataType(String datatype) {
		textContentVariants.setDataType(datatype);
	}

	@Override
	public String getDataTypeDetails() {
		return textContentVariants.getDataTypeDetails();
	}

	@Override
	public void setDataTypeDetails(String details) {
		textContentVariants.setDataTypeDetails(details);
	}

	/*@Override
	public String getMergeWithNextRule() {
		return textContentVariants.getMergeWithNextRule();
	}

	@Override
	public void setMergeWithNextRule(String rule) {
		textContentVariants.setMergeWithNextRule(rule);
	}

	@Override
	public String getMergeWithNextRuleData() {
		return textContentVariants.getMergeWithNextRuleData();
	}

	@Override
	public void setMergeWithNextRuleData(String data) {
		textContentVariants.setMergeWithNextRuleData(data);
	}*/

	@Override
	public int getTextContentVariantCount() {
		return textContentVariants.getTextContentVariantCount();
	}

	@Override
	public TextContent getTextContentVariant(int index) {
		return textContentVariants.getTextContentVariant(index);
	}

	@Override
	public TextContent addTextContentVariant() {
		return textContentVariants.addTextContentVariant();
	}

	@Override
	public void reomveTextContentVariant(int index) {
		textContentVariants.reomveTextContentVariant(index);
	}
}
