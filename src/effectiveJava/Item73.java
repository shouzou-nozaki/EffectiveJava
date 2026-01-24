package effectiveJava;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Item73 {

	public static void main(String[] args) {
		Storage storage = new FileStorage(Path.of("/invalid/path"));
		try {
			storage.save("data.txt", "hello".getBytes());
		} catch (StorageException e) {
			System.out.println("抽象レベルの例外を捕捉できた。");
			System.out.println("message：" + e.getMessage());
			System.out.println("cause：" + e.getCause());
		}
	}

	/* ========= 抽象レベル ========= */

	/**
	* 抽象概念: データを保存する
	* → ファイルかDBかネットワークかは利用者は知らない
	*/
	interface Storage {
		void save(String name, byte[] data) throws StorageException;
	}

	/**
	 * 抽象概念に対応した例外
	 */
	static class StorageException extends RuntimeException {
		public StorageException(String message, Throwable cause) {
			super(message, cause);
		}
	}

	/* ========= 実装レベル ========= */

	/**
	 * ファイルシステム実装（低レイヤー）
	 */
	static class FileStorage implements Storage {
		private final Path baseDir;

		FileStorage(Path baseDir) {
			this.baseDir = baseDir;
		}

		@Override
		public void save(String name, byte[] data) throws StorageException {
			try {
				Files.write(baseDir.resolve(name), data);
			} catch (IOException e) {
				// 低レイヤー例外を抽象例外に翻訳する
				throw new StorageException("保存に失敗しました。：" + name, e);
			}
		}
	}
}
