package effectiveJava;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Item83 {

	public static void main(String[] args) throws InterruptedException {
		int threadCount = 10;

		System.out.println("=== 検証1: 同期なし (UnsafeLazy) ===");
		// 同期なしの場合、複数回コンストラクタが走る可能性がある
		runVerification(threadCount, () -> {
			UnsafeLazy instance = new UnsafeLazy();
			instance.get();
		});

		System.out.println("\n=== 検証2: Holder idiom (SafeHolder) ===");
		// Holder idiom の場合、JVMが絶対に1回であることを保証する
		runVerification(threadCount, () -> {
			SafeHolder.getInstance();
		});
	}

	/**
	 * 指定された初期化タスクを複数のスレッドで一斉に実行し、結果を検証する
	 */
	private static void runVerification(int threadCount, Runnable task) throws InterruptedException {
		HeavyResource.resetCount();
		ExecutorService exec = Executors.newFixedThreadPool(threadCount);

		// 一斉スタート用のラッチ
		CountDownLatch startLatch = new CountDownLatch(1);
		CountDownLatch doneLatch = new CountDownLatch(threadCount);

		for (int i = 0; i < threadCount; i++) {
			exec.submit(() -> {
				try {
					startLatch.await(); // ここで全員待機
					task.run();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				} finally {
					doneLatch.countDown();
				}
			});
		}

		startLatch.countDown(); // 一斉スタート！
		doneLatch.await(); // 全員の終了を待つ
		exec.shutdown();

		System.out.println("コンストラクタが呼ばれた回数: " + HeavyResource.getCount());
		if (HeavyResource.getCount() > 1) {
			System.out.println("結果: 【NG】レース条件が発生し、複数回初期化されました。");
		} else {
			System.out.println("結果: 【OK】正しく1回だけ初期化されました。");
		}
	}

	// --- 検証用のクラス群 ---

	static class HeavyResource {
		private static final AtomicInteger constructorCount = new AtomicInteger(0);

		public HeavyResource() {
			constructorCount.incrementAndGet();
			// 初期化が重いことをシミュレート
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
			}
		}

		public static int getCount() {
			return constructorCount.get();
		}

		public static void resetCount() {
			constructorCount.set(0);
		}
	}

	// 悪い例: 同期なし
	static class UnsafeLazy {
		private HeavyResource resource;

		public HeavyResource get() {
			if (resource == null) { // 複数のスレッドが同時にここを通過してしまう
				resource = new HeavyResource();
			}
			return resource;
		}
	}

	// 良い例: Holder idiom
	static class SafeHolder {
		private SafeHolder() {
		}

		private static class Holder {
			// クラスがロードされるタイミングで一度だけ初期化される
			static final HeavyResource INSTANCE = new HeavyResource();
		}

		public static HeavyResource getInstance() {
			return Holder.INSTANCE;
		}
	}
}