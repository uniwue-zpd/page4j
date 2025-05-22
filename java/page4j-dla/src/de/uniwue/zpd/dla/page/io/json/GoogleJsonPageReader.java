/*
 * Copyright 2019 PRImA Research Lab, University of Salford, United Kingdom
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.uniwue.zpd.dla.page.io.json;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.uniwue.zpd.dla.page.Page;
import de.uniwue.zpd.dla.page.io.InputSource;
import de.uniwue.zpd.dla.page.io.PageReader;
import de.uniwue.zpd.dla.page.io.PageReaderBase;
import de.uniwue.zpd.dla.page.layout.PageLayout;
import de.uniwue.zpd.dla.page.layout.physical.Region;
import de.uniwue.zpd.dla.page.layout.physical.shared.RegionType;
import de.uniwue.zpd.dla.page.layout.physical.text.impl.Glyph;
import de.uniwue.zpd.dla.page.layout.physical.text.impl.TextLine;
import de.uniwue.zpd.dla.page.layout.physical.text.impl.TextRegion;
import de.uniwue.zpd.dla.page.layout.physical.text.impl.Word;
import de.uniwue.zpd.io.UnsupportedFormatVersionException;
import de.uniwue.zpd.maths.geometry.Point;
import de.uniwue.zpd.maths.geometry.Polygon;
import de.uniwue.zpd.maths.geometry.Rect;

/**
 * Reads Google Cloud Vision JSON output and returns Page object.
 * (2019 JSON format; only reads the first page)
 *
 * @author Christian Clausner
 *
 */
public class GoogleJsonPageReader extends PageReaderBase implements PageReader {

	private static final String KEY_responses = "responses";
	private static final String KEY_fullTextAnnotation = "fullTextAnnotation";
	private static final String KEY_pages = "pages";
	private static final String KEY_property = "property";
	private static final String KEY_width = "width";
	private static final String KEY_height = "height";
	private static final String KEY_blocks = "blocks";
	private static final String KEY_blockType = "blockType";
	private static final String KEY_boundingBox = "boundingBox";
	private static final String KEY_vertices = "vertices";
	private static final String KEY_x = "x";
	private static final String KEY_y = "y";
	private static final String KEY_paragraphs = "paragraphs";
	private static final String KEY_words = "words";
	private static final String KEY_detectedBreak = "detectedBreak";
	private static final String KEY_type = "type";
	private static final String KEY_symbols = "symbols";
	private static final String KEY_text = "text";

	@Override
	public Page read(InputSource source) throws UnsupportedFormatVersionException {

		InputStream is = null;
		JsonNode rootJsonNode = null;
		try
		{
			is = getInputStream(source);
			ObjectMapper objectMapper = new ObjectMapper();
			rootJsonNode = objectMapper.readTree(new InputStreamReader(is, StandardCharsets.UTF_8));
		}
		catch (Exception e)
		{
			System.err.println(e.getMessage());
			return null;
		}
		finally
		{
			try { if(null != is) is.close(); } catch (Exception e) { e.printStackTrace(); }
		}

		if (!rootJsonNode.has(KEY_responses))
			throw new UnsupportedFormatVersionException("No 'responses' object found in JSON");

		JsonNode responsesNode = rootJsonNode.get(KEY_responses);

		if (responsesNode.isObject())
			return handleRoot(responsesNode);
		else if (responsesNode.isArray())
			return handleRoot(responsesNode.get(0));
		else
			throw new UnsupportedFormatVersionException("Unexpected JSON format");
	}

	private Page handleRoot(JsonNode json) throws UnsupportedFormatVersionException {

		if (json.has(KEY_fullTextAnnotation)) {
			JsonNode fullTextObj = json.get(KEY_fullTextAnnotation);
			if (fullTextObj.isObject()) {
				if (fullTextObj.has(KEY_pages)) {
					JsonNode pagesArrayNode = fullTextObj.get(KEY_pages);
					if (pagesArrayNode.isArray())
						return handlePage(pagesArrayNode.get(0));
					else
						throw new UnsupportedFormatVersionException("Expected array in 'pages' object");
				} else
					throw new UnsupportedFormatVersionException("Expected 'pages' object");
			}
			else
				throw new UnsupportedFormatVersionException("Expected 'fullTextAnnotation' to be an object");
		}
		throw new UnsupportedFormatVersionException("Expected 'fullTextAnnotation' object under responses");
	}

	private Page handlePage(JsonNode json) {
		Page page = new Page();

		page.getLayout().setSize(getIntAttribute(json, KEY_width), getIntAttribute(json, KEY_height));

		if (json.has(KEY_blocks)) {
			JsonNode blocksNode = json.get(KEY_blocks);
			if (blocksNode.isArray()) {
				for (int i=0; i<blocksNode.size(); i++) {
					handleBlock(blocksNode.get(i), page.getLayout());
				}
			}
		}
		return page;
	}

	private void handleBlock(JsonNode json, PageLayout pageLayout) {
		String blockTypeStr = getStringAttribute(json, KEY_blockType);
		RegionType regionType = googleBlockTypeToRegionType(blockTypeStr);

		if (regionType == RegionType.TextRegion) {
			if (json.has(KEY_paragraphs)) {
				JsonNode paragraphsNode = json.get(KEY_paragraphs);
				if (paragraphsNode.isArray()) {
					for (int i = 0; i < paragraphsNode.size(); i++) {
						handleParagraph(paragraphsNode.get(i), pageLayout);
					}
				}
			}
		}
		else {
			Polygon coords = handleBoundingBox(json);
			if (coords != null) {
				Region region = pageLayout.createRegion(regionType);
				region.setCoords(coords);
			}
		}
	}

