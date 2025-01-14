package com.ebp.openQuarterMaster.lib.core.storage.items.stored;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import org.bson.types.ObjectId;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Describes an item stored in the system.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class Stored {
	
	/**
	 * When the item(s) held expire. Null if it does not expire.
	 */
	private ZonedDateTime expires = null;
	
	/**
	 * The condition of the stored object. 100 = mint, 0 = completely deteriorated. Null if N/A.
	 */
	@Max(100)
	@Min(0)
	private Integer condition = null;
	
	/**
	 * Notes on the condition on the thing(s) stored.
	 */
	private String conditionNotes = null;
	
	/**
	 * List of images related to the object.
	 */
	@NonNull
	@NotNull
	List<@NonNull ObjectId> imageIds = new ArrayList<>();
	
	/**
	 * Attributes related to the item
	 */
	private Map<@NotBlank @NotNull String, String> attributes = new HashMap<>();
	
	/**
	 * Keywords for the item
	 */
	@NotNull
	@NonNull
	private List<@NotBlank String> keywords = new ArrayList<>();
}
