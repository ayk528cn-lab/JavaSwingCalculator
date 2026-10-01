/**
 * アプリケーションで発生したエラーの状態管理と、エラーメッセージの保持を行うクラスです。
 */
public class ErrorHandler {
	private boolean hasError = false;
	private String errorMessage = "エラー";

	/**
	 * 発生した例外を処理し、エラー状態とメッセージを更新します。
	 *  
	 * @param e 発生した例外
	 */
	public void handle(Exception e) {
		hasError = true;
		if (e instanceof DivisionByZeroException) {
			errorMessage = e.getMessage();
		} else {
			errorMessage = "エラー";
		}
	}

	/**
	 * 現在エラーが発生しているかどうかを返します。
	 * 
	 * @return エラーが発生している場合は true、それ以外は false
	 */
	public boolean isError() {
		return hasError;
	}

	/**
	 * 設定されているエラーメッセージを取得します。
	 * 
	 * @return エラーメッセージの文字列
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * エラー状態とメッセージを初期状態にリセットします。
	 */
	public void allClear() {
		hasError = false;
		errorMessage = "エラー";
	}
}