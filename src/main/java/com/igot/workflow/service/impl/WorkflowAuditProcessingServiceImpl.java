package com.igot.workflow.service.impl;

import com.igot.workflow.models.WfRequest;
import com.igot.workflow.postgres.entity.WfAuditEntity;
import com.igot.workflow.postgres.entity.WfStatusEntity;
import com.igot.workflow.postgres.repo.WfAuditRepo;
import com.igot.workflow.postgres.repo.WfStatusRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class WorkflowAuditProcessingServiceImpl {

    @Autowired
    private WfAuditRepo wfAuditRepo;

    @Autowired
    private WfStatusRepo wfStatusRepo;

    public void createAudit(WfRequest wfRequest){
        WfStatusEntity wfStatusEntity = wfStatusRepo.findByApplicationIdAndWfId(wfRequest.getApplicationId(), wfRequest.getWfId());
        WfAuditEntity wfAuditEntity = new WfAuditEntity();
        wfAuditEntity.setActorUUID(wfRequest.getActorUserId());
        wfAuditEntity.setComment(wfRequest.getComment());
        wfAuditEntity.setCreatedOn(new Date());
        wfAuditEntity.setAction(wfRequest.getAction());
        wfAuditEntity.setState(wfRequest.getState());
        wfAuditEntity.setRootOrg(wfStatusEntity.getRootOrg());
        wfAuditEntity.setUserId(wfRequest.getUserId());
        wfAuditEntity.setInWorkflow(wfStatusEntity.getInWorkflow());
        wfAuditEntity.setWfId(wfRequest.getWfId());
        wfAuditEntity.setApplicationId(wfRequest.getApplicationId());
        wfAuditEntity.setServiceName(wfRequest.getServiceName());
        wfAuditEntity.setUpdateFieldValues(wfStatusEntity.getUpdateFieldValues());
        wfAuditRepo.save(wfAuditEntity);
    }
}
