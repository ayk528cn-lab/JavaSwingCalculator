/**
 * 電卓の入力状態を表す列挙型です。
 * 
 * @link READY 初期状態
 * @link INPUT_NUMBER 数値入力中
 * @link INPUT_OPERATOR 演算子入力中
 * @link AFTER_RESULT 計算結果表示後
 * @link ERROR エラー状態
 */
public enum InputState {
	READY,
	INPUT_NUMBER,
	INPUT_OPERATOR,
	AFTER_RESULT,
	ERROR
}