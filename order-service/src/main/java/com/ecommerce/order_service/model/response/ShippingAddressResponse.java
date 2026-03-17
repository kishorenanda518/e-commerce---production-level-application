package com.ecommerce.order_service.model.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShippingAddressResponse {
    private String name;
    private String phone;
    private String street;
    private String city;
    private String state;
    private String country;
    private String postalCode;
}