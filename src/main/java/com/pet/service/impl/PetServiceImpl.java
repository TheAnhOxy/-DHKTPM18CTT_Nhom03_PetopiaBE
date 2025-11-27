package com.pet.service.impl;

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

import java.time.LocalDateTime;
import java.util.*;
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
    public PetResponseDTO addOrUpdatePet(PetRequestDTO requestDTO) {
        Pet pet;
        boolean isUpdate = requestDTO.getPetId() != null;

        if (isUpdate) {
            pet = petRepository.findById(requestDTO.getPetId())
                    .orElseThrow(() -> new ResourceNotFoundException("Pet not found"));
            pet.setUpdatedAt(LocalDateTime.now());
        } else {
            pet = new Pet();
            pet.setCreatedAt(LocalDateTime.now());
        }
        pet = petConverter.mapToEntity(requestDTO, pet);
        Pet savedPet = petRepository.save(pet);
        if (requestDTO.getImages() != null && !requestDTO.getImages().isEmpty()) {
            handlePetImages(savedPet, requestDTO.getImages());
        }

        return petConverter.mapToDTO(savedPet);
    }

    private void handlePetImages(Pet pet, List<PetImageDTO> imageDTOs) {
        Map<String, PetImageDTO> dtoMap = imageDTOs.stream()
                .filter(dto -> dto.getId() != null)
                .collect(Collectors.toMap(PetImageDTO::getId, dto -> dto));
        Iterator<PetImage> iterator = pet.getImages().iterator();
        while (iterator.hasNext()) {
            PetImage oldImg = iterator.next();

            if (!dtoMap.containsKey(oldImg.getImageId())) {
                iterator.remove();
            }
        }
        for (PetImage img : pet.getImages()) {
            PetImageDTO dto = dtoMap.get(img.getImageId());
            if (dto != null) {
                img.setImageUrl(dto.getImageUrl());
                img.setIsThumbnail(Boolean.TRUE.equals(dto.getIsThumbnail()));
            }
        }
        for (PetImageDTO dto : imageDTOs) {
            if (dto.getId() == null) {
                PetImage newImg = new PetImage();
                newImg.setImageId(generateNextPetImageId());
                newImg.setPet(pet);
                newImg.setCreatedAt(LocalDateTime.now());
                newImg.setImageUrl(dto.getImageUrl());
                newImg.setIsThumbnail(Boolean.TRUE.equals(dto.getIsThumbnail()));

                pet.getImages().add(newImg);
            }
        }
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