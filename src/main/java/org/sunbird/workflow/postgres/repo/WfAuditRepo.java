package org.sunbird.workflow.postgres.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.sunbird.workflow.postgres.entity.WfAuditEntity;

import java.util.List;

public interface WfAuditRepo extends JpaRepository<WfAuditEntity, Integer> {

    List<WfAuditEntity> findByRootOrgAndApplicationIdAndWfIdOrderByCreatedOnDesc(String rootOrg, String applicationId, String wfId);

    List<WfAuditEntity> findByRootOrgAndApplicationIdOrderByCreatedOnDesc(String rootOrg, String applicationId);

}
