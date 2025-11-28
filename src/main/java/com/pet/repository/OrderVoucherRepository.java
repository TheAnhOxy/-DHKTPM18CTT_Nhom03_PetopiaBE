package com.pet.repository;

import com.pet.entity.OrderVoucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface OrderVoucherRepository extends JpaRepository<OrderVoucher, String> {
    @Query("SELECT ov.orderVoucherId FROM OrderVoucher ov ORDER BY ov.orderVoucherId DESC LIMIT 1")
    Optional<String> findLastId();
}