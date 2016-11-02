package org.warp.picalculator.math.functions;

import static org.warp.picalculator.device.graphicengine.Display.Render.glGetStringWidth;
import static org.warp.picalculator.device.graphicengine.Display.Render.glColor3f;
import static org.warp.picalculator.device.graphicengine.Display.Render.glDrawStringLeft;
import static org.warp.picalculator.device.graphicengine.Display.Render.glFillRect;

import java.util.ArrayList;
import java.util.List;

import org.nevec.rjm.NumeroAvanzatoVec;
import org.warp.picalculator.Error;
import org.warp.picalculator.Errors;
import org.warp.picalculator.Utils;
import org.warp.picalculator.device.PIDisplay;
import org.warp.picalculator.device.graphicengine.Display;
import org.warp.picalculator.device.graphicengine.Display.Render;
import org.warp.picalculator.math.MathematicalSymbols;
import org.warp.picalculator.math.rules.FractionsRule1;
import org.warp.picalculator.math.rules.FractionsRule2;
import org.warp.picalculator.math.rules.FractionsRule3;
import org.warp.picalculator.math.rules.NumberRule1;
import org.warp.picalculator.math.rules.NumberRule2;
import org.warp.picalculator.math.rules.UndefinedRule2;

public class Division extends FunctionTwoValues {

	public Division(Function parent, Function value1, Function value2) {
		super(parent, value1, value2);
	}

	@Override
	public String getSymbol() {
		return MathematicalSymbols.DIVISION;
	}


	@Override
	protected boolean isSolvable() throws Error {
		if (FractionsRule1.compare(this)) return true;
		if (FractionsRule2.compare(this)) return true;
		if (FractionsRule3.compare(this)) return true;
		if (UndefinedRule2.compare(this)) return true;
		return false;
	}
	
	@Override
	public List<Function> solveOneStep() throws Error {
		ArrayList<Function> result = new ArrayList<>();
		if (FractionsRule1.compare(this)) {
			result = FractionsRule1.execute(this);
		} else if (FractionsRule2.compare(this)) {
			result = FractionsRule2.execute(this);
		} else if (FractionsRule3.compare(this)) {
			result = FractionsRule3.execute(this);
		} else if (UndefinedRule2.compare(this)) {
			result = UndefinedRule2.execute(this);
		} else {
			List<Function> l1 = new ArrayList<Function>();
			List<Function> l2 = new ArrayList<Function>();
			if (variable1.isSolved()) {
				l1.add(variable1);
			} else {
				l1.addAll(variable1.solveOneStep());
			}
			if (variable2.isSolved()) {
				l2.add(variable2);
			} else {
				l2.addAll(variable2.solveOneStep());
			}

			Function[][] results = Utils.joinFunctionsResults(l1, l2);
			
			for (Function[] f : results) {
				result.add(new Division(this.parent, (Function)f[0], (Function)f[1]));
			}
		}
		return result;
	}

	public boolean hasMinus() {
		String numerator = variable1.toString();
		if (numerator.startsWith("-")) {
			return true;
		}
		return false;
	}

	public void draw(int x, int y, boolean small, boolean drawMinus) {
		boolean beforedrawminus = this.drawMinus;
		this.drawMinus = drawMinus;
		draw(x, y);
		this.drawMinus = beforedrawminus;
	}

	private boolean drawMinus = true;

	@Override
	public void generateGraphics() {
		variable1.setSmall(true);
		variable1.generateGraphics();
		
		variable2.setSmall(true);
		variable2.generateGraphics();
		
		width = calcWidth();
		height = calcHeight();
		line = variable1.getHeight() + 1;
	}
	
	@Override
	public void draw(int x, int y) {
//		glColor3f(255, 127-50+new Random().nextInt(50), 0);
//		glFillRect(x,y,width,height);
//		glColor3f(0, 0, 0);
		
		Object var1 = variable1;
		Object var2 = variable2;
		boolean minus = false;
		int minusw = 0;
		int minush = 0;
		String numerator = ((Function) var1).toString();
		if (numerator.startsWith("-") && ((Function) var1) instanceof Number && ((Number) var1).term.isBigInteger(true)) {
			minus = true;
			numerator = numerator.substring(1);
		}
		int w1 = 0;
		int h1 = 0;
		Display.Render.glSetFont(Utils.getFont(small));
		if (minus) {
			w1 = glGetStringWidth(numerator);
			h1 = Render.glGetCurrentFontHeight();
		} else {
			w1 = ((Function) var1).getWidth();
			h1 = ((Function) var1).getHeight();
		}
		int w2 = ((Function) var2).getWidth();
		int maxw;
		if (w1 > w2) {
			maxw = 1 + w1;
		} else {
			maxw = 1 + w2;
		}
		if (minus && drawMinus) {
			minusw = glGetStringWidth("-") + 1;
			minush = Render.glGetCurrentFontHeight();
			glDrawStringLeft(x+1, y + h1 + 1 + 1 - (minush / 2), "-");
			glDrawStringLeft((int) (x+1 + minusw + 1 + (maxw - w1) / 2d), y, numerator);
		} else {
			((Function) var1).draw((int) (x+1 + minusw + (maxw - w1) / 2d), y);
		}
		((Function) var2).draw((int) (x+1 + minusw + (maxw - w2) / 2d), y + h1 + 1 + 1 + 1);
		glColor3f(0, 0, 0);
		glFillRect(x+1+ minusw, y + h1 + 1, maxw, 1);
	}

	@Override
	public int getHeight() {
		return height;
	}
	
	@Override
	protected int calcHeight() {

		boolean minus = false;
		String numerator = variable1.toString();
		if (numerator.startsWith("-") && variable1 instanceof Number && ((Number) variable1).term.isBigInteger(true)) {
			minus = true;
			numerator = numerator.substring(1);
		}
		int h1 = 0;
		if (minus) {
			h1 = Utils.getFontHeight(small);
		} else {
			h1 = variable1.getHeight();
		}
		int h2 = variable2.getHeight();
		return h1 + 3 + h2;
	}

	@Override
	public int getLine() {
		return line;
	}
	
	@Override
	public int getWidth() {
		return width;
	}
	
	@Override
	protected int calcWidth() {
		boolean minus = false;
		String numerator = variable1.toString();
		if (numerator.startsWith("-") && variable1 instanceof Number && ((Number) variable1).term.isBigInteger(true)) {
			minus = true;
			numerator = numerator.substring(1);
		}
		int w1 = 0;
		if (minus) {
			w1 = glGetStringWidth(numerator);
		} else {
			w1 = variable1.getWidth();
		}
		int w2 = variable2.getWidth();
		int maxw = 0;
		if (w1 > w2) {
			maxw = w1+1;
		} else {
			maxw = w2+1;
		}
		if (minus && drawMinus) {
			return 1 + glGetStringWidth("-") + 1 + maxw;
		} else {
			return 1 + maxw;
		}
	}
}