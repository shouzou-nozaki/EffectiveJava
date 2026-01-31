package effectiveJava;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Item79 {
	private static final int THREAD_COUNT = 10;
	private static final int TASKS_PER__THREAD = 5;

	public static void main(String[] args) throws InterruptedException {
		System.out.println("=== 1. 悪い例：長時間ロック(I/Oをロック内で実行) ===");
		testPerformance(new LongLockStore(), "長時間ロック");

		System.out.println("=== 2. 良い例：最小限の同期(I/Oをロック外で実行) ===");
		testPerformance(new MinimalSyncStore(), "最小限の同期");
	}

	public static void testPerformance(DataStore store, String label) throws InterruptedException {
		ExecutorService executor = Executors.newFixedThreadPool(TASKS_PER__THREAD);
		long startTime = System.currentTimeMillis();

		for (int i = 0; i < THREAD_COUNT * TASKS_PER__THREAD; i++) {
			executor.submit(store::updateData);
		}

		executor.shutdown();
		executor.awaitTermination(1, TimeUnit.MINUTES);
		long endTime = System.currentTimeMillis();

		System.out.printf("%s -> 完了までの時間: %dms%n", label, (endTime - startTime));
	}

	public interface DataStore {
		void updateData();
	}

	// 悪い例；ロックの中で重い処理(模擬I/O)をしてしまう
	public static class LongLockStore implements DataStore {
		private String data;
		private final Object lock = new Object();

		public void updateData() {
			synchronized (lock) {
				// NG:ロックを保持したまま重い処理(100msスリープ)
				String result = simulateHeavyIO();
				this.data = result;
			}
		}

		private String simulateHeavyIO() {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {

			}
			return "new Date";
		}
	}

	// 良い例：ロックを書けるのは書き換えの瞬間だけ
	public static class MinimalSyncStore implements DataStore {
		private String data;
		private final Object lock = new Object();

		public void updateData() {
			// Good:重い処理はロックの外で行う(並列に実行される)
			String result = simulateHeavyIO();

			// 最後の書き換えの瞬間だけ同期(クリティカルセクションの極小化)
			synchronized (lock) {
				this.data = result;
			}

		}

		private String simulateHeavyIO() {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {

			}
			return "new Date";
		}
	}
}
