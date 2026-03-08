package effectiveJava;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

// 1. 【悪い例】普通のシングルトン（デシリアライズで増殖する）
class BrokenSingleton implements Serializable {
	public static final BrokenSingleton INSTANCE = new BrokenSingleton();

	private BrokenSingleton() {
	}
}

// 2. 【改善例】readResolveを使ったシングルトン
class ReadResolveSingleton implements Serializable {
	public static final ReadResolveSingleton INSTANCE = new ReadResolveSingleton();

	private ReadResolveSingleton() {
	}

	// 復元時に実行され、新しく作られた偽物を捨てて本物を返す
	private Object readResolve() {
		return INSTANCE;
	}
}

// 3. 【良い例】enumを使ったシングルトン（言語仕様でガード）
enum EnumSingleton {
	INSTANCE;

	public void doSomething() {
		/* 処理 */
	}
}

public class Item89 {
	public static void main(String[] args) throws Exception {
		System.out.println("=== シングルトンのデシリアライズ検証 ===");

		// --- 検証1: 普通のシングルトン ---
		// ①通常シングルトンパターンでオブジェクトを作成
		BrokenSingleton broken1 = BrokenSingleton.INSTANCE;
		// ②(シリアライズ&)デシリアライズを行うと、元のオブジェクトとは別物になる。
		BrokenSingleton broken2 = serializeAndDeserialize(broken1);
		// ③そのため、以下の判定はfalseになる。
		System.out.println("1. 通常のクラス (Serializableのみ):");
		System.out.println("   同一インスタンスか? : " + (broken1 == broken2)); // false

		// --- 検証2: readResolveあり ---
		// ①通常シングルトンパターンでオブジェクトを作成
		ReadResolveSingleton rr1 = ReadResolveSingleton.INSTANCE;
		// ②(シリアライズ&)デシリアライズを行うと、元のおジェクトとは別者になる。
		//	ただし、ReadResolveSingletonクラスに独自のreadResolveメソッドが宣言されており、
		//	自オブジェクトを返すように制御しているため、同一オブジェクトとなる
		ReadResolveSingleton rr2 = serializeAndDeserialize(rr1);
		// ③同一オブジェクトであるため以下の判定はtrueになる
		System.out.println("2. readResolve実装済み:");
		System.out.println("   同一インスタンスか? : " + (rr1 == rr2)); // true

		// --- 検証3: enumシングルトン ---
		// ①enumによって、単一のオブジェクトを作成
		//	★enumの各定数は「JVMの中でたった一度だけインスタンス化される」と厳格に決まってる。
		EnumSingleton enum1 = EnumSingleton.INSTANCE;
		// ②デシリアライズしても、enumの場合、オブジェクトがもう一つ作られることはない
		EnumSingleton enum2 = serializeAndDeserialize(enum1);
		// ③そのため、同一オブジェクトであるため、以下の判定はtrueになる。
		System.out.println("3. enum型シングルトン:");
		System.out.println("   同一インスタンスか? : " + (enum1 == enum2)); // true

	}

	// シリアライズ&デシリアライズメソッド
	@SuppressWarnings("unchecked")
	private static <T> T serializeAndDeserialize(T obj) throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try (ObjectOutputStream out = new ObjectOutputStream(bos)) {
			out.writeObject(obj);
		}

		ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
		try (ObjectInputStream in = new ObjectInputStream(bis)) {
			return (T) in.readObject();
		}
	}
}