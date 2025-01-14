package com.ebp.openQuarterMaster.baseStation.interfaces.ui;

import com.ebp.openQuarterMaster.baseStation.testResources.data.TestUserService;
import com.ebp.openQuarterMaster.baseStation.testResources.lifecycleManagers.TestResourceLifecycleManager;
import com.ebp.openQuarterMaster.baseStation.testResources.profiles.ExternalAuthTestProfile;
import com.ebp.openQuarterMaster.baseStation.testResources.testClasses.WebUiTest;
import com.ebp.openQuarterMaster.baseStation.testResources.ui.pages.General;
import com.ebp.openQuarterMaster.baseStation.testResources.ui.pages.KeycloakLogin;
import com.ebp.openQuarterMaster.baseStation.testResources.ui.pages.Root;
import com.ebp.openQuarterMaster.lib.core.user.User;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.ResourceArg;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static com.ebp.openQuarterMaster.baseStation.testResources.data.TestUserService.TEST_PASSWORD_ATT_KEY;
import static com.ebp.openQuarterMaster.baseStation.testResources.ui.assertions.LocationAssertions.assertOnPage;
import static com.ebp.openQuarterMaster.baseStation.testResources.ui.assertions.UserRelated.assertUserLoggedIn;
import static com.mongodb.assertions.Assertions.assertTrue;

@Tag("integration")
@Tag("externalAuth")
@Slf4j
@QuarkusTest
@TestProfile(ExternalAuthTestProfile.class)
@QuarkusTestResource(
	value = TestResourceLifecycleManager.class,
	initArgs = {
		@ResourceArg(name = TestResourceLifecycleManager.EXTERNAL_AUTH_ARG, value = "true"),
		@ResourceArg(name = TestResourceLifecycleManager.UI_TEST_ARG, value = "true")
	},
	restrictToAnnotatedClass = true
)
public class LoginExternalTest extends WebUiTest {
	
	@Inject
	TestUserService testUserService;
	
	@Test
	public void testLogin() {
		User testUser = this.testUserService.getTestUser(false, true);
		this.webDriverWrapper.goToIndex();
		
		this.webDriverWrapper.waitForPageLoad();
		
		this.webDriverWrapper.getWebDriver().findElement(Root.LOGIN_WITH_EXTERNAL_LINK).click();
		
		log.info("Went to keycloak at: {}", this.webDriverWrapper.getWebDriver().getCurrentUrl());
		
		this.webDriverWrapper.waitFor(KeycloakLogin.USERNAME_INPUT).sendKeys(testUser.getUsername());
		this.webDriverWrapper.findElement(KeycloakLogin.PASSWORD_INPUT).sendKeys(testUser.getAttributes().get(TEST_PASSWORD_ATT_KEY));
		
		this.webDriverWrapper.findElement(KeycloakLogin.LOGIN_BUTTON).click();
		
		this.webDriverWrapper.waitForPageLoad();
		assertUserLoggedIn(this.webDriverWrapper, testUser);
		assertOnPage(this.webDriverWrapper, "/overview");
		
		//test refresh keys
		this.webDriverWrapper.getWebDriver().navigate().refresh();
		
		this.webDriverWrapper.waitForPageLoad();
		assertUserLoggedIn(this.webDriverWrapper, testUser);
		
	}
	
	@Test
	public void testLoginWithReturnPath() {
		User testUser = this.testUserService.getTestUser(false, true);
		String queryPath = "/storage?label=some&pageNum=1";
		
		this.webDriverWrapper.goTo(queryPath);
		
		this.webDriverWrapper.waitForPageLoad();
		
		log.info(
			"Login link url: {}",
			this.webDriverWrapper.getWebDriver().findElement(Root.LOGIN_WITH_EXTERNAL_LINK).getAttribute("href")
		);
		
		this.webDriverWrapper.getWebDriver().findElement(Root.LOGIN_WITH_EXTERNAL_LINK).click();
		
		this.webDriverWrapper.waitFor(KeycloakLogin.USERNAME_INPUT).sendKeys(testUser.getUsername());
		log.info("Went to keycloak at: {}", this.webDriverWrapper.getWebDriver().getCurrentUrl());
		this.webDriverWrapper.findElement(KeycloakLogin.PASSWORD_INPUT).sendKeys(testUser.getAttributes().get(TEST_PASSWORD_ATT_KEY));
		
		this.webDriverWrapper.findElement(KeycloakLogin.LOGIN_BUTTON).click();
		
		this.webDriverWrapper.waitForPageLoad();
		assertUserLoggedIn(this.webDriverWrapper, testUser);
		
		assertOnPage(this.webDriverWrapper, "/storage");
		assertTrue(this.webDriverWrapper.getWebDriver().getCurrentUrl().endsWith(queryPath));
	}
	
	@Test
	public void testLogout() {
		User testUser = this.testUserService.getTestUser(true, true);
		
		this.webDriverWrapper.loginUser(testUser);
		
		this.webDriverWrapper.waitFor(General.USERNAME_DISPLAY).click();
		this.webDriverWrapper.waitFor(General.LOGOUT_BUTTON).click();
		
		this.webDriverWrapper.waitFor(Root.LOGIN_WITH_EXTERNAL_LINK);
		
		assertOnPage(this.webDriverWrapper, "/");
		
		//TODO:: assert message saying logged out
	}
}
