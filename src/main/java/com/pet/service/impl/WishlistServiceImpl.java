package com.pet.service.impl;

import com.pet.converter.WishlistConverter;
import com.pet.entity.Pet;
import com.pet.entity.User;
import com.pet.entity.Wishlist;
import com.pet.exception.ResourceNotFoundException;
import com.pet.modal.response.PageResponse;
import com.pet.modal.response.TopFavoritedPetDTO;
import com.pet.modal.response.WishlistResponseDTO;
import com.pet.repository.PetRepository;
import com.pet.repository.UserRepository;
import com.pet.repository.WishlistRepository;
import com.pet.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class WishlistServiceImpl implements WishlistService {

    @Autowired private WishlistRepository wishlistRepository;
    @Autowired private PetRepository petRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private WishlistConverter wishlistConverter;

    @Override
    @Transactional
    @CacheEvict(value = "top_wishlist", allEntries = true)
    public String toggleWishlist(String userId, String petId) {
        // Check xem đã like chưa
        Optional<Wishlist> existingWishlist = wishlistRepository.findByUser_UserIdAndPet_PetId(userId, petId);

        // CASE 1: Đã có -> XÓA (Hủy tim)
        if (existingWishlist.isPresent()) {
            wishlistRepository.delete(existingWishlist.get());
            return "Removed"; // Trả về trạng thái để FE đổi màu trái tim (màu gì tùy chọn)
        }

        //  Chưa có -> THÊM (Thả tim)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new ResourceNotFoundException("Pet not found"));

        Wishlist wishlist = new Wishlist();
        wishlist.setWishlistId(generateWishlistId());
        wishlist.setUser(user);
        wishlist.setPet(pet);

        wishlistRepository.save(wishlist);
        return "Added"; // Trả về trạng thái để FE đổi màu trái tim (Màu tùy chọn)
    }

    @Override
    public PageResponse<WishlistResponseDTO> getMyWishlist(String userId, int page, int size) {
        Page<Wishlist> pageResult = wishlistRepository.findByUser_UserIdOrderByAddedAtDesc(userId, PageRequest.of(page, size));
        return wishlistConverter.toPageResponse(pageResult);
    }

    @Override
    // Lưu kết quả vào Redis với key là "top_wishlist::page-size"
    @Cacheable(value = "top_wishlist", key = "#page + '-' + #size")
    public PageResponse<TopFavoritedPetDTO> getTopFavoritedPets(int page, int size) {
        System.out.println("--- Query Database lấy Top Stats (Chưa có Cache hoặc Cache hết hạn) ---");
        Page<TopFavoritedPetDTO> pageResult = wishlistRepository.findTopFavoritedPets(PageRequest.of(page, size));
        PageResponse<TopFavoritedPetDTO> response = new PageResponse<>();
        response.setContent(pageResult.getContent());
        response.setPage(pageResult.getNumber());
        response.setSize(pageResult.getSize());
        response.setTotalElements(pageResult.getTotalElements());

        return response;
    }

    private String generateWishlistId() {
        String lastId = wishlistRepository.findLastWishlistId().orElse("W000");
        try {
            int num = Integer.parseInt(lastId.substring(1));
            return String.format("W%03d", num + 1);
        } catch (NumberFormatException e) {
            return "W" + System.currentTimeMillis();
        }
    }
}