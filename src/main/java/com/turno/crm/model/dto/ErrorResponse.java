package com.turno.crm.model.dto;

import java.util.Map;

public class ErrorResponse {

    private ErrorBody error;

    public ErrorResponse() {}

    public ErrorResponse(ErrorBody error) {
        this.error = error;
    }

    public ErrorBody getError() {
        return error;
    }

    public void setError(ErrorBody error) {
        this.error = error;
    }

    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(new ErrorBody(code, message, null, null));
    }

    public static ErrorResponse of(String code, String message, Object details) {
        return new ErrorResponse(new ErrorBody(code, message, details, null));
    }

    public static ErrorResponse of(String code, String message, Map<String, String> fields) {
        return new ErrorResponse(new ErrorBody(code, message, null, fields));
    }

    public static class ErrorBody {

        private String code;
        private String message;
        private Object details;
        private Map<String, String> fields;

        public ErrorBody() {}

        public ErrorBody(String code, String message, Object details, Map<String, String> fields) {
            this.code = code;
            this.message = message;
            this.details = details;
            this.fields = fields;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Object getDetails() {
            return details;
        }

        public void setDetails(Object details) {
            this.details = details;
        }

        public Map<String, String> getFields() {
            return fields;
        }

        public void setFields(Map<String, String> fields) {
            this.fields = fields;
        }
    }
}
