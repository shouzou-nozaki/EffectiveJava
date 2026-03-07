package effectiveJava;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Date;

// 1. 防御を固めた Period クラス
class Period implements Serializable {
	private Date start;
	private Date end;

	public Period(Date start, Date end) {
		// コンストラクタでのチェック
		if (start.compareTo(end) > 0) {
			throw new IllegalArgumentException("開始日が終了日より後です！");
		}
		this.start = new Date(start.getTime());
		this.end = new Date(end.getTime());
	}

	// ★ 独自のreadObject
	private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
		s.defaultReadObject();

		// 防御的コピー
		start = new Date(start.getTime());
		end = new Date(end.getTime());

		// コンストラクタと同様にデータチェックを行うことで、
		// 不正データを流出させない
		if (start.compareTo(end) > 0) {
			throw new InvalidObjectException("【防御成功】開始日が終了日より後です: " + start + " > " + end);
		}
	}

	@Override
	public String toString() {
		return "Period { start=" + start + ", end=" + end + " }";
	}
}

// 2. 検証メインクラス
public class Item88 {
	public static void main(String[] args) throws Exception {
		// 攻撃用の改ざんされたデータを作成
		byte[] serializedData = createIllegalByteArray();

		System.out.println("=== デシリアライズ検証開始 ===");
		try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(serializedData))) {

			// Periodクラスに独自のreadObjectメソッドがない場合は、
			// デフォルトのreadObjectが呼ばれるため、不整合データをチェックできない
			Period p = (Period) in.readObject();

			// readObject のチェックを通り抜けてしまった場合のみここが表示される
			System.err.println("【警告】不正なオブジェクトが復元されてしまいました！");
			System.err.println(p);

		} catch (InvalidObjectException e) {
			// readObject のチェックで例外が投げられた場合
			System.out.println(e.getMessage());
			System.out.println(">>> 検証結果: readObject が不正な入力を正しくブロックしました。");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// バイト列をピンポイントで改ざんするメソッド
	private static byte[] createIllegalByteArray() throws Exception {
		// 正常な期間 (start: 123,456,789ms, end: 987,654,321ms) で作成
		long startTime = 123456789L;
		long endTime = 987654321L;
		Period normal = new Period(new Date(startTime), new Date(endTime));

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(bos);
		out.writeObject(normal);
		out.close();

		byte[] bytes = bos.toByteArray();

		// 【改ざんロジック】
		// startTime (123456789) をバイト配列から探し、
		// endTime (987654321) よりも大きな値 (2,000,000,000) に書き換える
		byte[] target = ByteBuffer.allocate(8).putLong(startTime).array();
		byte[] replacement = ByteBuffer.allocate(8).putLong(2000000000L).array();

		for (int i = 0; i < bytes.length - 8; i++) {
			boolean match = true;
			for (int j = 0; j < 8; j++) {
				if (bytes[i + j] != target[j]) {
					match = false;
					break;
				}
			}
			if (match) {
				// 見つけた場所を 2,000,000,000ms（未来）に書き換え！
				System.arraycopy(replacement, 0, bytes, i, 8);
				break;
			}
		}
		return bytes;
	}
}