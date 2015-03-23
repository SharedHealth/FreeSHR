package org.freeshr.interfaces.encounter.ws;

import org.freeshr.application.fhir.EncounterResponse;
import org.freeshr.infrastructure.security.UserInfo;
import org.freeshr.interfaces.encounter.ws.exceptions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

// Base controller for SHR - get user info, log access and get Error responses for free
public class ShrController {
    private static final Logger logger = LoggerFactory.getLogger(ShrController.class);

    protected UserInfo getUserInfo() {
        return (UserInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    protected void logAccessDetails(UserInfo userInfo, String action) {
        logger.info(String.format("ACCESS: USER=%s TYPE=%s ACTION=%s", userInfo.getId(), userInfo.getName(), action));
    }

    @ResponseStatus(value = HttpStatus.PRECONDITION_FAILED)
    @ResponseBody
    @ExceptionHandler(PreconditionFailed.class)
    public EncounterResponse preConditionFailed(PreconditionFailed preconditionFailed) {
        return preconditionFailed.getResult();
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ResponseBody
    @ExceptionHandler(BadRequest.class)
    public ErrorInfo badRequest(BadRequest badRequest) {
        return new ErrorInfo(HttpStatus.BAD_REQUEST, badRequest.getMessage());
    }

    @ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
    @ResponseBody
    @ExceptionHandler(UnProcessableEntity.class)
    public EncounterResponse unProcessableEntity(UnProcessableEntity unProcessableEntity) {
        return unProcessableEntity.getResult();
    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @ResponseBody
    @ExceptionHandler(ResourceNotFound.class)
    public ErrorInfo resourceNotFound(ResourceNotFound resourceNotFound) {
        return new ErrorInfo(HttpStatus.NOT_FOUND, resourceNotFound.getMessage());
    }

    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    @ResponseBody
    @ExceptionHandler(Unauthorized.class)
    public ErrorInfo unauthorized(Unauthorized unauthorized) {
        return new ErrorInfo(HttpStatus.UNAUTHORIZED, unauthorized.getMessage());
    }

    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    @ResponseBody
    @ExceptionHandler(Forbidden.class)
    public ErrorInfo forbidden(Forbidden forbidden) {
        return new ErrorInfo(HttpStatus.FORBIDDEN, forbidden.getMessage());
    }

    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    @ExceptionHandler(Exception.class)
    public ErrorInfo catchAll(Exception exception) {
        return new ErrorInfo(HttpStatus.INTERNAL_SERVER_ERROR, exception.getLocalizedMessage());
    }
}
