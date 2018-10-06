package it.cavallium.warppi.math.rules.dsl.patterns;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.functions.Sum;
import it.cavallium.warppi.math.rules.dsl.Pattern;
import it.cavallium.warppi.math.rules.dsl.VisitorPattern;

import java.util.Map;
import java.util.Optional;

/**
 * Matches and generates a sum of two other patterns.
 */
public class SumPattern extends VisitorPattern {
    private final Pattern left;
    private final Pattern right;

    public SumPattern(Pattern left, Pattern right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public Optional<Map<String, Function>> visit(Sum sum) {
        return left.match(sum.getParameter1())
                .flatMap(leftMatch -> right.match(sum.getParameter2())
                        .flatMap(rightMatch -> mergeMatches(leftMatch, rightMatch))
                );
    }

    @Override
    public Function replace(Map<String, Function> subFunctions) {
        return new Sum(
                null,
                left.replace(subFunctions),
                right.replace(subFunctions)
        );
    }
}