package com.innowise.paymentservice.common.constants.messages;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ErrorMessages {

    public static final String ACCESS_DENIED = "Access denied";

    public static final String UNAUTHORIZED = "Unauthorized";

    public static final String INTERNAL_SERVER_ERROR = "An unexpected error occurred. Please try again later.";

    public static final String PAYMENT_NOT_FOUND = "Payment with id %s not found";

    public static final String INVALID_GATEWAY_KEY = "Invalid Gateway Key";
}