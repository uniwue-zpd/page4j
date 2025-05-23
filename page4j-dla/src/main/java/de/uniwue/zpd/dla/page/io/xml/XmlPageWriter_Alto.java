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
package de.uniwue.zpd.dla.page.io.xml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Validator;

import de.uniwue.zpd.dla.page.Page;
import de.uniwue.zpd.dla.page.io.FileTarget;
import de.uniwue.zpd.dla.page.io.OutputTarget;
import de.uniwue.zpd.dla.page.layout.PageLayout;
import de.uniwue.zpd.dla.page.layout.converter.ConversionMessage;
import de.uniwue.zpd.dla.page.layout.logical.Group;
import de.uniwue.zpd.dla.page.layout.logical.GroupMember;
import de.uniwue.zpd.dla.page.layout.logical.RegionRef;
import de.uniwue.zpd.dla.page.layout.physical.Region;
import de.uniwue.zpd.dla.page.layout.physical.shared.RegionType;
import de.uniwue.zpd.dla.page.layout.physical.text.LowLevelTextContainer;
import de.uniwue.zpd.dla.page.layout.physical.text.TextContent;
import de.uniwue.zpd.dla.page.layout.physical.text.TextObject;
import de.uniwue.zpd.dla.page.layout.physical.text.impl.Glyph;
import de.uniwue.zpd.dla.page.layout.physical.text.impl.TextLine;
import de.uniwue.zpd.dla.page.layout.physical.text.impl.TextRegion;
import de.uniwue.zpd.dla.page.layout.physical.text.impl.Word;
import de.uniwue.zpd.dla.page.layout.shared.GeometricObject;
import de.uniwue.zpd.dla.page.metadata.MetadataItem;
import de.uniwue.zpd.ident.Id;
import de.uniwue.zpd.io.UnsupportedFormatVersionException;
import de.uniwue.zpd.io.xml.IOError;
import de.uniwue.zpd.io.xml.XmlValidator;
import de.uniwue.zpd.labels.HasLabels;
import de.uniwue.zpd.labels.Label;
import de.uniwue.zpd.labels.LabelGroup;
import de.uniwue.zpd.labels.Labels;
import de.uniwue.zpd.maths.geometry.Point;
import de.uniwue.zpd.maths.geometry.Polygon;
import de.uniwue.zpd.maths.geometry.Rect;
import de.uniwue.zpd.shared.variable.BooleanValue;
import de.uniwue.zpd.shared.variable.DoubleValue;
import de.uniwue.zpd.shared.variable.IntegerValue;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

/**
 *  Page writer implementation for ALTO XML files.
 *  Experimental.
 *
 * @author Christian Clausner
 *
 */
public class XmlPageWriter_Alto implements XmlPageWriter {

	private XmlValidator validator;
	private PageErrorHandler lastErrors;
	private Page page = null;
	private PageLayout layout = null;
	private String namespace;
	private Document doc;
	private Map<String, String> propagatedWordTexts; //Map[ID, text]
	private Map<String, String> propagatedGlyphTexts; //Map[ID, text]
	private List<TextStyle> textStyles;
	private List<ParagraphStyle> paragraphStyles;
	private List<Tag> tags;

	/**
	 * Constructor
	 *
	 * @param validator Optional schema validator (use null if not required).
	 */
	public XmlPageWriter_Alto(XmlValidator validator) {
		this.validator = validator;
	}

	@Override
	public boolean write(Page page, OutputTarget target) throws UnsupportedFormatVersionException {
		return run(page, target, false);
	}

	@Override
	public boolean validate(Page page) throws UnsupportedFormatVersionException {
		return run(page, null, true);
	}

	@Override
	public String getSchemaVersion() {
		return "http://www.loc.gov/standards/alto/ns-v4#";
	}

	@Override
	public String getSchemaLocation() {
		//return "http://www.loc.gov/standards/alto/v4";
		return "http://www.loc.gov/standards/alto/ns-v4#";
	}

	@Override
	public String getSchemaUrl() {
		return "http://www.loc.gov/standards/alto/v4/alto-4-1.xsd";
	}

	@Override
	public String getNamespace() {
		return "http://www.loc.gov/standards/alto/ns-v4#";
	}

	@Override
	public List<ConversionMessage> getConversionInformation() {
		return null;
	}

	/**
	 * Returns a list of writing errors
	 */
	public List<IOError> getErrors() {
		return lastErrors != null ? lastErrors.getErrors() : null;
	}

	/**
	 * Returns a list of writing warnings
	 */
	public List<IOError> getWarnings() {
		return lastErrors != null ? lastErrors.getWarnings() : null;
	}

