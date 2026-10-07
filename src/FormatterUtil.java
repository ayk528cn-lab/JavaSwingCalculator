import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import javax.swing.JTextField;

public class FormatterUtil {
	private JTextField displayField;

	public void updateDisplay(String text) {
		displayField.setText(text);
	}

	public static String formatResult(BigDecimal result, int maxDigits) {
		/**　9桁を超える場合（または非常に小さい場合）の指数表記変換 */
		String resultStr;
		BigDecimal thresholdHigh = BigDecimal.valueOf(Math.pow(10, maxDigits));
		BigDecimal thresholdLow = new BigDecimal("1e-7");

		if (result.abs().compareTo(thresholdHigh) >= 0
				|| (result.compareTo(BigDecimal.ZERO) != 0 && result.abs().compareTo(thresholdLow) < 0)) {
			/** Locale.US を指定することで、環境によって小数点の文字（, や .）が変わるのを防ぎます */
			DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
			DecimalFormat decimalformat = new DecimalFormat("0.0000000E0", symbols);

			resultStr = decimalformat.format(result);

			if (resultStr.contains("e") || resultStr.contains("E")) {
				String[] parts = resultStr.toLowerCase().split("e");
				if (parts.length >= 2) {
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
			}
		} else {
			resultStr = result.toPlainString();
			if (resultStr.contains(".")) {
				resultStr = resultStr.replaceAll("0+$", "");
				resultStr = resultStr.replaceAll("\\.$", "");
			}
		}
		return resultStr.replace("E", "e");
	}
}
