package com.ebp.openQuarterMaster.baseStation.endpoints.user;

import com.ebp.openQuarterMaster.baseStation.data.pojos.*;
import com.ebp.openQuarterMaster.baseStation.endpoints.EndpointProvider;
import com.ebp.openQuarterMaster.baseStation.service.JwtService;
import com.ebp.openQuarterMaster.baseStation.service.PasswordService;
import com.ebp.openQuarterMaster.baseStation.service.mongo.UserService;
import com.ebp.openQuarterMaster.baseStation.utils.AuthMode;
import com.ebp.openQuarterMaster.baseStation.utils.TimeUtils;
import io.quarkus.security.identity.SecurityIdentity;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.eclipse.microprofile.opentracing.Traced;

import javax.annotation.security.PermitAll;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.Date;

@Traced
@Slf4j
@Path("/user/auth")
@Tags({@Tag(name = "User Auth", description = "Endpoints for user authorization.")})
@RequestScoped
public class Auth extends EndpointProvider {
    @Inject
    UserService userService;

    @Inject
    PasswordService passwordService;

    @Inject
    JwtService jwtService;

    @ConfigProperty(name = "service.authMode")
    AuthMode authMode;

    @Inject
    JsonWebToken jwt;
    @Inject
    SecurityIdentity identity;


    private void assertSelfAuthMode() {
        if (!AuthMode.SELF.equals(this.authMode)) {
            throw new ForbiddenException("Service not set to authenticate its own users.");
        }
    }

    @POST
    @Operation(
            summary = "Authenticates a user"
    )
    @APIResponse(
            responseCode = "202",
            description = "User was logged in.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserLoginResponse.class)
            )
    )
    @APIResponse(
            responseCode = "401",
            description = "Incorrect credentials given.",
            content = @Content(mediaType = "text/plain")
    )
    @APIResponse(
            responseCode = "403",
            description = "If the account has been locked.",
            content = @Content(mediaType = "text/plain")
    )
    @APIResponse(
            responseCode = "429",
            description = "Happens when too many requests to login were sent in a given time period.",
            content = @Content(mediaType = "text/plain")
    )
    @PermitAll
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response authenticateUser(
            @Context SecurityContext securityContext,
            @Valid UserLoginRequest loginRequest
    ) {
        logRequestContext(this.jwt, securityContext);
        this.assertSelfAuthMode();
        log.info("Authenticating user.");

        User user = this.userService.getFromLoginRequest(loginRequest);

        if (user == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorMessage("User not found.")).build();
        }

        if (!this.passwordService.passwordMatchesHash(user, loginRequest)) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorMessage("Invalid Password.")).build();
        }

        //TODO:: additional checks on locked status, etc

        log.info("User {} authenticated, generating token and returning.", user.getId());

        return Response.status(Response.Status.ACCEPTED)
                .entity(this.jwtService.getUserJwt(user, false))
                .build();
    }

    @GET
    @Path("tokenCheck")
    @Operation(
            summary = "Checks a users' token."
    )
    @APIResponse(
            responseCode = "200",
            description = "The check happened.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TokenCheckResponse.class)
            )
    )
    @SecurityRequirement(name = "JwtAuth")
    @PermitAll
    @Produces(MediaType.APPLICATION_JSON)
    public Response tokenCheck(@Context SecurityContext ctx) {
        logRequestContext(this.jwt, ctx);
        log.info("Checking user's token.");
        this.assertSelfAuthMode();

        TokenCheckResponse response = new TokenCheckResponse();
        if (jwt.getRawToken() != null) {
            log.info("User roles: {}", this.identity.getRoles());
            log.info("User JWT claims: {}", this.jwt.getClaimNames());

            response.setHadToken(true);
            response.setTokenSecure(ctx.isSecure());
            response.setExpired(jwt.getExpirationTime() <= TimeUtils.currentTimeInSecs());
            response.setExpirationDate(new Date(jwt.getExpirationTime()));
        }
        return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON_TYPE).entity(response).build();
    }
}