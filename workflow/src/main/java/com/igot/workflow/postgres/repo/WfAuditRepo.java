package com.igot.workflow.postgres.repo;

import com.igot.workflow.postgres.entity.WfAuditEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WfAuditRepo extends JpaRepository<WfAuditEntity, Integer> {

    List<WfAuditEntity> findByRootOrgAndApplicationIdAndWfId(String rootOrg, String userId, String wfId);

    List<WfAuditEntity> findByRootOrgAndApplicationId(String rootOrg, String userId);

}
