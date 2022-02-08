package com.ebp.openQuarterMaster.baseStation.endpoints.inventory.items;

import com.ebp.openQuarterMaster.baseStation.service.mongo.InventoryItemService;
import com.ebp.openQuarterMaster.baseStation.testResources.data.InternalTestUserService;
import com.ebp.openQuarterMaster.baseStation.testResources.data.InventoryItemTestObjectCreator;
import com.ebp.openQuarterMaster.baseStation.testResources.lifecycleManagers.TestResourceLifecycleManager;
import com.ebp.openQuarterMaster.baseStation.testResources.testClasses.RunningServerTest;
import com.ebp.openQuarterMaster.lib.core.rest.user.UserGetResponse;
import com.ebp.openQuarterMaster.lib.core.storage.InventoryItem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.restassured.http.ContentType;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import static com.ebp.openQuarterMaster.baseStation.testResources.TestRestUtils.setupJwtCall;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@QuarkusIntegrationTest
@QuarkusTestResource(value = TestResourceLifecycleManager.class)
@TestHTTPEndpoint(InventoryItemsCrud.class)
class InventoryItemsCrudTest extends RunningServerTest {
    @Inject
    InventoryItemTestObjectCreator testObjectCreator;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    InventoryItemService inventoryItemService;

    @Inject
    InternalTestUserService testUserService;

    @Test
    public void testCreate() throws JsonProcessingException {
        UserGetResponse user = this.testUserService.getTestUser(false, true);
        InventoryItem item = testObjectCreator.getTestObject();
        ObjectId returned = setupJwtCall(given(), this.testUserService.getTestUserToken(user))
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(item))
                .when()
                .post()
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .extract().body().as(ObjectId.class);
        log.info("Got object id back from create request: {}", returned);

        InventoryItem stored = inventoryItemService.get(returned);
        assertNotNull(stored);

        assertFalse(stored.getHistory().isEmpty());
        assertEquals(1, stored.getHistory().size());

        item.setHistory(stored.getHistory());
        item.setId(returned);


        assertEquals(item, stored);
    }
}