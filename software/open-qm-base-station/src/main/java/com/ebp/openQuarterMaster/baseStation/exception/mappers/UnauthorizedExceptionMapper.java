package com.ebp.openQuarterMaster.baseStation.exception.mappers;

import io.quarkus.security.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.ext.Provider;

@Slf4j
@Provider
public class UnauthorizedExceptionMapper extends UiNotAuthorizedExceptionMapper<UnauthorizedException> {

}
