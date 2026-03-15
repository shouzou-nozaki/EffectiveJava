package effectiveJava;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

// 1. プロキシパターンを適用したクラス
class Period2 implements Serializable {
	private final long start;
	private final long end;

	public Period2(long start, long end) {
		// コンストラクタが呼ばれたことを証明
		System.out.println(">>> Periodコンストラクタ実行: " + start + " to " + end);

		if (start > end) {
			throw new IllegalArgumentException("開始 > 終了");
		}
		this.start = start;
		this.end = end;
	}

	// ① シリアライズ時：本物の代わりに「身代わり（Proxy）」を送り出す
	private Object writeReplace() {
		System.out.println("--- writeReplace: Proxyを生成します ---");
		return new SerializationProxy(this);
	}

	// ② 直接のデシリアライズを禁止
	// ※ シリアライズしたオブジェクトをそのまま復元しようとした際の防御用
	private void readObject(ObjectInputStream s) throws InvalidObjectException {
		throw new InvalidObjectException("Proxy required (直接の復元は禁止されています)");
	}

	// ③ シリアライズ・プロキシクラス
	private static class SerializationProxy implements Serializable {
		private final long start;
		private final long end;

		SerializationProxy(Period2 p) {
			this.start = p.start; // フィールドの値をコピー
			this.end = p.end;
		}

		// ④ 復元時：保存されたデータを使って「本物のコンストラクタ」を呼ぶ
		private Object readResolve() {
			System.out.println("--- readResolve: Proxyから本物を再構築します ---");
			return new Period2(start, end); // ここでコンストラクタが動く
		}

		private static final long serialVersionUID = 234098230498L; // 任意
	}

	@Override
	public String toString() {
		return "Period [start=" + start + ", end=" + end + "]";
	}
}

// 2. 検証メイン
public class Item90 {
	public static void main(String[] args) throws Exception {
		System.out.println("=== シリアライズ・プロキシ検証開始 ===");

		Period2 p = new Period2(100, 200);

		// シリアライズ実行
		byte[] serialized = serialize(p);
		System.out.println("\nシリアライズ完了（バイト列になりました）\n");

		// デシリアライズ実行
		System.out.println("デシリアライズを開始します...");
		Period2 p2 = (Period2) deserialize(serialized);

		System.out.println("\n復元されたオブジェクト: " + p2);
		System.out.println("同一性確認: " + (p != p2)); // インスタンスは別物
		System.out.println("=== 検証終了 ===");
	}

	private static byte[] serialize(Object o) throws IOException {
		ByteArrayOutputStream bao = new ByteArrayOutputStream();
		try (ObjectOutputStream oos = new ObjectOutputStream(bao)) {
			// ①writeRelaceが呼ばれ、プロキシを取得
			oos.writeObject(o);
		}
		return bao.toByteArray();
	}

	private static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
		try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
			// 中には、プロキシが入っているため
			// プロキシクラスの②readResolveが実行される
			return ois.readObject();
		}
	}
}