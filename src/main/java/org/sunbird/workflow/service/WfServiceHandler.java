package org.sunbird.workflow.service;

import org.sunbird.workflow.models.WfRequest;

public interface WfServiceHandler {
	public void processMessage(WfRequest wfRequest);
}
