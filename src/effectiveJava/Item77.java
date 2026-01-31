package effectiveJava;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Item77 {

	public static void main(String[] args) {
		System.out.println("=== 1. 悪い例: 例外の握りつぶし ===");
		ExceptionHandlingLab.runBadExample();

		System.out.println("\n=== 2. 良い例: 適切な伝搬と記録 ===");
		try {
			ExceptionHandlingLab.runGoodExample();
		} catch (Exception e) {
			System.out.println("メイン処理でエラーを検知: " + e.getMessage());
		}

	}

	// カスタム例外クラス
	public static class BusinessException extends Exception {
		public BusinessException(String message, Throwable cause) {
			super(message, cause);
		}
	}

	public class ExceptionHandlingLab {
		private static final Logger logger = Logger.getLogger(ExceptionHandlingLab.class.getName());

		// 悪い例：例外を無視
		public static void runBadExample() {
			try {
				Files.readAllLines(Paths.get("non_existent.txt"));
			} catch (Exception e) {
				// 【最悪】何もせず無視。ログも残さない
				// 呼び出し元は成功したと誤解して処理を続けてしまう。
			}
			System.out.println("エラーが発生しているはずだが、成功として処理を続けてしまう。");
		}

		// 良い例：例外を記録し、上位へ伝える
		public static void runGoodExample() throws BusinessException {
			Path path = Paths.get("important_config.txt");
			try {
				Files.readAllLines(path);

			} catch (Exception e) {
				// ログに詳細な文脈を記録(運用面)
				logger.log(Level.SEVERE, "設定ファイルの読み込みに失敗しました。Path:" + path, e);
				// 意味のある例外に包みなおして再送出(呼び出し元に異常を知らせる)
				throw new BusinessException("起動に必要な設定が読み込めませんでした。", e);
			}
		}

	}

}