	private boolean run(Page page, OutputTarget target, boolean validateOnly) throws UnsupportedFormatVersionException {
		//if (validator != null && !validator.getSchemaVersion().equals(page.getFormatVersion()))
		//	throw new UnsupportedFormatVersionException("XML page writer doesn't support format: "+page.getFormatVersion().toString());

		this.page = page;
		layout = page.getLayout();
		lastErrors = new PageErrorHandler();
		propagatedWordTexts = new HashMap<String, String>();
		propagatedGlyphTexts = new HashMap<String, String>();
		propagateText();
		textStyles = new LinkedList<TextStyle>();
		paragraphStyles = new LinkedList<ParagraphStyle>();
		tags = new LinkedList<Tag>();
		findTextStyles();
		findParagraphStyles();
		findTags();

        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        dbfac.setValidating(false);
        dbfac.setNamespaceAware(true);
        //if (validator != null)
        	//dbfac.setSchema(validator.getSchema());

        DocumentBuilder docBuilder;
		try {
			docBuilder = dbfac.newDocumentBuilder();
			//docBuilder.setErrorHandler(lastErrors);

			DOMImplementation domImpl = docBuilder.getDOMImplementation();
	        //doc = docBuilder.newDocument();
			namespace = getNamespace();
			doc = domImpl.createDocument(namespace, AltoXmlNames.ELEMENT_alto, null);

	        writeRoot();

	        //Validation errors?
	        if (validator != null) {
	        	Validator domVal = validator.getSchema().newValidator();
	        	domVal.setErrorHandler(lastErrors);

	        	try {
					domVal.validate(new DOMSource(doc));
				} catch (SAXException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
	        }
	        if (lastErrors.hasErrors()) {
	        	return false;
	        }

	        //Write XML
	        if (!validateOnly) {

	            TransformerFactory transfac = TransformerFactory.newInstance();
	            Transformer trans = transfac.newTransformer();
	            DOMSource source = new DOMSource(doc);

	            OutputStream os = null;

	            if (target instanceof FileTarget) {
					File f = ((FileTarget)target).getFile();
	            	os = new FileOutputStream(f);
	            } else if (target instanceof StreamTarget)
	            	os = ((StreamTarget) target).getOutputStream();

	            StreamResult result = new StreamResult(os);
	            trans.transform(source, result);
	            os.close();
	        }
            return true;
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	private void addAttribute(Element node, String name, String value) {
		node.setAttributeNS(null, name, value);
	}

	/**
	 * Writes a single element with text content.
	 */
	private void addTextElement(Element parent, String elementName, String text) /*throws XMLStreamException*/ {
		Element node = doc.createElementNS(getNamespace(), elementName);
		parent.appendChild(node);

		Text textNode = doc.createTextNode(text != null ? text : "");
		node.appendChild(textNode);
	}

	private void writeRoot() {

		Element root = doc.getDocumentElement();

		//Schema location
		String schemaLocation = getSchemaLocation() + " " + getSchemaUrl();
		root.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "xsi:schemaLocation", schemaLocation);

		addAttribute(root, AltoXmlNames.ATTR_SCHEMAVERSION, "4.1");

		addDescription(root);

		if (!textStyles.isEmpty() || !paragraphStyles.isEmpty()) {
			Element stylesNode = doc.createElementNS(getNamespace(), AltoXmlNames.ELEMENT_Styles);
			root.appendChild(stylesNode);
			addTextStyles(stylesNode);
			addParagraphStyles(stylesNode);
		}

		if (!tags.isEmpty()) {
			Element tagsNode = doc.createElementNS(getNamespace(), AltoXmlNames.ELEMENT_Tags);
			root.appendChild(tagsNode);
			addTags(tagsNode);
		}

		addLayout(root);
	}

	private void addDescription(Element parent) {

		Element descriptionNode = doc.createElementNS(getNamespace(), AltoXmlNames.ELEMENT_Description);
		parent.appendChild(descriptionNode);

		//Measurement unit
		addTextElement(descriptionNode, AltoXmlNames.ELEMENT_MeasurementUnit, "pixel");

		//Image
		Element imageNode = doc.createElementNS(getNamespace(), AltoXmlNames.ELEMENT_sourceImageInformation);
		descriptionNode.appendChild(imageNode);

		addTextElement(imageNode, AltoXmlNames.ELEMENT_fileName, page.getImageFilename());

		//Processing
		if (page.getMetaData().getMetadataItems() != null) {
			int id = 1;
			for (MetadataItem item :  page.getMetaData().getMetadataItems()) {
				if ("processingStep".equals(item.getType())) {

					Element processingNode = doc.createElementNS(getNamespace(), AltoXmlNames.ELEMENT_Processing);
					descriptionNode.appendChild(processingNode);

					//ID
					addAttribute(processingNode, AltoXmlNames.ATTR_ID, "pro"+id);

					//TODO: processingCategory (contentGeneration, contentModification, preOperation, postOperation, other)

					//processingDateTime
					if (item.getDate() != null)
						addTextElement(processingNode, AltoXmlNames.ELEMENT_processingDateTime, item.getDate());

					//TODO: processingAgency

					//processingStepDescription
					String descr = "" + item.getName() + " " + item.getValue();
					addTextElement(processingNode, AltoXmlNames.ELEMENT_processingStepDescription, descr);

					//TODO
					//processingStepSettings
					//processingSoftware

					id++;
				}
			}
		}
	}

	private void addTextStyles(Element parent) {
		for (TextStyle textStyle : textStyles) {
			Element styleNode = doc.createElementNS(getNamespace(), AltoXmlNames.ELEMENT_TextStyle);
			parent.appendChild(styleNode);

			if (textStyle.ID == null)
				continue;

			addAttribute(styleNode, AltoXmlNames.ATTR_ID, textStyle.ID);

			if (textStyle.fontFamily != null)
				addAttribute(styleNode, AltoXmlNames.ATTR_FONTFAMILY, textStyle.fontFamily);
			if (textStyle.fontType != null)
				addAttribute(styleNode, AltoXmlNames.ATTR_FONTTYPE, textStyle.fontType);
			if (textStyle.fontWidthType != null)
				addAttribute(styleNode, AltoXmlNames.ATTR_FONTWIDTH, textStyle.fontWidthType);
			if (textStyle.fontSize != null)
				addAttribute(styleNode, AltoXmlNames.ATTR_FONTSIZE, ""+textStyle.fontSize.doubleValue());
			if (textStyle.fontColor != null)
				addAttribute(styleNode, AltoXmlNames.ATTR_FONTCOLOR, textStyle.fontColor);
			if (textStyle.fontStyle != null)
				addAttribute(styleNode, AltoXmlNames.ATTR_FONTSTYLE, textStyle.fontStyle);
		}
	}

	private void addParagraphStyles(Element parent) {
		for (ParagraphStyle paragraphStyle : paragraphStyles) {
			Element styleNode = doc.createElementNS(getNamespace(), AltoXmlNames.ELEMENT_ParagraphStyle);
			parent.appendChild(styleNode);

			if (paragraphStyle.ID == null)
				continue;

			addAttribute(styleNode, AltoXmlNames.ATTR_ID, paragraphStyle.ID);

			if (paragraphStyle.align != null)
				addAttribute(styleNode, AltoXmlNames.ATTR_ALIGN, paragraphStyle.align);
			if (paragraphStyle.leftIndent != null)
				addAttribute(styleNode, AltoXmlNames.ATTR_LEFT, ""+paragraphStyle.leftIndent);
			if (paragraphStyle.rightIndent != null)
				addAttribute(styleNode, AltoXmlNames.ATTR_RIGHT, ""+paragraphStyle.rightIndent);
			if (paragraphStyle.lineSpace != null)
				addAttribute(styleNode, AltoXmlNames.ATTR_LINESPACE, ""+paragraphStyle.lineSpace);
			if (paragraphStyle.firstLineIndent != null)
				addAttribute(styleNode, AltoXmlNames.ATTR_FIRSTLINE, ""+paragraphStyle.firstLineIndent);
		}
	}

	private void addTags(Element parent) {
		for (Tag tag : tags) {
			Element tagNode = doc.createElementNS(getNamespace(), AltoXmlNames.ELEMENT_OtherTag);
			parent.appendChild(tagNode);

			if (tag.ID == null)
				continue;

			addAttribute(tagNode, AltoXmlNames.ATTR_ID, tag.ID);

			if (tag.type != null)
				addAttribute(tagNode, AltoXmlNames.ATTR_TYPE, tag.type);
			if (tag.label != null)
				addAttribute(tagNode, AltoXmlNames.ATTR_LABEL, tag.label);
			if (tag.description != null)
				addAttribute(tagNode, AltoXmlNames.ATTR_DESCRIPTION, tag.description);
			if (tag.uri != null)
				addAttribute(tagNode, AltoXmlNames.ATTR_URI, tag.uri);
		}
	}

	private void addLayout(Element parent) {

		Element layoutNode = doc.createElementNS(getNamespace(), AltoXmlNames.ELEMENT_Layout);
		parent.appendChild(layoutNode);

		//Page
		Element pageNode = doc.createElementNS(getNamespace(), AltoXmlNames.ELEMENT_Page);
		layoutNode.appendChild(pageNode);

		//ID
		String id = "p0";
		if (page.getGtsId() != null)
			id = page.getGtsId().toString();
		addAttribute(pageNode, AltoXmlNames.ATTR_ID, id);

		//PHYSICAL_IMG_NR
		addAttribute(pageNode, AltoXmlNames.ATTR_PHYSICAL_IMG_NR, "0");

		//PRINTED_IMG_NR - Not supported in PAGE

		//Width, height
		addAttribute(pageNode, AltoXmlNames.ATTR_WIDTH, ""+layout.getWidth());
		addAttribute(pageNode, AltoXmlNames.ATTR_HEIGHT, ""+layout.getHeight());

		//Page class
		if (page.getAttributes().get("type") != null && page.getAttributes().get("type").getValue() != null)
			addAttribute(pageNode, AltoXmlNames.ATTR_PAGECLASS, page.getAttributes().get("type").getValue().toString());

		//Confidence
		if (page.getAttributes().get("conf") != null && page.getAttributes().get("conf").getValue() != null)
			addAttribute(pageNode, AltoXmlNames.ATTR_PC, page.getAttributes().get("conf").getValue().toString());

		//ACCURACY - Not supported in PAGE

		//PROCESSINGREFS - Not supported in PAGE
		//PROCESSING

		//QUALITY - Not supported in PAGE
		//QUALITY_DETAIL

		//POSITION - Not supported in PAGE

		//TODO
		//Styles

		//TODO: TopMargin, LeftMargin, RightMargin, BottomMargin
		//Treat everything outside of print space as margin
		addMargins(pageNode);


		//Print space
		addPrintSpace(pageNode);
	}

	private void addMargins(Element parent) {

		Element topMarginNode = doc.createElementNS(getNamespace(), AltoXmlNames.ELEMENT_TopMargin);
		parent.appendChild(topMarginNode);
		addAttribute(topMarginNode, AltoXmlNames.ATTR_ID, "TopMarginTypeID"+0);

		Element leftMarginNode = doc.createElementNS(getNamespace(), AltoXmlNames.ELEMENT_LeftMargin);
		parent.appendChild(leftMarginNode);
		addAttribute(leftMarginNode, AltoXmlNames.ATTR_ID, "LeftMarginTypeID"+0);

		Element rightMarginNode = doc.createElementNS(getNamespace(), AltoXmlNames.ELEMENT_RightMargin);
		parent.appendChild(rightMarginNode);
		addAttribute(rightMarginNode, AltoXmlNames.ATTR_ID, "RightMarginTypeID"+0);

		Element bottomMarginNode = doc.createElementNS(getNamespace(), AltoXmlNames.ELEMENT_BottomMargin);
		parent.appendChild(bottomMarginNode);
		addAttribute(bottomMarginNode, AltoXmlNames.ATTR_ID, "BottomMarginTypeID"+0);

		Rect printSpaceCoords;

		if (layout.getPrintSpace() != null) {
			printSpaceCoords = layout.getPrintSpace().getCoords().getBoundingBox();
		}else {
			Polygon regionPolygons = new Polygon();
			for (int i = 0; i < layout.getRegionCount(); i++) {
				Rect regionBoundingBox = layout.getRegion(i).getCoords().getBoundingBox();
				regionPolygons.addPoint(regionBoundingBox.top, regionBoundingBox.left);
				regionPolygons.addPoint(regionBoundingBox.top, regionBoundingBox.right);
				regionPolygons.addPoint(regionBoundingBox.bottom, regionBoundingBox.left);
				regionPolygons.addPoint(regionBoundingBox.bottom, regionBoundingBox.right);
			}
			printSpaceCoords = regionPolygons.getBoundingBox();
		}

		int height = layout.getHeight()-1;
		int width = layout.getWidth()-1;

		Polygon topMarginCoords = new Polygon();
		Polygon leftMarginCoords = new Polygon();
		Polygon rightMarginCoords = new Polygon();
		Polygon bottomMarginCoords = new Polygon();

		topMarginCoords.addPoint(0, 0);
		topMarginCoords.addPoint(width, 0);
		topMarginCoords.addPoint(width, printSpaceCoords.top-1);
		topMarginCoords.addPoint(0, printSpaceCoords.top-1);

		bottomMarginCoords.addPoint(0, printSpaceCoords.bottom+1);
		bottomMarginCoords.addPoint(width, printSpaceCoords.bottom+1);
		bottomMarginCoords.addPoint(width, height);
		bottomMarginCoords.addPoint(0, height);

		leftMarginCoords.addPoint(0, topMarginCoords.getBoundingBox().bottom+1);
		leftMarginCoords.addPoint(printSpaceCoords.left-1, topMarginCoords.getBoundingBox().bottom+1);
		leftMarginCoords.addPoint(printSpaceCoords.left-1, bottomMarginCoords.getBoundingBox().top-1);
		leftMarginCoords.addPoint(0, bottomMarginCoords.getBoundingBox().top-1);

		rightMarginCoords.addPoint(printSpaceCoords.right+1, topMarginCoords.getBoundingBox().bottom+1);
		rightMarginCoords.addPoint(width, topMarginCoords.getBoundingBox().bottom+1);
		rightMarginCoords.addPoint(width, bottomMarginCoords.getBoundingBox().top-1);
		rightMarginCoords.addPoint(printSpaceCoords.right+1, bottomMarginCoords.getBoundingBox().top-1);

		addPositionAttributes(topMarginNode, topMarginCoords);
		addPositionAttributes(leftMarginNode, leftMarginCoords);
		addPositionAttributes(rightMarginNode, rightMarginCoords);
		addPositionAttributes(bottomMarginNode, bottomMarginCoords);
	}

	private void addPrintSpace(Element parent) {

		Element printSpaceNode = doc.createElementNS(getNamespace(), AltoXmlNames.ELEMENT_PrintSpace);
		parent.appendChild(printSpaceNode);

		addAttribute(printSpaceNode, AltoXmlNames.ATTR_ID, "PageSpaceTypeID"+0);

		if (layout.getPrintSpace() != null) {
			addPositionAttributes(printSpaceNode, layout.getPrintSpace().getCoords());

			addShape(printSpaceNode, layout.getPrintSpace().getCoords());
		}

		//Blocks
		for (int i=0; i<layout.getRegionCount(); i++) {
			addBlock(printSpaceNode, layout.getRegion(i));
		}
	}

	void addShape(Element parent, Polygon outline) {

		Element shapeNode = doc.createElementNS(getNamespace(), AltoXmlNames.ELEMENT_Shape);
		parent.appendChild(shapeNode);

		Element polygonNode = doc.createElementNS(getNamespace(), AltoXmlNames.ELEMENT_Polygon);
		shapeNode.appendChild(polygonNode);

		StringBuilder sb = new StringBuilder();
		for (int i=0; i<outline.getSize(); i++) {
			if (sb.length() > 0)
				sb.append(' ');
			Point p = outline.getPoint(i);
			sb.append(p.x);
			sb.append(',');
			sb.append(p.y);
		}
		addAttribute(polygonNode, AltoXmlNames.ATTR_POINTS, sb.toString());
	}

	void addBlock(Element parent, Region region) {

		Element blockNode = doc.createElementNS(getNamespace(), getAltoBlockType(region));

		if(getAltoBlockType(region).equals("ComposedBlock")) {
			//Type (use PAGE region type)
			addAttribute(blockNode, AltoXmlNames.ATTR_TYPE, region.getType().toString());
		}

		parent.appendChild(blockNode);

		//ID
		addAttribute(blockNode, AltoXmlNames.ATTR_ID, region.getId().toString());

		//HPOS, VPOS, WIDTH, HEIGHT
		addPositionAttributes(blockNode, region.getCoords());

		//ROTATION
		if (region.getAttributes().get("orientation") != null && region.getAttributes().get("orientation").getValue() != null) {
			double orientation = ((DoubleValue)region.getAttributes().get("orientation").getValue()).val;
			addAttribute(blockNode, AltoXmlNames.ATTR_ROTATION, ""+orientation);
		}

		//CS - Not available in PAGE

		//Styles
		if (region instanceof TextRegion) {
			String styleRefs = "";

			TextStyle textStyle = getTextStyle((TextRegion)region);
			if (textStyle != null)
				styleRefs += textStyle.ID;

			ParagraphStyle paragraphStyle = getParagraphStyle((TextRegion)region);
			if (paragraphStyle != null) {
				if (!styleRefs.isEmpty())
					styleRefs += " ";
				styleRefs += paragraphStyle.ID;
			}

			if (!styleRefs.isEmpty())
				addAttribute(blockNode, AltoXmlNames.ATTR_STYLEREFS, styleRefs);
		}

		//IDNEXT
		if (layout.getReadingOrder() != null) {
			//Look if region in ordered group and then use element after this region (if there is one)
			Group group = findReadingOrderGroup(layout.getReadingOrder().getRoot(), region.getId());
			if (group != null && group.isOrdered()) {
				for (int i=0; i<group.getSize(); i++) {
					GroupMember member = group.getMember(i);
					if (member instanceof RegionRef) {
						if (((RegionRef)member).getRegionId().equals(region.getId())) {
							int j = i + 1;
							if (j < group.getSize()) {
								GroupMember nextMember = group.getMember(j);
								if (nextMember instanceof RegionRef) {
									addAttribute(blockNode, AltoXmlNames.ATTR_IDNEXT, ((RegionRef)nextMember).getRegionId().toString());
								}
							}
						}
					}
				}
			}
		}

		//TAGREFS
		addTagRefs(blockNode, region);

		//TextRegion Type TAGREFS
		if (region instanceof TextRegion) {
			addTypeTagRefs(blockNode, region);
		}

		//PROCESSINGREFS
		// Not supported in PAGE

		addShape(blockNode, region.getCoords());

		if (region.getRegionCount() > 0) {
			//Child regions
			for (int i=0; i<region.getRegionCount(); i++)
				addBlock(blockNode, region.getRegion(i));
		} else {
			//Specialised content
			if (RegionType.TextRegion.equals(region.getType()))
				addTextBlockContent(blockNode, (TextRegion)region);
			else if (RegionType.SeparatorRegion.equals(region.getType()))
				addGraphicalBlockContent(blockNode, region);
			else //Illustration
				addIllustrationBlockContent(blockNode, region);
		}
	}

	private void addTagRefs(Element parentNode, HasLabels objWithLabels) {
		if (objWithLabels.getLabels() == null || objWithLabels.getLabels().getGroupCount() == 0)
			return;

		for (LabelGroup group : objWithLabels.getLabels().getGroups().values()) {
			if (group.getLabels() == null)
				continue;

			StringBuilder tagRefs = new StringBuilder();

			for (Label label : group.getLabels()) {
				Tag tag = getTag(label);
				if (tag != null && tag.label != null) {
					if (tagRefs.length() > 0)
						tagRefs.append(' ');
					tagRefs.append(tag.ID);
				}
			}

			if (tagRefs.length() > 0)
				addAttribute(parentNode, AltoXmlNames.ATTR_TAGREFS, tagRefs.toString());
		}
	}

	private void addTypeTagRefs(Element parentNode, Region region){
		StringBuilder tagRefs = new StringBuilder();

		Tag tag = getTag((TextRegion)region);
		if (tag != null && tag.label != null) {
			if (tagRefs.length() > 0)
				tagRefs.append(' ');
			tagRefs.append(tag.ID);
		}

		if (tagRefs.length() > 0)
			addAttribute(parentNode, AltoXmlNames.ATTR_TAGREFS, tagRefs.toString());
	}

	Group findReadingOrderGroup(Group startGroup, Id regionId) {

		for (int i=0; i<startGroup.getSize(); i++) {
			GroupMember member = startGroup.getMember(i);
			if (member instanceof RegionRef) {
				if (((RegionRef)member).getRegionId().equals(regionId))
					return startGroup;
			}
			else if (member instanceof Group) {
				//Recursion
				Group res = findReadingOrderGroup((Group)member, regionId);
				if (res != null)
					return res;
			}
		}
		return null;
	}

	void addPositionAttributes(Element element, Polygon coords) {
		if (coords == null || coords.getSize() == 0)
			return;

		Rect box = coords.getBoundingBox();
		addAttribute(element, AltoXmlNames.ATTR_HPOS, ""+box.left);
		addAttribute(element, AltoXmlNames.ATTR_VPOS, ""+box.top);
		addAttribute(element, AltoXmlNames.ATTR_WIDTH, ""+box.getWidth());
		addAttribute(element, AltoXmlNames.ATTR_HEIGHT, ""+box.getHeight());
	}

	String getAltoBlockType(Region region) {
		if (region.getRegionCount() > 0)
			return AltoXmlNames.ELEMENT_ComposedBlock;
		if (RegionType.TextRegion.equals(region.getType()))
			return AltoXmlNames.ELEMENT_TextBlock;
		if (RegionType.SeparatorRegion.equals(region.getType()))
			return AltoXmlNames.ELEMENT_GraphicalElement;

		return AltoXmlNames.ELEMENT_Illustration;
	}

	void addTextBlockContent(Element blockNode, TextRegion region) {

		//LANG
		if (region.getAttributes().get("primaryLanguage") != null && region.getAttributes().get("primaryLanguage").getValue() != null) {
			String lang = getAltoLanguage(region.getAttributes().get("primaryLanguage").getValue().toString());
			if (lang != null)
				addAttribute(blockNode, AltoXmlNames.ATTR_LANG, lang);
		}

		//Text lines
		for (int i=0; i<region.getTextObjectCount(); i++)
			addTextLine(blockNode, (TextLine)region.getTextObject(i));
	}

	void addGraphicalBlockContent(Element blockNode, Region region) {
		//No additional content
	}

	void addIllustrationBlockContent(Element blockNode, Region region) {

		//Type (use PAGE region type)
		addAttribute(blockNode, AltoXmlNames.ATTR_TYPE, region.getType().toString());

		//Alternative image
		if (!region.getAlternativeImages().isEmpty())
			addAttribute(blockNode, AltoXmlNames.ATTR_FILEID, region.getAlternativeImages().get(0).getFilename());
	}

	void addTextLine(Element blockNode, TextLine textLine) {

		if (textLine.getTextObjectCount() == 0)
			return; //We need words

		Element textLineNode = doc.createElementNS(getNamespace(), AltoXmlNames.ELEMENT_TextLine);
		blockNode.appendChild(textLineNode);

		//ID
		addAttribute(textLineNode, AltoXmlNames.ATTR_ID, textLine.getId().toString());

		//LANG
		if (textLine.getAttributes().get("primaryLanguage") != null && textLine.getAttributes().get("primaryLanguage").getValue() != null) {
			String lang = getAltoLanguage(textLine.getAttributes().get("primaryLanguage").getValue().toString());
			if (lang != null)
				addAttribute(textLineNode, AltoXmlNames.ATTR_LANG, lang);
		}

		//HPOS, VPOS, WIDTH, HEIGHT
		addPositionAttributes(textLineNode, textLine.getCoords());

		//Styles
		TextStyle textStyle = getTextStyle(textLine);
		if (textStyle != null)
			addAttribute(textLineNode, AltoXmlNames.ATTR_STYLEREFS, textStyle.ID);

		//CS - Not available in PAGE

		//PROCESSINGREFS - Not available in PAGE

		//TAGREFS
		addTagRefs(textLineNode, textLine);

		//TODO
		//BASELINE

		addShape(textLineNode, textLine.getCoords());

		//Words (strings)
		for (int i=0; i<textLine.getTextObjectCount(); i++)
			addWord(textLineNode, (Word)textLine.getTextObject(i));

		//TODO
		//SP

		//HYP
	}

	void addWord(Element textLineNode, Word word) {

		Element wordNode = doc.createElementNS(getNamespace(), AltoXmlNames.ELEMENT_String);
		textLineNode.appendChild(wordNode);

		//ID
		addAttribute(wordNode, AltoXmlNames.ATTR_ID, word.getId().toString());

		//CONTENT
		String textContent = word.getText() != null ? word.getText() : word.composeText(false, false);
		if (textContent.isEmpty() && propagatedWordTexts.containsKey(word.getId().toString()))
			textContent = propagatedWordTexts.get(word.getId().toString());

		addAttribute(wordNode, AltoXmlNames.ATTR_CONTENT, textContent);

		//LANG
		if (word.getAttributes().get("language") != null && word.getAttributes().get("language").getValue() != null) {
			String lang = getAltoLanguage(word.getAttributes().get("language").getValue().toString());
			if (lang != null)
				addAttribute(wordNode, AltoXmlNames.ATTR_LANG, lang);
		}

		//HPOS, VPOS, WIDTH, HEIGHT
		addPositionAttributes(wordNode, word.getCoords());

		//Styles
		TextStyle textStyle = getTextStyle(word);
		if (textStyle != null)
			addAttribute(wordNode, AltoXmlNames.ATTR_STYLEREFS, textStyle.ID);

		//CS - Not available in PAGE

		//PROCESSINGREFS - Not available in PAGE

		//WC
		if (word.getAttributes().get("conf") != null && word.getAttributes().get("conf").getValue() != null)
			addAttribute(wordNode, AltoXmlNames.ATTR_WC, word.getAttributes().get("conf").getValue().toString());

		//TAGREFS
		addTagRefs(wordNode, word);

		//TODO
		//STYLE
		//SUBS_TYPE
		//SUBS_CONTENT
		//CC - Confidence level of each character in that string. A list of numbers, one number between 0 (sure) and 9 (unsure) for each character.

		addShape(wordNode, word.getCoords());

		//TODO: ALTERNATIVE

		//Glyphs
		for (int i=0; i<word.getTextObjectCount(); i++)
			addGlyph(wordNode, (Glyph)word.getTextObject(i));
	}

	void addGlyph(Element wordNode, Glyph glyph) {

		Element glyphNode = doc.createElementNS(getNamespace(), AltoXmlNames.ELEMENT_Glyph);
		wordNode.appendChild(glyphNode);

		//ID
		addAttribute(glyphNode, AltoXmlNames.ATTR_ID, glyph.getId().toString());

		//CONTENT
		String textContent = glyph.getText() != null ? glyph.getText() : "";
		if (textContent.isEmpty() && propagatedGlyphTexts.containsKey(glyph.getId().toString()))
			textContent = propagatedGlyphTexts.get(glyph.getId().toString());

		addAttribute(glyphNode, AltoXmlNames.ATTR_CONTENT, textContent.isEmpty() ? "?" : textContent);

		//HPOS, VPOS, WIDTH, HEIGHT
		addPositionAttributes(glyphNode, glyph.getCoords());

		//Styles
		TextStyle textStyle = getTextStyle(glyph);
		if (textStyle != null)
			addAttribute(glyphNode, AltoXmlNames.ATTR_STYLEREFS, textStyle.ID);

		//GC
		if (glyph.getAttributes().get("conf") != null && glyph.getAttributes().get("conf").getValue() != null)
			addAttribute(glyphNode, AltoXmlNames.ATTR_GC, glyph.getAttributes().get("conf").getValue().toString());

		//Shape
		addShape(glyphNode, glyph.getCoords());

		//Variant
		if (glyph.getTextContentVariantCount() > 1) {
			for (int i=0; i<glyph.getTextContentVariantCount(); i++) {
				TextContent variant = glyph.getTextContentVariant(i);
				if (variant != null && variant.getText() != null && !variant.getText().isEmpty()) {
					Element variantNode = doc.createElementNS(getNamespace(), AltoXmlNames.ELEMENT_Variant);
					glyphNode.appendChild(variantNode);

					//CONTENT
					addAttribute(variantNode, AltoXmlNames.ATTR_CONTENT, variant.getText());

					//VC
					if (variant.getConfidence() != null)
						addAttribute(variantNode, AltoXmlNames.ATTR_VC, ""+variant.getConfidence());

				}
			}
		}
	}

	String getAltoLanguage(String pageLanguage) {
		Map<String, String> AltoLanguages = Stream.of(new String[][] {
			{"Abkhaz", "ab"},
			{"Afar", "aa"},
			{"Afrikaans", "af"},
			{"Akan", "ak"},
			{"Albanian", "sq"},
			{"Amharic", "am"},
			{"Arabic", "ar"},
			{"Aragonese", "an"},
			{"Armenian", "hy"},
			{"Assamese", "as"},
			{"Avaric", "av"},
			{"Avestan", "ae"},
			{"Aymara", "ay"},
			{"Azerbaijani", "az"},
			{"Bambara", "bm"},
			{"Bashkir", "ba"},
			{"Basque", "eu"},
			{"Belarusian", "be"},
			{"Bengali", "bn"},
			{"Bihari", "bh"},
			{"Bislama", "bi"},
			{"Bosnian", "bs"},
			{"Breton", "br"},
			{"Bulgarian", "bg"},
			{"Burmese", "my"},
			{"Cambodian", "km"},
			{"Catalan", "ca"},
			{"Chamorro", "ch"},
			{"Chechen", "ce"},
			{"Chichewa", "ny"},
			{"Chinese", "zh"},
			{"Chuvash", "cv"},
			{"Cornish", "kw"},
			{"Corsican", "co"},
			{"Cree", "cr"},
			{"Croatian", "hr"},
			{"Czech", "cs"},
			{"Danish", "da"},
			{"Divehi", "dv"},
			{"Dutch", "nl"},
			{"Dzongkha", "dz"},
			{"English", "en"},
			{"Esperanto", "eo"},
			{"Estonian", "et"},
			{"Ewe", "ee"},
			{"Faroese", "fo"},
			{"Fijian", "fj"},
			{"Finnish", "fi"},
			{"French", "fr"},
			{"Fula", "ff"},
			{"Gaelic", "gd"},
			{"Galician", "gl"},
			{"Ganda", "lg"},
			{"Georgian", "ka"},
			{"German", "de"},
			{"Greek", "el"},
			{"Guaraní", "gn"},
			{"Gujarati", "gu"},
			{"Haitian", "ht"},
			{"Hausa", "ha"},
			{"Hebrew", "he"},
			{"Herero", "hz"},
			{"Hindi", "hi"},
			{"Hiri Motu", "ho"},
			{"Hungarian", "hu"},
			{"Icelandic", "is"},
			{"Ido", "io"},
			{"Igbo", "ig"},
			{"Indonesian", "id"},
			{"Interlingua", "ia"},
			{"Interlingue", "ie"},
			{"Inuktitut", "iu"},
			{"Inupiaq", "ik"},
			{"Irish", "ga"},
			{"Italian", "it"},
			{"Japanese", "ja"},
			{"Javanese", "jv"},
			{"Kalaallisut", "kl"},
			{"Kannada", "kn"},
			{"Kanuri", "kr"},
			{"Kashmiri", "ks"},
			{"Kazakh", "kk"},
			{"Khmer", "km"},
			{"Kikuyu", "ki"},
			{"Kinyarwanda", "rw"},
			{"Kirundi", "rn"},
			{"Komi", "kv"},
			{"Kongo", "kg"},
			{"Korean", "ko"},
			{"Kurdish", "ku"},
			{"Kwanyama", "kj"},
			{"Kyrgyz", "ky"},
			{"Lao", "lo"},
			{"Latin", "la"},
			{"Latvian", "lv"},
			{"Limburgish", "li"},
			{"Lingala", "ln"},
			{"Lithuanian", "lt"},
			{"Luba-Katanga", "lu"},
			{"Luxembourgish", "lb"},
			{"Macedonian", "mk"},
			{"Malagasy", "mg"},
			{"Malay", "ms"},
			{"Malayalam", "ml"},
			{"Maltese", "mt"},
			{"Manx", "gv"},
			{"Māori", "mi"},
			{"Marathi", "mr"},
			{"Marshallese", "mh"},
			{"Mongolian", "mn"},
			{"Nauru", "na"},
			{"Navajo", "nv"},
			{"Ndonga", "ng"},
			{"Nepali", "ne"},
			{"North Ndebele", "nd"},
			{"Northern Sami", "se"},
			{"Norwegian", "no"},
			{"Norwegian Bokmål", "nb"},
			{"Norwegian Nynorsk", "nn"},
			{"Nuosu", "ii"},
			{"Occitan", "oc"},
			{"Ojibwe", "oj"},
			{"Old Church Slavonic", "cu"},
			{"Oriya", "or"},
			{"Oromo", "om"},
			{"Ossetian", "os"},
			{"Pāli", "pi"},
			{"Panjabi", "pa"},
			{"Pashto", "ps"},
			{"Persian", "fa"},
			{"Polish", "pl"},
			{"Portuguese", "pt"},
			{"Punjabi", "pa"},
			{"Quechua", "qu"},
			{"Romanian", "ro"},
			{"Romansh", "rm"},
			{"Russian", "ru"},
			{"Samoan", "sm"},
			{"Sango", "sg"},
			{"Sanskrit", "sa"},
			{"Sardinian", "sc"},
			{"Serbian", "sr"},
			{"Shona", "sn"},
			{"Sindhi", "sd"},
			{"Sinhala", "si"},
			{"Slovak", "sk"},
			{"Slovene", "sl"},
			{"Somali", "so"},
			{"South Ndebele", "nr"},
			{"Southern Sotho", "st"},
			{"Spanish", "es"},
			{"Sundanese", "su"},
			{"Swahili", "sw"},
			{"Swati", "ss"},
			{"Swedish", "sv"},
			{"Tagalog", "tl"},
			{"Tahitian", "ty"},
			{"Tajik", "tg"},
			{"Tamil", "ta"},
			{"Tatar", "tt"},
			{"Telugu", "te"},
			{"Thai", "th"},
			{"Tibetan", "bo"},
			{"Tigrinya", "ti"},
			{"Tonga", "to"},
			{"Tsonga", "ts"},
			{"Tswana", "tn"},
			{"Turkish", "tr"},
			{"Turkmen", "tk"},
			{"Twi", "tw"},
			{"Uighur", "ug"},
			{"Ukrainian", "uk"},
			{"Urdu", "ur"},
			{"Uzbek", "uz"},
			{"Venda", "ve"},
			{"Vietnamese", "vi"},
			{"Volapük", "vo"},
			{"Walloon", "wa"},
			{"Welsh", "cy"},
			{"Western Frisian", "fy"},
			{"Wolof", "wo"},
			{"Xhosa", "xh"},
			{"Yiddish", "yi"},
			{"Yoruba", "yo"},
			{"Zhuang", "za"},
			{"Zulu", "zu"}
		}).collect(Collectors.toMap(data -> data[0], data -> data[1]));

		return AltoLanguages.get(pageLanguage);
	}

	//Propagate text from regions to words / glyphs
	void propagateText() {
		//Regions
		for (int i=0; i<layout.getRegionCount(); i++) {
			if (layout.getRegion(i) instanceof TextRegion) {
				TextRegion textRegion = (TextRegion)layout.getRegion(i);
				if (textRegion.getTextObjectCount() == 0)
					continue;

				String regionText = textRegion.getText() != null && !textRegion.getText().isEmpty() ? textRegion.getText() : textRegion.composeText(false, true);
				//regionText = regionText.replaceAll("\r\n", "\n");

				String[] regionTextSplit = regionText.split("\r\n|\n");

				//Text lines
				List<TextLine> textLinesSorted = new ArrayList<TextLine>(textRegion.getTextObjectCount());
				for (int t=0; t<textRegion.getTextObjectCount(); t++)
					textLinesSorted.add((TextLine)textRegion.getTextObject(t));
				// Sort
				Collections.sort(textLinesSorted, new TextObjectComparator(true, true));

				for (int t=0; t<textLinesSorted.size(); t++) {
					TextLine textLine = textLinesSorted.get(t);

					String textLineText = textLine.getText() != null && !textLine.getText().isEmpty() ? textLine.getText() : textLine.composeText(false, true);
					if (textLineText.isEmpty() && t < regionTextSplit.length)
						textLineText = regionTextSplit[t];

					String[] textLineTextSplit = textLineText.split(" ");

					//Words
					List<Word> wordsSorted = new ArrayList<Word>(textLine.getTextObjectCount());
					for (int w=0; w<textLine.getTextObjectCount(); w++)
						wordsSorted.add((Word)textLine.getTextObject(w));
					// Sort
					Collections.sort(wordsSorted, new TextObjectComparator(false, true));

					for (int w=0; w<wordsSorted.size(); w++) {
						Word word = wordsSorted.get(w);

						String popagatedWordText = "";
						if (w < textLineTextSplit.length) {
							popagatedWordText = textLineTextSplit[w];
							propagatedWordTexts.put(word.getId().toString(), popagatedWordText);
						}

						String wordText = word.getText() != null && !word.getText().isEmpty() ? word.getText() : word.composeText(false, true);
						if (wordText.isEmpty())
							wordText = popagatedWordText;

						//Glyphs
						List<Glyph> glyphsSorted = new ArrayList<Glyph>(word.getTextObjectCount());
						for (int g=0; g<word.getTextObjectCount(); g++)
							glyphsSorted.add((Glyph)word.getTextObject(g));
						// Sort
						Collections.sort(glyphsSorted, new TextObjectComparator(false, true));

						for (int g=0; g<glyphsSorted.size(); g++) {
							Glyph glyph = glyphsSorted.get(g);

							String popagatedGlyphText = "";
							if (g < wordText.length()) {
								popagatedGlyphText = "" + wordText.charAt(g);
								propagatedGlyphTexts.put(glyph.getId().toString(), popagatedGlyphText);
							}
						}
					}
				}
			}
		}
	}

	private void findTextStyles() {
		//Regions
		for (int i=0; i<layout.getRegionCount(); i++) {
			if (layout.getRegion(i) instanceof TextRegion)
				findTextStyles((TextRegion)layout.getRegion(i));
		}
	}

	private void findTextStyles(TextRegion reg) {

		getTextStyle(reg);

		findTextStyles((LowLevelTextContainer)reg);

		//Nested regions
		for (int i=0; i<reg.getRegionCount(); i++) {
			if (reg.getRegion(i) instanceof TextRegion)
				findTextStyles((TextRegion)reg.getRegion(i));
		}
	}

	private void findTextStyles(LowLevelTextContainer textContainer) {
		//Children
		for (int i=0; i<textContainer.getTextObjectCount(); i++) {
			TextObject child = textContainer.getTextObject(i);

			getTextStyle(child);

			if (child instanceof LowLevelTextContainer)
				findTextStyles((LowLevelTextContainer)child);
		}
	}

	/**
	 * Get ALTO text style for given PAGE text object
	 * @param textObj Text object with possible PAGE text style
	 * @return Text style object or null (if no text style attributes or no font size (required))
	 */
	private TextStyle getTextStyle(TextObject textObj) {
		TextStyle newTextStyle = new TextStyle(textObj);

		//Look if already exists, otherwise add
		for (TextStyle textStyle : textStyles) {
			if (textStyle.equals(newTextStyle))
				return textStyle;
		}

		//Font size is required!
		if (!newTextStyle.isEmpty() && newTextStyle.fontSize != null) {
			textStyles.add(newTextStyle);
			newTextStyle.ID = "ts" + textStyles.size();
			return newTextStyle;
		}
		return null;
	}

	private void findParagraphStyles() {
		//Regions
		for (int i=0; i<layout.getRegionCount(); i++) {
			if (layout.getRegion(i) instanceof TextRegion)
				findParagraphStyles((TextRegion)layout.getRegion(i));
		}
	}

	private void findParagraphStyles(TextRegion reg) {

		getParagraphStyle(reg);

		//Nested regions
		for (int i=0; i<reg.getRegionCount(); i++) {
			if (reg.getRegion(i) instanceof TextRegion)
				findParagraphStyles((TextRegion)reg.getRegion(i));
		}
	}

	private ParagraphStyle getParagraphStyle(TextRegion region) {
		ParagraphStyle newParagraphStyle = new ParagraphStyle(region);

		//Look if already exists, otherwise add
		for (ParagraphStyle paragraphStyle : paragraphStyles) {
			if (paragraphStyle.equals(newParagraphStyle))
				return paragraphStyle;
		}

		if (!newParagraphStyle.isEmpty()) {
			paragraphStyles.add(newParagraphStyle);
			newParagraphStyle.ID = "ps" + paragraphStyles.size();
			return newParagraphStyle;
		}
		return null;
	}

	private void findTags() {

		//Page
		findObjectTags(page);

		//Regions
		for (int i=0; i<layout.getRegionCount(); i++) {
			if (layout.getRegion(i) instanceof TextRegion) {
				findTags((TextRegion) layout.getRegion(i));
				getTag((TextRegion)layout.getRegion(i));
			}
		}
	}

	private void findTags(TextRegion reg) {

		findObjectTags(reg);

		findTags((LowLevelTextContainer)reg);

		//Nested regions
		for (int i=0; i<reg.getRegionCount(); i++) {
			if (reg.getRegion(i) instanceof TextRegion)
				findTags((TextRegion)reg.getRegion(i));
		}
	}

	private void findTags(LowLevelTextContainer textContainer) {
		//Children
		for (int i=0; i<textContainer.getTextObjectCount(); i++) {
			TextObject child = textContainer.getTextObject(i);

			if (child instanceof HasLabels)
				findObjectTags((HasLabels)child);

			if (child instanceof LowLevelTextContainer)
				findTags((LowLevelTextContainer)child);
		}
	}

	private void findObjectTags(HasLabels objWithLabels) {
		if (objWithLabels.getLabels() != null) {
			Labels labels = objWithLabels.getLabels();
			for (int i=0; i<labels.getGroupCount(); i++) {
				if (labels.getGroupCount() > 0)
					for (LabelGroup group : labels.getGroups().values()) {
						for (Label label : group.getLabels())
							getTag(label);
					}
			}
		}
	}

	/**
	 * Get ALTO tag for given PAGE object
	 * @param objWithLabels Object with possible PAGE labels
	 * @return Tag or null
	 */
	private Tag getTag(Label label) {
		Tag newTag = new Tag(label);

		//Look if already exists, otherwise add
		for (Tag tag : tags) {
			if (tag.equals(newTag))
				return tag;
		}

		//Tag label is required!
		if (!newTag.isEmpty() && newTag.label != null) {
			tags.add(newTag);
			newTag.ID = "tag" + tags.size();
			return newTag;
		}
		return null;
	}

	private Tag getTag(TextRegion region) {
		Tag newTag = new Tag(region);
		//Look if already exists, otherwise add
		for (Tag tag : tags) {
			if (tag.equals(newTag))
				return tag;
		}

		//Tag label is required!
		if (!newTag.isEmpty() && newTag.label != null) {
			tags.add(newTag);
			newTag.ID = "tag" + tags.size();
			return newTag;
		}
		return null;
	}

	/** Comparator for sorting polygons by vertical or horizontal position */
	private static final class TextObjectComparator implements Comparator<GeometricObject> {

		private boolean sortVertically;
		private boolean ascending;

		public TextObjectComparator(boolean sortVertically, boolean ascending) {
			this.sortVertically = sortVertically;
			this.ascending = ascending;
		}

		@Override
		public int compare(GeometricObject obj1, GeometricObject obj2) {
			if (obj1 == null || obj2 == null || obj1.getCoords() == null || obj2.getCoords() == null)
				return 0;

			Rect box1 = obj1.getCoords().getBoundingBox();
			Rect box2 = obj2.getCoords().getBoundingBox();

			if (sortVertically) {
				int c1 = (box1.top + box1.bottom) / 2;
				int c2 = (box2.top + box2.bottom) / 2;
				if (ascending)
					return c1 - c2;
				else //descending
					return c2 - c1;
			}
			else { //horizontally
				int c1 = (box1.left + box1.right) / 2;
				int c2 = (box2.left + box2.right) / 2;
				if (ascending)
					return c1 - c2;
				else //descending
					return c2 - c1;
			}
		}
	}

	/** ALTO Text Styles */
	private static final class TextStyle {
		public String ID;
		public String fontFamily;
		public String fontType; //serif or sans-serif
		public String fontWidthType; //proportional or fixed
		public Double fontSize; //points (required)
		public String fontColor; //RGB in hex notation
		public String fontStyle; //Whitespace-separated list of bold, italics, subscript, superscript, smallcaps, underline

		/**
		 * Constructor
		 * @param textObj PAGE text object
		 */
		public TextStyle(TextObject textObj) {
			//Font family
			if (textObj.getAttributes().get("fontFamily") != null && textObj.getAttributes().get("fontFamily").getValue() != null)
				if (!textObj.getAttributes().get("fontFamily").getValue().toString().isEmpty())
					fontFamily = textObj.getAttributes().get("fontFamily").getValue().toString();

			//Font type
			if (textObj.getAttributes().get("serif") != null && textObj.getAttributes().get("serif").getValue() != null)
				if (((BooleanValue)textObj.getAttributes().get("serif").getValue()).val)
					fontType = "serif";
				else
					fontType = "sans-serif";

			//Font width type
			if (textObj.getAttributes().get("monospace") != null && textObj.getAttributes().get("monospace").getValue() != null)
				if (((BooleanValue)textObj.getAttributes().get("monospace").getValue()).val)
					fontWidthType = "fixed";
				else
					fontWidthType = "proportional";

			//Font Size
			if (textObj.getAttributes().get("fontSize") != null && textObj.getAttributes().get("fontSize").getValue() != null)
				fontSize = ((DoubleValue)textObj.getAttributes().get("fontSize").getValue()).val;
			//Font Colour (hex)
			if (textObj.getAttributes().get("textColourRgb") != null && textObj.getAttributes().get("textColourRgb").getValue() != null) {
				fontColor = getHexColor(Integer.parseInt(textObj.getAttributes().get("textColourRgb").getValue().toString()));
			}

			//Font style (bold, italics, subscript, superscript, smallcaps, underline)
			StringBuilder fontStyle = new StringBuilder();
			if (textObj.isBold() != null && textObj.isBold())
				fontStyle.append("bold");
			if (textObj.isItalic() != null && textObj.isItalic()) {
				if (fontStyle.length() > 0)
					fontStyle.append(' ');
				fontStyle.append("italics");
			}
			if (textObj.isSubscript() != null && textObj.isSubscript()) {
				if (fontStyle.length() > 0)
					fontStyle.append(' ');
				fontStyle.append("subscript");
			}
			if (textObj.isSuperscript() != null && textObj.isSuperscript()) {
				if (fontStyle.length() > 0)
					fontStyle.append(' ');
				fontStyle.append("superscript");
			}
			if (textObj.isSmallCaps() != null && textObj.isSmallCaps()) {
				if (fontStyle.length() > 0)
					fontStyle.append(' ');
				fontStyle.append("smallcaps");
			}
			if (textObj.isUnderlined() != null && textObj.isUnderlined()) {
				if (fontStyle.length() > 0)
					fontStyle.append(' ');
				fontStyle.append("underline");
			}

			if (fontStyle.length() > 0)
				this.fontStyle = fontStyle.toString();
		}

		/**
		 * Converts text colour in RGB encoded format (red value) + (256 x green value) + (65536 x blue value) to
		 * text colour in hexadecimal encoded format (RRGGBB).
		 * **/
		private String getHexColor(Integer Color){
			int r = Color % 256;
			int g = (Color / 256) % 256;
			int b = (Color / 65536) % 256;

			return String.format("%02X%02X%02X", r, g, b);
		}

		/** Returns true if no attribute is set */
		public boolean isEmpty() {
			return fontFamily == null
					&& fontType == null
					&& fontWidthType == null
					&& fontSize == null
					&& fontColor == null
					&& fontStyle == null;
		}

		@Override
		public boolean equals(Object other) {
			if (other instanceof TextStyle) {
				TextStyle otherStyle = (TextStyle)other;

				if (!compareAttributes(fontFamily, otherStyle.fontFamily))
					return false;
				if (!compareAttributes(fontType, otherStyle.fontType))
					return false;
				if (!compareAttributes(fontWidthType, otherStyle.fontWidthType))
					return false;
				if (!compareAttributes(fontSize, otherStyle.fontSize))
					return false;
				if (!compareAttributes(fontColor, otherStyle.fontColor))
					return false;
				if (!compareAttributes(fontStyle, otherStyle.fontStyle))
					return false;

				return true;
			}
			return false;
		}
	}

	public static boolean compareAttributes(Object attr1, Object attr2) {
		if (attr1 != null || attr2 != null) {
			if (attr1 != null)
				return attr1.equals(attr2);
			else //if (attr2 != null)
				return attr2.equals(attr1);
		}
		return true; //Both null
	}

	/** ALTO paragraph styles */
	private static final class ParagraphStyle {
		public String ID;
		public String align; //Left, Right, Center, Block
		public Double leftIndent; //Left indent of the paragraph in relation to the column.
		public Double rightIndent; //Right indent of the paragraph in relation to the column.
		public Double lineSpace; //Line spacing between two lines of the paragraph. Measurement calculated from baseline to baseline.
		public Double firstLineIndent; //Indent of the first line of the paragraph if this is different from the other lines. A negative value indicates an indent to the left, a positive value indicates an indent to the right.

		/**
		 * Constructor
		 * @param region PAGE text region
		 */
		public ParagraphStyle(TextRegion region) {
			//Align
			if (region.getAttributes().get("align") != null && region.getAttributes().get("align").getValue() != null)
				if ("left".equals(region.getAttributes().get("align").getValue().toString()))
					align = "Left";
				else if ("right".equals(region.getAttributes().get("align").getValue().toString()))
					align = "Right";
				else if ("centre".equals(region.getAttributes().get("align").getValue().toString()))
					align = "Center";
				else //Justify
					align = "Block";

			//Left indent
			// Not available in PAGE

			//Right indent
			// Not available in PAGE

			//Line space
			if (region.getAttributes().get("leading") != null && region.getAttributes().get("leading").getValue() != null)
				lineSpace = (double)((IntegerValue)region.getAttributes().get("leading").getValue()).val;

			//First line indent
			// Not available in PAGE
		}


		/** Returns true if no attribute is set */
		public boolean isEmpty() {
			return align == null
					&& leftIndent == null
					&& rightIndent == null
					&& lineSpace == null
					&& firstLineIndent == null;
		}

		@Override
		public boolean equals(Object other) {
			if (other instanceof ParagraphStyle) {
				ParagraphStyle otherStyle = (ParagraphStyle)other;

				if (!compareAttributes(align, otherStyle.align))
					return false;
				if (!compareAttributes(leftIndent, otherStyle.leftIndent))
					return false;
				if (!compareAttributes(rightIndent, otherStyle.rightIndent))
					return false;
				if (!compareAttributes(lineSpace, otherStyle.lineSpace))
					return false;
				if (!compareAttributes(firstLineIndent, otherStyle.firstLineIndent))
					return false;

				return true;
			}
			return false;
		}
	}

	/** ALTO Tag */
	private static final class Tag {
		public String ID;
		public String type;
		public String label; //required
		public String description;
		public String uri;

		/**
		 * Constructor
		 * @param label PAGE label
		 */
		public Tag(Label label) {
			if (label.getType() != null && !label.getType().isEmpty())
				type = label.getType();
			if (label.getValue() != null && !label.getValue().isEmpty())
				this.label = label.getValue();
			if (label.getComments() != null && !label.getComments().isEmpty())
				description = label.getComments();
			if (label.getExternalModel() != null && !label.getExternalModel().isEmpty())
				uri = label.getExternalModel();
		}

		public Tag(Region region) {
			if (region.getAttributes().get("type") != null && region.getAttributes().get("type").getValue() != null)
				label = region.getAttributes().get("type").getValue().toString();
			description="PAGE XML text region type";
		}

		/** Returns true if no attribute is set */
		public boolean isEmpty() {
			return type == null
					&& label == null
					&& description == null
					&& uri == null;
		}

		@Override
		public boolean equals(Object other) {
			if (other instanceof Tag) {
				Tag otherTag = (Tag)other;

				if (!compareAttributes(type, otherTag.type))
					return false;
				if (!compareAttributes(label, otherTag.label))
					return false;
				if (!compareAttributes(description, otherTag.description))
					return false;
				if (!compareAttributes(uri, otherTag.uri))
					return false;

				return true;
			}
			return false;
		}
	}
}
