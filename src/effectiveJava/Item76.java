package effectiveJava;

import java.util.concurrent.atomic.AtomicReference;

public class Item76 {

	public static void main(String[] args) {
		AtomicityLab lab = new AtomicityLab();

		// 悪い例の実行
		try {
			// 名前だけ変更されてしまう ➡ NG
			lab.updateBadly("NewName", 30);
		} catch (RuntimeException e) {
			System.out.println("Bad Update Failed: " + e.getMessage());
		}
		System.out.println("After Bad Update: name=" + lab.name + ", age=" + lab.age);

		// 良い例の実行
		try {
			// 変更されない ➡ OK
			lab.updateAtomically("NewName", 30);
		} catch (RuntimeException e) {
			System.out.println("Atomic Update Failed: " + e.getMessage());
		}
		UserProfile profile = lab.profileRef.get();
		System.out.println("After Atomic Update: " + lab.name);

	}

	// ユーザーモデル(不変)
	public static class UserProfile {
		public final String name;
		public final int age;

		public UserProfile(String name, int age) {
			this.name = name;
			this.age = age;
		}

		@Override
		public String toString() {
			return "UserProfile{name='" + name + "', age=" + age + "}";
		}
	}

	public static class AtomicityLab {
		//　悪い例の状態保持
		private String name = "OldName";
		private int age = 20;

		// 良い例の状態保持(オブジェクトの原始的な差し替え)
		private final AtomicReference<UserProfile> profileRef = new AtomicReference<>(new UserProfile("OldName", 20));

		// 悪い例：途中でこけると状態が中途半端になる
		public void updateBadly(String newName, int newAge) {
			// 各フィールド値を個別に更新(NG)
			this.name = newName;

			if (true) {
				throw new RuntimeException("年齢変更中にクラッシュ！");
			}
			this.age = newAge;
		}

		// 良い例：新しいオブジェクトを作ってから最後に差し替える
		public void updateAtomically(String newName, int newAge) {
			//　まず新しい状態をメモリ上に作る(OKパターン)
			UserProfile newProfile = new UserProfile(newName, newAge);

			if (true) {
				throw new RuntimeException("差し替え前にクラッシュ！");
			}
			profileRef.set(newProfile);
		}
	}

}
