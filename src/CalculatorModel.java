import java.math.BigDecimal;
import java.math.RoundingMode;

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
		if (state == InputState.ERROR) {
			return false;
		}
		
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
		if (state == InputState.ERROR) {
			return false;
		}
		
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
			if (op.equals("-")) {
				clearAll();
				firstNum = "";
				state = InputState.INPUT_NUMBER;
				return true;
			}
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
	 * @throws DivisionByZeroExceptiBigDecimal行われた場合
	 */
	public BigDecimal apply() throws DivisionByZeroException {
		BigDecimal n1 = new BigDecimal(firstNum.replace('e', 'E'));
		BigDecimal n2 = new BigDecimal(secondNum.replace('e', 'E'));

		/** Operator イーナムで分岐する */
		switch (operator) {
		case ADD:
			return n1.add(n2);
		case SUB:
			return n1.subtract(n2);
		case MUL:
			return n1.multiply(n2);
		case DIV:
			if (n2.compareTo(BigDecimal.ZERO) == 0) {
				throw new DivisionByZeroException();
			}
			return n1.divide(n2, 10, RoundingMode.HALF_UP);
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

		BigDecimal result = BigDecimal.ZERO;
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

		/** FormatterUtil を経由して指数表記と通常表記を切り替える */
		firstNum = FormatterUtil.formatResult(result, maxDigits);

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