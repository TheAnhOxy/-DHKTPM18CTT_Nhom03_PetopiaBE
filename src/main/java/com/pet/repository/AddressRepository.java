package com.pet.repository;

import com.pet.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, String> {

    List<Address> findByUser_UserId(String userId);
    @Query("SELECT a.addressId FROM Address a ORDER BY a.addressId DESC LIMIT 1")
    Optional<String> findLastAddressId();
    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.user.userId = :userId")
    void setAllAddressesNonDefault(String userId);

    long countByUser_UserId(String userId);
}