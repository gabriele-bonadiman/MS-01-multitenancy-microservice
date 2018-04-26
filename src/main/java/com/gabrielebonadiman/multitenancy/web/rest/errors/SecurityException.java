package com.gabrielebonadiman.multitenancy.web.rest.errors;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class SecurityException extends AbstractThrowableProblem {

    private final String entityName;

    private final String errorKey;

    public SecurityException(String defaultMessage, String entityName, String errorKey) {
        this(ErrorConstants.SECURITY_TYPE, defaultMessage, entityName, errorKey);
    }

    public SecurityException(URI type, String defaultMessage, String entityName, String errorKey) {
        super(type, defaultMessage, Status.UNAUTHORIZED, null, null, null, getAlertParameters(entityName, errorKey));
        this.entityName = entityName;
        this.errorKey = errorKey;
    }

    public String getEntityName() {
        return entityName;
    }

    public String getErrorKey() {
        return errorKey;
    }

    private static Map<String, Object> getAlertParameters(String entityName, String errorKey) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("message", "error." + errorKey);
        parameters.put("params", entityName);
        return parameters;
    }
}
