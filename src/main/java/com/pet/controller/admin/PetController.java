package com.pet.controller.admin;

import com.pet.modal.request.PetRequestDTO;
import com.pet.modal.response.PageResponse;
import com.pet.modal.response.PetForListResponseDTO;
import com.pet.modal.response.PetResponseDTO;
import com.pet.modal.response.ApiResponse;
import com.pet.modal.search.PetSearchRequestDTO;
import com.pet.service.PetService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/admin/pets")
public class PetController {

    @Autowired
    private PetService petService;

    @GetMapping("/hello")
    public String hello() {
        return "Hello Pet";
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getAllPets() {
        var pets = petService.getPets();

        ApiResponse response = ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Lấy danh sách thú cưng thành công")
                .data(pets)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getPetById(@PathVariable String id) {
        PetResponseDTO pet = petService.getPetById(id);

        if (pet == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.builder()
                            .status(HttpStatus.NOT_FOUND.value())
                            .message("Không tìm thấy thú cưng")
                            .build());
        }

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .status(HttpStatus.OK.value())
                        .message("Lấy chi tiết thú cưng thành công")
                        .data(pet)
                        .build()
        );
    }

//
//    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<ApiResponse> createOrUpdatePet(
//            @RequestPart("pet") String petJson,
//            @RequestPart(value = "files", required = false) List<MultipartFile> files
//    ) {
//        try {
//            log.info("Đang tạo Pet với JSON: {}", petJson);
//            PetResponseDTO pet = petService.addOrUpdatePetWithImages(petJson, files);
//
//            return ResponseEntity.status(HttpStatus.CREATED).body(
//                    ApiResponse.builder()
//                            .status(HttpStatus.CREATED.value())
//                            .message("Tạo/Cập nhật thú cưng thành công")
//                            .data(pet)
//                            .build()
//            );
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.badRequest().body(
//                    ApiResponse.builder()
//                            .status(400)
//                            .message("Lỗi: " + e.getMessage())
//                            .build()
//            );
//        }
//    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse> createOrUpdatePet(
            @RequestPart("pet") @Valid PetRequestDTO petRequest,
            BindingResult bindingResult,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        try {
            if (bindingResult.hasErrors()) {
                String errorMessage = bindingResult.getAllErrors().stream()
                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                        .collect(Collectors.joining(", "));

                return ResponseEntity.badRequest().body(
                        ApiResponse.builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .message("Dữ liệu không hợp lệ: " + errorMessage)
                                .build()
                );
            }
            log.info("Đang tạo Pet: {}", petRequest.getName());
            PetResponseDTO pet = petService.addOrUpdatePetWithImages(petRequest, files);

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    ApiResponse.builder()
                            .status(HttpStatus.CREATED.value())
                            .message("Tạo/Cập nhật thú cưng thành công")
                            .data(pet)
                            .build()
            );
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder()
                            .status(400)
                            .message("Lỗi: " + e.getMessage())
                            .build()
            );
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deletePet(@PathVariable String id) {
        petService.deletePetPermanent(id);

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .status(HttpStatus.OK.value())
                        .message("Xóa thú cưng thành công")
                        .build()
        );
    }


    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse> getPetsByCategory(
            @PathVariable String categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageResponse<PetForListResponseDTO> pets =
                petService.getPetsByCategory(categoryId, page, size);

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .status(HttpStatus.OK.value())
                        .message("Lấy danh sách thú cưng theo category thành công")
                        .data(pets)
                        .build()
        );
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse> getAllPetsList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageResponse<PetForListResponseDTO> pets = petService.getAllPets(page, size);

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .status(HttpStatus.OK.value())
                        .message("Lấy danh sách thú cưng thành công")
                        .data(pets)
                        .build()
        );
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse> getAllPetsWithStatusActive(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageResponse<PetForListResponseDTO> pets = petService.getAllPetsWithStatusActive(page, size);

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .status(HttpStatus.OK.value())
                        .message("Lấy danh sách thú cưng khả dụng thành công")
                        .data(pets)
                        .build()
        );
    }

    @PostMapping("/search")
    public ResponseEntity<ApiResponse> advanceSearch(
            @Valid @RequestBody PetSearchRequestDTO request
    ) {
        log.info("Searching pets with filters...");
        request.validate();

        var result = petService.advanceSearch(request);

        String message = result.getContent().isEmpty()
                ? "Không tìm thấy thú cưng nào phù hợp"
                : "Tìm kiếm thú cưng thành công";

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .status(HttpStatus.OK.value())
                        .message(message)
                        .data(result)
                        .build()
        );
    }

    @PutMapping("/{id}/inactive")
    public ResponseEntity<ApiResponse> inactivePet(@PathVariable String id) {
        PetResponseDTO pet = petService.inactivePet(id);

        if (pet == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.builder()
                            .status(HttpStatus.NOT_FOUND.value())
                            .message("Không tìm thấy thú cưng")
                            .build());
        }

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .status(HttpStatus.OK.value())
                        .message("Vô hiệu hóa thú cưng thành công")
                        .data(pet)
                        .build()
        );
    }
}
