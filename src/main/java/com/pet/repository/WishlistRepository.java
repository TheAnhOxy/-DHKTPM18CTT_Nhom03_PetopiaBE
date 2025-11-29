package com.pet.repository;

import com.pet.entity.Wishlist;
import com.pet.modal.response.TopFavoritedPetDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, String> {

    // Check user đã like pet này chưa (để xử lý toggle)
    Optional<Wishlist> findByUser_UserIdAndPet_PetId(String userId, String petId);

    //  danh sách yêu thích của User
    Page<Wishlist> findByUser_UserIdOrderByAddedAtDesc(String userId, Pageable pageable);

    @Query("SELECT w.wishlistId FROM Wishlist w ORDER BY w.wishlistId DESC LIMIT 1")
    Optional<String> findLastWishlistId();

    // 4. Thống kê cho Admin: Top thú cưng nhiều tim nhất
    // JPQL Group By
    @Query("SELECT new com.pet.modal.response.TopFavoritedPetDTO(p.petId, p.name, p.price, COUNT(w)) " +
            "FROM Wishlist w JOIN w.pet p " +
            "GROUP BY p.petId, p.name, p.price " +
            "ORDER BY COUNT(w) DESC")
    Page<TopFavoritedPetDTO> findTopFavoritedPets(Pageable pageable);

    long count();
}