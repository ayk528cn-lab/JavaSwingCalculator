import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 電卓の実装と状態管理を担当するモデルクラス
 * 四則演算の計算、入力データのバリデーション、エラーハンドリングを管理します。
 * 
 */
public class CalculatorModel {
	private String leftOperand = "";
	private String rightOperand = "";
	private Operator operator = null;

	private InputState state = InputState.READY;

	private ErrorHandler errorHandler = new ErrorHandler();
	private static final int MAX_DIGITS = 8;
	
	/** 内部計算用の十分な精度（スケール）を定数として定義する */
    private static final int INTERNAL_CALCULATION_SCALE = 30;

	private boolean isJustCleared = true;
	
	

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

		String target = (operator == null) ? leftOperand : rightOperand;

		if (target.equals("0") && ch == '0') {
			return false;
		}
		
		if (target.equals("-0")) {
			return false;
		}

		if (target.equals("0")) {
			if (operator == null) {
				leftOperand = String.valueOf(ch);
			} else {
				rightOperand = String.valueOf(ch);
			}
			state = InputState.INPUT_NUMBER;
			return true;
		}

		int effectiveLength = target.replace("-", "").replace(".", "").length();
		if (effectiveLength >= MAX_DIGITS) {
			return false;
		}

		if (operator == null) {
			leftOperand += ch;
		} else {
			rightOperand += ch;
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

		if (state == InputState.AFTER_RESULT) {
			return false;
		}
		
		if (operator != null && rightOperand.isEmpty()) {
			return false;
		}

		prepareForInput();

		String target = (operator == null) ? leftOperand : rightOperand;

		if (target.equals("-")) {
			return false;
		}

		/** 1. すでに小数点が入力されている場合は追加しない */
		if (target.contains(".")) {
			return false;
		}

		/** 2. 桁数制限を確認 */
		int effectiveLength = target.replace("-", "").replace(".", "").length();
		if (effectiveLength >= MAX_DIGITS) {
			return false;
		}

		/** 3. 小数点の追加処理 */
		if (operator == null) {
			if (leftOperand.isEmpty()) {
				leftOperand = "0.";
			} else {
				leftOperand += ".";
			}
		} else {
			if (rightOperand.isEmpty()) {
				rightOperand = "0.";
			} else {
				rightOperand += ".";
			}
		}
		state = InputState.INPUT_NUMBER;
		return true;
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
		if (leftOperand.equals("-") || rightOperand.equals("-") || leftOperand.equals("-0") && rightOperand.isEmpty()) {
			return false;
		}
		
		if (!rightOperand.isEmpty() && (rightOperand.equals("-") || rightOperand.equals("-0"))) {
			return false;
		}

		/** すでに数式（1つ目の数字、演算子、2つ目の数字）が揃っている状態で演算子が押された場合、一度中間計算を行う */
		if (!leftOperand.isEmpty() && !rightOperand.isEmpty() && operator != null) {
			calculate();
			
			if (state == InputState.ERROR) {
				return false;
			}
		
		/** 「0.」や「0.0」「0.00」などの状態で演算子が押されたら無視する */
		//String targetCheck = (operator == null) ? leftOperand : rightOperand;
		//if (targetCheck.matches("^0\\.0*$")) {
			//return true;
		//}

		/** 既に1つ目の数字と演算子が決まっていて、2つ目の数字がまだ押されていない状態で、更に演算子が押されたとき演算子を上書きする */
		//if (!leftOperand.isEmpty() && operator != null && rightOperand.isEmpty()) {
			Operator newOp = parseOperator(op);
			if (newOp == null) {
				return false;
			}
			
			this.operator = newOp;
			state = InputState.INPUT_OPERATOR;
			return true;
		}
		/** 最初が空または "0" のとき、"-" なら負号（マイナス）として受け付ける */
		if (leftOperand.isEmpty() || leftOperand.equals("0")) {
		if (operator != null) {
			return false;
		}
		
			if (op.equals("-")) {
				leftOperand = "-";
				state = InputState.INPUT_NUMBER;
				return true;
			} else {
				return false;
			}
		}

		if (operator == null) {
			if (leftOperand.endsWith(".")) {
				leftOperand = leftOperand.substring(0, leftOperand.length() - 1);
			}
		} else {
			if (rightOperand.endsWith(".")) {
				rightOperand = rightOperand.substring(0, rightOperand.length() - 1);
			}
		}

		
		/** 文字列の演算子を Operator 列挙型に変換 */
		Operator newOp = parseOperator(op);
		if (newOp == null) {
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
	 * @throws DivisionByZeroException 除数が0の場合
	 */
	public BigDecimal apply() throws DivisionByZeroException {
		BigDecimal n1 = new BigDecimal(leftOperand.replace('e', 'E'));
		BigDecimal n2 = new BigDecimal(rightOperand.replace('e', 'E'));

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
			return n1.divide(n2, INTERNAL_CALCULATION_SCALE, RoundingMode.DOWN);
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
		if (state == InputState.ERROR) {
			return errorHandler.getErrorMessage();
		}
		
		if (leftOperand.isEmpty() || rightOperand.isEmpty() || operator == null) {
			return leftOperand;
		}

		BigDecimal result = BigDecimal.ZERO;
		try {
			result = apply();
		} catch (DivisionByZeroException e) {
			errorHandler.handle(e);
			state = InputState.ERROR;
			leftOperand = errorHandler.getErrorMessage(); /** "エラー" */
			rightOperand = "";
			operator = null;
			return leftOperand;
		}

		/** FormatterUtil を経由して指数表記と通常表記を切り替える */
		leftOperand = FormatterUtil.formatResult(result, MAX_DIGITS);

		rightOperand = "";
		operator = null; /** リセット時は null にする */
		state = InputState.AFTER_RESULT;
		return leftOperand;
	}

	/**
	 * すべての状態を初期状態（READY）にリセットします。
	 */
	public void clearAll() {
		this.leftOperand = "";
		this.rightOperand = "";
		this.operator = null;
		this.state = InputState.READY;
		this.isJustCleared = true;
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
			return leftOperand.isEmpty() ? "0" : leftOperand;
		}
		String opStr = getOperatorSymbol(operator);
		/** 記号に変換する */
		/** 演算子が入力された状態で、２番目の数字が空の場合 */
		if (rightOperand.isEmpty()) {
			return leftOperand + " " + opStr;
		}
		/** 演算子と２番目の数字がある場合 */
		return leftOperand + " " + opStr + " " + rightOperand;
	}

	/**
	 * 入力操作を受け付ける前の事前準備を行い、必要に応じて状態をリセットまたは遷移させます。
	 */
	private void prepareForInput() {
		if (state == InputState.AFTER_RESULT) {
			clearAll();
		} else if (state == InputState.READY || state == InputState.INPUT_OPERATOR) {
			state = InputState.INPUT_NUMBER;
		}
		isJustCleared = false;
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
	
	/**
	 * 文字列の演算子を Operator 列挙型に変換します。
	 * 
	 * @param op 変換する演算子の文字列（"+", "-", "×", "÷"）
	 * @return 対応する Operator、無効な場合は null
	 */
	private Operator parseOperator(String op) {
		switch (op) {
		case "+":
			return Operator.ADD;
		case "-":
			return Operator.SUB;
		case "×":
			return Operator.MUL;
		case "÷":
			return Operator.DIV;
		default:
			return null;
		}
	}
}