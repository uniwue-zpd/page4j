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
package de.uniwue.zpd.io;

/**
 * Interface for format versions (for instance XML schema versions).
 *
 * @author Christian Clausner
 *
 */
public interface FormatVersion {

	public boolean equals(Object obj);

	public int hashCode();

	/**
	 * Checks if this format version is newer than the specified other version.
	 */
	public boolean isNewerThan(FormatVersion otherVersion);

	/**
	 * Checks if this format version is older than the specified other version.
	 */
	public boolean isOlderThan(FormatVersion otherVersion);
}
