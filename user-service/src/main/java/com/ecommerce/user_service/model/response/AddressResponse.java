package com.ecommerce.user_service.model.response;

import com.ecommerce.user_service.enums.AddressType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddressResponse {
    private String      id;
    private String      street;
    private String      city;
    private String      state;
    private String      country;
    private String      postalCode;
    private AddressType addressType;
    private Boolean     isDefault;
}