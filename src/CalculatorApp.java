/**
 * 電卓アプリケーションの起動クラス（エントリーポイント）です。
 * MVCアーキテクチャのコンポーネント（Model, View, Controller）を初期化し、
 * アプリケーションの画像を表示します。
 */
public class CalculatorApp {

	/**
	 * アプリケーションのエントリーポイントです。
	 * 
	 * @param args コマンドライン引数（使用しません）
	 */
	public static void main(String[] args) {
		CalculatorModel model = new CalculatorModel();
		CalculatorFrame frame = new CalculatorFrame();
		CalculatorController controller = new CalculatorController(model, frame);

		frame.setDisplay("0");
		frame.bindController(controller);
		frame.setVisible(true);
	}
}