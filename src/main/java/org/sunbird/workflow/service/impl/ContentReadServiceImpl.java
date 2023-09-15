package org.sunbird.workflow.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.sunbird.workflow.config.Configuration;
import org.sunbird.workflow.config.Constants;
import org.sunbird.workflow.service.ContentReadService;

import java.util.Map;

/**
 * @author mahesh.vakkund
 */
public class ContentReadServiceImpl implements ContentReadService {

    private Logger logger = LoggerFactory.getLogger(ContentReadServiceImpl.class);

    @Autowired
    private Configuration configuration;

    @Autowired
    private RequestServiceImpl requestServiceImpl;

    /**
     * Service Method to get the service name and other details from the content-service.
     *
     * @param courseId - Blended course -courseId based on this field the content service API is called.
     * @return - returns the serviceName based on which the enrollment configuration json is fetched.
     */
    public String getServiceNameDetails(String courseId) {
        try {
            StringBuilder builder = new StringBuilder(configuration.getContentServiceHost());
            builder.append(configuration.getContentReadSearchEndPoint());
            builder.append(courseId);
            builder.append("?" + Constants.FIELDS + "=");
            builder.append(Constants.MULTILEVEL_BP_ENROLL_FIELDS);
            Map<String, Object> response = (Map<String, Object>) requestServiceImpl.fetchResultUsingGet(builder);
            if (response != null && Constants.OK.equalsIgnoreCase((String) response.get(Constants.RESPONSE_CODE))) {
                Map<String, Object> map = (Map<String, Object>) response.get(Constants.RESULT);
                if (map.get(Constants.CONTENT) != null) {
                    Map<String, Object> responseObj = (Map<String, Object>) map.get(Constants.CONTENT);
                    return (String) responseObj.get("wfApprovalType");
                }
            }
        } catch (Exception e) {
            logger.info("There is a error occured while searching for the Org details : " + e);
        }
        return null;
    }
}
