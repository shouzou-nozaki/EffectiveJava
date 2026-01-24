package effectiveJava;

import java.io.IOException;

/**
 * ユーザー情報を取得するアプリケーションサービス
 */
public class Item74 {

	public static void main(String[] args) throws UserNotFoundException, UserServiceException {
		UserRepository repo = new FileUserRepository();
		UserService service = new UserService(repo);

		// 正常系
		try {
			User user = service.getUser("1");
			System.out.println("User found: " + user.name);

			service.getUser("999");

		} catch (UserNotFoundException e) {
			e.printStackTrace();
		} catch (UserServiceException e) {
			e.printStackTrace();
		}

		// 業務的に存在しない（正常系の分岐）
		try {
			service.getUser("999");
		} catch (UserNotFoundException e) {
			System.out.println("ユーザーが存在しません");
		} catch (UserServiceException e) {
			e.printStackTrace();
		}

		// プログラミングエラー
		service.getUser(null);
	}

	/* ===== ドメイン ===== */

	static class User {
		final String id;
		final String name;

		User(String id, String name) {
			this.id = id;
			this.name = name;
		}
	}

	/* ===== 例外定義 ===== */

	/**
	 * ユーザーが存在しないことを表す（回復可能）
	 */
	static class UserNotFoundException extends Exception {
		public UserNotFoundException(String message) {
			super(message);
		}
	}

	/**
	 * ユーザー取得処理全体の失敗（抽象例外）
	 */
	static class UserServiceException extends Exception {
		public UserServiceException(String message, Throwable cause) {
			super(message, cause);
		}
	}

	/**
	 * 永続化層の失敗
	 */
	static class RepositoryException extends Exception {
		public RepositoryException(String message, Throwable cause) {
			super(message, cause);
		}
	}

	/* ===== Repository ===== */

	interface UserRepository {

		/**
		 * 指定IDのユーザーを取得する
		 *
		 * @param id ユーザーID（null不可）
		 * @return ユーザー
		 * @throws UserNotFoundException ユーザーが存在しない場合（回復可能）
		 * @throws RepositoryException 永続化層の障害が発生した場合
		 * @throws IllegalArgumentException id が null の場合（呼び出し側のバグ）
		 */
		User findById(String id) throws UserNotFoundException, RepositoryException;
	}

	static class FileUserRepository implements UserRepository {

		@Override
		public User findById(String id) throws UserNotFoundException, RepositoryException {
			if (id == null) {
				throw new IllegalArgumentException("id must not be null");
			}

			try {
				// 疑似的なI/O処理
				if ("1".equals(id)) {
					return new User("1", "Alice");
				}
				if ("999".equals(id)) {
					throw new UserNotFoundException("user not found: " + id);
				}

				// 想定外のI/O失敗
				throw new IOException("disk error");

			} catch (UserNotFoundException e) {
				throw e; // 抽象レベルが変わらないのでそのまま
			} catch (IOException e) {
				throw new RepositoryException("failed to load user", e);
			}
		}
	}

	/* ===== Application Service ===== */

	static class UserService {
		private final UserRepository repository;

		UserService(UserRepository repository) {
			this.repository = repository;
		}

		/**
		 * ユーザーを取得するユースケース
		 *
		 * @param id ユーザーID
		 * @return ユーザー
		 * @throws UserNotFoundException ユーザーが存在しない場合
		 * @throws UserServiceException 取得処理に失敗した場合（原因は {@link Throwable#getCause()} 参照）
		 * @throws IllegalArgumentException id が null の場合
		 */
		public User getUser(String id) throws UserNotFoundException, UserServiceException {
			try {
				return repository.findById(id);
			} catch (RepositoryException e) {
				// 抽象レベル変換
				throw new UserServiceException("ユーザー取得に失敗しました", e);
			}
		}
	}
}
