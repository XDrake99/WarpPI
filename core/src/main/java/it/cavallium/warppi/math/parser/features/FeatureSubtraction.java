package it.cavallium.warppi.math.parser.features;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Subtraction;
import it.cavallium.warppi.util.Error;

public class FeatureSubtraction extends FeatureDoubleImpl {

	public FeatureSubtraction(final Object child1, final Object child2) {
		super(child1, child2);
	}

	@Override
	public Function toFunction(final MathContext context) throws Error {
		return new Subtraction(context, getFunction1(), getFunction2());
	}

}
