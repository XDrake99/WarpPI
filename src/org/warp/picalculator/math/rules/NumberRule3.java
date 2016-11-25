package org.warp.picalculator.math.rules;

import java.util.ArrayList;

import org.warp.picalculator.Error;
import org.warp.picalculator.math.functions.Function;
import org.warp.picalculator.math.functions.Multiplication;
import org.warp.picalculator.math.functions.Negative;
import org.warp.picalculator.math.functions.Number;
import org.warp.picalculator.math.functions.Subtraction;
import org.warp.picalculator.math.functions.Sum;
import org.warp.picalculator.math.functions.SumSubtraction;

/**
 * Number rule<br>
 * <b>a - a = 0</b><br>
 * <b>-a + a = 0</b><br>
 * <b>a ± a = {0, 2a}</b>
 * @author Andrea Cavalli
 *
 */
public class NumberRule3 {

	public static boolean compare(Function f) {
		if (f instanceof Subtraction) {
			Subtraction sub = (Subtraction) f;
			if (sub.getVariable1().equals(sub.getVariable2())) {
				return true;
			}
		} else if (f instanceof Sum) {
			Sum sub = (Sum) f;
			if (sub.getVariable1() instanceof Negative) {
				Negative neg = (Negative) sub.getVariable1();
				if (neg.getVariable().equals(sub.getVariable2())) {
					return true;
				}
			}
		} else if (f instanceof SumSubtraction) {
			SumSubtraction sub = (SumSubtraction) f;
			if (sub.getVariable1().equals(sub.getVariable2())) {
				return true;
			}
		}
		return false;
	}

	public static ArrayList<Function> execute(Function f) throws Error {
		ArrayList<Function> result = new ArrayList<>();
		if (f instanceof SumSubtraction) {
			Multiplication mul = new Multiplication(f.getParent(), null, null);
			mul.setVariable1(new Number(null, 2));
			mul.setVariable2(f);
			result.add(mul);
		}
		result.add(new Number(f.getParent(), 0));
		return result;
	}

}
