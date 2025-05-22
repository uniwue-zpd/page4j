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
package de.uniwue.zpd.dla.page.layout;

import de.uniwue.zpd.dla.page.layout.shared.GeometricObject;
import de.uniwue.zpd.maths.geometry.Polygon;

/**
 * Basic implementation of an object that can be located on the document page.
 *
 * @author Christian Clausner
 *
 */
public class GeometricObjectImpl implements GeometricObject {

	private Polygon coords = null;

	/**
	 * Constructor
	 * @param coords The polygon locating the object on the page.
	 * @throws IllegalArgumentException if the passed polygon is null.
	 */
	public GeometricObjectImpl(Polygon coords) throws IllegalArgumentException {
		if (coords == null)
			throw new IllegalArgumentException("GeometricObjectImpl requires a polygon");
		this.coords = coords;
	}

	@Override
	public Polygon getCoords() {
		return coords;
	}

	@Override
	public void setCoords(Polygon coords) {
		this.coords = coords;
	}

}
