package com.ebp.openQuarterMaster.lib.core;

import systems.uom.common.USCustomary;
import tech.units.indriya.AbstractUnit;
import tech.units.indriya.unit.Units;

import javax.measure.Unit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UnitUtils {
	
	/**
	 * List of units that are applicable to storage.
	 */
	public static final Map<String, List<Unit<?>>> ALLOWED_UNITS_MAP = new LinkedHashMap<>() {{
		put(
			"Number",
			List.of(
				AbstractUnit.ONE,
				Units.MOLE
			)
		);
		put(
			"Length",
			List.of(
				Units.METRE,
				USCustomary.INCH,
				USCustomary.FOOT,
				USCustomary.FOOT_SURVEY,
				USCustomary.YARD,
				USCustomary.MILE,
				USCustomary.NAUTICAL_MILE
			)
		);
		put(
			"Mass",
			List.of(
				Units.GRAM,
				Units.KILOGRAM,
				USCustomary.OUNCE,
				USCustomary.POUND,
				USCustomary.TON
			)
		);
		put(
			"Area",
			List.of(
				Units.SQUARE_METRE,
				USCustomary.SQUARE_FOOT,
				USCustomary.ARE,
				USCustomary.HECTARE,
				USCustomary.ACRE
			)
		);
		put(
			"Volume",
			List.of(
				Units.LITRE,
				Units.CUBIC_METRE,
				USCustomary.LITER,
				USCustomary.CUBIC_INCH,
				USCustomary.CUBIC_FOOT,
				USCustomary.ACRE_FOOT,
				USCustomary.GALLON_DRY,
				USCustomary.GALLON_LIQUID,
				USCustomary.FLUID_OUNCE,
				USCustomary.GILL_LIQUID,
				USCustomary.MINIM,
				USCustomary.FLUID_DRAM,
				USCustomary.CUP,
				USCustomary.TEASPOON,
				USCustomary.TABLESPOON,
				USCustomary.BARREL,
				USCustomary.PINT
			)
		);
		put(
			"Energy",
			List.of(
				Units.JOULE
			)
		);
	}};
	
	public static final List<Unit<?>> ALLOWED_UNITS = new ArrayList<>();
	
	public static final Map<Unit<?>, List<Unit<?>>> UNIT_COMPATIBILITY_MAP = new LinkedHashMap<>();
	
	static {
		for (List<Unit<?>> curList : ALLOWED_UNITS_MAP.values()) {
			ALLOWED_UNITS.addAll(curList);
		}
		for (Unit<?> curUnit : ALLOWED_UNITS) {
			List<Unit<?>> compatibleList = new ArrayList<>();
			
			for (Unit<?> curComparison : ALLOWED_UNITS) {
				if (curUnit.isCompatible(curComparison)) {
					compatibleList.add(curComparison);
				}
			}
			
			UNIT_COMPATIBILITY_MAP.put(curUnit, compatibleList);
		}
	}
	
	public static String stringFromUnit(Unit<?> unit) {
		return unit.toString();
	}
	
	public static Unit<?> unitFromString(String unitStr) {
		for (Unit<?> curUnit : ALLOWED_UNITS) {
			if (stringFromUnit(curUnit).equals(unitStr)) {
				return curUnit;
			}
		}
		return null;
	}
	
}
