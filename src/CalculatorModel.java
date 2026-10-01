import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * 電卓の実装と状態管理を担当するモデルクラス
 * 四則演算の計算、入力データのバリデーション、エラーハンドリングを管理します。
 * 
 */
public class CalculatorModel {
	private String firstNum = "";
	private String secondNum = "";
	private Operator operator = null;

	private InputState state = InputState.READY;

	private ErrorHandler errorHandler = new ErrorHandler();
	private static final int maxDigits = 8;

	/**
	 * 数字（0〜9）が入力されたときの処理を行います。
	 * 
	 * @param ch　入力された文字
	 * @return　入力が成功した場合は true、桁数オーバーや無効な場合は false
	 */
	public boolean appendDigit(char ch) {
		prepareForInput();

		String target = (operator == null) ? firstNum : secondNum;

		if (target.equals("0") && ch == '0') {
			return false;
		}
		if (target.equals("-0") && ch == '0') {
			return false;
		}

		int effectiveLength = target.startsWith("-") ? target.length() - 1 : target.length();
		if (effectiveLength >= maxDigits) {
			return false;
		}

		if (operator == null) {
			firstNum += ch;
		} else {
			secondNum += ch;
		}
		state = InputState.INPUT_NUMBER;
		return true;
	}

	/**
	 * 小数点（.）が入力されたときの処理を行います。
	 * 
	 * @return　小数点の追加に成功した場合は true、すでに含まれている場合や桁数オーバーは　false
	 */
	public boolean appendDot() {
		prepareForInput();

		String target = (operator == null) ? firstNum : secondNum;

		/** 1. すでに小数点が入力されている場合は追加しない */
		if (target.contains(".")) {
			return false;
		}
		/** 2. 桁数制限を確認（小数点も1桁としてカウントする場合）*/
		int effectiveLength = target.startsWith("-") ? target.length() - 1 : target.length();
		if (effectiveLength >= maxDigits) {
			return false;
		}

		/** 3. 小数点の追加処理 */
		if (operator == null) {
			if (firstNum.isEmpty()) {
				firstNum = "0.";
			} else {
				firstNum += ".";
			}
		} else {
			if (secondNum.isEmpty()) {
				secondNum = "0.";
			} else {
				secondNum += ".";
			}
		}
		state = InputState.INPUT_NUMBER;
		return true;
	}

	/**
	 * 現在入力中の最初の数値を返します。
	 * 
	 * @return　firstNum の文字列
	 */
	public String getCurrentInput() {
		return firstNum;
	}

	/**
	 * 演算子（+, -, ×, ÷）が選択されたときの処理を行います。
	 * 
	 * @param op 選択された演算子
	 * @return 演算子の設定に成功した場合は true、無効な場合は false
	 */
	public boolean setOperator(String op) {
		if (state == InputState.ERROR) {
			return false;
		}
		if (state == InputState.AFTER_RESULT) {
			state = InputState.INPUT_NUMBER;
		}

		/** "-" だけの状態でさらに演算子が押されたら無視する */
		if (firstNum.equals("-") || secondNum.equals("-")) {
			return false;
		}
		/** 最初が空または "0" のとき、"-" なら負号（マイナス）として受け付ける */
		if (firstNum.isEmpty() || firstNum.equals("0")) {
			if (op.equals("-")) {
				firstNum = "-";
				state = InputState.INPUT_NUMBER;
				return true;
			} else {
				return false;
			}
		}
		/** すでに数式（1つ目の数字、演算子、2つ目の数字）が揃っている状態で演算子が押された場合、一度中間計算を行う */
		if (!firstNum.isEmpty() && !secondNum.isEmpty() && operator != null) {
			calculate();
			state = InputState.INPUT_NUMBER;
		}
		/** 文字列の演算子を Operator 列挙型に変換 */
		Operator newOp = null;
		switch (op) {
		case "+":
			newOp = Operator.ADD;
			break;
		case "-":
			newOp = Operator.SUB;
			break;
		case "×":
			newOp = Operator.MUL;
			break;
		case "÷":
			newOp = Operator.DIV;
			break;
		default:
			return false;
		}
		this.operator = newOp;
		state = InputState.INPUT_OPERATOR;
		return true;
	}

