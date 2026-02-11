package effectiveJava;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;

public class Item85 {

	public static void main(String[] args) {
		try {
			// 正しい期間データを作成
			Date start = new Date();
			Date end = new Date(start.getTime() + 100000);
			SafePeriod period = new SafePeriod(start, end);

			System.out.println("--- 正常系テスト ---");
			byte[] serialized = serialize(period);
			SafePeriod deserialized = (SafePeriod) deserialize(serialized);
			System.out.println("復元成功: " + deserialized);

			// 2. 脆弱性の検証（概念）
			System.out.println("\n--- 堅牢性の検証 ---");
			System.out.println("このクラスは readObject を禁止しているため、");
			System.out.println("外部から不正なバイト列を送り込まれても、");
			System.out.println("直接復元されることはありません。");

		} catch (Exception e) {
			System.err.println("意図したエラーまたは例外が発生しました: " + e.getMessage());
		}
	}

	// --- シリアライゼーション・プロキシ・パターンを適用したクラス ---
	public static final class SafePeriod implements Serializable {
		private final Date start;
		private final Date end;

		public SafePeriod(Date start, Date end) {
			if (start.compareTo(end) > 0) {
				throw new IllegalArgumentException(start + " が " + end + " より後です");
			}
			this.start = new Date(start.getTime());
			this.end = new Date(end.getTime());
		}

		// 内部のデータだけを保持するプロキシ
		private static class SerializationProxy implements Serializable {
			private final Date start;
			private final Date end;
			private static final long serialVersionUID = 1L;

			SerializationProxy(SafePeriod p) {
				this.start = p.start;
				this.end = p.end;
			}

			// 復元時に呼ばれる。ここで「正規のコンストラクタ」が走る！
			private Object readResolve() {
				// SafePeriodのコンストラクタ経由でオブジェクトが作られるので、
				// 不正データが作られず安全!!!
				return new SafePeriod(start, end);
			}
		}

		// シリアライズ時は自分を隠してプロキシを渡す
		private Object writeReplace() {
			return new SerializationProxy(this);
		}

		// デフォルトのデシリアライズを明示的にブロック
		private void readObject(ObjectInputStream s) throws InvalidObjectException {
			// SafePeriod自体がシリアライズされたときの保険用
			throw new InvalidObjectException("プロキシが必要です (裏口は閉鎖されています)");
		}

		@Override
		public String toString() {
			return start + " - " + end;
		}
	}

	// --- 通信・保存の代わりとなるユーティリティ ---
	private static byte[] serialize(Object obj) throws IOException {
		ByteArrayOutputStream bao = new ByteArrayOutputStream();
		try (ObjectOutputStream oos = new ObjectOutputStream(bao)) {
			// ここで実行されるのは、SafePeriodのwriteReplaceメソッドになる(Javaのシリアライズ機構の裏仕様)
			// writeReplaceというメソッドは予約語で、ObjectOutputStream.writeObjet(obj)が呼ばれると、
			// このobjにはwriteReplaceがあるかと毎回チェックする
			oos.writeObject(obj);
		}
		// SafePeriod本体のバイト配列ではなく、プロキシのバイト配列が返される
		return bao.toByteArray();
	}

	private static Object deserialize(byte[] bytes) throws Exception {
		try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
			// 実行されるのは、プロキシのreadObjectメソッド
			return ois.readObject();
		}
	}
}