package com.amisha.moviebooking.discount;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DiscountEngineTest {

    private DiscountEngine discountEngine;

    @BeforeEach
    void setUp() {
        discountEngine = new DiscountEngine(List.of(
                new ThirdTicketDiscountStrategy(),
                new AfternoonShowDiscountStrategy()
        ));
    }

    @Test
    void noDiscount_whenOneTicket() {
        BookingContext context = new BookingContext(1, new BigDecimal("300.00"),
                LocalDateTime.now().withHour(10));
        BigDecimal discount = discountEngine.calculate(context);
        assertThat(discount).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void fiftyPercentOffThirdTicket() {
        // 3 tickets at 300 — 3rd ticket gets 50% off = 150 discount
        BookingContext context = new BookingContext(3, new BigDecimal("300.00"),
                LocalDateTime.now().withHour(10));
        BigDecimal discount = discountEngine.calculate(context);
        assertThat(discount).isEqualByComparingTo(new BigDecimal("150.00"));
    }

    @Test
    void afternoonDiscount_appliedCorrectly() {
        // 2 tickets at 300, afternoon show — 20% of 600 = 120
        BookingContext context = new BookingContext(2, new BigDecimal("300.00"),
                LocalDateTime.now().withHour(14));
        BigDecimal discount = discountEngine.calculate(context);
        assertThat(discount).isEqualByComparingTo(new BigDecimal("120.00"));
    }

    @Test
    void bothDiscountsStack_threeTicketsAfternoonShow() {
        // 3 tickets at 300, afternoon show
        // ThirdTicket: 150  AfternoonShow: 20% of 900 = 180  Total: 330
        BookingContext context = new BookingContext(3, new BigDecimal("300.00"),
                LocalDateTime.now().withHour(15));
        BigDecimal discount = discountEngine.calculate(context);
        assertThat(discount).isEqualByComparingTo(new BigDecimal("330.00"));
    }

    @Test
    void noAfternoonDiscount_forEveningShow() {
        // hour = 18, outside 12–17 window
        BookingContext context = new BookingContext(2, new BigDecimal("350.00"),
                LocalDateTime.now().withHour(18));
        BigDecimal discount = discountEngine.calculate(context);
        assertThat(discount).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
