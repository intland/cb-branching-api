/**
 * Copyright 2020 Intland Software GmbH
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 
package com.intland.codebeamer.controller.rest.v2.branching.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.intland.codebeamer.controller.rest.v2.model.reference.TrackerReferenceModel;
import com.intland.codebeamer.controller.support.branch.TrackerBranchPermissionInheritance;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CreateBranch")
public class CreateBranchModel {

	@Schema(description = "Tracker to create branches from", required = true)
	private TrackerReferenceModel source;

	@Schema(description = "Baseline of source", example = "1234")
	private Integer baselineId;

	@Schema(description = "Name of the new branch", example = "1.2")
	private String name;

	@Schema(description = "Key name of the new branch", example = "PROD \u00BB 1.2")
	private String keyName;

	@Schema(description = "Color of the new branch (#RRGGBB)", example = "#ffab46")
	private String color;

	@Schema(description = "Description of the new branch")
	private String description;

	@Schema(description = "Method to set up permissions")
	private TrackerBranchPermissionInheritance permissionInheritance;

	public TrackerReferenceModel getSource() {
		return this.source;
	}

	public void setSource(final TrackerReferenceModel source) {
		this.source = source;
	}

	public Integer getBaselineId() {
		return this.baselineId;
	}

	public void setBaselineId(final Integer baseline) {
		this.baselineId = baseline;
	}

	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getKeyName() {
		return this.keyName;
	}

	public void setKeyName(final String keyName) {
		this.keyName = keyName;
	}

	public String getColor() {
		return this.color;
	}

	public void setColor(final String color) {
		this.color = color;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public TrackerBranchPermissionInheritance getPermissionInheritance() {
		return this.permissionInheritance;
	}

	public void setPermissionInheritance(final TrackerBranchPermissionInheritance permissionInheritance) {
		this.permissionInheritance = permissionInheritance;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof CreateBranchesModel) {
			return EqualsBuilder.reflectionEquals(this, obj);
		}
		return false;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
