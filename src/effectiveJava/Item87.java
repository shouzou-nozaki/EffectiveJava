package effectiveJava;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;

public class Item87 {

	public static void main(String[] args) {
		try {
			System.out.println("=== 項目87: カスタムシリアライズ形式の検証 ===");

			// 正常なユーザーの作成
			CustomUser user = new CustomUser("Nozaki", 25, new Date());
			System.out.println("元のオブジェクト: " + user);

			// シリアライズ（保存）
			byte[] data = serialize(user);
			System.out.println("シリアライズ完了。サイズ: " + data.length + " バイト");

			// デシリアライズ（復元）
			CustomUser recovered = (CustomUser) deserialize(data);
			System.out.println("復元されたオブジェクト: " + recovered);

			// 不正データの検証（バリデーションが機能するか）
			System.out.println("\n=== バリデーションの検証 ===");
			try {
				new CustomUser("攻撃者", -1, new Date());
			} catch (IllegalArgumentException e) {
				System.out.println("期待通りのエラーを検知: " + e.getMessage());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * カスタム形式を実装したクラス
	 */
	public static class CustomUser implements Serializable {
		private static final long serialVersionUID = 1L;

		// 【ポイント1】transient（一時的）キーワード
		// これを付けると Java 標準の「丸投げシリアライズ」の対象外になります。
		// フィールド構成が変わっても、外部への保存形式を固定するために必須です。
		// transientにするべきか否かは、
		// オブジェクトの本質的な意味(データ)かどうかで判断する。
		// 今回のような、name, age, birthday は基本的にはtransientではない
		// ただ、transientにした方が将来起こりうる変更時の柔軟性が高いため
		// ★基本的には、全てtransientにすることが推奨されている。
		private transient String name;
		private transient int age;
		private transient Date birthday;

		public CustomUser(String name, int age, Date birthday) {
			validate(name, age);
			this.name = name;
			this.age = age;
			this.birthday = new Date(birthday.getTime()); // 防御的コピー
		}

		private void validate(String name, int age) {
			if (age < 0)
				throw new IllegalArgumentException("年齢に負の値は設定できません");
			if (name == null)
				throw new IllegalArgumentException("名前を null にすることはできません");
		}

		/**
		 * 【ポイント2】独自のシリアライズ（書き出し）
		 * クラスの内部フィールドをそのまま出すのではなく、
		 * 「名前、年齢、誕生日のミリ秒」という論理的なデータだけを書き出します。
		 */
		private void writeObject(ObjectOutputStream out) throws IOException {
			// 将来 non-transient なフィールドが追加された時のため、お作法として呼ぶ
			out.defaultWriteObject();

			// 自分で決めた「仕様（フォーマット）」に従って書き出す
			out.writeUTF(name);
			out.writeInt(age);
			out.writeLong(birthday.getTime());
		}

		/**
		 * 【ポイント3】独自のデシリアライズ（読み込み）
		 * 単に値を戻すだけでなく、復元時に必ずバリデーションを通します。
		 */
		private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
			// defaultWriteObject() で書き出した分を読み込む
			in.defaultReadObject();

			// 書き出した順序（UTF -> Int -> Long）で読み込む
			String name = in.readUTF();
			int age = in.readInt();
			long time = in.readLong();

			// ★ ここが重要！復元したデータが正しいかチェックする
			validate(name, age);

			// フィールドに値をセット
			this.name = name;
			this.age = age;
			this.birthday = new Date(time);
		}

		@Override
		public String toString() {
			return String.format("User[名前=%s, 年齢=%d, 誕生日=%s]", name, age, birthday);
		}
	}

	// --- 検証用ユーティリティ ---
	private static byte[] serialize(Object obj) throws IOException {
		ByteArrayOutputStream bao = new ByteArrayOutputStream();
		try (ObjectOutputStream oos = new ObjectOutputStream(bao)) {
			oos.writeObject(obj);
		}
		return bao.toByteArray();
	}

	private static Object deserialize(byte[] bytes) throws Exception {
		try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
			// CostomUserバイト配列が入ってくるため、
			// ここで実行されるのは、CostomUserのreadObject
			// 以下、処理の流れ
			//  1. osi.readObject()
			//  2. ストリームから「これはCustomUserクラスのデータだ」と判断する
			//  3. JavaがCustomUserクラスの中に private void readObject(ObjectInputStream in)
			//     という特定のシグネチャを持つメソッドがあるかをこっそり探す
			//  4. 見つかったら、Javaが自分自身(osi)を引数にセットして、そのメソッドを強制的に実行する。
			return ois.readObject();
		}
	}
}