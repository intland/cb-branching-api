package com.intland.codebeamer.controller.rest.v2.branching;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriTemplate;

import com.google.common.collect.Multimap;
import com.intland.codebeamer.controller.rest.v2.AbstractRestController;
import com.intland.codebeamer.controller.rest.v2.AbstractUserAwareRestController;
import com.intland.codebeamer.controller.rest.v2.branching.model.CreateBranchModel;
import com.intland.codebeamer.controller.rest.v2.branching.model.CreateBranchesModel;
import com.intland.codebeamer.controller.rest.v2.converter.reference.TrackerReferenceConverter;
import com.intland.codebeamer.controller.rest.v2.exception.BadRequestException;
import com.intland.codebeamer.controller.rest.v2.exception.ResourceForbiddenException;
import com.intland.codebeamer.controller.rest.v2.exception.ResourceNotFoundException;
import com.intland.codebeamer.controller.rest.v2.exception.ResourceUnauthorizedException;
import com.intland.codebeamer.controller.rest.v2.model.base.IdentifiableModel;
import com.intland.codebeamer.controller.rest.v2.model.reference.TrackerReferenceModel;
import com.intland.codebeamer.controller.rest.v2.support.TrackerRestSupport;
import com.intland.codebeamer.controller.support.branch.BranchReferenceModel;
import com.intland.codebeamer.controller.support.branch.BranchSupport;
import com.intland.codebeamer.controller.support.branch.CreateBranchParameterDto;
import com.intland.codebeamer.controller.support.branch.CreateBranchParameters;
import com.intland.codebeamer.controller.support.branch.jobs.BackgroundBranchCreator;
import com.intland.codebeamer.manager.AccessRightsException;
import com.intland.codebeamer.persistence.dao.BranchDao;
import com.intland.codebeamer.persistence.dao.impl.EntityCache;
import com.intland.codebeamer.persistence.dto.BranchDto;
import com.intland.codebeamer.persistence.dto.ProjectDto;
import com.intland.codebeamer.persistence.dto.ProjectPermission;
import com.intland.codebeamer.persistence.dto.TrackerDto;
import com.intland.codebeamer.persistence.dto.TrackerLayoutLabelDto;
import com.intland.codebeamer.persistence.dto.UserDto;
import com.intland.codebeamer.persistence.dto.UserPermission;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@OpenAPIDefinition
@RestController
@RequestMapping(AbstractRestController.API_URI_V3)
@Validated
public class BranchRestController extends AbstractUserAwareRestController {
	private static final String CREATE_URI = "create-branches";
	private static final String GET_BRANCHES_URI = "trackers/{trackerId}/branches";

	@Autowired
	private BranchSupport branchSupport;

	@Autowired
	private BackgroundBranchCreator branchCreator;

	@Autowired
	private TrackerRestSupport trackerRestSupport;

	@Autowired
	private BranchDao branchDao;

	@Autowired
	private TrackerReferenceConverter trackerReferenceConverter;

	@Operation(summary = "Creates branches asynchronously", tags = "Branches")
	@ApiResponses({
			@ApiResponse(responseCode = "202", description = "Branch creation process has started"),
			@ApiResponse(responseCode = "401", description = "Authorization required"),
			@ApiResponse(responseCode = "403", description = "Access denied for one of the resources"),
			@ApiResponse(responseCode = "404", description = "One of the resources is not found"),
	})
	@RequestMapping(
			value = BranchRestController.CREATE_URI,
			method = RequestMethod.POST,
			consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE
	)
	@ResponseBody
	public ResponseEntity<Void> asyncCreateBranches(@RequestBody final CreateBranchesModel model, final HttpServletRequest request)
			throws ResourceUnauthorizedException, ResourceForbiddenException, ResourceNotFoundException, BadRequestException {
		final String uri = BranchRestController.CREATE_URI;
		final UserDto user = this.checkUserHasPermission(uri);

		if(!branchSupport.hasBranchingLicense()) {
			throw new ResourceForbiddenException("Missing branching license.", uri);
		}

		this.branchCreator.createMultipleBranchesInBackground(request, user, this.prepareParameters(user, model), false);

		return ResponseEntity.status(HttpStatus.ACCEPTED).body(null);
	}

