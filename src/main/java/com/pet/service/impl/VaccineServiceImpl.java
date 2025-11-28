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
import com.pet.repository.*;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    @Autowired private OrderRepository orderRepository;

    @Transactional
    @Override
    public List<VaccineResponseDTO> createVaccineBatch(VaccineBatchCreateRequestDTO request) {
        // Validate ngày tháng
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("Ngày kết thúc không được trước ngày bắt đầu");
        }

        List<Vaccin> createdVaccines = new ArrayList<>();
        String lastId = vaccineRepository.findLastVaccineId().orElse("VC000");
        AtomicInteger counter = new AtomicInteger(Integer.parseInt(lastId.substring(2)));

        // Map dùng để gom nhóm gửi email: <Chủ sở hữu, List<Tên Thú Cưng>>
        Map<User, List<String>> mailMap = new HashMap<>();

        // 1. Duyệt qua từng Pet ID từ FE gửi lên
        for (String petId : request.getPetIds()) {
            Pet pet = petRepository.findById(petId)
                    .orElseThrow(() -> new ResourceNotFoundException("Pet not found: " + petId));

            // --- TỰ ĐỘNG TÌM CHỦ ---
            User owner = findOwnerOfPet(pet);

            if (owner == null) {
                // Log cảnh báo nếu pet không có chủ, nhưng vẫn tạo lịch (lịch nội bộ shop)
                System.out.println("Cảnh báo: Pet " + pet.getName() + " chưa có chủ sở hữu trong hệ thống đơn hàng.");
                // Tùy logic: Có thể continue (bỏ qua) hoặc tạo vaccine với user = null (nếu DB cho phép)
                // Ở đây giả sử bắt buộc phải có chủ, nếu không có thì bỏ qua con này
                continue;
            }

            // Tạo Vaccine
            Vaccin v = new Vaccin();
            v.setVaccineId(String.format("VC%03d", counter.incrementAndGet()));
            v.setPet(pet);
            v.setUser(owner); // Gán đúng chủ vừa tìm được

            modelMapper.map(request, v); // Map các thông tin chung (Tên vaccin, ngày...)
            v.setStartDate(request.getStartDate());
            v.setEndDate(request.getEndDate());
            v.setStatus(VaccineStatus.CHUA_TIEM);

            createdVaccines.add(v);

            // Gom vào Map để tí nữa gửi mail
            // Nếu owner đã có trong Map thì add thêm tên pet, chưa có thì tạo list mới
            mailMap.computeIfAbsent(owner, k -> new ArrayList<>()).add(pet.getName());
        }

        // 2. Lưu tất cả lịch tiêm
        List<Vaccin> savedList = vaccineRepository.saveAll(createdVaccines);

        // 3. Gửi Email & Notification (Duyệt theo danh sách chủ)
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String dateRangeStr = request.getStartDate().format(fmt) + " - " + request.getEndDate().format(fmt);

        for (Map.Entry<User, List<String>> entry : mailMap.entrySet()) {
            User owner = entry.getKey();
            List<String> petNames = entry.getValue(); // Danh sách tên pet của chủ này

            // Gửi Noti
            createInAppNotification(owner, request.getVaccineName(), petNames, dateRangeStr);

            // Gửi Email
            sendEmailNotification(owner, request.getVaccineName(), petNames, dateRangeStr, request.getNote());
        }

        return savedList.stream().map(vaccineConverter::toResponseDTO).collect(Collectors.toList());
    }

    // Hàm tìm chủ (Vẫn dùng logic cũ hoặc logic mới getOwner() nếu đã sửa Entity Pet)
    private User findOwnerOfPet(Pet pet) {
        // Ưu tiên 1: Lấy trực tiếp nếu Entity Pet đã có owner
        // return pet.getOwner();

        // Ưu tiên 2: Tìm qua đơn hàng (Logic cũ của bạn)
        List<User> owners = orderRepository.findOwnersByPetId(pet.getPetId());
        return owners.isEmpty() ? null : owners.get(0);
    }

    // Helper: Noti In-App
    private void createInAppNotification(User user, String vaccineName, List<String> petNames, String dateRangeStr) {
        Notification noti = new Notification();
        String lastNotiId = notificationRepository.findLastNotificationId().orElse("N000");
        int nextId = Integer.parseInt(lastNotiId.substring(1)) + 1;
        noti.setNotificationId(String.format("N%03d", nextId));

        noti.setUser(user);
        noti.setTitle("Lịch tiêm phòng mới");
        noti.setTypeNote(NotificationType.VACCINATION_REMINDER);
        noti.setIsRead(false);

        String pets = String.join(", ", petNames);
        noti.setContent("Lịch tiêm " + vaccineName + " cho " + pets + " vào thời gian: " + dateRangeStr);

        notificationRepository.save(noti);
    }

    @Override
    public List<VaccineResponseDTO> getVaccineHistoryByPet(String petId) {
        return vaccineRepository.findByPetId(petId).stream()
                .map(vaccineConverter::toResponseDTO)
                .collect(Collectors.toList());
    }

    // Helper: Gửi Email (Gọi hàm mới đã sửa ở bước 1)
    private void sendEmailNotification(User user, String vaccineName, List<String> petNames, String dateRangeStr, String note) {
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            String petsStr = String.join(", ", petNames);
            // Gọi đúng hàm có 6 tham số
            emailService.sendVaccineNotification(
                    user.getEmail(),
                    user.getFullName(),
                    vaccineName,
                    petsStr,
                    dateRangeStr, // Truyền chuỗi range ngày đã format
                    note
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