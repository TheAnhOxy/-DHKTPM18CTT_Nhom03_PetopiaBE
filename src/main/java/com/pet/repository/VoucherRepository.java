package com.pet.repository;

import com.pet.entity.Voucher;
import com.pet.enums.PromotionVoucherStatus;
import com.pet.enums.VoucherDiscountType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, String> {
    Optional<Voucher> findByCode(String code);

    @Query("""
        SELECT v FROM Voucher v
        WHERE (:keyword IS NULL OR LOWER(v.code) LIKE :keyword OR LOWER(v.description) LIKE :keyword)
          AND (:status IS NULL OR v.status = :status)
          AND (:discountType IS NULL OR v.discountType = :discountType)
        """)
    Page<Voucher> searchVouchers(
            @Param("keyword") String keyword,
            @Param("status") PromotionVoucherStatus status,
            @Param("discountType") VoucherDiscountType discountType,
            Pageable pageable
    );
}
