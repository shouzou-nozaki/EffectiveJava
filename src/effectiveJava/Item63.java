package effectiveJava;

import utils.StopWatch;

public class Item63 {
	public static void main(String[] args) {
		Item63 lab = new Item63(); // 自分のインスタンスを作る
		StopWatch sw = new StopWatch();

		// 悪い例
		sw.start();
		lab.slowStatement(); // インスタンス経由で呼ぶ
		sw.stop("String (+=)");

		// 良い例
		sw.start();
		lab.fastStatement(); // インスタンス経由で呼ぶ
		sw.stop("StringBuilder");

		// 結果：
		//	[String (+=)] 実行時間: 201.767 ms
		//	[StringBuilder] 実行時間: 1.493 ms
	}

	private int numItems() {
		return 10_000;
	}

	private String lineForItem(int i) {
		return "Item description " + i + "\n";
	}

	// 悪い例
	public String slowStatement() {
		String result = "";
		for (int i = 0; i < numItems(); i++) {
			result += lineForItem(i);
		}
		return result;
	}

	// 良い例
	public String fastStatement() {
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < numItems(); i++) {
			b.append(lineForItem(i));
		}
		return b.toString();
	}
}