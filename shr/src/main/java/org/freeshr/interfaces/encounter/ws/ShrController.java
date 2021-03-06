package org.freeshr.interfaces.encounter.ws;

import org.freeshr.infrastructure.security.UserInfo;
import org.freeshr.interfaces.encounter.ws.exceptions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
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
        logger.info(String.format("ACCESS: EMAIL=%s ACTION=%s", userInfo.getProperties().getEmail(), action));
    }

    @ResponseStatus(value = HttpStatus.PRECONDITION_FAILED)
    @ResponseBody
    @ExceptionHandler(PreconditionFailed.class)
    public ErrorInfo preConditionFailed(PreconditionFailed preconditionFailed) {
        logger.error(preconditionFailed.getMessage());
        ErrorInfo errorInfo = new ErrorInfo(HttpStatus.PRECONDITION_FAILED, preconditionFailed);
        errorInfo.setErrors(preconditionFailed.getResult().getErrors());
        return errorInfo;
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ResponseBody
    @ExceptionHandler(BadRequest.class)
    public ErrorInfo badRequest(BadRequest badRequest) {
        logger.error(badRequest.getMessage());
        return new ErrorInfo(HttpStatus.BAD_REQUEST, badRequest.getMessage());
    }

    @ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
    @ResponseBody
    @ExceptionHandler(UnProcessableEntity.class)
    public ErrorInfo unProcessableEntity(UnProcessableEntity unProcessableEntity) {
        logger.error(unProcessableEntity.getMessage());
        ErrorInfo errorInfo = new ErrorInfo(HttpStatus.UNPROCESSABLE_ENTITY, unProcessableEntity);
        errorInfo.setErrors(unProcessableEntity.getResult().getErrors());
        return errorInfo;
    }

    @ResponseStatus(value = HttpStatus.PERMANENT_REDIRECT)
    @ResponseBody
    @ExceptionHandler(Redirect.class)
    public ErrorInfo redirect(Redirect exception) {
        logger.warn(exception.getMessage());
        return new ErrorInfo(HttpStatus.PERMANENT_REDIRECT, exception.getMessage());
    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @ResponseBody
    @ExceptionHandler(ResourceNotFound.class)
    public ErrorInfo resourceNotFound(ResourceNotFound resourceNotFound) {
        logger.error(resourceNotFound.getMessage());
        return new ErrorInfo(HttpStatus.NOT_FOUND, resourceNotFound.getMessage());
    }

    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    @ResponseBody
    @ExceptionHandler(Unauthorized.class)
    public ErrorInfo unauthorized(Unauthorized unauthorized) {
        logger.error(unauthorized.getMessage());
        return new ErrorInfo(HttpStatus.UNAUTHORIZED, unauthorized.getMessage());
    }

    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    @ResponseBody
    @ExceptionHandler(Forbidden.class)
    public ErrorInfo forbidden(Forbidden forbidden) {
        logger.error(forbidden.getMessage());
        return new ErrorInfo(HttpStatus.FORBIDDEN, forbidden.getMessage());
    }

    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    @ResponseBody
    @ExceptionHandler(AccessDeniedException.class)
    public ErrorInfo accessDenied(AccessDeniedException exception) {
        logger.error(exception.getMessage());
        return new ErrorInfo(HttpStatus.FORBIDDEN, exception.getMessage());
    }

    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    @ExceptionHandler(Exception.class)
    public ErrorInfo catchAll(Exception exception) {
        logger.error(exception.getMessage());
        return new ErrorInfo(HttpStatus.INTERNAL_SERVER_ERROR, exception.getLocalizedMessage());
    }
}
