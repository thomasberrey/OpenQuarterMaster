package com.ebp.openQuarterMaster.baseStation.testResources.data;

import com.ebp.openQuarterMaster.baseStation.utils.AuthMode;
import com.ebp.openQuarterMaster.lib.core.rest.user.UserCreateRequest;
import com.ebp.openQuarterMaster.lib.core.rest.user.UserGetResponse;
import com.ebp.openQuarterMaster.lib.core.rest.user.UserLoginRequest;
import com.ebp.openQuarterMaster.lib.core.rest.user.UserLoginResponse;
import com.ebp.openQuarterMaster.lib.core.user.User;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.Response;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.ebp.openQuarterMaster.baseStation.testResources.TestRestUtils.setupJwtCall;
import static com.ebp.openQuarterMaster.baseStation.utils.AuthMode.EXTERNAL;
import static com.ebp.openQuarterMaster.baseStation.utils.AuthMode.SELF;
import static io.restassured.RestAssured.given;

@ApplicationScoped
@Slf4j
public class ExternalTestUserService extends TestUserService {
    public static ExternalTestUserService fromStaticProps(){
        AuthMode authMode = ConfigProvider.getConfig().getValue("service.authMode", AuthMode.class);
        if(authMode == SELF){
            return new ExternalTestUserService(authMode);
        } else {
            return new ExternalTestUserService(
                    authMode,
                    ConfigProvider.getConfig().getValue("test.keycloak.adminName", String.class),
                    ConfigProvider.getConfig().getValue("test.keycloak.adminPass", String.class),
                    ConfigProvider.getConfig().getValue("test.keycloak.realm", String.class),
                    ConfigProvider.getConfig().getValue("test.keycloak.clientId", String.class),
                    ConfigProvider.getConfig().getValue("test.keycloak.clientSecret", String.class)
            );
        }
    }


    @ConfigProperty(name = "service.authMode")
    AuthMode authMode;

//    @ConfigProperty(name = "test.keycloak.authUrl", defaultValue = "")
//    String keycloakAuthUrl;

    @ConfigProperty(name = "test.keycloak.adminName", defaultValue = "admin")
    String keycloakAdminName;
    @ConfigProperty(name = "test.keycloak.adminPass", defaultValue = "admin")
    String keycloakAdminPass;
    @ConfigProperty(name = "service.externalAuth.realm", defaultValue = "")
    String keycloakRealm;
    @ConfigProperty(name = "service.externalAuth.clientId", defaultValue = "")
    String keycloakClientId;
    @ConfigProperty(name = "service.externalAuth.clientSecret", defaultValue = "")
    String keycloakClientSecret;

    public ExternalTestUserService(AuthMode authMode, String keycloakAdminName, String keycloakAdminPass, String keycloakRealm, String keycloakClientId, String keycloakClientSecret) {
        this.authMode = authMode;
        this.keycloakAdminName = keycloakAdminName;
        this.keycloakAdminPass = keycloakAdminPass;
        this.keycloakRealm = keycloakRealm;
        this.keycloakClientId = keycloakClientId;
        this.keycloakClientSecret = keycloakClientSecret;
    }
    public ExternalTestUserService(AuthMode authMode) {
        this.authMode = authMode;
    }
    public ExternalTestUserService(){}

