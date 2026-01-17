package effectiveJava;

public class Item65 {

	public static void main(String[] args) {
		// 起動時に差し替え（Composition Root）
		// ※通常この切り替えは、DIで行う
		PaymentProcessor p = new StripePaymentProcessor(); // 本番
		// PaymentProcessor p = new StubPaymentProcessor(); // テスト
		CheckoutService svc = new CheckoutService(p);

		PaymentRequest req = new PaymentRequest();
		svc.checkout(req);
	}

	// クライアント側処理
	public static class CheckoutService {
		private final PaymentProcessor processor;

		public CheckoutService(PaymentProcessor processor) {
			this.processor = processor;
		}

		public Receipt checkout(PaymentRequest req) {
			return processor.process(req);
		}
	}

	//以下、サーバー側処理

	// レシートクラス
	public static class Receipt {
		private final String env;
		private final boolean isSomething;

		public Receipt(String env, boolean isSomething) {
			this.env = env;
			this.isSomething = isSomething;
		}
	}

	// dto
	private static class PaymentRequest {
	}

	// インターフェース
	public interface PaymentProcessor {
		Receipt process(PaymentRequest req);
	}

	// 実装A(本番)
	public static class StripePaymentProcessor implements PaymentProcessor {
		public Receipt process(PaymentRequest req) {
			// ※本来は、別層の処理を呼び出しReceiptを取得
			return new Receipt("Prod", false);
		}
	}

	// 実装B(テスト用スタブ)
	public class StubPaymentProcessor_Test implements PaymentProcessor {
		public Receipt process(PaymentRequest req) {
			return new Receipt("stub", true);
		}
	}

}
