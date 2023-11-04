package org.sunbird.workflow.service;

import java.util.Map;

/**
 * @author mahesh.vakkund
 */
public interface ContentReadService {

    /**
     * @param courseId - CourseId of the blended program.
     * @return - serviceName which is used to fetch the wf enroll configuration json.
     */
    public Map<String,Object> getServiceNameDetails(String courseId);
}
