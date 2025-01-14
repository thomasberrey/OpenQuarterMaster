package com.ebp.openQuarterMaster.baseStation.testResources.data;

import com.ebp.openQuarterMaster.lib.core.storage.items.InventoryItem;
import com.ebp.openQuarterMaster.lib.core.storage.items.SimpleAmountItem;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class InventoryItemTestObjectCreator extends TestObjectCreator<InventoryItem> {
	
	@Override
	public InventoryItem<?> getTestObject() {
		SimpleAmountItem item = new SimpleAmountItem();//TODO:: use a regular SimpleAmountItem once we know how to do that
		
		item.setName(faker.commerce().productName());
		
		return item;
	}
}
