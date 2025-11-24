package com.pet.service;

import com.pet.entity.Notification;
import com.pet.entity.User;
import com.pet.enums.NotificationType;
import com.pet.modal.request.VaccineBatchCreateRequestDTO;
import com.pet.modal.request.VaccineUpdateRequestDTO;
import com.pet.modal.response.PageResponse;
import com.pet.modal.response.VaccineResponseDTO;
import com.pet.modal.response.VaccineStatsDTO;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public interface VaccineService {



    VaccineResponseDTO updateVaccine(String vaccineId, VaccineUpdateRequestDTO request);

    void deleteVaccine(String vaccineId);
    List<VaccineResponseDTO> createVaccineBatch(VaccineBatchCreateRequestDTO request);
    PageResponse<VaccineResponseDTO> getAllVaccines(int page, int size);

    VaccineStatsDTO getVaccineStats();
}
