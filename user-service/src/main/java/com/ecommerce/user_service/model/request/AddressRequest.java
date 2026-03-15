package com.ecommerce.user_service.model.request;

import com.ecommerce.user_service.enums.AddressType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddressRequest {

    @NotBlank(message = "Street is required")
    @Size(max = 200)
    private String street;

    @NotBlank(message = "City is required")
    @Size(max = 100)
    private String city;

    @NotBlank(message = "State is required")
    @Size(max = 100)
    private String state;

    @NotBlank(message = "Country is required")
    @Size(max = 100)
    private String country;

    @NotBlank(message = "Postal code is required")
    @Size(max = 20)
    private String postalCode;

    private AddressType addressType = AddressType.HOME;

    private boolean defaultAddress;
}