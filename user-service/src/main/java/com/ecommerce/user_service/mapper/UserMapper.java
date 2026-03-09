package com.ecommerce.user_service.mapper;


import com.ecommerce.user_service.entity.Role;
import com.ecommerce.user_service.entity.User;
import com.ecommerce.user_service.model.response.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring") // makes it a Spring bean — inject with @Autowired
public interface UserMapper {

    @Mapping(target = "roles",     source = "roles",           qualifiedByName = "rolesToStrings")
    @Mapping(target = "firstName", source = "profile.firstName")
    @Mapping(target = "lastName",  source = "profile.lastName")
    UserResponse toUserResponse(User user);

    // Convert Set<Role> → Set<String> (e.g. ["ROLE_USER"])
    @Named("rolesToStrings")
    default Set<String> rolesToStrings(Set<Role> roles) {
        if (roles == null) return Set.of();
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }
}