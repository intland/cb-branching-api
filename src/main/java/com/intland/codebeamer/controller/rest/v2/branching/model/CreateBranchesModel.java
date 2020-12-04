package com.intland.codebeamer.controller.rest.v2.branching.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CreateBranches")
public class CreateBranchesModel {


	@Schema(description = "List of desired branches", required = true)
	private List<CreateBranchModel> branches = new ArrayList<>(0);

	public List<CreateBranchModel> getBranches() {
		return this.branches;
	}

	public void setBranches(final List<CreateBranchModel> branches) {
		this.branches = branches;
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
