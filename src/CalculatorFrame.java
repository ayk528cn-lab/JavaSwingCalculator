import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * 電卓のGUI画面を担当するフレームクラスです。
 * Java Swingを用いて電卓のレイアウトやボタン、ディスプレイを構築します。
 */
public class CalculatorFrame extends JFrame {
	private JLabel displayLabel;
	private CalculatorController controller;

	/**
	 * 電卓のウィンドウフレームを初期化します。
	 */
	public CalculatorFrame() {
		setTitle("Calculator");
		setSize(300, 400);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());

		displayLabel = new JLabel("0", JLabel.RIGHT);
		displayLabel.setFont(new Font("Arial", Font.PLAIN, 32));
		add(displayLabel, BorderLayout.NORTH);
	}

	/**
	 * 電卓のウィンドウフレームを初期化します。
	 * 
	 * @param text 表示する文字列
	 */
	public void setDisplay(String text) {
		displayLabel.setText(text);
	}

	/**
	 * コントローラーをバインドし、キーパッド（ボタン群）を生成・配置します。
	 * 
	 * @param c 連携するコントローラー
	 */
	public void bindController(CalculatorController c) {
		this.controller = c;
		JPanel keypadPanel = new JPanel();
		keypadPanel.setLayout(new GridLayout(5, 4, 4, 4)); /** 行, 列, 水平間隔, 垂直間隔 */

		/** ボタンのラベル配列 */
		String[] buttons = {
				"7", "8", "9", "÷",
				"4", "5", "6", "×",
				"1", "2", "3", "-",
				"0", ".", "=", "+",
				"C"
		};
		/** フレームに追加 */
		add(keypadPanel, BorderLayout.CENTER);
		revalidate();
		repaint();

		/** ボタンの追加 */
		for (String text : buttons) {
			JButton button = new JButton(text);

			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String clickedButton = button.getText();

					if ("+-×÷".contains(clickedButton)) {
						controller.onOperator(clickedButton);
					} else if ("=".contains(clickedButton)) {
						controller.onEquals();
					} else if (clickedButton.equals("C")) {
						controller.onClear();
					} else if (clickedButton.equals(".")) {
						controller.onDot();
					} else {
						controller.onDigit(clickedButton.charAt(0));
					}
				}
			});
			keypadPanel.add(button);
		}
	}
}