package effectiveJava;

public class Item66 {

	public static void main(String[] args) {

	}

	public interface Crypto {
		byte[] hash(byte[] data);
	}

	public static class CryptoException extends RuntimeException {
		public CryptoException(String message, Throwable cause) {
			super(message, cause);
		}
	}

	// Java実装(フォールバック)
	public static class JavaCrypto implements Crypto {
		@Override
		public byte[] hash(byte[] data) {
			try {
				var md = java.security.MessageDigest.getInstance("SHA-256");
				return md.digest(data);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

		}
	}

	// ネイティブ実装ラッパー(薄いFacade)
	public static class NativeCrypto implements Crypto {
		static {
			try {
				System.loadLibrary("nativeCrypto");
			} catch (UnsatisfiedLinkError e) {
				// ロード失敗は起動時に検出して呼び出し側がフォールバック可能にする
				throw new ExceptionInInitializerError(e);
			}
		}

		// native メソッドは private で小さく保つ(直接公開しない)
		private static native byte[] nativeHash(byte[] data);

		@Override
		public byte[] hash(byte[] data) throws CryptoException {
			if (data == null) {
				throw new NullPointerException("data");
			}

			try {
				return nativeHash(data);
			} catch (RuntimeException e) {
				throw new CryptoException("native hash failed", e);
			}

		}

	}
}
