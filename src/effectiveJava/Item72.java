package effectiveJava;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Item72 {

	public static void main(String[] args) {
		// ①IllegalArgumentException(引数が不正)
		try {
			Calculator.devide(10, 0);
		} catch (IllegalArgumentException e) {
			System.out.println("①引数エラー：" + e.getMessage());
		}

		// ②NullPointerException(nullは許可されない)
		try {
			UserService.printUserName(null);
		} catch (NullPointerException e) {
			System.out.println("②nullエラー：" + e.getMessage());
		}

		// ③IllegalStateException(状態が不正)
		try {
			Machine machine = new Machine();
			machine.run();
		} catch (IllegalStateException e) {
			System.out.println("③状態エラー：" + e.getMessage());
		}

		// ④IndexOutOfBoundsException(範囲外アクセス)
		try {
			List<String> list = new ArrayList<>();
			list.add("A");
			list.get(5);
		} catch (IndexOutOfBoundsException e) {
			System.out.println("④範囲エラー：" + e.getMessage());
		}

		// ⑤UnsupportedOperationException(未対応操作)
		try {
			ReadOnlyConfig config = new ReadOnlyConfig();
			config.setValue("test");
		} catch (UnsupportedOperationException e) {
			System.out.println("⑤未対応エラー：" + e.getMessage());
		}

		// ⑥IOException(外部リソース失敗)
		try {
			FileLoader.load();
		} catch (IOException e) {
			System.out.println("⑥IOエラー：" + e.getMessage());
		}

	}

	// ①IllegalArgumentExecption
	public static class Calculator {
		public static int devide(int a, int b) {
			if (b == 0) {
				throw new IllegalArgumentException("b must not be 0");
			}
			return a / b;
		}
	}

	// ②NullPointerException
	public static class UserService {
		public static void printUserName(String name) {
			if (name == null) {
				throw new NullPointerException("name is null");
			}
			System.out.println(name);
		}
	}

	// ③IllegalStatuException
	public static class Machine {
		private boolean started = false;

		public void start() {
			started = true;
		}

		public void run() {
			if (!started) {
				throw new IllegalStateException("Machine is not started");
			}
			System.out.println("Machine running");
		}
	}

	// ⑤UnsuportedOperationException
	public static class ReadOnlyConfig {
		void setValue(String value) {
			throw new UnsupportedOperationException("Read-only configuration");
		}
	}

	// ⑥IOException
	public static class FileLoader {
		static void load() throws IOException {
			throw new IOException("File not found");
		}
	}
}
