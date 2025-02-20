package com.ebp.openQuarterMaster.lib.core.storage.items;

import com.ebp.openQuarterMaster.lib.core.history.EventType;
import com.ebp.openQuarterMaster.lib.core.history.HistoryEvent;
import com.ebp.openQuarterMaster.lib.core.storage.items.stored.Stored;
import com.ebp.openQuarterMaster.lib.core.testUtils.BasicTest;
import org.apache.commons.lang3.RandomUtils;
import org.bson.types.ObjectId;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class InventoryItemTest extends BasicTest {
	private static final int NUM_ATTS = 50;
	private static final int NUM_KEYWORDS = 25;
	private static final int NUM_IMAGES = 10;
	private static final int NUM_HIST_EVENTS = 10_000;
	private static final int NUM_DISTINCT_STORAGE = 500;
	public static final int NUM_STORED = 10_000;
	
	static void fillCommon(InventoryItem<?> item) {
		
		item.setName(FAKER.commerce().productName());
		item.setDescription(FAKER.lorem().paragraph());
		
		for (int i = 0; i < NUM_ATTS; i++) {
			item.getAttributes().put(
				"" + i,
				FAKER.letterify("???" + i)
			);
		}
		
		for (int i = 0; i < NUM_KEYWORDS; i++) {
			item.getKeywords().add(
				FAKER.letterify("???" + i)
			);
		}
		for (int i = 0; i < NUM_IMAGES; i++) {
			item.getImageIds().add(
				ObjectId.get()
			);
		}
		
		item.updated(new HistoryEvent(
						 EventType.CREATE,
						 ObjectId.get(),
						 ZonedDateTime.now(),
						 FAKER.lorem().sentence()
					 )
		);
		
		for (int i = 1; i < NUM_HIST_EVENTS; i++) {
			item.updated(new HistoryEvent(
				EventType.UPDATE,
				ObjectId.get(),
				ZonedDateTime.now(),
				FAKER.lorem().sentence()
			));
		}
	}
	
	public static void fillCommon(Stored stored) {
		stored.setCondition(RandomUtils.nextInt(0, 101));
		stored.setConditionNotes(FAKER.lorem().paragraph());
		stored.setExpires(ZonedDateTime.now());
		
		for (int j = 0; j < NUM_IMAGES; j++) {
			stored.getImageIds().add(
				ObjectId.get()
			);
		}
		
		for (int j = 0; j < NUM_ATTS; j++) {
			stored.getAttributes().put(
				"" + j,
				FAKER.letterify("???" + j)
			);
		}
		
		for (int j = 0; j < NUM_KEYWORDS; j++) {
			stored.getKeywords().add(
				FAKER.letterify("???" + j)
			);
		}
	}
	
	public static List<ObjectId> getStorageList() {
		List<ObjectId> output = new ArrayList<>(NUM_DISTINCT_STORAGE);
		
		for (int i = 0; i < NUM_DISTINCT_STORAGE; i++) {
			output.add(ObjectId.get());
		}
		
		return output;
	}
	
	
}
