package effectiveJava;

import java.io.IOException;
import java.util.UUID;

public class Item75 {

	public static void main(String[] args) {
		UserService service = new UserService();

		try {
			service.getUserName("500");
		} catch (UserServiceException e) {
			// スタックトレースを見るのが目的
			e.printStackTrace();
		}
	}

	public static class UserService {

		private final UserRepository repository = new UserRepository();

		public String getUserName(String userId) {
			// リクエスト単位での識別子(ログ追跡用)
			String correlationId = UUID.randomUUID().toString();

			try {
				String name = repository.findUserNameById(userId);

				if (name == null) {
					throw new UserServiceException(
							"User not found. userId=" + userId + " correlationId=" + correlationId, null);
				}

				return name;
			} catch (IOException e) {
				String message = "Failed to get user from repository." + " correlationId=" + correlationId;
				throw new UserServiceException(message, e);
			}
		}
	}

	public static class UserServiceException extends RuntimeException {
		public UserServiceException(String message, Throwable cause) {
			super(message, cause);
		}
	}

	public static class UserRepository {
		public String findUserNameById(String userId) throws IOException {
			// DB障害を疑似的に発生させる
			if ("500".equals(userId)) {
				throw new IOException("Database connection error");
			}

			if ("1".equals(userId)) {
				return "Taro";
			}

			return null;
		}
	}

}
