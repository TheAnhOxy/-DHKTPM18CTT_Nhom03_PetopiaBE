package com.pet.controller.web;

import com.pet.entity.Pet;
import com.pet.modal.request.PetRequestDTO;

import com.pet.modal.response.PageResponse;
import com.pet.modal.response.PetForListResponseDTO;
import com.pet.modal.response.PetResponseDTO;
import com.pet.modal.search.PetSearchRequestDTO;
import com.pet.service.PetService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/api/pets")
public class PetWebController {

    @Autowired
    private PetService petService;

    @GetMapping()
    public ResponseEntity<PageResponse<PetForListResponseDTO>> getAllPetsWithStatusActive(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageResponse<PetForListResponseDTO> pets = petService.getAllPetsWithStatusActive(page, size);
        if (pets == null || pets.getContent().isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(pets);
    }
    @PostMapping("/search")
    public ResponseEntity<PageResponse<PetForListResponseDTO>> searchPets(@RequestBody PetSearchRequestDTO request) {
        request.validate();
        PageResponse<PetForListResponseDTO> result = petService.advanceSearch(request);
        if (result == null || result.getContent().isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(result);
    }


}