    public UserGetResponse persistTestUser(UserCreateRequest userCreateRequest, boolean admin) {
        if (SELF.equals(this.authMode)) {
            ObjectId id = given()
                .body(userCreateRequest)
                .post("/api/user")
                .then()
                .statusCode(202)
                .extract().body().as(ObjectId.class);
            log.info("Created test user {}", id);
        } else if (EXTERNAL.equals(this.authMode)) {
            try (
                    Keycloak keycloak = KeycloakBuilder.builder()
                            .serverUrl(ConfigProvider.getConfig().getValue("test.keycloak.authUrl", String.class))
                            .realm("master")
                            .grantType(OAuth2Constants.PASSWORD)
                            .clientId("admin-cli")
                            .username(this.keycloakAdminName)
                            .password(this.keycloakAdminPass)
                            .build();
            ) {

                UserRepresentation userRep = userToRepresentation(userCreateRequest, admin);

                RealmResource realmResource = keycloak.realm(this.keycloakRealm);
                ClientRepresentation clientRepresentation = realmResource.clients().findAll().stream().filter(client -> client.getClientId().equals(this.keycloakClientId)).collect(Collectors.toList()).get(0);
                ClientResource clientResource = realmResource.clients().get(clientRepresentation.getId());
                UsersResource usersResource = realmResource.users();

                String userId;
                try (Response response = usersResource.create(userRep);) {
                    log.info("Response from creating test user: {} {}", response.getStatus(), response.getStatusInfo());
                    userId = CreatedResponseUtil.getCreatedId(response);
                    userCreateRequest.getAttributes().put(TEST_EXTERN_ID_ATT_KEY, userId);
                    log.info("ID of user in keycloak: {}", userCreateRequest.getAttributes().get(TEST_EXTERN_ID_ATT_KEY));
                }

                if (usersResource.search(userCreateRequest.getUsername()).size() != 1) {
                    throw new IllegalStateException("Test user cannot be found after creation!");
                }
                UserResource testUserResource = usersResource.get(userId);

                {
                    CredentialRepresentation passwordCred = new CredentialRepresentation();
                    passwordCred.setTemporary(false);
                    passwordCred.setType(CredentialRepresentation.PASSWORD);
                    passwordCred.setValue(userCreateRequest.getAttributes().get(TEST_PASSWORD_ATT_KEY));

                    testUserResource.resetPassword(passwordCred);
                }
                {
//                    UserRepresentation testUserRepresentation = testUserResource.toRepresentation();
//                    RoleRepresentation roleRepresentation =;
                    Set<String> roles = new HashSet<>();
                    roles.add("user");

                    if(admin){
                        roles.add("userAdmin");
                    }

                    testUserResource.roles().clientLevel(clientRepresentation.getId()).add(
                            roles.stream().map((String role) -> {
                                return clientResource.roles().list().stream()
                                        .filter(element -> element.getName().equals(role))
                                        .collect(Collectors.toList())
                                        .get(0);
                            }).collect(Collectors.toList())
                    );
                }

            }

        }
        return setupJwtCall(given(), getTestUserToken(userCreateRequest.getUsername(), userCreateRequest.getPassword()))
                .body(userCreateRequest)
                .post("/api/user/self")
                .then()
                .statusCode(200)
                .extract().body().as(UserGetResponse.class);
    }

    public static UserCreateRequest getTestUserCreateRequest(){
        UserCreateRequest.Builder createRequestBuilder = UserCreateRequest.builder();

        createRequestBuilder.username(faker.name().username());
        createRequestBuilder.firstName(faker.name().firstName());
        createRequestBuilder.lastName(faker.name().lastName());
        createRequestBuilder.email(faker.internet().emailAddress());
        createRequestBuilder.title(faker.company().profession());

        String password = getRandomPassword();
        createRequestBuilder.password(password);

        createRequestBuilder.attributes(Map.of(
                TEST_PASSWORD_ATT_KEY, password
        ));

        return createRequestBuilder.build();
    }

    public UserGetResponse getTestUser(boolean admin, boolean persisted) {
        UserCreateRequest ucr = getTestUserCreateRequest();
        UserGetResponse output;
        if (persisted) {
            output = this.persistTestUser(ucr, admin);
        } else {
            User.Builder userBuilder = User.builder(ucr);
            if(admin){
                userBuilder.roles(Set.of("user", "userAdmin"));
            }
            output = UserGetResponse.builder(userBuilder.build()).build();
        }
        log.debug("Done creating new user: {} - {} {}", output.getUsername(), output.getFirstName(), output.getLastName());

        return output;
    }

    public String getTestUserToken(String username, String password) {
        if (SELF.equals(this.authMode)) {
            return given()
                    .body(new UserLoginRequest(username, password, false))
                    .post("/api/user/auth")
                    .then()
                    .statusCode(202)
                    .extract().body().as(UserLoginResponse.class).getToken();
        } else if (EXTERNAL.equals(this.authMode)) {
            return getUserTokenFromKeycloak(
                    this.keycloakRealm,
                    this.keycloakClientId,
                    this.keycloakClientSecret,
                    username,
                    password
            );
        }
        throw new IllegalStateException("Should not get here");
    }

    public String getTestUserToken(UserGetResponse testUser) {
        return this.getTestUserToken(testUser.getEmail(), testUser.getAttributes().get(TEST_PASSWORD_ATT_KEY));
    }
}
