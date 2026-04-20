package com.amisha.moviebooking.discount;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

// 50% off on the 3rd ticket when booking 3 or more seats
@Component
public class ThirdTicketDiscountStrategy implements DiscountStrategy {

    @Override
    public BigDecimal apply(BookingContext context) {
        if (context.getTicketCount() < 3) {
            return BigDecimal.ZERO;
        }
        return context.getTicketPrice()
                .multiply(new BigDecimal("0.50"))
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String name() {
        return "THIRD_TICKET_50_PERCENT";
    }
}
