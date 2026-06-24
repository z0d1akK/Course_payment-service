package com.innowise.paymentservice.payment.mapper;

import com.innowise.paymentservice.config.MapStructConfig;
import com.innowise.paymentservice.payment.dto.request.CreatePaymentRequestDto;
import com.innowise.paymentservice.payment.dto.response.PaymentResponseDto;
import com.innowise.paymentservice.payment.entity.Payment;
import org.mapstruct.Mapper;

@Mapper(config = MapStructConfig.class)
public interface PaymentMapper {

    Payment toEntity(CreatePaymentRequestDto requestDto);

    PaymentResponseDto toResponseDto(Payment payment);
}