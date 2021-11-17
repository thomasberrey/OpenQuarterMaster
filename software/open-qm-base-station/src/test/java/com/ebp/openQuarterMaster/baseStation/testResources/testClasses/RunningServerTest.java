package com.ebp.openQuarterMaster.baseStation.testResources.testClasses;

import com.ebp.openQuarterMaster.baseStation.service.mongo.MongoService;
import com.ebp.openQuarterMaster.baseStation.testResources.data.TestUserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Execution(ExecutionMode.SAME_THREAD)//TODO:: remove this, when know how to make this work
public abstract class RunningServerTest extends WebServerTest {

    @AfterEach
    public void afterEach() {
        log.info("Searching for MongoServices to use in cleanup: {}", this.getClass());
        List<MongoService> svcList = (Arrays.stream(this.getClass().getDeclaredFields()).filter((Field curField) -> {
            log.debug("Field: {}", curField.getType());
            return MongoService.class.isAssignableFrom(curField.getType()) ||
                    curField.getType().isAssignableFrom(TestUserService.class);
        }).map((Field curField) -> {
            Object cur;
            log.debug("Mongo service field: {}", curField.getType());
            try {
                if (!curField.canAccess(this)) {
                    curField.setAccessible(true);
                }
                cur = curField.get(this);
            } catch (IllegalAccessException e) {
                log.warn("Cannot access field: {}. ", curField, e);
                return (MongoService) null;
            }
            log.debug("Value: {}", cur);

            if (MongoService.class.isAssignableFrom(cur.getClass())) {
                log.debug("Was regular MongoService!");
                return (MongoService) cur;
            } else if (cur instanceof TestUserService) {
                log.debug("Was testUser service!");
                return ((TestUserService) cur).getUserService();
            }
            log.warn("Was not a service we recognize!");
            return (MongoService) null;
        }).collect(Collectors.toList())
        );
        log.info("Found {} services to cleanup entries from: {}", svcList.size(), svcList);
        this.cleanup(svcList.toArray(new MongoService[svcList.size()]));
    }

    public void cleanup(MongoService... services) {
        log.info("Cleaning up test env.");

        long totalNumCleaned = 0;
        for (MongoService service : services) {
            if (service == null) {
                log.debug("Null service!");
                continue;
            }
            long numCleaned = service.removeAll();
            totalNumCleaned += numCleaned;
            log.info("Cleaned {} entries from service: {}", numCleaned, service.getClass());
        }
        log.info("Cleaned a total of {} entries from {} services.", totalNumCleaned, services.length);
    }
}
