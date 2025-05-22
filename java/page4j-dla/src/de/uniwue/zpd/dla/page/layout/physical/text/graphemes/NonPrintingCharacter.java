package de.uniwue.zpd.dla.page.layout.physical.text.graphemes;

import de.uniwue.zpd.dla.page.layout.physical.AttributeFactory;
import de.uniwue.zpd.dla.page.layout.physical.shared.ContentType;
import de.uniwue.zpd.dla.page.layout.physical.shared.LowLevelTextType;
import de.uniwue.zpd.dla.page.layout.physical.text.impl.Glyph;
import de.uniwue.zpd.ident.Id;
import de.uniwue.zpd.ident.IdRegister;
import de.uniwue.zpd.shared.variable.VariableMap;

/**
 * A grapheme element representing a non-visual / non-printing character (as component of a glyph).
 *
 * @author Christian Clausner
 *
 */
public class NonPrintingCharacter extends GraphemeElementImpl implements GraphemeElement {

	protected NonPrintingCharacter(IdRegister idRegister, Id id, VariableMap attributes, Glyph parent,
			AttributeFactory attrFactory) {
		super(idRegister, id, attributes, parent, attrFactory);
	}

	@Override
	public ContentType getType() {
		return LowLevelTextType.NonPrintingCharacter;
	}

}