	private void handleParagraph(JsonNode json, PageLayout pageLayout) {
		Polygon coords = handleBoundingBox(json);
		if (coords != null) {
			TextRegion region = (TextRegion)pageLayout.createRegion(RegionType.TextRegion);
			region.setCoords(coords);

			if (json.has(KEY_words))
				handleWords(json.get(KEY_words), region);
		}
	}

	private void handleWords(JsonNode json, TextRegion region) {
		if (json.isArray()) {
			TextLine currentTextLine = null;

			for (int i=0; i<json.size(); i++) {
				JsonNode wordJson = json.get(i);

				Polygon coords = handleBoundingBox(wordJson);

				if (coords != null) {
					if (currentTextLine == null)
						currentTextLine = region.createTextLine();

					Word word = currentTextLine.createWord();
					word.setCoords(coords);

					String breakType = handleSymbols(wordJson, word);

					if (BreakTypes.LineBreak.equals(breakType)
							|| BreakTypes.EndOfLine.equals(breakType)
							|| BreakTypes.EndOfLineHyphen.equals(breakType)) {
						finishTextLine(currentTextLine);
						currentTextLine = null;
					}

					String txt = word.composeText(false, false);
					if (BreakTypes.EndOfLineHyphen.equals(breakType))
						txt += "-";
					word.setText(txt);
				}
			}
			if (currentTextLine != null)
				finishTextLine(currentTextLine);
		}
	}

	private String handleSymbols(JsonNode wordJson, Word word) {
		String breakType = "";

		if (wordJson.has(KEY_symbols)) {
			JsonNode symbolsNode = wordJson.get(KEY_symbols);
			if (symbolsNode.isArray()) {
				for (int i=0; i<symbolsNode.size(); i++) {
					JsonNode symbol = symbolsNode.get(i);

					String symbolBreakType = getBreakType(symbol);
					if (!"".equals(symbolBreakType))
						breakType = symbolBreakType;

					Polygon coords = handleBoundingBox(symbol);
					if (coords != null) {
						Glyph glyph = word.createGlyph();
						glyph.setCoords(coords);
						glyph.setText(getStringAttribute(symbol, KEY_text));
					}
				}
			}
		}
		return breakType;
	}

	private String getBreakType(JsonNode json) {
		if (json.has(KEY_property)) {
			JsonNode propertyNode = json.get(KEY_property);
			if (propertyNode.isObject()) {
				if (propertyNode.has(KEY_detectedBreak)) {
					JsonNode detectedBreakNode = propertyNode.get(KEY_detectedBreak);
					if (detectedBreakNode.isObject()) {
						return getStringAttribute(detectedBreakNode, KEY_type);
					}
				}
			}
		}
		return "";
	}

	private void finishTextLine(TextLine line) {
		int l = Integer.MAX_VALUE;
		int r = 0;
		int t = Integer.MAX_VALUE;
		int b = 0;
		for (int i=0; i<line.getTextObjectCount(); i++) {
			Word word = (Word)line.getTextObject(i);
			Rect box = word.getCoords().getBoundingBox();
			if (box.left < l)
				l = box.left;
			if (box.right > r)
				r = box.right;
			if (box.top < t)
				t = box.top;
			if (box.bottom > b)
				b = box.bottom;
		}
		Polygon coords = new Polygon();
		coords.addPoint(l, t);
		coords.addPoint(r, t);
		coords.addPoint(r, b);
		coords.addPoint(l, b);
		line.setCoords(coords);
	}

	private Polygon handleBoundingBox(JsonNode parentJson) {
		if (parentJson.has(KEY_boundingBox)) {
			JsonNode boundingBoxNode = parentJson.get(KEY_boundingBox);
			if (boundingBoxNode.isObject()) {
				if (boundingBoxNode.has(KEY_vertices)) {
					JsonNode verticesNode = boundingBoxNode.get(KEY_vertices);
					if (verticesNode.isArray()) {
						if (verticesNode.size() == 4) {
							Polygon ret = new Polygon();
							ret.addPoint(getPoint(verticesNode.get(0)));
							ret.addPoint(getPoint(verticesNode.get(1)));
							ret.addPoint(getPoint(verticesNode.get(2)));
							ret.addPoint(getPoint(verticesNode.get(3)));
							return ret;
						}
					}
				}
			}
		}
		return null;
	}

	private int getIntAttribute(JsonNode parentJson, String key) {
		if (parentJson.has(key)) {
			return Integer.parseInt(parentJson.get(key).asText());
		}
		return 0;
	}

	private String getStringAttribute(JsonNode parentJson, String key) {
		if (parentJson.has(key)) {
			return parentJson.get(key).asText();
		}
		return "";
	}

	private Point getPoint(JsonNode node) {
		if (node != null && node.isObject()) {
			return new Point(Math.max(0, getIntAttribute(node, KEY_x)),
					Math.max(0, getIntAttribute(node, KEY_y)));
		}
		return new Point();
	}

	private RegionType googleBlockTypeToRegionType(String blockType) {
		if ("PICTURE".equals(blockType)) return RegionType.ImageRegion;
		if ("RULER".equals(blockType)) return RegionType.SeparatorRegion;
		if ("BARCODE".equals(blockType)) return RegionType.GraphicRegion;
		if ("TABLE".equals(blockType)) return RegionType.TableRegion;
		if ("TEXT".equals(blockType)) return RegionType.TextRegion;
		return RegionType.UnknownRegion;
	}

	private static final class BreakTypes {
		public static final String LineBreak = "LINE_BREAK";
		public static final String EndOfLine = "EOL_SURE_SPACE";
		public static final String EndOfLineHyphen = "HYPHEN";
	}
}
