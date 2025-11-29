package com.pet.converter;

import com.pet.config.ModelMapperConfig;
import com.pet.entity.*;
import com.pet.enums.PetFurType;
import com.pet.enums.PetGender;
import com.pet.enums.PetStatus;
import com.pet.exception.ResourceNotFoundException;
import com.pet.modal.request.PetRequestDTO;
import com.pet.modal.response.PageResponse;
import com.pet.modal.response.PetForListResponseDTO;
import com.pet.modal.response.PetImageResponseDTO;
import com.pet.modal.response.PetResponseDTO;
import com.pet.repository.CategoryRepository;
import com.pet.repository.OrderItemRepository;
import com.pet.repository.PetImageRepository;
import com.pet.repository.PetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class PetConverter {
    @Autowired
    private ModelMapperConfig modelMapper;
    @Autowired
    private PetImageRepository petImageRepository;
    @Autowired
    private PetRepository petRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;

    public PetForListResponseDTO mapToPetForListDTO(Pet pet) {
        return mapPet(pet, PetForListResponseDTO.class);
    }

//    public PetResponseDTO mapToDTO(Pet pet) {
//        return mapPet(pet, PetResponseDTO.class);
//    }

    public PetResponseDTO mapToDTO(Pet pet) {
        PetResponseDTO dto = modelMapper.getModelMapper().map(pet, PetResponseDTO.class);
        if (pet.getCategory() != null) {
            dto.setCategoryId(pet.getCategory().getCategoryId());
            dto.setCategoryName(pet.getCategory().getName());
        }

        // Lấy ảnh thumbnail chính
        if (pet.getImages() != null && !pet.getImages().isEmpty()) {
            Optional<PetImage> mainImageOpt = pet.getImages().stream()
                    .filter(img -> Boolean.TRUE.equals(img.getIsThumbnail()))
                    .findFirst();
            String mainImageUrl = mainImageOpt.map(PetImage::getImageUrl)
                    .orElse(pet.getImages().iterator().next().getImageUrl());
            dto.setMainImageUrl(mainImageUrl);
        }

        if (pet.getImages() != null && !pet.getImages().isEmpty()) {
            List<PetImageResponseDTO> imageDTOs = pet.getImages().stream()
                    .map(img -> modelMapper.getModelMapper().map(img, PetImageResponseDTO.class))
                    .collect(Collectors.toList());
            dto.setImages(imageDTOs);
        } else {
            dto.setImages(new ArrayList<>());
        }

        // Tính rating trung bình và số lượng đánh giá
        dto.setRating(calculateAverageRating(pet));
        dto.setReviewCount(pet.getReviews() != null ? pet.getReviews().size() : 0);

        return dto;
    }

    public PetForListResponseDTO mapToPetListDTO(Pet pet) {
        return mapPet(pet, PetForListResponseDTO.class);
    }

    public PageResponse<PetForListResponseDTO> toPageResponseFromList(
            List<PetForListResponseDTO> dtoList, int page, int size, long totalElements) {
        PageResponse<PetForListResponseDTO> response = new PageResponse<>();
        response.setContent(dtoList);
        response.setPage(page);
        response.setSize(size);
        response.setTotalElements(totalElements);
        return response;
    }


    public PetForListResponseDTO convertToDto(Pet pet){
        PetForListResponseDTO petListResponseDTO = modelMapper.getModelMapper().map(pet, PetForListResponseDTO.class);
        Category category = categoryRepository.findById(pet.getCategory().getCategoryId()).orElseThrow(() -> new RuntimeException("Category not found") );
        petListResponseDTO.setCategoryName(category.getName());
        Long totalSold = orderItemRepository.sumQuantityByPet(pet);
        petListResponseDTO.setTotalSold(totalSold != null ? totalSold : 0L);
        return petListResponseDTO;
    }

    public Pet mapToEntity(PetRequestDTO dto, Pet pet) {
        Set<PetImage> oldImages = pet.getImages();
        Set<PreBooking> oldPreBookings = pet.getPreBookings();
        Set<Review> oldReviews = pet.getReviews();
        Set<Vaccin> oldVaccines = pet.getVaccines();
        Set<Wishlist> wishlists = pet.getWishlists();
        Set<OrderItem> orderItems = pet.getOrderItems();

        modelMapper.getModelMapper().map(dto, pet);
//        pet.setImages(oldImages);
        pet.setImages(oldImages != null ? oldImages : new HashSet<>());
        pet.setPreBookings(oldPreBookings != null ? oldPreBookings : new HashSet<>());
        pet.setReviews(oldReviews != null ? oldReviews : new HashSet<>());
        pet.setVaccines(oldVaccines != null ? oldVaccines : new HashSet<>());
        pet.setWishlists(wishlists != null ? wishlists : new HashSet<>());
        pet.setOrderItems(orderItems != null ? orderItems : new HashSet<>());

        if (pet.getPetId() != null) {
            pet.setStatus(PetStatus.valueOf(dto.getStatus().toUpperCase()));
        } else {
            pet.setPetId(generatePetId());
            pet.setStatus(PetStatus.AVAILABLE);
        }

        if (dto.getFurType() != null) {
            pet.setFurType(PetFurType.valueOf(dto.getFurType().toUpperCase()));
        }

        if (dto.getGender() != null) {
            pet.setGender(PetGender.valueOf(dto.getGender().toUpperCase()));
        }

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + dto.getCategoryId()));
            pet.setCategory(category);
        }

        return pet;
    }


    private <T> T mapPet(Pet pet, Class<T> dtoClass) {
        T dto = modelMapper.getModelMapper().map(pet, dtoClass);

        // Lấy ảnh thumbnail
        Optional<PetImage> mainImageOpt = pet.getImages().stream()
                .filter(img -> Boolean.TRUE.equals(img.getIsThumbnail()))
                .findFirst();
        String mainImageUrl = mainImageOpt.map(PetImage::getImageUrl)
                .orElse(pet.getImages().isEmpty() ? null : pet.getImages().iterator().next().getImageUrl());

        if (dto instanceof PetForListResponseDTO) {
            PetForListResponseDTO petListDTO = (PetForListResponseDTO) dto;
            petListDTO.setMainImageUrl(mainImageUrl);
            if(pet.getCategory() != null) petListDTO.setCategoryName(pet.getCategory().getName());
            petListDTO.setRating(calculateAverageRating(pet));
            petListDTO.setReviewCount(pet.getReviews() != null ? pet.getReviews().size() : 0);

        } else if (dto instanceof PetResponseDTO) {
            PetResponseDTO petDTO = (PetResponseDTO) dto;
            petDTO.setMainImageUrl(mainImageUrl);
            if(pet.getCategory() != null) {
                petDTO.setCategoryName(pet.getCategory().getName());
                petDTO.setCategoryId(pet.getCategory().getCategoryId());
            }
            List<PetImageResponseDTO> imgDtos = pet.getImages().stream()
                    .map(img -> modelMapper.getModelMapper().map(img, PetImageResponseDTO.class))
                    .collect(Collectors.toList());
            petDTO.setImages(imgDtos);

            petDTO.setRating(calculateAverageRating(pet));
            petDTO.setReviewCount(pet.getReviews() != null ? pet.getReviews().size() : 0);
        }
        return dto;
    }

    public PageResponse<PetForListResponseDTO> toPageResponse(Page<Pet> petPage) {
        List<PetForListResponseDTO> petDTOs = petPage.getContent().stream()
                .map(this::mapToPetForListDTO)
                .toList();

        PageResponse<PetForListResponseDTO> response = new PageResponse<>();
        response.setContent(petDTOs);
        response.setPage(petPage.getNumber());
        response.setSize(petPage.getSize());
        response.setTotalElements(petPage.getTotalElements());
        return response;
    }

    public PageResponse<PetForListResponseDTO> toPageResponseList(Page<Pet> petPage) {
        List<PetForListResponseDTO> petDTOs = petPage.getContent().stream()
                .map(this::mapToPetListDTO)
                .toList();

        PageResponse<PetForListResponseDTO> response = new PageResponse<>();
        response.setContent(petDTOs);
        response.setPage(petPage.getNumber());
        response.setSize(petPage.getSize());
        response.setTotalElements(petPage.getTotalElements());
        return response;
    }

    private Double calculateAverageRating(Pet pet) {
        return pet.getReviews() != null && !pet.getReviews().isEmpty() ?
                pet.getReviews().stream()
                        .mapToDouble(review -> review.getRating() != null ? review.getRating() : 0)
                        .average()
                        .orElse(5.0)
                : 5.0;
    }

    private String generatePetId() {
        long count = petRepository.count() + 1;
        return String.format("P%03d", count);
    }
}
