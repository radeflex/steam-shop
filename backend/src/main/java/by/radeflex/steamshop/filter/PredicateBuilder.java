package by.radeflex.steamshop.filter;


import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class PredicateBuilder {
    private final List<Predicate> predicates = new ArrayList<>();

    public static PredicateBuilder builder() {
        return new PredicateBuilder();
    }

    public <T> PredicateBuilder add(T o, Function<T, Predicate> f) {
        if (o != null)
            predicates.add(f.apply(o));
        return this;
    }

    public Predicate buildAnd() {
        return ExpressionUtils.allOf(predicates);
    }
}
