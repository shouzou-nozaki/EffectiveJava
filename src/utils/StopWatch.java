package utils;

public class StopWatch {
	private long startTime;

	public void start() {
		this.startTime = System.nanoTime();
	}

	public void stop(String label) {
		long endTime = System.nanoTime();
		double duration = (endTime - startTime) / 1_000_000.0;
		System.out.printf("[%s] 実行時間: %.3f ms%n", label, duration);
	}
}
