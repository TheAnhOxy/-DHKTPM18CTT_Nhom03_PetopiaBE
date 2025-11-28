package com.pet.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pet.converter.PetConverter;
import com.pet.entity.Pet;
import com.pet.entity.PetImage;
import com.pet.enums.PetStatus;
import com.pet.exception.ResourceNotFoundException;
import com.pet.modal.request.PetImageDTO;
import com.pet.modal.request.PetRequestDTO;
import com.pet.modal.response.PageResponse;
import com.pet.modal.response.PetForListResponseDTO;
import com.pet.modal.response.PetResponseDTO;
import com.pet.modal.search.PetSearchRequestDTO;
import com.pet.repository.PetImageRepository;
import com.pet.repository.PetRepository;
import com.pet.repository.spec.PetSpecification;
import com.pet.service.CloudinaryService;
import com.pet.service.PetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class PetServiceImpl implements PetService {

    @Autowired
    private PetRepository petRepository;
    @Autowired
    private PetImageRepository petImageRepository;
    @Autowired
    private PetConverter petConverter;

    @Autowired private CloudinaryService cloudinaryService;
    @Autowired private ObjectMapper objectMapper;

    @Override
    public List<PetResponseDTO> getPets() {
        List<Pet> pets = petRepository.findAll();
        return pets.stream()
                .map(pet -> petConverter.mapToDTO(pet))
                .collect(Collectors.toList());
    }

    @Override
    public PetResponseDTO getPetById(String petId) {
        return petRepository.findById(petId)
                .map(petConverter::mapToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Pet not found with id: " + petId));
    }

    @Override
    public PageResponse<PetForListResponseDTO> getPetsByCategory(String categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Pet> petPage = petRepository.findByCategory_CategoryId(categoryId, pageable);
        return petConverter.toPageResponse(petPage);
    }

    @Override
    public PageResponse<PetForListResponseDTO> getAllPets(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Pet> petPage = petRepository.findAll(pageable);
        return petConverter.toPageResponse(petPage);
    }

    @Override
    public PageResponse<PetForListResponseDTO> getAllPetsWithStatusActive(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Pet> petPage = petRepository.findAllByStatus(PetStatus.AVAILABLE, pageable);
        return petConverter.toPageResponse(petPage);
    }

    @Override
    public PageResponse<PetForListResponseDTO> advanceSearch(PetSearchRequestDTO request) {
        Specification<Pet> spec = new PetSpecification(request);

        Sort sort;
        if ("asc".equalsIgnoreCase(request.getSortDirection())) {
            sort = Sort.by(Sort.Direction.ASC, getSortField(request.getSortBy()));
        } else {
            sort = Sort.by(Sort.Direction.DESC, getSortField(request.getSortBy()));
        }

        Pageable pageable = PageRequest.of(request.getPage(), request.getPageSize(), sort);
        Page<Pet> petPage = petRepository.findAll(spec, pageable);
        return petConverter.toPageResponse(petPage);
    }

    @Override
    @Transactional
    public PetResponseDTO addOrUpdatePetWithImages(String petJson, List<MultipartFile> files) throws IOException {
        PetRequestDTO requestDTO = objectMapper.readValue(petJson, PetRequestDTO.class);

        Pet pet;
        boolean isUpdate = requestDTO.getPetId() != null && !requestDTO.getPetId().isEmpty();
        if (isUpdate) {
            pet = petRepository.findById(requestDTO.getPetId())
                    .orElseThrow(() -> new ResourceNotFoundException("Pet not found"));
            pet.setUpdatedAt(LocalDateTime.now());
        } else {
            pet = new Pet();
            pet.setPetId(generatePetId());
            pet.setCreatedAt(LocalDateTime.now());
            pet.setImages(new HashSet<>());
        }
        petConverter.mapToEntity(requestDTO, pet);

        //  Xử lý ảnh CŨ (Nếu có gửi danh sách ảnh cũ cần giữ lại)
        if (isUpdate && requestDTO.getOldImages() != null) {
            handlePetImages(pet, requestDTO.getOldImages());
        }

        //  Xử lý ảnh MỚI (Dùng logic AtomicInteger để không trùng ID)
        if (files != null && !files.isEmpty()) {
            String lastImageId = petImageRepository.findMaxImageId();
            int imageCounter = 1;
            if (lastImageId != null && lastImageId.startsWith("PI")) {
                try {
                    imageCounter = Integer.parseInt(lastImageId.substring(2)) + 1;
                } catch (NumberFormatException ignored) {}
            }
            AtomicInteger counter = new AtomicInteger(imageCounter);

            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                // Check file rỗng
                if (file.isEmpty()) continue;

                String imageUrl = cloudinaryService.uploadImage(file);

                PetImage petImage = new PetImage();

                // Sinh ID tăng dần
                petImage.setImageId(String.format("PI%03d", counter.getAndIncrement()));
                petImage.setPet(pet);
                petImage.setImageUrl(imageUrl);
                petImage.setCreatedAt(LocalDateTime.now());

                // Logic thumbnail: Nếu pet chưa có ảnh nào -> Ảnh đầu tiên là thumbnail
                boolean hasThumbnail = pet.getImages().stream().anyMatch(img -> Boolean.TRUE.equals(img.getIsThumbnail()));
                if (!hasThumbnail && i == 0) {
                    petImage.setIsThumbnail(true);
                } else {
                    petImage.setIsThumbnail(false);
                }

                pet.getImages().add(petImage);
            }
        }

        Pet savedPet = petRepository.save(pet);
        return petConverter.mapToDTO(savedPet);
    }

//    @Override
//    @Transactional
//    public PetResponseDTO addOrUpdatePetWithImages(PetRequestDTO requestDTO) throws IOException {
//        Pet pet;
//        boolean isUpdate = requestDTO.getPetId() != null && !requestDTO.getPetId().isEmpty();
//
//        // 1. Tìm hoặc Init
//        if (isUpdate) {
//            pet = petRepository.findById(requestDTO.getPetId())
//                    .orElseThrow(() -> new ResourceNotFoundException("Pet not found"));
//            pet.setUpdatedAt(LocalDateTime.now());
//        } else {
//            pet = new Pet();
//            pet.setPetId(generatePetId()); // P001...
//            pet.setCreatedAt(LocalDateTime.now());
//            // Init list ảnh rỗng để tránh null pointer
//            pet.setImages(new HashSet<>());
//        }
//
//        // 2. Map dữ liệu (Converter đã sửa ở Bước 2 sẽ giữ lại list ảnh cũ)
//        petConverter.mapToEntity(requestDTO, pet);
//
//        // 3. Xử lý ảnh CŨ (Chỉ chạy khi Update)
//        // Nếu FE gửi list oldImages, ta giữ lại những ảnh có trong list đó, xóa những cái không có
//        if (isUpdate && requestDTO.getOldImages() != null) {
//            handlePetImages(pet, requestDTO.getOldImages());
//        }
//
//        String lastImageId = petImageRepository.findMaxImageId(); // Lấy từ DB 1 lần thôi
//        int imageCounter = 1;
//        if (lastImageId != null && lastImageId.startsWith("PI")) {
//            try {
//                imageCounter = Integer.parseInt(lastImageId.substring(2)) + 1;
//            } catch (NumberFormatException ignored) {}
//        }
//        AtomicInteger counter = new AtomicInteger(imageCounter);
//        List<MultipartFile> files = requestDTO.getFiles();
//        if (files != null && !files.isEmpty()) {
//            for (int i = 0; i < files.size(); i++) {
//                MultipartFile file = files.get(i);
//                if (file.isEmpty()) continue;
//
//                String imageUrl = cloudinaryService.uploadImage(file);
//
//                PetImage petImage = new PetImage();
//
//                // Sinh ID tăng dần: PI011, PI012...
//                petImage.setImageId(String.format("PI%03d", counter.getAndIncrement()));
//
//                petImage.setPet(pet);
//                petImage.setImageUrl(imageUrl);
//                petImage.setCreatedAt(LocalDateTime.now());
//
//                boolean isFirst = pet.getImages().isEmpty(); // Check nếu list đang rỗng thì set thumb
//                petImage.setIsThumbnail(isFirst);
//
//                pet.getImages().add(petImage);
//            }
//        }
//
//        Pet savedPet = petRepository.save(pet);
//        return petConverter.mapToDTO(savedPet);
//    }

    private void handlePetImages(Pet pet, List<PetImageDTO> imageDTOs) {
        // Tạo Map từ danh sách ảnh cũ gửi lên
        Map<String, PetImageDTO> dtoMap = imageDTOs.stream()
                .filter(dto -> dto.getId() != null)
                .collect(Collectors.toMap(PetImageDTO::getId, dto -> dto));

        // Duyệt danh sách ảnh hiện tại trong DB
        Iterator<PetImage> iterator = pet.getImages().iterator();
        while (iterator.hasNext()) {
            PetImage oldImg = iterator.next();

            // Nếu ảnh trong DB không có trong danh sách gửi lên -> XÓA
            if (!dtoMap.containsKey(oldImg.getImageId())) {
                iterator.remove(); // Hibernate sẽ tự delete orphan
            }
            // Nếu có -> Cập nhật thông tin (ví dụ thumbnail)
            else {
                PetImageDTO dto = dtoMap.get(oldImg.getImageId());
                if (dto != null) {
                    oldImg.setIsThumbnail(Boolean.TRUE.equals(dto.getIsThumbnail()));
                }
            }
        }
    }



    private String generatePetId() {
        String maxId = petRepository.findMaxPetId().orElse(null);
        int next = 1;

        if (maxId != null && maxId.startsWith("P")) {
            try {
                next = Integer.parseInt(maxId.substring(1)) + 1;
            } catch (NumberFormatException ignored) {}
        }
        return String.format("P%03d", next);
    }

    private String generateNextPetImageId() {
        String maxId = petImageRepository.findMaxImageId();
        int next = 1;

        if (maxId != null && maxId.startsWith("PI")) {
            try {
                next = Integer.parseInt(maxId.substring(2)) + 1;
            } catch (NumberFormatException ignored) {}
        }

        return String.format("PI%03d", next);
    }



    @Override
    @Transactional
    public PetResponseDTO inactivePet(String petId) {
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new ResourceNotFoundException("Pet not found with id: " + petId));
        pet.setStatus(PetStatus.DRAFT);
        pet.setUpdatedAt(LocalDateTime.now());
        Pet savedPet = petRepository.save(pet);
        return petConverter.mapToDTO(savedPet);
    }

    @Override
    @Transactional
    public void deletePetPermanent(String petId) {
        if(!petRepository.existsById(petId)){
            throw new ResourceNotFoundException("Pet not found");
        }
        petRepository.deleteById(petId);
    }

    @Override
    public PageResponse<PetForListResponseDTO> getAllPetsList(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Pet> petPage = petRepository.findAll(pageable);

        List<PetForListResponseDTO> dtoList = petPage.getContent().stream()
                .map(petConverter::convertToDto)
                .collect(Collectors.toList());

        return petConverter.toPageResponseFromList(dtoList, petPage.getNumber(), petPage.getSize(), petPage.getTotalElements());
    }

    private String getSortField(String sortBy) {
        if (sortBy == null) return "createdAt";
        return switch (sortBy) {
            case "name" -> "name";
            case "price" -> "price";
            case "rating" -> "rating";
            default -> "createdAt";
        };
    }
}