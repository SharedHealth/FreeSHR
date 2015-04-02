package org.freeshr.interfaces.encounter.ws;

import org.freeshr.infrastructure.security.UserInfo;
import org.freeshr.interfaces.encounter.ws.exceptions.BadRequest;
import org.freeshr.interfaces.encounter.ws.exceptions.ErrorInfo;
import org.freeshr.interfaces.encounter.ws.exceptions.Forbidden;
import org.freeshr.interfaces.encounter.ws.exceptions.PreconditionFailed;
import org.freeshr.interfaces.encounter.ws.exceptions.ResourceNotFound;
import org.freeshr.interfaces.encounter.ws.exceptions.UnProcessableEntity;
import org.freeshr.interfaces.encounter.ws.exceptions.Unauthorized;
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
        logger.info(String.format("ACCESS: USER=%s EMAIL=%s ACTION=%s", userInfo.getProperties().getId(), userInfo.getProperties().getEmail(), action));
    }

    @ResponseStatus(value = HttpStatus.PRECONDITION_FAILED)
    @ResponseBody
    @ExceptionHandler(PreconditionFailed.class)
    public ErrorInfo preConditionFailed(PreconditionFailed preconditionFailed) {
        logger.debug(preconditionFailed.getMessage());
        ErrorInfo errorInfo = new ErrorInfo(HttpStatus.PRECONDITION_FAILED, preconditionFailed);
        errorInfo.setErrors(preconditionFailed.getResult().getErrors());
        return errorInfo;
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ResponseBody
    @ExceptionHandler(BadRequest.class)
    public ErrorInfo badRequest(BadRequest badRequest) {
        logger.debug(badRequest.getMessage());
        return new ErrorInfo(HttpStatus.BAD_REQUEST, badRequest.getMessage());
    }

    @ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
    @ResponseBody
    @ExceptionHandler(UnProcessableEntity.class)
    public ErrorInfo unProcessableEntity(UnProcessableEntity unProcessableEntity) {
        logger.debug(unProcessableEntity.getMessage());
        ErrorInfo errorInfo = new ErrorInfo(HttpStatus.UNPROCESSABLE_ENTITY, unProcessableEntity);
        errorInfo.setErrors(unProcessableEntity.getResult().getErrors());
        return errorInfo;
    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @ResponseBody
    @ExceptionHandler(ResourceNotFound.class)
    public ErrorInfo resourceNotFound(ResourceNotFound resourceNotFound) {
        logger.debug(resourceNotFound.getMessage());
        return new ErrorInfo(HttpStatus.NOT_FOUND, resourceNotFound.getMessage());
    }

    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    @ResponseBody
    @ExceptionHandler(Unauthorized.class)
    public ErrorInfo unauthorized(Unauthorized unauthorized) {
        logger.debug(unauthorized.getMessage());
        return new ErrorInfo(HttpStatus.UNAUTHORIZED, unauthorized.getMessage());
    }

    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    @ResponseBody
    @ExceptionHandler(Forbidden.class)
    public ErrorInfo forbidden(Forbidden forbidden) {
        logger.debug(forbidden.getMessage());
        return new ErrorInfo(HttpStatus.FORBIDDEN, forbidden.getMessage());
    }

    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    @ResponseBody
    @ExceptionHandler(AccessDeniedException.class)
    public ErrorInfo accessDenied(AccessDeniedException exception) {
        logger.debug(exception.getMessage());
        return new ErrorInfo(HttpStatus.FORBIDDEN, exception.getMessage());
    }

    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    @ExceptionHandler(Exception.class)
    public ErrorInfo catchAll(Exception exception) {
        logger.debug(exception.getClass().getName() + " : " + exception.getMessage());
        return new ErrorInfo(HttpStatus.INTERNAL_SERVER_ERROR, exception.getLocalizedMessage());
    }
}
