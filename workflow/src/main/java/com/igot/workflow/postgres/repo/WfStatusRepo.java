package com.igot.workflow.postgres.repo;


import com.igot.workflow.postgres.entity.WfStatusEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WfStatusRepo extends JpaRepository<WfStatusEntity, String> {

    WfStatusEntity findByRootOrgAndOrgAndApplicationIdAndWfId(String rootOrg, String org, String userId, String wfId);


    Page<WfStatusEntity> findByRootOrgAndOrgAndServiceNameAndCurrentStatus(String rootOrg, String org, String servicename , String status, Pageable pageable);

}
