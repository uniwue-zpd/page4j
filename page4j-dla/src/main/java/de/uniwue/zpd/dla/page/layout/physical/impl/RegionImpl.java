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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uniwue.zpd.dla.page.AlternativeImage;
import de.uniwue.zpd.dla.page.layout.physical.ContentFactory;
import de.uniwue.zpd.dla.page.layout.physical.Region;
import de.uniwue.zpd.dla.page.layout.physical.RegionContainer;
import de.uniwue.zpd.dla.page.layout.physical.role.RegionRole;
import de.uniwue.zpd.dla.page.layout.physical.shared.ContentType;
import de.uniwue.zpd.dla.page.layout.physical.shared.RegionType;
import de.uniwue.zpd.dla.page.layout.physical.shared.RoleType;
import de.uniwue.zpd.ident.Id;
import de.uniwue.zpd.ident.IdRegister;
import de.uniwue.zpd.ident.IdRegister.InvalidIdException;
import de.uniwue.zpd.labels.Labels;
import de.uniwue.zpd.maths.geometry.Polygon;
import de.uniwue.zpd.shared.variable.VariableMap;

/**
 * Basic implementation for layout regions.
 *
 * @author Christian Clausner
 *
 */
public abstract class RegionImpl implements Region {

	private RegionType type;
	private Id id;
	private IdRegister idRegister;

	private Polygon coords;

	private VariableMap attributes;

	private VariableMap userDefinedAttributes = null;

	private RegionContainer parentRegion;

	private List<Region> nestedRegions = new ArrayList<Region>();

	private Map<RoleType, RegionRole> roles = null;

	transient private Labels labels;

	private ContentFactory contentFactory;

	transient private List<AlternativeImage> alternativeImages;


	/**
	 * Constructor
	 * @param idRegister ID register (for creating child objects)
	 * @param type Region type
	 * @param id Region ID
	 * @param coords Region outline
	 * @param attributes Region attributes
	 * @param parentRegion (optional) Parent region
	 */
	protected RegionImpl(IdRegister idRegister, ContentFactory contentFactory, RegionType type, Id id, Polygon coords, VariableMap attributes, RegionContainer parentRegion) {
		this.id = id;
		this.idRegister = idRegister;
		this.contentFactory = contentFactory;
		this.coords = coords;
		this.attributes = attributes;
		this.parentRegion = parentRegion;
		this.type = type;
	}

	@Override
	public ContentType getType() {
		return type;
	}

	@Override
	public boolean hasRegions() {
		return !nestedRegions.isEmpty();
	}

	@Override
	public int getRegionCount() {
		return nestedRegions.size();
	}

	@Override
	public Region getRegion(int index) {
		return nestedRegions.get(index);
	}

	@Override
	public RegionContainer getParentRegion() {
		return parentRegion;
	}

	@Override
	public VariableMap getAttributes() {
		return attributes;
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
	public Id getId() {
		return id;
	}

	@Override
	public IdRegister getIdRegister() {
		return idRegister;
	}

	@Override
	public void setId(String id) throws InvalidIdException {
		this.id = idRegister.registerId(id, this.id);
	}

	@Override
	public void setId(Id id) throws InvalidIdException {
		idRegister.registerId(id, this.id);
		this.id = id;
	}

	@Override
	public boolean isTemporary() {
		return this.getId().toString().equals(TEMP_ID_SUFFIX);
	}

	@Override
	public void addRegion(Region region) {
		nestedRegions.add(region);
	}

	@Override
	public void removeRegion(Region region) {
		nestedRegions.remove(region);
	}

	/**
	 * User-defined attributes (text, int, decimal or boolean)
	 * @param createIfNotExists Set to true if to create an empty variable map if none exists yet.
	 * @return Variable map or <code>null</code>
	 */
	public VariableMap getUserDefinedAttributes(boolean createIfNotExists) {
		if (userDefinedAttributes == null && createIfNotExists)
			userDefinedAttributes = new VariableMap();
		return userDefinedAttributes;
	}

	/**
	 *  User-defined attributes (text, int, decimal or boolean)
	 * @param attrs Variable map
	 */
	public void setUserDefinedAttributes(VariableMap attrs) {
		userDefinedAttributes = attrs;
	}

	@Override
	public boolean hasRole(RoleType type) {
		if (roles == null)
			return false;
		return roles.containsKey(type);
	}

	@Override
	public RegionRole getRole(RoleType type) {
		if (roles == null)
			return null;
		return roles.get(type);
	}

	@Override
	public RegionRole addRole(RoleType type) {
		if (roles == null)
			roles = new HashMap<RoleType, RegionRole>();

		RegionRole role = contentFactory.createRegionRole(type);

		if (role != null)
			roles.put(type, role);

		return role;
	}

	@Override
	public void removeRole(RoleType type) {
		if (roles == null)
			return;
		roles.remove(type);
	}


	@Override
	public Labels getLabels() {
		return labels;
	}

	@Override
	public void setLabels(Labels labels) {
		this.labels = labels;
	}

	/**
	 * Returns a list of alternative images that are associated with this region
	 * @return List with image objects
	 */
	public List<AlternativeImage> getAlternativeImages() {
		if (alternativeImages == null)
			alternativeImages = new ArrayList<AlternativeImage>();
		return alternativeImages;
	}

}
