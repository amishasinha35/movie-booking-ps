package com.amisha.moviebooking.discount;

import java.math.BigDecimal;

public interface DiscountStrategy {

    BigDecimal apply(BookingContext context);

    String name();
}