	/**
	 * 設定されている演算子と数値を用いて四則演算の計算を実行します。
	 * 
	 * @return 計算結果の数値
	 * @throws DivisionByZeroException 0で割る除算が行われた場合
	 */
	public double apply() throws DivisionByZeroException {
		double n1 = Double.parseDouble(firstNum);
		double n2 = Double.parseDouble(secondNum);

		/** Operator イーナムで分岐する */
		switch (operator) {
		case ADD:
			return n1 + n2;
		case SUB:
			return n1 - n2;
		case MUL:
			return n1 * n2;
		case DIV:
			/** 0除算のチェック */
			if (n2 == 0 || n2 == 0.0) {
				throw new DivisionByZeroException();
			}
			return n1 / n2;
		default:
			return n1;
		}
	}

	/**
	 * 計算処理全体を統括し、結果のフォーマットやエラー遷移、状態遷移を行います。
	 * 
	 * @return 計算結果の文字列（またはエラーメッセージ）
	 */
	public String calculate() {
		if (firstNum.isEmpty() || secondNum.isEmpty() || operator == null) {
			return firstNum;
		}

		double result = 0;
		try {
			result = apply();
		} catch (DivisionByZeroException e) {
			errorHandler.handle(e);
			state = InputState.ERROR;
			firstNum = errorHandler.getErrorMessage(); /** "エラー" */
			secondNum = "";
			operator = null;
			return firstNum;
		}

		/**　9桁を超える場合（または非常に小さい場合）の指数表記変換 */
		String resultStr;

		if (Math.abs(result) >= Math.pow(10, maxDigits) || (result != 0 && Math.abs(result) < 1e-7)) {
			/** Locale.US を指定することで、環境によって小数点の文字（, や .）が変わるのを防ぎます */
			DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
			DecimalFormat decimalformat = new DecimalFormat("0.0000000E0", symbols);

			resultStr = decimalformat.format(result).replace('E', 'e');

			if (resultStr.contains("e")) {
				String[] parts = resultStr.split("e");
				String base = parts[0];
				String exp = parts[1];

				/** 末尾の不要な ".0" や "0" をトリムする */
				if (base.contains(".")) {
					/** 整数値として扱える場合（例: 10.0 -> 10　／　10.500 -> 10.5) */
					base = base.replaceAll("0+$", "");
					if (base.endsWith(".")) {
						base += "0";
					}
				}
				resultStr = base + "e" + exp;
			}
		} else {
			resultStr = String.valueOf(result);
			if (resultStr.contains(".")) {
				resultStr = resultStr.replaceAll("0+$", "");
				resultStr = resultStr.replaceAll("\\.$", "");
			}
		}

		/** 計算結果を firstNum に戻して再利用可能にする */
		firstNum = resultStr;
		secondNum = "";
		operator = null; /** リセット時は null にする */
		state = InputState.AFTER_RESULT;
		return firstNum;
	}

	/**
	 * すべての状態を初期状態（READY）にリセットします。
	 */
	public void clearAll() {
		this.firstNum = "";
		this.secondNum = "";
		this.operator = null;
		this.state = InputState.READY;
		errorHandler.allClear();
	}

	/**
	 * 画面（UI）に表示すべき文字列を取得します。
	 * 
	 * @return ディスプレイに表示する文字列
	 */
	public String getDisplayText() {
		if (state == InputState.ERROR) {
			return errorHandler.getErrorMessage();
		}
		if (operator == null) {
			return firstNum.isEmpty() ? "0" : firstNum;
		}
		String opStr = getOperatorSymbol(operator);
		/** 記号に変換する */
		/** 演算子が入力された状態で、２番目の数字が空の場合 */
		if (secondNum.isEmpty()) {
			return firstNum + " " + opStr;
		}
		/** 演算子と２番目の数字がある場合 */
		return firstNum + " " + opStr + " " + secondNum;
	}

	/**
	 * 入力操作を受け付ける前の事前準備を行い、必要に応じて状態をリセットまたは遷移させます。
	 */
	private void prepareForInput() {
		if (state == InputState.ERROR || state == InputState.AFTER_RESULT) {
			clearAll();
		} else if (state == InputState.READY || state == InputState.INPUT_OPERATOR) {
			state = InputState.INPUT_NUMBER;
		}
	}

	/**
	 * Operator 列挙型を対応する画面表示用の記号文字列に変換します。
	 * 
	 * @param op 変換する Operator
	 * @return 変換する Operator
	 */
	private String getOperatorSymbol(Operator op) {
		switch (op) {
		case ADD:
			return "+";
		case SUB:
			return "-";
		case MUL:
			return "×";
		case DIV:
			return "÷";
		default:
			return "";
		}
	}
}