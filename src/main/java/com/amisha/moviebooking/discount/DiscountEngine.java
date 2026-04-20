package com.amisha.moviebooking.discount;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DiscountEngine {

    private final List<DiscountStrategy> strategies;

    public DiscountEngine(List<DiscountStrategy> strategies) {
        this.strategies = strategies;
    }

    public BigDecimal calculate(BookingContext context) {
        BigDecimal total = BigDecimal.ZERO;
        for (DiscountStrategy strategy : strategies) {
            total = total.add(strategy.apply(context));
        }
        return total;
    }
}
