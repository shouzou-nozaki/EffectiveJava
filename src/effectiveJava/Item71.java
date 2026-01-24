package effectiveJava;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Item71 {

	public static void main(String[] args) {
		UserRepository repo = new UserRepository();
		repo.save(new User("1", "Alice"));

		// 正常系パターン
		Optional<User> userOpt = repo.findById("1");
		userOpt.ifPresent(u -> System.out.println("Fund:" + u.name));

		// 見つからないパターン
		Optional<User> notFound = repo.findById("999");
		System.out.println("User exists? " + notFound.isPresent());

		// 回復可能な例外(checked)
		BankAccount account = new BankAccount(1_000);
		try {
			account.withdrow(2_000);
		} catch (InsufficientFundsException e) {
			System.out.println("残高不足: ユーザーに通知して別処理へ");
		}

		// プログラミングエラー
		try {
			account.withdrow(-100);
		} catch (InsufficientFundsException e) {
			System.out.println("残高不足: ユーザーに通知して別処理へ");
		}
	}

	static class User {
		final String id;
		final String name;

		User(String id, String name) {
			this.id = id;
			this.name = name;
		}
	}

	// レポジトリ
	static class UserRepository {
		private final Map<String, User> store = new HashMap<>();

		public void save(User user) {
			store.put(user.id, user);
		}

		Optional<User> findById(String id) {
			return Optional.ofNullable(store.get(id));
		}
	}

	// checked例外(回復可能)
	static class InsufficientFundsException extends Exception {
		public InsufficientFundsException(String message) {
			super(message);
		}
	}

	// ドメインサービス
	static class BankAccount {
		private long balance;

		public BankAccount(long balance) {
			if (balance < 0) {
				// 呼び出し側のミス → unchecked
				throw new IllegalArgumentException("balance must be >= 0");
			}
			this.balance = balance;
		}

		public void withdrow(long amount) throws InsufficientFundsException {
			if (amount <= 0) {
				// 呼び出し側のミス → unchecked
				throw new IllegalArgumentException("amount must be > 0");
			}
			if (balance < amount) {
				throw new InsufficientFundsException("残高不足です。");
			}
			balance -= amount;
		}

	}

}
