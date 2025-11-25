package com.pet.service;

import com.pet.modal.response.PageResponse;
import com.pet.modal.response.TopFavoritedPetDTO;
import com.pet.modal.response.WishlistResponseDTO;

public interface WishlistService {
    // User: Toggle (Thêm/Xóa) - Trả về message "Added" hoặc "Removed"
    String toggleWishlist(String userId, String petId);

    // User: Xem danh sách yêu thích
    PageResponse<WishlistResponseDTO> getMyWishlist(String userId, int page, int size);

    // Admin: Thống kê top yêu thích
    PageResponse<TopFavoritedPetDTO> getTopFavoritedPets(int page, int size);
}