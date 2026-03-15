package com.ecommerce.user_service.repository;

import com.ecommerce.user_service.entity.Address;
import com.ecommerce.user_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, String> {  // ← String not Long

    List<Address>     findByUser(User user);
    Optional<Address> findByIdAndUser(String id, User user);           // ← String not Long
    Optional<Address> findByUserAndIsDefaultTrue(User user);           // ← isDefault not defaultAddress
}