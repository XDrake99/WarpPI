package it.cavallium.warppi.math.rules.dsl.patterns;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Undefined;
import it.cavallium.warppi.math.rules.dsl.VisitorPattern;

import java.util.Map;
import java.util.stream.Stream;

/**
 * Matches and generates <code>Undefined</code>.
 */
public class UndefinedPattern extends VisitorPattern {
	@Override
	public Boolean visit(final Undefined undefined, final Map<String, Function> subFunctions) {
		return true;
	}

	@Override
	public Function replace(MathContext mathContext, Map<String, Function> subFunctions) {
		return new Undefined(mathContext);
	}

	@Override
	public Stream<SubFunctionPattern> getSubFunctions() {
		return Stream.empty();
	}

	@Override
	public boolean equals(final Object o) {
		return o instanceof UndefinedPattern;
	}

	@Override
	public int hashCode() {
		return UndefinedPattern.class.hashCode();
	}
}
