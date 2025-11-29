package com.pet.service.impl;

import com.pet.converter.UserConverter;
import com.pet.entity.Address;
import com.pet.entity.User;
import com.pet.enums.UserRole;
import com.pet.exception.ResourceNotFoundException;
import com.pet.modal.request.*;
import com.pet.modal.response.*;
import com.pet.repository.AddressRepository;
import com.pet.repository.UserRepository;
import com.pet.service.IUserService;
import com.pet.utils.StringUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
//@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    @Autowired
    private  UserRepository userRepository;

    @Autowired
    private  AddressRepository addressRepository;
    @Autowired
    private  UserConverter userConverter;
    @Autowired
    private  PasswordEncoder passwordEncoder;

    @Override
    public UserResponseDTO getUserProfile(String userId) {
        User user = getUserById(userId);
        return userConverter.toUserResponseDTO(user);
    }

    @Override
    @Transactional
    public UserResponseDTO updateUserProfile(String userId, UserUpdateRequestDTO request) {
        User user = getUserById(userId);

        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getAvatar() != null) user.setAvatar(request.getAvatar());
        if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());
        if (request.getEmail() != null) user.setEmail(request.getEmail());

        return userConverter.toUserResponseDTO(userRepository.save(user));
    }

//    @Override
//    @Transactional
//    public void changePassword(String userId, ChangePasswordRequestDTO request) {
//        User user = getUserById(userId);
//
//        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
//            throw new RuntimeException("Mật khẩu cũ không chính xác");
//        }
//        if (!request.getNewPassword().equals(request.getRetypeNewPassword())) {
//            throw new RuntimeException("Mật khẩu nhập lại không khớp");
//        }
//
//        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
//        userRepository.save(user);
//    }

    @Override
    public PageResponse<UserResponseDTO> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());
        return userConverter.toPageResponse(userRepository.findAll(pageable));
    }

    @Override
    @Transactional
    public UserResponseDTO toggleUserStatus(String userId) {
        User user = getUserById(userId);
        user.setIsActive(!user.getIsActive());
        return userConverter.toUserResponseDTO(userRepository.save(user));
    }

    @Override
    public List<AddressResponseDTO> getMyAddresses(String userId) {
        return addressRepository.findByUser_UserId(userId).stream()
                .map(userConverter::toAddressResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AddressResponseDTO addAddress(String userId, AddressRequestDTO request) {
        User user = getUserById(userId);

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            addressRepository.setAllAddressesNonDefault(userId);
        }

        Address address = new Address();
        address.setAddressId(generateAddressId());
        address.setUser(user);
        mapAddressRequestToEntity(request, address);

        if (user.getAddresses() == null || user.getAddresses().isEmpty()) {
            address.setIsDefault(true);
        }

        return userConverter.toAddressResponseDTO(addressRepository.save(address));
    }

    @Override
    @Transactional
    public AddressResponseDTO updateAddress(String userId, String addressId, AddressRequestDTO request) {
        Address address = getAddressByIdAndUser(addressId, userId);

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            addressRepository.setAllAddressesNonDefault(userId);
        }

        mapAddressRequestToEntity(request, address);
        return userConverter.toAddressResponseDTO(addressRepository.save(address));
    }

    @Override
    @Transactional
    public void deleteAddress(String userId, String addressId) {
        Address address = getAddressByIdAndUser(addressId, userId);
        addressRepository.delete(address);
    }

    @Override
    @Transactional
    public void setDefaultAddress(String userId, String addressId) {
        Address address = getAddressByIdAndUser(addressId, userId);
        addressRepository.setAllAddressesNonDefault(userId);

        address.setIsDefault(true);
        addressRepository.save(address);
    }

    private User getUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Address getAddressByIdAndUser(String addressId, String userId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        if (!address.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền sửa địa chỉ này");
        }
        return address;
    }

    private void mapAddressRequestToEntity(AddressRequestDTO req, Address address) {
        address.setStreet(req.getStreet());
        address.setWard(req.getWard());
        address.setDistrict(req.getDistrict());
        address.setProvince(req.getProvince());
        address.setCountry(req.getCountry() != null ? req.getCountry() : "Vietnam");
        if (req.getIsDefault() != null) address.setIsDefault(req.getIsDefault());
    }


    private String generateAddressId() {
        String lastId = addressRepository.findLastAddressId().orElse(null);
        if (lastId == null) return "A001";
        try {
            int num = Integer.parseInt(lastId.substring(2));
            return String.format("A%03d", num + 1);
        } catch (Exception e) {
            return "AD" + System.currentTimeMillis();
        }
    }
    @Override
    @Transactional
    public UserResponseDTO saveUser(UserSaveRequestDTO request) {
        User user;
        if (StringUtil.isNotEmpty(request.getUserId())) {
            user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getUserId()));

            checkDuplicateForUpdate(request.getPhoneNumber(), request.getEmail(), user.getUserId(), request.getUserName());

            if(request.getUserName() != null) user.setUsername(request.getUserName());
            if (request.getFullName() != null) user.setFullName(request.getFullName());
            if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());
            if (request.getEmail() != null) user.setEmail(request.getEmail());
            if (request.getAvatar() != null) user.setAvatar(request.getAvatar());
            if (request.getRole() != null) user.setRole(request.getRole());
            if (request.getIsActive() != null) user.setIsActive(request.getIsActive());

            if (StringUtil.isNotEmpty(request.getPassword())) {
                user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            }
        }
        else {
            if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
                throw new DataIntegrityViolationException("Số điện thoại đã tồn tại");
            }
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new DataIntegrityViolationException("Email đã tồn tại");
            }
            if (userRepository.existsByUsername(request.getUserName())) {
                throw new DataIntegrityViolationException("Username đã tồn tại");
            }
            if (StringUtils.isEmpty(request.getPassword())) {
                throw new IllegalArgumentException("Mật khẩu là bắt buộc khi tạo mới");
            }

            user = User.builder()
                    .userId(generateUserId())
                    .fullName(request.getFullName())
                    .phoneNumber(request.getPhoneNumber())
                    .username(request.getUserName())
                    .email(request.getEmail())
                    .passwordHash(passwordEncoder.encode(request.getPassword()))
                    .avatar(request.getAvatar())
                    .role(request.getRole() != null ? request.getRole() : UserRole.CUSTOMER)
                    .isActive(true)
                    .build();
        }
        User savedUser = userRepository.save(user);

        if (request.getAddresses() != null && !request.getAddresses().isEmpty()) {
            for (AddressRequestDTO addrReq : request.getAddresses()) {
                Address address = new Address();

                address.setAddressId(generateAddressId());
                address.setUser(savedUser);

                address.setStreet(addrReq.getStreet());
                address.setWard(addrReq.getWard());
                address.setDistrict(addrReq.getDistrict());
                address.setProvince(addrReq.getProvince());
                address.setCountry(addrReq.getCountry() != null ? addrReq.getCountry() : "Vietnam");
                boolean isDefault = addrReq.getIsDefault() != null && addrReq.getIsDefault();
                address.setIsDefault(isDefault);

                if (isDefault) {
                    addressRepository.setAllAddressesNonDefault(savedUser.getUserId());
                }

                addressRepository.save(address);
            }
        }
        return userConverter.toUserResponseDTO(savedUser);
    }

    private void checkDuplicateForUpdate(String newPhone, String newEmail, String currentUserId, String newUserName) {
        if (newPhone != null) {
            Optional<User> userByPhone = userRepository.findByPhoneNumber(newPhone);
            if (userByPhone.isPresent() && !userByPhone.get().getUserId().equals(currentUserId)) {
                throw new DataIntegrityViolationException("Số điện thoại đã được sử dụng bởi người khác");
            }
        }
        if(newUserName != null){
            Optional<User> userByUserName = userRepository.findByUsernameContainingIgnoreCase(newUserName);
            if(userByUserName.isPresent() && !userByUserName.get().getUserId().equals(currentUserId)){
                throw  new DataIntegrityViolationException("Username đã tồn tại !");
            }
        }
        if (newEmail != null) {
            Optional<User> userByEmail = userRepository.findByEmail(newEmail);
            if (userByEmail.isPresent() && !userByEmail.get().getUserId().equals(currentUserId)) {
                throw new DataIntegrityViolationException("Email đã được sử dụng bởi người khác");
            }
        }
    }

    private String generateUserId() {
        String lastId = userRepository.findLastUserId().orElse(null);
        if (lastId == null) return "U001";
        try {
            int number = Integer.parseInt(lastId.substring(1));
            return String.format("U%03d", number + 1);
        } catch (NumberFormatException e) {
            return "U" + System.currentTimeMillis();
        }
    }
}