package com.ebp.openQuarterMaster.lib.core.rest.user;

import com.ebp.openQuarterMaster.lib.core.MainObject;
import com.ebp.openQuarterMaster.lib.core.history.HistoryEvent;
import com.ebp.openQuarterMaster.lib.core.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The response object from getting a user
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserGetResponse extends MainObject {
	
	private ObjectId id;
	private String username;
	private String firstName;
	private String lastName;
	private String email;
	private String title;
	private Map<String, String> externIds = new HashMap<>();
	private Set<String> roles = new HashSet<>();
	private Map<String, String> attributes = new HashMap<>();
	private List<HistoryEvent> history = new ArrayList<>(List.of());
	
	public static Builder builder(User user) {
		return new Builder()
			.id(user.getId())
			.username(user.getUsername())
			.firstName(user.getFirstName())
			.lastName(user.getLastName())
			.email(user.getEmail())
			.title(user.getTitle())
			.externIds(user.getExternIds())
			.roles(user.getRoles())
			.attributes(user.getAttributes())
			.history(user.getHistory());
	}
	
}
