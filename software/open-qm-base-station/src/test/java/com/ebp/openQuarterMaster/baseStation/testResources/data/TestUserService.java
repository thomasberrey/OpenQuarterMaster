package com.ebp.openQuarterMaster.baseStation.testResources.data;

import com.ebp.openQuarterMaster.lib.core.rest.user.UserCreateRequest;
import com.ebp.openQuarterMaster.lib.core.rest.user.UserGetResponse;
import com.ebp.openQuarterMaster.lib.core.user.User;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.apache.commons.lang3.RandomStringUtils;
import org.eclipse.microprofile.config.ConfigProvider;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public abstract class TestUserService {
    public static final String TEST_PASSWORD_ATT_KEY = "TEST_PASSWORD";
    public static final String TEST_EXTERN_ID_ATT_KEY = "TEST_EXTERNAL_KEY";

    protected static final Faker faker = Faker.instance();

    protected static String getRandomPassword() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; i += 4) {
            sb.append(RandomStringUtils.random(1, "abcdefg"));
            sb.append(RandomStringUtils.random(1, "ABCDEFG"));
            sb.append(RandomStringUtils.random(1, "1234567"));
            sb.append(RandomStringUtils.random(1, "!@#$%^&"));
        }
        return sb.toString();
    }

    protected static UserRepresentation userToRepresentation(
            String username,
            String firstName,
            String lastName,
            String email,
            boolean admin
    ) {
        UserRepresentation rep = new UserRepresentation();

        rep.setEnabled(true);

        rep.setUsername(username);
        rep.setFirstName(firstName);
        rep.setLastName(lastName);
        rep.setEmail(email);
//        rep.setAttributes(Map.of("origin", List.of("tests")));
        rep.setEmailVerified(true);
        rep.setOrigin("tests");


        {
//            rep.setGroups(testUser.getRoles());
            List<String> roles = new ArrayList<>();
            roles.add("user");
            if(admin){
                roles.add("userAdmin");
            }
            rep.setClientRoles(Map.of("quartermaster", roles));
        }

        return rep;
    }

    protected static UserRepresentation userToRepresentation(UserCreateRequest testUser, boolean admin) {
        return userToRepresentation(
                testUser.getUsername(),
                testUser.getFirstName(),
                testUser.getLastName(),
                testUser.getEmail(),
                admin
        );
    }
    protected static UserRepresentation userToRepresentation(User testUser) {
        return userToRepresentation(
                testUser.getUsername(),
                testUser.getFirstName(),
                testUser.getLastName(),
                testUser.getEmail(),
                testUser.getRoles().contains("userAdmin")
        );
    }

    protected static String getUserTokenFromKeycloak(
            String keycloakRealm,
            String keycloakClientId,
            String keycloakClientSecret,
            String testUserUsername,
            String testUserPassword
    ) {
        KeycloakBuilder keycloakBuilder = KeycloakBuilder.builder()
                .serverUrl(ConfigProvider.getConfig().getValue("test.keycloak.authUrl", String.class))
                .realm(keycloakRealm)
                .clientId(keycloakClientId)
                .clientSecret(keycloakClientSecret)
                .grantType(OAuth2Constants.PASSWORD)
                .username(testUserUsername)
                .password(testUserPassword);

        try (
                Keycloak keycloak = keycloakBuilder.build();
        ) {
            keycloak.realms();

            AccessTokenResponse response = keycloak
                    .tokenManager()
                    .getAccessToken();

            log.info("Get user token response: {}", response.getSessionState());

            return response.getToken();
        } catch (Exception e) {
            log.error("FAILED to get token for user: ", e);
            throw e;
        }
    }

    protected static String getUserTokenFromKeycloak(
            String keycloakRealm,
            String keycloakClientId,
            String keycloakClientSecret,
            User testUser
    ) {
        return getUserTokenFromKeycloak(
                keycloakRealm,
                keycloakClientId,
                keycloakClientSecret,
                testUser.getUsername(),
                testUser.getAttributes().get(TEST_PASSWORD_ATT_KEY)
        );
    }
    protected static String getUserTokenFromKeycloak(
            String keycloakRealm,
            String keycloakClientId,
            String keycloakClientSecret,
            UserGetResponse testUser
    ) {
        return getUserTokenFromKeycloak(
                keycloakRealm,
                keycloakClientId,
                keycloakClientSecret,
                testUser.getUsername(),
                testUser.getAttributes().get(TEST_PASSWORD_ATT_KEY)
        );
    }
}
