package com.ebp.openQuarterMaster.baseStation.testResources.testClasses;

import com.ebp.openQuarterMaster.baseStation.testResources.data.MongoTestConnector;
import com.ebp.openQuarterMaster.baseStation.testResources.data.TestUserService;
import com.ebp.openQuarterMaster.baseStation.testResources.lifecycleManagers.OurTestDescription;
import com.ebp.openQuarterMaster.baseStation.testResources.lifecycleManagers.SeleniumGridServerManager;
import com.ebp.openQuarterMaster.baseStation.testResources.lifecycleManagers.TestResourceLifecycleManager;
import com.ebp.openQuarterMaster.baseStation.testResources.ui.WebDriverWrapper;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Slf4j
@Execution(ExecutionMode.SAME_THREAD)
//@ExtendWith(SeleniumRecordingTriggerExtension.class)
public abstract class RunningServerTest extends WebServerTest {
	
	@AfterEach
	public void afterEach(
		TestInfo testInfo
	) {
		log.info("Running after method for test {}", testInfo.getDisplayName());
		
		if(ConfigProvider.getConfig().getOptionalValue("quarkus.mongodb.connection-string", String.class).isEmpty()){
			log.info("Mongo not started.");
		} else {
			MongoTestConnector.getInstance().clearDb();
		}
		
		if(SeleniumGridServerManager.RECORD) {
			TestResourceLifecycleManager.BROWSER_CONTAINER.triggerRecord(
				new OurTestDescription(testInfo),
				//TODO:: actually pass something real https://stackoverflow.com/questions/71354431/junit5-get-results-from-test-in-aftereach
				Optional.empty()
			);
		}
		findAndCleanupWebDriverWrapper();
		
		log.info("Completed after step.");
	}
	
	private Stream<Field> allFieldsForSelf() {
		return walkInheritanceTreeForSelf().flatMap( k -> Arrays.stream(k.getDeclaredFields()) );
	}
	
	private Stream<Class> walkInheritanceTreeForSelf() {
		return iterate( this.getClass(), k -> Optional.ofNullable(k.getSuperclass()) );
	}
	private <T> Stream<T> iterate( T seed, Function<T,Optional<T>> fetchNextFunction ) {
		Objects.requireNonNull(fetchNextFunction);
		
		Iterator<T> iterator = new Iterator<T>() {
			private Optional<T> t = Optional.ofNullable(seed);
			
			public boolean hasNext() {
				return t.isPresent();
			}
			
			public T next() {
				T v = t.get();
				
				t = fetchNextFunction.apply(v);
				
				return v;
			}
		};
		
		return StreamSupport.stream(
			Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED | Spliterator.IMMUTABLE),
			false
		);
	}
	
	private void findAndCleanupWebDriverWrapper() {
		log.info("Searching for WebDriverWrappers to cleanup: {}", this.getClass());
		List<WebDriverWrapper> webDriverWrapperList = (
			allFieldsForSelf().filter((Field curField)->{
				log.debug("Field: {}", curField.getType());
				return WebDriverWrapper.class.isAssignableFrom(curField.getType()) ||
					   curField.getType().isAssignableFrom(TestUserService.class);
			}).map((Field curField)->{
				Object cur;
				log.debug("WebDriverWrapper field: {}", curField.getType());
				try {
					if (!curField.canAccess(this)) {
						curField.setAccessible(true);
					}
					cur = curField.get(this);
				} catch(IllegalAccessException e) {
					log.warn("Cannot access field: {}. ", curField, e);
					return (WebDriverWrapper) null;
				}
				if(cur == null){
					log.debug("Null value from field.");
					return null;
				}
				log.debug("Value({}): {}", cur.getClass(), cur);
				
				if (WebDriverWrapper.class.isAssignableFrom(cur.getClass())) {
					log.debug("Was regular WebDriverWrapper!");
					return (WebDriverWrapper) cur;
				}
				log.warn("Was not a service we recognize!");
				return (WebDriverWrapper) null;
			}).collect(Collectors.toList())
		);
		log.info("Found {} web driver wrappers.", webDriverWrapperList.size());
		
		for (WebDriverWrapper curWrapper : webDriverWrapperList) {
			if (curWrapper == null) {
				log.debug("Null web driver wrapper!");
				continue;
			}
			curWrapper.cleanup();
		}
	}
}
