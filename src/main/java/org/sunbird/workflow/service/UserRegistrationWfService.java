package org.sunbird.workflow.service;

import org.sunbird.workflow.models.WfRequest;

public interface UserRegistrationWfService {

	public void processMessage(WfRequest wfRequest);
}
