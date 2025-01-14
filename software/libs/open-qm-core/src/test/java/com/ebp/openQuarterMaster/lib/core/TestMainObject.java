package com.ebp.openQuarterMaster.lib.core;

import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Map;

@NoArgsConstructor
public class TestMainObject extends MainObject {
	
	public TestMainObject(ObjectId objectId, Map<String, String> atts, List<String> keywords) {
		super(objectId, atts, keywords);
	}
}
