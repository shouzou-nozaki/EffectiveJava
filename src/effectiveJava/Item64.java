package effectiveJava;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Item64 {

	public static void main(String[] args) {
		// Mapインターフェースを実装している具象型ならなんでもOK
		UserService svc = new UserService(new ConcurrentHashMap<>()); // ConcurrentHashMap(具象型)
		UserService svc2 = new UserService(new LinkedHashMap<>()); // LinkedHashMap(具象型)
		UserService svc3 = new UserService(new HashMap<>()); // HashMap(具象型)
	}

	private static class User {
		private final String id;
		private final String name;

		public User(String id, String name) {
			this.id = id;
			this.name = name;
		}

		public String getId() {
			return this.id;
		}
	}

	private static class UserService {
		private final Map<String, User> users;

		public UserService(Map<String, User> users) {
			this.users = users;
		}

		public Collection<User> getAllUsers() {
			return Collections.unmodifiableCollection(users.values());
		}

		public void addUsers(Collection<User> newUsers) {
			for (User u : newUsers) {
				users.put(u.getId(), u);
			}
		}
	}
}
