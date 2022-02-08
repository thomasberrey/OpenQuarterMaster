package com.ebp.openQuarterMaster.baseStation.endpoints.user;

import com.ebp.openQuarterMaster.baseStation.testResources.data.ExternalTestUserService;
import com.ebp.openQuarterMaster.baseStation.testResources.data.InternalTestUserService;
import com.ebp.openQuarterMaster.baseStation.testResources.lifecycleManagers.TestResourceLifecycleManager;
import com.ebp.openQuarterMaster.baseStation.testResources.testClasses.RunningServerTest;
import com.ebp.openQuarterMaster.lib.core.Utils;
import com.ebp.openQuarterMaster.lib.core.rest.ErrorMessage;
import com.ebp.openQuarterMaster.lib.core.rest.user.TokenCheckResponse;
import com.ebp.openQuarterMaster.lib.core.rest.user.UserGetResponse;
import com.ebp.openQuarterMaster.lib.core.rest.user.UserLoginRequest;
import com.ebp.openQuarterMaster.lib.core.rest.user.UserLoginResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.Response;

import static com.ebp.openQuarterMaster.baseStation.testResources.TestRestUtils.setupJwtCall;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@QuarkusIntegrationTest
@QuarkusTestResource(TestResourceLifecycleManager.class)
@TestHTTPEndpoint(Auth.class)
class AuthTest extends RunningServerTest {
    ExternalTestUserService testUserService = ExternalTestUserService.fromStaticProps();
    ObjectMapper objectMapper = Utils.OBJECT_MAPPER;

    @Test
    public void testBadLoginNoUser() throws JsonProcessingException {
        UserGetResponse testUser = this.testUserService.getTestUser(false, true);

        UserLoginRequest ulr = new UserLoginRequest("bad", "login", true);
        ErrorMessage errorMessage = given()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(ulr))
                .when()
                .post()
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .extract().body().as(ErrorMessage.class);

        log.info("Error Message: {}", errorMessage);
        assertEquals("User not found.", errorMessage.getError());
    }

    @Test
    public void testBadLoginBadPass() throws JsonProcessingException {
        UserGetResponse testUser = this.testUserService.getTestUser(false, true);

        UserLoginRequest ulr = new UserLoginRequest(testUser.getEmail(), "badPass", true);
        ErrorMessage errorMessage = given()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(ulr))
                .when()
                .post()
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .extract().body().as(ErrorMessage.class);

        log.info("Error Message: {}", errorMessage);
        assertEquals("Invalid Password.", errorMessage.getError());
    }

    @Test
    public void testLogin() throws JsonProcessingException {
        UserGetResponse testUser = this.testUserService.getTestUser(false, true);

        UserLoginRequest ulr = new UserLoginRequest(testUser.getEmail(), testUser.getAttributes().get(InternalTestUserService.TEST_PASSWORD_ATT_KEY), true);
        UserLoginResponse loginResponse = given()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(ulr))
                .when()
                .post()
                .then()
                .statusCode(Response.Status.ACCEPTED.getStatusCode())
                .extract().body().as(UserLoginResponse.class);

        assertNotNull(loginResponse);
        assertFalse(loginResponse.getToken().isBlank());
        assertNotNull(loginResponse.getExpires());

        ValidatableResponse response = setupJwtCall(
                given(),
                loginResponse.getToken()
        )
                .contentType(ContentType.JSON)
                .when()
                .get("/tokenCheck")
                .then();
        log.info("token check response: {}", response.extract().body().asString());

        response.statusCode(Response.Status.OK.getStatusCode());
        TokenCheckResponse tokenCheckResponse = response.extract().body().as(TokenCheckResponse.class);

        log.info("Token Check Response: {}", tokenCheckResponse);
    }
}