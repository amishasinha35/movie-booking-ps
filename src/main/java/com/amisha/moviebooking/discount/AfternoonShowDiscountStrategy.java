package com.amisha.moviebooking.discount;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

// 20% off total when show is between 12:00 and 17:00
@Component
public class AfternoonShowDiscountStrategy implements DiscountStrategy {

    private static final int AFTERNOON_START = 12;
    private static final int AFTERNOON_END = 17;

    @Override
    public BigDecimal apply(BookingContext context) {
        int hour = context.getShowTime().getHour();
        if (hour < AFTERNOON_START || hour >= AFTERNOON_END) {
            return BigDecimal.ZERO;
        }
        BigDecimal subtotal = context.getTicketPrice()
                .multiply(BigDecimal.valueOf(context.getTicketCount()));
        return subtotal
                .multiply(new BigDecimal("0.20"))
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String name() {
        return "AFTERNOON_SHOW_20_PERCENT";
    }
}
