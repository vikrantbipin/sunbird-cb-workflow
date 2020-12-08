package com.igot.workflow.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class RequestServiceImpl {

    Logger log = LogManager.getLogger(RequestServiceImpl.class);

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private RestTemplate restTemplate;

    public Object fetchResult(StringBuilder uri, Object request, Class<?> classType) {
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        Object response = null;
        StringBuilder str = new StringBuilder(this.getClass().getCanonicalName()).append(".fetchResult:")
                .append(System.lineSeparator());
        str.append("URI: ").append(uri.toString()).append(System.lineSeparator());
        try {
            str.append("Request: ").append(mapper.writeValueAsString(request)).append(System.lineSeparator());
            log.debug(str.toString());
            response = restTemplate.postForObject(uri.toString(), request, classType);
        } catch (HttpClientErrorException e) {
            log.error("External Service threw an Exception: ", e);
        } catch (Exception e) {
            log.error("Exception while fetching from external service: ", e);
        }
        return response;
    }

    public Object fetchResultUsingGet(StringBuilder uri) {
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        Object response = null;
        StringBuilder str = new StringBuilder(this.getClass().getCanonicalName()).append(".fetchResult:")
                .append(System.lineSeparator());
        str.append("URI: ").append(uri.toString()).append(System.lineSeparator());
        try {
            log.debug(str.toString());
            response = restTemplate.getForObject(uri.toString(), Map.class);
        } catch (HttpClientErrorException e) {
            log.error("External Service threw an Exception: ", e);
        } catch (Exception e) {
            log.error("Exception while fetching from searcher: ", e);
        }
        return response;
    }

}
