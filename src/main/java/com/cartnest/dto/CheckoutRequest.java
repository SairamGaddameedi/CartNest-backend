package com.cartnest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CheckoutRequest {

    @NotBlank(message = "Address is required")
    private String shippingAddress;

    @NotBlank(message = "City is required")
    private String shippingCity;

    @NotBlank(message = "State is required")
    private String shippingState;

    @NotBlank(message = "Pincode is required")
    private String shippingPincode;

    @NotBlank(message = "Phone is required")
    private String shippingPhone;

    @NotNull(message = "Payment method is required")
    private String paymentMethod;
}
