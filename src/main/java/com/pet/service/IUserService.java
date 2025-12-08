package com.pet.service;

import com.pet.modal.request.*;
import com.pet.modal.response.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface IUserService {
    UserResponseDTO getUserProfile(String userId);
    UserResponseDTO updateUserProfile(String userId, UserUpdateRequestDTO request, MultipartFile avatarFile) throws IOException;
//    void changePassword(String userId, ChangePasswordRequestDTO request);


    PageResponse<UserResponseDTO> getAllUsers(int page, int size);
    UserResponseDTO toggleUserStatus(String userId);


    List<AddressResponseDTO> getMyAddresses(String userId);
    AddressResponseDTO addAddress(String userId, AddressRequestDTO request);
    AddressResponseDTO updateAddress(String userId, String addressId, AddressRequestDTO request);
    void deleteAddress(String userId, String addressId);
    void setDefaultAddress(String userId, String addressId);

    UserResponseDTO saveUser(UserSaveRequestDTO request);
    void changePassword(String userId, ChangePasswordRequestDTO request);
    PageResponse<UserResponseDTO> searchUsers(String keyword, String role, Boolean isActive, int page, int size);
}