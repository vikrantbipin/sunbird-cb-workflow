package org.sunbird.workflow.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.sunbird.workflow.config.Configuration;
import org.sunbird.workflow.config.Constants;
import org.sunbird.workflow.exception.BadRequestException;
import org.sunbird.workflow.models.Response;
import org.sunbird.workflow.models.SearchCriteria;
import org.sunbird.workflow.models.WfRequest;
import org.sunbird.workflow.postgres.entity.WfStatusEntity;
import org.sunbird.workflow.postgres.repo.WfStatusRepo;
import org.sunbird.workflow.service.DomainWhiteListWorkFlowService;
import org.sunbird.workflow.service.UserProfileWfService;
import org.sunbird.workflow.service.Workflowservice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DomainWhiteListWorkFlowServiceImpl implements DomainWhiteListWorkFlowService {

    @Autowired
    private Workflowservice workflowService;

    @Autowired
    private Configuration configuration;

    @Autowired
    private WfStatusRepo wfStatusRepo;

    @Autowired
    private UserProfileWfService userProfileWfService;

    @Override
    public Response createDomainWorkFlow(String rootOrg, String org, WfRequest wfRequest) {
        Response response = workflowService.workflowTransition(rootOrg, org, wfRequest);
        return response;
    }

    @Override
    public Response updateDomainWorkFlow(String rootOrg, String org, WfRequest wfRequest) {
        Response response = workflowService.workflowTransition(rootOrg, org, wfRequest);
        return response;
    }

    @Override
    public Response readDomainWFApplication(String rootOrg, String org, String wfId, String applicationId) {
        Response response = workflowService.getWfApplication(rootOrg, org, wfId, applicationId);
        return response;
    }

    @Override
    public Response domainSearch(String rootOrg, String org, SearchCriteria criteria) {
        Response response = applicationSearchOnApplicationIdGroup(criteria, rootOrg);
        return response;
    }

    public Response applicationSearchOnApplicationIdGroup(SearchCriteria criteria, String rootOrg) {
        Pageable pageable = getPageReqForApplicationSearch(criteria);
        List<String> applicationIds = criteria.getApplicationIds();
        Map<String, List<WfStatusEntity>> infos = null;
        if (CollectionUtils.isEmpty(applicationIds)) {
            applicationIds = wfStatusRepo.getListOfDistinctApplicationUsingDept(criteria.getServiceName(),
                    criteria.getApplicationStatus(), criteria.getDeptName(), pageable);
        }
        List<WfStatusEntity> wfStatusEntities = null;
        wfStatusEntities = wfStatusRepo.findByServiceNameAndCurrentStatusAndDeptNameAndApplicationIdIn(
                criteria.getServiceName(), criteria.getApplicationStatus(), criteria.getDeptName(), applicationIds);

        infos = wfStatusEntities.stream().collect(Collectors.groupingBy(WfStatusEntity::getApplicationId));
        Response response = new Response();
        response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
        response.put(Constants.DATA, infos);
        response.put(Constants.STATUS, HttpStatus.OK);
        return getResponse(rootOrg, response);
    }

    private Pageable getPageReqForApplicationSearch(SearchCriteria criteria) {
        Pageable pageable;
        if (criteria.isEmpty()) {
            throw new BadRequestException(Constants.SEARCH_CRITERIA_VALIDATION);
        }
        Integer limit = configuration.getDefaultLimit();
        Integer offset = configuration.getDefaultOffset();
        if (criteria.getLimit() == null && criteria.getOffset() == null)
            limit = configuration.getMaxLimit();
        if (criteria.getLimit() != null && criteria.getLimit() <= configuration.getDefaultLimit())
            limit = criteria.getLimit();
        if (criteria.getLimit() != null && criteria.getLimit() > configuration.getDefaultLimit())
            limit = configuration.getDefaultLimit();
        if (criteria.getOffset() != null)
            offset = criteria.getOffset();
        pageable = PageRequest.of(offset, limit);
        return pageable;
    }

    private Response getResponse(String rootOrg, Response wfApplicationSearchResponse) {
        Response response;
        List<Map<String, Object>> userProfiles = userProfileWfService.enrichUserData(
                (Map<String, List<WfStatusEntity>>) wfApplicationSearchResponse.get(Constants.DATA), rootOrg);
        response = new Response();
        response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
        response.put(Constants.DATA, userProfiles);
        response.put(Constants.STATUS, HttpStatus.OK);
        return response;
    }
}
