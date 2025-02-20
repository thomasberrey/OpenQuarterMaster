package com.ebp.openQuarterMaster.lib.core.testUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public abstract class ObjectSerializationTest<T> extends BasicTest {
	
	/** The max amount of time in seconds we want de/serialization to take (triggers warning if over this) */
	private static final int SERIALIZATION_TIME_THRESHOLD = 1;
	/** The threshold for logging out the json string representation of the data */
	private static final int SERIALIZED_SIZE_LOG_THRESHOLD = 2_000;
	
	
	/** The speed of the theoretical connection. Used to calculate {@link #SERIALIZED_SIZE_THRESHOLD}. In bytes per second. */
	private static final int TRANSFER_SPEED = 100_000_000;
	/** The max time in seconds we would want a transfer to take. Used to calculate {@link #SERIALIZED_SIZE_THRESHOLD}. */
	private static final int MAX_TRANSFER_TIME = 1;
	/**
	 * The max size of a json document we want to make (triggers warning if over this). Calculated based on how long it would take to
	 * transfer
	 */
	private static final int SERIALIZED_SIZE_THRESHOLD = TRANSFER_SPEED * MAX_TRANSFER_TIME;
	
	/**
	 * The class of the object we are de/serializing.
	 */
	private final Class<T> clazz;
	
	protected ObjectSerializationTest(Class<T> clazz) {
		this.clazz = clazz;
	}
	
	@ParameterizedTest
	@MethodSource("getObjects")
	public void testSerialization(T object) throws JsonProcessingException {
		StopWatch sw = StopWatch.createStarted();
		String json = OBJECT_MAPPER.writeValueAsString(object);
		sw.stop();
		log.info("Serialized object in {}", sw);
		
		if (sw.getTime(TimeUnit.SECONDS) >= SERIALIZATION_TIME_THRESHOLD) {
			log.warn("Serialization took longer than threshold {} seconds to complete.", SERIALIZATION_TIME_THRESHOLD);
		}
		
		log.info("Length of json string: {}", json.length());
		log.info("Would take {}s to send over {}bps connection.", (double) json.length() / (double) TRANSFER_SPEED, TRANSFER_SPEED);
		
		if (json.length() > SERIALIZED_SIZE_THRESHOLD) {
			log.warn("Length of JSON string very, very long.");
		}
		
		if (json.length() < SERIALIZED_SIZE_LOG_THRESHOLD) {
			log.info("json: {}", json);
		}
		
		sw = StopWatch.createStarted();
		T objectBack = OBJECT_MAPPER.readValue(json, clazz);
		sw.stop();
		log.info("Deserialized object in {}", sw);
		
		if (sw.getTime(TimeUnit.SECONDS) >= SERIALIZATION_TIME_THRESHOLD) {
			log.warn("Deserialization took longer than the threshold {} seconds to complete.", SERIALIZATION_TIME_THRESHOLD);
		}
		
		assertEquals(object, objectBack, "Deserialized object was not equal to original.");
	}
	
}
