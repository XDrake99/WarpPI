package it.cavallium.warppi.math.rules.dsl.patterns;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.equations.Equation;
import it.cavallium.warppi.math.rules.dsl.Pattern;
import it.cavallium.warppi.math.rules.dsl.VisitorPattern;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Matches and generates an equation of two other patterns.
 */
public class EquationPattern extends VisitorPattern {
	private final Pattern left;
	private final Pattern right;

	public EquationPattern(final Pattern left, final Pattern right) {
		this.left = left;
		this.right = right;
	}

	@Override
	public Boolean visit(final Equation equation, final Map<String, Function> subFunctions) {
		return left.match(equation.getParameter1(), subFunctions)
			&& right.match(equation.getParameter2(), subFunctions);
	}

	@Override
	public Function replace(final MathContext mathContext, final Map<String, Function> subFunctions) {
		return new Equation(
			mathContext,
			left.replace(mathContext, subFunctions),
			right.replace(mathContext, subFunctions)
		);
	}

	@Override
	public Stream<SubFunctionPattern> getSubFunctions() {
		return Stream.of(left, right)
			.flatMap(Pattern::getSubFunctions);
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof EquationPattern)) {
			return false;
		}
		final EquationPattern other = (EquationPattern) o;
		return left.equals(other.left) && right.equals(other.right);
	}

	@Override
	public int hashCode() {
		return Objects.hash(left, right);
	}
}
