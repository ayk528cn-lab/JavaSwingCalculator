/**
 * 電卓のユーザー操作を制御するコントローラークラスです。
 * ユーザーからの入力イベント（数字、小数点、演算子、クリア、イコールなど）を受け取り、
 * モデル（CalculatorModel）の操作と画面（CalculatorFrame）の更新を仲介します。
 */
public class CalculatorController {
	private CalculatorModel model;
	private CalculatorFrame frame;

	/**
	 * モデルとフレームを指定してコントローラーを初期化します。
	 * 
	 * @param model 電卓の計算モデル
	 * @param frame 電卓のGUIフレーム
	 */
	public CalculatorController(CalculatorModel model, CalculatorFrame frame) {
		this.model = model;
		this.frame = frame;
	}

	/**
	 * 数字キーが押されたときの処理を行います。
	 * 
	 * @param ch 入力された数字文字
	 */
	public void onDigit(char ch) {
		if (model.appendDigit(ch)) {
			frame.setDisplay(model.getDisplayText());
		}
	}

	/**
	 * 小数点キーが押されたときの処理を行います。
	 */
	public void onDot() {
		if (model.appendDot()) {
			frame.setDisplay(model.getDisplayText());
		}
	}

	/**
	 * 演算子キー（+, -, ×, ÷）が押されたときの処理を行います。
	 * 
	 * @param op 選択された演算子の文字列
	 */
	public void onOperator(String op) {
		if (model.setOperator(op)) {
			frame.setDisplay(model.getDisplayText());
		}
	}

	/**
	 * イコールキー（=）が押されたときの処理を行います。
	 * 計算を実行し、結果を画面に反映します。
	 */
	public void onEquals() {
		model.calculate();
		frame.setDisplay(model.getDisplayText());
	}

	/**
	 * クリアキー（C）が押されたときの処理を行います。
	 * 電卓の状態を初期化し、表示を "0" に戻します。
	 */
	public void onClear() {
		model.clearAll();
		frame.setDisplay(model.getDisplayText());
	}
}