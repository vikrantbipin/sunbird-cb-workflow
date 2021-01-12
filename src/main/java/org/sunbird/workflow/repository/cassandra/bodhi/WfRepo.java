package org.sunbird.workflow.repository.cassandra.bodhi;


import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;
import org.sunbird.workflow.models.cassandra.WfPrimaryKey;
import org.sunbird.workflow.models.cassandra.Workflow;

@Repository
public interface WfRepo extends CassandraRepository<Workflow, WfPrimaryKey> {

    @Query("SELECT * FROM work_flow WHERE root_org=?0 AND org=?1 AND service=?2;")
    Workflow getWorkFlowForService(String rootOrg, String org, String service);

}
