package effectiveJava;

public class Item70 {

	public static void main(String[] args) {
		BankAccount acount = new BankAccount(30, 1_000);
		try {
			acount.withdraw(1_000_000);
		} catch (InsufficientFundsException e) {
			// ユーザーに残高不足を通知して別の処理を促す(回復)
			e.printStackTrace();
		}

	}

	// ドメイン的に回復可能(支払い前に残高がない等)
	public static class InsufficientFundsException extends Exception {
		public InsufficientFundsException(String message) {
			super(message);
		}
	}

	public static class BankAccount {
		int age;
		long balance;

		public BankAccount(int age, long balance) {
			if (age < 0) {
				throw new IllegalArgumentException("age must be >= 0");
			}
			if (balance < 0) {
				throw new IllegalArgumentException("balance must be >= 0");
			}
			this.age = age;
			this.balance = balance;
		}

		public void withdraw(long amount) throws InsufficientFundsException {
			if (amount < 0) {
				throw new IllegalArgumentException("amount must be > 0");
			}
			if (balance < amount) {
				throw new InsufficientFundsException("残高不足");
			}
			balance -= amount;
		}

	}

}
