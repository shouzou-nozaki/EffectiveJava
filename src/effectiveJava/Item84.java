package effectiveJava;

import java.util.concurrent.CountDownLatch;

public class Item84 {

	public static void main(String[] args) throws InterruptedException {
		System.out.println("=== 検証1: スレッドスケジューラに依存（NG: Thread.sleep） ===");
		badExample();

		System.out.println("\n=== 検証2: 明示的な同期（OK: CountDownLatch） ===");
		goodExample();
	}

	/**
	 * 悪い例: 「たぶん1秒あれば終わるだろう」という希望的観測に基づく設計。
	 * 実行環境の負荷やCPU性能によって、成功したり失敗したりする（非決定的）。
	 */
	private static void badExample() throws InterruptedException {
		// 初期化状態を管理するフラグ（本来は volatile が必要だが、ここでは不完全さを際立たせる）
		boolean[] initialized = { false };

		Thread worker = new Thread(() -> {
			try {
				// 重い処理をシミュレート
				Thread.sleep(1000);
				initialized[0] = true;
				System.out.println("  [Worker1] 初期化完了！");
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		});

		worker.start();

		// スケジューラに依存した待ち方
		System.out.println("  [Main1] 1秒待ちます...");
		Thread.sleep(1000);

		if (initialized[0]) {
			System.out.println("  [Main1] 結果: 成功（たまたま間に合った）");
		} else {
			System.out.println("  [Main1] 結果: 失敗（間に合わなかった！）");
		}
	}

	/**
	 * 良い例: CountDownLatch を使い、時間がどれだけかかっても「終わった瞬間」に次へ進む。
	 * 環境に依存せず、常に論理的に正しい。
	 */
	private static void goodExample() throws InterruptedException {
		CountDownLatch latch = new CountDownLatch(1);

		Thread worker = new Thread(() -> {
			try {
				// 処理時間が変動しても大丈夫
				Thread.sleep(1500);
				System.out.println("  [Worker2] 初期化完了！");
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			} finally {
				latch.countDown(); // 終わったことを「明示的に」通知
			}
		});

		worker.start();

		System.out.println("  [Main2] 完了を待ち合わせます（await）...");
		// 1秒だろうが10秒だろうが、準備ができるまで正確に待つ
		latch.await();

		System.out.println("  [Main2] 結果: 成功（確実に初期化されている）");
	}
}