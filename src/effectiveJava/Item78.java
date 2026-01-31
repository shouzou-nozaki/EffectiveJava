package effectiveJava;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Item78 {
	private static final int THREAD_COUNT = 10;
	private static final int INCREMENTS_PER_THREAD = 10000;
	private static final int EXPECTED_TOTAL = THREAD_COUNT * INCREMENTS_PER_THREAD;

	public static void main(String[] args) throws InterruptedException {
		System.out.println("期待される合計値：" + EXPECTED_TOTAL);
		System.out.println("--------------------------------------");

		// 1. 悪い例：同期なし(Race Conditionが発生)
		SynchronizationLab.testCounter(new UnsafeCounter(), "悪い例：同期なし");

		// 2. 良い例：synchronized
		SynchronizationLab.testCounter(new SynchronizedCounter(), "良い例：synchronized");

		// 3. 良い例：AtomicInteger
		SynchronizationLab.testCounter(new AtomicIntergerCounter(), "良い例：AtomicInteger");
	}

	public static class SynchronizationLab {

		private static void testCounter(Counter counter, String label) throws InterruptedException {
			ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

			long startTime = System.currentTimeMillis();
			for (int i = 0; i < THREAD_COUNT; i++) {
				executor.submit(() -> {
					for (int j = 0; j < INCREMENTS_PER_THREAD; j++) {
						counter.increment();
					}
				});
			}

			executor.shutdown();
			executor.awaitTermination(1, TimeUnit.MINUTES);
			long endTime = System.currentTimeMillis();

			System.out.printf("%-20s -> 結果: %d (%s) 実行時間: %dms%n",
					label,
					counter.get(),
					(counter.get() == EXPECTED_TOTAL ? "成功" : "失敗"),
					(endTime - startTime));
		}
	}

	// カウンターの共通インターフェース
	interface Counter {
		void increment();

		int get();
	}

	// 悪い例：同期なし
	public static class UnsafeCounter implements Counter {
		private int count = 0;

		public void increment() {
			// count++は「読み込み」「加算」「書き込み」の3ステップに分解されるため
			// 他のスレッドが途中に割り込むと更新が上書きされて消える
			count++;
		}

		public int get() {
			return count;
		}
	}

	// 良い例①：synchronized 
	public static class SynchronizedCounter implements Counter {
		private int count = 0;

		// メソッド全体をロック。1度に1スレッドしか入れない。
		public synchronized void increment() {
			count++;
		}

		public synchronized int get() {
			return count;
		}
	}

	// 良い例②：AtomicInteger
	public static class AtomicIntergerCounter implements Counter {
		private final AtomicInteger count = new AtomicInteger();

		// CPUレベルの原子操作(CAS)を使用するため、ロックより高速な場合が多い
		public void increment() {
			count.incrementAndGet();
		}

		public int get() {
			return count.get();
		}
	}

}
