package effectiveJava;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Item80 {
	private static final int TASK_COUNT = 10_000;

	public static void main(String[] args) throws InterruptedException {
		System.out.println("=== 検証開始: Task Count = " + TASK_COUNT + " ===");

		// 1. 生のスレッド (NGパターン) のシミュレーション
		// 注意: 実行環境によっては OutOfMemoryError や OSの制限でクラッシュします
		// verifyRawThreads();

		// 2. ExecutorService による制御 (推奨パターン)
		verifyExecutorService();

		// 3. CompletableFuture による合成 (モダンなパターン)
		verifyCompletableFuture();
	}

	/**
	 * 生のスレッドを乱立させる検証
	 * リソース（メモリ・スタック）の枯渇や、終了管理の難しさを確認
	 */
	static void verifyRawThreads() {
		System.out.println("\n[検証1]生スレッド作成");
		long start = System.currentTimeMillis();
		for (int i = 0; i < TASK_COUNT; i++) {
			Thread t = new Thread(() -> {
				try {
					// 何らかの処理
					Thread.sleep(10);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			});
			t.start();
			// 終了を待機する仕組みが標準でないため、制御が困難
		}

		System.out.println("生スレッドの起動完了(時間は計測不能に近い)");
	}

	/**
	 * ExecutorService による検証
	 * スレッドプールによるリソース制御と例外補足を確認
	 */
	static void verifyExecutorService() throws InterruptedException {
		System.out.println("\n[検証2]ExecutorService(FixedThreadPool)");
		int poolSize = Runtime.getRuntime().availableProcessors();
		ExecutorService executor = Executors.newFixedThreadPool(poolSize);

		AtomicInteger successCount = new AtomicInteger();
		AtomicInteger failureCount = new AtomicInteger();

		long start = System.currentTimeMillis();
		List<Callable<Integer>> tasks = new ArrayList<>();

		for (int i = 0; i < TASK_COUNT; i++) {
			tasks.add(() -> {
				if (Math.random() < 0.1) {
					throw new RuntimeException("エラー発生");
				}
				return 1;
			});
		}

		List<Future<Integer>> futures = executor.invokeAll(tasks);

		for (Future<Integer> f : futures) {
			try {
				successCount.addAndGet(f.get());
			} catch (ExecutionException e) {
				failureCount.incrementAndGet();
			}
		}

		executor.shutdown();
		executor.awaitTermination(1, TimeUnit.MINUTES);

		long end = System.currentTimeMillis();
		System.out.printf("完了時間: %d ms, 成功: %d, 失敗: %d\n",
				(end - start), successCount.get(), failureCount.get());
	}

	/**
	 * CompletableFuture による検証
	 * パイプライン処理と非同期例外ハンドリングを確認
	 */
	static void verifyCompletableFuture() {
		System.out.println("\n[検証3] CompletableFuture (Async Chain)");
		ExecutorService exec = Executors.newFixedThreadPool(10);

		CompletableFuture<String> task = CompletableFuture.supplyAsync(() -> {
			return "Step 1 (Data Fetch)";
		}, exec).thenApplyAsync(res -> {
			return res + " -> Step 2 (Transform)";
		}, exec).handle((res, ex) -> {
			return (ex != null) ? "Recovered from error" : res + " -> Done";
		});

		try {
			System.out.println("Result: " + task.get());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			exec.shutdown();
		}
		System.out.println("非同期チェイン完了");
	}

}
