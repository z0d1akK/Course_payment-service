package com.innowise.paymentservice.common.constants.messages;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ValidationMessages {

    public static final String PAYMENT_ORDER_ID_REQUIRED = "Order ID is required";

    public static final String PAYMENT_AMOUNT_REQUIRED = "Payment amount is required";

    public static final String PAYMENT_AMOUNT_POSITIVE = "Payment amount must be greater than zero";

    public static final String PAYMENT_AMOUNT_MAX_INTEGER = "Payment amount integer part is too large";

    public static final String PAYMENT_AMOUNT_MAX_FRACTION = "Payment amount fractional part must contain at most 2 digits";
}