	@Operation(summary = "Fetches branches of a tracker", tags = "Branches")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "List of branches as TrackerReferences"),
			@ApiResponse(responseCode = "401", description = "Authorization required"),
			@ApiResponse(responseCode = "403", description = "Access denied for one of the resources"),
			@ApiResponse(responseCode = "404", description = "Tracker not found"),
	})
	@RequestMapping(
			value = BranchRestController.GET_BRANCHES_URI,
			method = RequestMethod.GET,
			produces = MediaType.APPLICATION_JSON_VALUE
	)
	@ResponseBody
	public List<TrackerReferenceModel> getBranches(@PathVariable("trackerId") final Integer trackerId)
			throws ResourceUnauthorizedException, ResourceForbiddenException, ResourceNotFoundException {
		final String uri = new UriTemplate(BranchRestController.GET_BRANCHES_URI).expand(trackerId).getPath();
		final UserDto user = this.checkUserHasPermission(uri);

		this.trackerRestSupport.findTracker(trackerId, user);

		return this.branchDao.findByTrackers(user, Collections.singletonList(trackerId)).stream()
				.map(this.trackerReferenceConverter::convert)
				.collect(Collectors.toList());
	}

	private List<CreateBranchParameterDto> prepareParameters(final UserDto user, final CreateBranchesModel model)
			throws ResourceNotFoundException, BadRequestException, ResourceForbiddenException {
		final List<CreateBranchParameterDto> result = new ArrayList<>(model.getBranches().size());

		final Set<Integer> trackerIds = model.getBranches().stream()
				.map(CreateBranchModel::getSource)
				.map(IdentifiableModel::getId)
				.collect(Collectors.toSet());

		for (final CreateBranchModel branchModel : model.getBranches()) {
			final TrackerDto tracker = this.trackerRestSupport.findTracker(branchModel.getSource().getId(), user);
			if (tracker.isA(BranchSupport.NON_BRANCHABLE_TYPES)) {
				throw new BadRequestException("Branch creation is not supported for this type of tracker: " + tracker.getId());
			}
			checkBranchingPermission(user, tracker);
			final BranchDto branchDto = this.createBranchDto(branchModel, tracker);
			final Map<Integer, List<BranchDto>> incomingReferencesToRewriteWithNewBranches =
					this.getIncomingReferencesToRewrite(user, trackerIds, tracker, branchDto);
			final CreateBranchParameters params =
					this.createCreateBranchParameters(branchModel, incomingReferencesToRewriteWithNewBranches);

			result.add(this.createCreateBranchParameterDto(tracker, branchDto, params));
		}

		return result;
	}


	private void checkBranchingPermission(final UserDto user, final TrackerDto tracker) throws ResourceForbiddenException {
		ProjectDto project = tracker.getProject();
		boolean hasBranchAdminPermission = EntityCache.getInstance(user).isProjectAdmin(project.getId()) ||
				EntityCache.getInstance(user).hasPermission(project, ProjectPermission.branch_admin);
		if(!hasBranchAdminPermission) {
			throw new ResourceForbiddenException(String.format("No permission to create branches in project %s.", project.getId()), CREATE_URI);
		}
	}

	private CreateBranchParameters createCreateBranchParameters(
			final CreateBranchModel branchModel, final Map<Integer, List<BranchDto>> incomingReferencesToRewriteWithNewBranches) {
		final BranchReferenceModel refModel = new BranchReferenceModel();
		refModel.setReplaceIncomingReferences(true);
		refModel.setIncomingReferencesToRewriteWithNewBranches(incomingReferencesToRewriteWithNewBranches);

		final CreateBranchParameters params = new CreateBranchParameters();
		params.setBaselineId(branchModel.getBaselineId());
		params.setBranchReferenceModel(refModel);
		params.setInheritance(branchModel.getPermissionInheritance());
		return params;
	}

	private CreateBranchParameterDto createCreateBranchParameterDto(
			final TrackerDto tracker, final BranchDto branchDto, final CreateBranchParameters params) {
		final CreateBranchParameterDto param = new CreateBranchParameterDto();
		param.setSource(tracker);
		param.setBranchParam(branchDto);
		param.setParameters(params);
		return param;
	}

	private Map<Integer, List<BranchDto>> getIncomingReferencesToRewrite(
			final UserDto user,
			final Set<Integer> involvedTrackerIds,
			final TrackerDto tracker,
			final BranchDto branchDto) {
		final Map<Integer, List<BranchDto>> result = new HashMap<>();

		final Multimap<TrackerDto, TrackerLayoutLabelDto> referringTrackers = this.branchSupport
				.getIncomingReferenceFields(user, tracker, object -> {
					// get only the referring trackers that are among the selected ones
					final TrackerLayoutLabelDto field = (TrackerLayoutLabelDto) object;
					return field != null && involvedTrackerIds.contains(field.getTrackerId());
				});

		// we need to rewrite all these references, so create the reference config accordingly
		for (final Map.Entry<TrackerDto, Collection<TrackerLayoutLabelDto>> entry : referringTrackers.asMap().entrySet()) {
			for (final TrackerLayoutLabelDto field : entry.getValue()) {
				// put the new branch for each tracker in the list where the key is the field
				if (!result.containsKey(field.getId())) {
					result.put(field.getId(), new ArrayList<>());
				}

				result.get(field.getId()).add(branchDto);
			}
		}

		return result;
	}

	private BranchDto createBranchDto(final CreateBranchModel branch, final TrackerDto tracker) throws BadRequestException {
		final BranchDto result = new BranchDto();
		result.setName(branch.getName());
		result.setKeyName(branch.getKeyName());
		result.setColor(validate(branch.getColor()));
		result.setDescription(branch.getDescription());
		result.setProject(tracker.getProject());
		return result;
	}


	private String validate(String color) throws BadRequestException {
		if(isNotEmpty(color)) {
			try {
				Color.decode(color);
			} catch (Exception e) {
				throw new BadRequestException("Invalid color: " + e.toString());
			}
		}
		return color;
	}

}
