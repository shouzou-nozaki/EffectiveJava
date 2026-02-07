package effectiveJava;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Item81 {

	public static void main(String[] args) throws InterruptedException {
		// 1. 手作りバッファ (wait/notify) の検証
		verifyManualBuffer();

		// 2. BlockingQueue (高級ユーティリティ) の検証
		verifyBlockingQueue();

		// 3. CountDownLatch (同期待ち合わせ) の検証
		verifyCountDownLatch();
	}

	/**
	 * [検証1] wait/notify による手作りバッファ
	 * ※ 注意: notifyAllを忘れたり、whileをifにしたりすると即座にバグります。
	 */
	static void verifyManualBuffer() throws InterruptedException {
		System.out.println("\n--- [検証1] Manual wait/notify ---");
		BadBuffer buffer = new BadBuffer();

		// 1人で作って、1人で使う
		Thread producer = new Thread(() -> {
			for (int i = 0; i < 5; i++) {
				buffer.put("Data-" + i);
			}
		});
		Thread consumer = new Thread(() -> {
			for (int i = 0; i < 5; i++) {
				System.out.println("Consumed: " + buffer.take());
			}
		});

		producer.start();
		consumer.start();
		producer.join();
		consumer.join();
	}

	/**
	 * [検証2] BlockingQueue による実装
	 * ロック管理や条件待ちを自前で書く必要が一切ない。
	 */
	static void verifyBlockingQueue() throws InterruptedException {
		System.out.println("\n--- [検証2] ArrayBlockingQueue ---");
		// サイズ10の安全な行列
		BlockingQueue<String> queue = new ArrayBlockingQueue<>(10);

		Thread producer = new Thread(() -> {
			try {
				for (int i = 0; i < 5; i++) {
					queue.put("SafeData-" + i);
					System.out.println("Produced: SafeData-" + i);
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		});

		Thread consumer = new Thread(() -> {
			try {
				for (int i = 0; i < 5; i++) {
					System.out.println("Consumed: " + queue.take());
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		});

		producer.start();
		consumer.start();
		producer.join();
		consumer.join();
	}

	/**
	 * [検証3] CountDownLatch による「一斉スタート」と「完了待ち」
	 */
	static void verifyCountDownLatch() throws InterruptedException {
		System.out.println("\n--- [検証3] CountDownLatch ---");
		int workers = 3;
		CountDownLatch startLatch = new CountDownLatch(1); // よーいドン！の合図
		CountDownLatch doneLatch = new CountDownLatch(workers); // 全員の完了通知

		ExecutorService exec = Executors.newFixedThreadPool(workers);

		for (int i = 0; i < workers; i++) {
			exec.submit(() -> {
				try {
					System.out.println(Thread.currentThread().getName() + " が待機中...");
					startLatch.await(); // 開始の合図まで全員ストップ
					System.out.println(Thread.currentThread().getName() + " が走り出しました！");
					Thread.sleep((long) (Math.random() * 1000));
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				} finally {
					System.out.println(Thread.currentThread().getName() + "がゴールしました。");
					doneLatch.countDown(); // 終わったよ！
				}
			});
		}

		Thread.sleep(1000);
		System.out.println("=== ゲートオープン！ ===");
		startLatch.countDown(); // ここで全員が一斉に動き出す

		doneLatch.await(); // 全員が終わるのを待つ
		System.out.println("全員がゴールしました。");
		exec.shutdown();
	}
}

/**
 * 低レベルな wait/notify を使ったバッファの実装（Bad Example の再現）
 */
class BadBuffer {
	private final Queue<String> q = new LinkedList<>();
	private final int CAP = 10;

	public synchronized void put(String s) {
		// while を使わないと「スプリアス・ウェイクアップ」が起きる
		while (q.size() == CAP) {
			try {
				wait();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		q.add(s);
		System.out.println("Produced: " + s);
		notifyAll();// 指定のコンシューマーだけ起こすことはできないため全起こし
	}

	public synchronized String take() {
		// whileを使わないと、「スプリアス・ウェイクアップ」が起きる
		while (q.isEmpty()) {
			try {
				wait();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		String r = q.remove();
		notifyAll(); // 指定のコンシューマーだけ起こすことはできないため全起こし
		return r;
	}

}
