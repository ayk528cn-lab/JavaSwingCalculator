/**
 * 電卓アプリケーションの起動クラス（エントリーポイント）です。
 * MVCアーキテクチャのコンポーネント（Model, View, Controller）を初期化し、
 * アプリケーションの画面を表示します。
 */
public class CalculatorApp {
	public static void main(String[] args) {
		CalculatorModel model = new CalculatorModel();
		CalculatorFrame frame = new CalculatorFrame();
		CalculatorController controller = new CalculatorController(model, frame);

		frame.setDisplay("0");
		frame.bindController(controller);
		frame.setVisible(true);
	}
}