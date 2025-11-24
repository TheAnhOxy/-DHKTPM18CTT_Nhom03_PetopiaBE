package com.pet.service.impl;

import com.pet.converter.VaccineConverter;
import com.pet.entity.Notification;
import com.pet.entity.Pet;
import com.pet.entity.User;
import com.pet.entity.Vaccin;
import com.pet.enums.NotificationType;
import com.pet.enums.VaccineStatus;
import com.pet.exception.ResourceNotFoundException;
import com.pet.modal.request.VaccineBatchCreateRequestDTO;
import com.pet.modal.request.VaccineUpdateRequestDTO;
import com.pet.modal.response.PageResponse;
import com.pet.modal.response.VaccineResponseDTO;
import com.pet.modal.response.VaccineStatsDTO;
import com.pet.repository.NotificationRepository;
import com.pet.repository.PetRepository;
import com.pet.repository.UserRepository;
import com.pet.repository.VaccineRepository;
import com.pet.service.EmailService;
import com.pet.service.VaccineService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class VaccineServiceImpl implements VaccineService {

    @Autowired private VaccineRepository vaccineRepository;
    @Autowired private PetRepository petRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private NotificationRepository notificationRepository;

    @Autowired private EmailService emailService;
    @Autowired private VaccineConverter vaccineConverter;
    @Autowired private ModelMapper modelMapper;

    @Transactional
    @Override
    public List<VaccineResponseDTO> createVaccineBatch(VaccineBatchCreateRequestDTO request) {
        // 1. Lấy User
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getUserId()));

        // 2. Chuẩn bị dữ liệu sinh ID
        List<Vaccin> createdVaccines = new ArrayList<>();
        String lastId = vaccineRepository.findLastVaccineId().orElse("VC000");
        AtomicInteger counter = new AtomicInteger(Integer.parseInt(lastId.substring(2)));

        List<String> petNames = new ArrayList<>(); // Để dùng cho email/noti

        // 3. Loop tạo lịch
        for (String petId : request.getPetIds()) {
            Pet pet = petRepository.findById(petId)
                    .orElseThrow(() -> new ResourceNotFoundException("Pet not found: " + petId));
            petNames.add(pet.getName());

            for (LocalDateTime date : request.getScheduledDates()) {
                Vaccin v = new Vaccin();
                v.setVaccineId(String.format("VC%03d", counter.incrementAndGet()));
                v.setUser(user);
                v.setPet(pet);
                modelMapper.map(request, v); // Map các trường chung
                v.setStartDate(date);
                v.setStatus(VaccineStatus.CHUA_TIEM);
                createdVaccines.add(v);
            }
        }

        // 4. Lưu Batch Vaccine
        List<Vaccin> savedList = vaccineRepository.saveAll(createdVaccines);

        // 5. TẠO NOTIFICATION (IN-APP)
        createInAppNotification(user, request.getVaccineName(), petNames, request.getScheduledDates());

        // 6. GỬI EMAIL
        sendEmailNotification(user, request.getVaccineName(), petNames, request.getScheduledDates());

        return savedList.stream()
                .map(vaccineConverter::toResponseDTO)
                .collect(Collectors.toList());
    }

    // --- Helper: Tạo thông báo In-App ---
    private void createInAppNotification(User user, String vaccineName, List<String> petNames, List<LocalDateTime> dates) {
        Notification noti = new Notification();

        // Sinh ID N001...
        String lastNotiId = notificationRepository.findLastNotificationId().orElse("N000");
        int nextId = Integer.parseInt(lastNotiId.substring(1)) + 1;
        noti.setNotificationId(String.format("N%03d", nextId));

        noti.setUser(user);
        noti.setTitle("Lịch tiêm phòng mới");
        noti.setTypeNote(NotificationType.VACCINATION_REMINDER);
        noti.setIsRead(false);

        // Format nội dung
        String pets = String.join(", ", petNames);
        String dateStr = dates.stream()
                .map(d -> d.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .collect(Collectors.joining(", "));

        noti.setContent("Bạn có lịch tiêm " + vaccineName + " cho " + pets + " vào các ngày: " + dateStr);

        notificationRepository.save(noti);
    }

    // --- Helper: Gửi Email ---
    private void sendEmailNotification(User user, String vaccineName, List<String> petNames, List<LocalDateTime> dates) {
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            String petsStr = String.join(", ", petNames);
            String datesStr = dates.stream()
                    .map(d -> d.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                    .collect(Collectors.joining("<br/>"));

            emailService.sendVaccineNotification(
                    user.getEmail(),
                    user.getFullName(),
                    vaccineName,
                    petsStr,
                    datesStr
            );
        }
    }



    private void validateDates(LocalDateTime start, LocalDateTime end) {
        if (start != null && end != null && end.isBefore(start)) {
            throw new IllegalArgumentException("Ngày hoàn thành không được trước ngày bắt đầu");
        }
    }
    @Transactional
    @Override
    public VaccineResponseDTO updateVaccine(String vaccineId, VaccineUpdateRequestDTO request) {
        Vaccin v = vaccineRepository.findById(vaccineId)
                .orElseThrow(() -> new ResourceNotFoundException("Vaccine schedule not found"));
        modelMapper.map(request, v);
        // Validate dates...
        if (v.getStartDate() != null && v.getEndDate() != null && v.getEndDate().isBefore(v.getStartDate())) {
            throw new IllegalArgumentException("Ngày hoàn thành không được trước ngày bắt đầu");
        }
        return vaccineConverter.toResponseDTO(vaccineRepository.save(v));
    }

    @Override
    public void deleteVaccine(String vaccineId) {
        if (!vaccineRepository.existsById(vaccineId)) throw new ResourceNotFoundException("Not found");
        vaccineRepository.deleteById(vaccineId);
    }

    @Override
    public PageResponse<VaccineResponseDTO> getAllVaccines(int page, int size) {
        return vaccineConverter.toPageResponse(vaccineRepository.findAll(PageRequest.of(page, size)));
    }

    @Override
    public VaccineStatsDTO getVaccineStats() {
        LocalDateTime now = LocalDateTime.now();
        return VaccineStatsDTO.builder()
                .totalSchedules(vaccineRepository.count())
                .completedSchedules(vaccineRepository.countByStatus(VaccineStatus.Da_TIEM))
                .upcomingSchedules(vaccineRepository.countUpcoming(now, now.plusDays(3)))
                .petsNeedVaccination(vaccineRepository.countPetsNeedVaccination())
                .build();
    }
}