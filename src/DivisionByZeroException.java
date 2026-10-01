/**
 * 0による除算（割り算）が行われた際にスローされる例外クラスです。
 */
public class DivisionByZeroException extends ArithmeticException {

	/**
	 * デフォルトのエラーメッセージ「エラー」を設定して例外を初期化します。
	 */
	public DivisionByZeroException() {
		super("エラー");
	}
}