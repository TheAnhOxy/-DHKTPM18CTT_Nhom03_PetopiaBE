package com.pet.service;

import com.pet.entity.Pet;
import com.pet.modal.request.PetRequestDTO;
import com.pet.modal.search.PetSearchRequestDTO;
import com.pet.modal.response.PageResponse;
import com.pet.modal.response.PetForListResponseDTO;
import com.pet.modal.response.PetResponseDTO;

import java.util.List;

public interface PetService {

    List<PetResponseDTO> getPets();

    PetResponseDTO getPetById(String petId);
    PageResponse<PetForListResponseDTO> getPetsByCategory(String categoryId, int page, int size);
    PageResponse<PetForListResponseDTO> getAllPets(int page, int size);
    PageResponse<PetForListResponseDTO> getAllPetsWithStatusActive(int page, int size);
    PageResponse<PetForListResponseDTO> advanceSearch(PetSearchRequestDTO request);
    PetResponseDTO addOrUpdatePet(PetRequestDTO request);
    PetResponseDTO inactivePet(String petId);
    PageResponse<PetForListResponseDTO> getAllPetsList(int page, int size);
    void deletePetPermanent(String petId);
}
