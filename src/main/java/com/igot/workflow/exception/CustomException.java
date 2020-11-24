package com.igot.workflow.exception;

import java.util.Map;

public class CustomException extends RuntimeException {

    private static final long serialVersionUID = 8859144435338793971L;

    private String code;
    private String message;
    private Map<String, String> errors;

    public CustomException() {
        super();
    }

    public CustomException(String code, String message) {
        super();
        this.code = code;
        this.message = message;
    }

    public CustomException(Map<String, String> errors) {
        super();
        this.message = errors.toString();
        this.errors = errors;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, String> errors) {
        this.errors = errors;
    }
}