package de.uniwue.zpd.dla.page.layout.physical.text.graphemes;

import de.uniwue.zpd.dla.page.layout.physical.AttributeFactory;
import de.uniwue.zpd.dla.page.layout.physical.ContentObject;
import de.uniwue.zpd.dla.page.layout.physical.shared.ContentType;
import de.uniwue.zpd.dla.page.layout.physical.shared.LowLevelTextType;
import de.uniwue.zpd.dla.page.layout.physical.text.impl.Glyph;
import de.uniwue.zpd.ident.Id;
import de.uniwue.zpd.ident.IdRegister;
import de.uniwue.zpd.labels.Labels;
import de.uniwue.zpd.maths.geometry.Polygon;
import de.uniwue.zpd.shared.variable.VariableMap;

public class Grapheme extends GraphemeElementImpl implements GraphemeElement, ContentObject {

	private Polygon coords;
	private Labels labels;

	/**
	 * Constructor
	 * @param idRegister
	 * @param id
	 * @param coords
	 * @param attributes
	 * @param parent
	 * @param attrFactory
	 */
	protected Grapheme(IdRegister idRegister, Id id, Polygon coords, VariableMap attributes,
			Glyph parent, AttributeFactory attrFactory) {
		super(idRegister, id, attributes, parent, attrFactory);
	}

	@Override
	public Polygon getCoords() {
		return coords;
	}

	@Override
	public void setCoords(Polygon coords) {
		this.coords = coords;
	}

	@Override
	public ContentType getType() {
		return LowLevelTextType.Grapheme;
	}

	@Override
	public boolean isTemporary() {
		return false;
	}

	@Override
	public Labels getLabels() {
		return labels;
	}

	@Override
	public void setLabels(Labels labels) {
		this.labels = labels;
	}

}
