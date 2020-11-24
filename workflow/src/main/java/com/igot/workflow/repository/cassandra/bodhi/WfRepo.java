package com.igot.workflow.repository.cassandra.bodhi;


import com.igot.workflow.models.cassandra.WfPrimaryKey;
import com.igot.workflow.models.cassandra.Workflow;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface WfRepo extends CassandraRepository<Workflow, WfPrimaryKey> {

    @Query("SELECT * FROM work_flow WHERE root_org=?0 AND org=?1 AND service=?2;")
    Workflow getWorkFlowForService(String rootOrg, String org, String service);

}
