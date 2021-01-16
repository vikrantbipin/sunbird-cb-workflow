package com.igot.workflow.postgres.repo;


import com.igot.workflow.postgres.entity.WfStatusEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WfStatusRepo extends JpaRepository<WfStatusEntity, String> {

    WfStatusEntity findByRootOrgAndOrgAndApplicationIdAndWfId(String rootOrg, String org, String applicationId, String wfId);

    Page<WfStatusEntity> findByRootOrgAndOrgAndServiceNameAndCurrentStatus(String rootOrg, String org, String servicename , String status, Pageable pageable);

    WfStatusEntity findByApplicationIdAndWfId(String applicationId, String wfId);

    WfStatusEntity findByRootOrgAndWfId(String rootOrg, String wfId);

    @Query(value = "select application_id from wingspan.wf_status where root_org= ?1 and service_name = ?2 and current_status = ?3 group by application_id", countQuery = "select count(application_id) from wingspan.wf_status where root_org= ?1 and service_name = ?2 and current_status = ?3 group by application_id", nativeQuery = true)
    List<String> getListOfDistinctApplication(String rootOrg, String serviceName, String currentStatus, Pageable pageable);

    @Query(value = "select application_id from wingspan.wf_status where root_org= ?1 and service_name = ?2 and current_status = ?3 and dept_name = ?4 group by application_id", countQuery = "select count(application_id) from wingspan.wf_status where root_org= ?1 and service_name = ?2 and current_status = ?3 group by application_id", nativeQuery = true)
    List<String> getListOfDistinctApplicationUsingDept(String rootOrg, String serviceName, String currentStatus, String deptName, Pageable pageable);

    List<WfStatusEntity> findByServiceNameAndCurrentStatusAndApplicationIdIn(String serviceName, String currentStatus, List<String> applicationId);

}
