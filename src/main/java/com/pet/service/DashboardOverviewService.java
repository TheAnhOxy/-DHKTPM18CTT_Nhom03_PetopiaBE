package com.pet.service;

import com.pet.entity.Order;
import com.pet.entity.Vaccin;
import com.pet.enums.UserRole;
import com.pet.modal.response.DashboardOverviewDTO;
import com.pet.modal.response.RecentActivityDTO;
import com.pet.modal.response.SocialStatsDTO;
import com.pet.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardOverviewService {

    @Autowired private OrderRepository orderRepository;
    @Autowired private PetRepository petRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ReviewRepository reviewRepository;
    @Autowired private VaccineRepository vaccineRepository;
    @Autowired private WishlistRepository wishlistRepository;

    // --- API 1: TỔNG QUAN (Overview) ---
//    @Cacheable(value = "dashboard_overview", key = "'overview'")
    public DashboardOverviewDTO getOverviewStats() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.with(TemporalAdjusters.firstDayOfMonth()).withHour(0).withMinute(0);

        return DashboardOverviewDTO.builder()
                .newPets(petRepository.countByCreatedAtBetween(startOfMonth, now))
                .newCustomers(userRepository.countByRoleAndCreatedAtBetween(UserRole.CUSTOMER, startOfMonth, now))
                .totalRevenue(orderRepository.calculateTotalRevenue())
                .ordersProcessing(orderRepository.countProcessingOrders())
                .newReviews(reviewRepository.countByCreatedAtBetween(startOfMonth, now))
                .build();
    }

    // --- API 2: HOẠT ĐỘNG GẦN ĐÂY (Orders + Vaccines) ---
    // Không cache để đảm bảo tính realtime cao nhất
    public List<RecentActivityDTO> getRecentActivities() {
        List<RecentActivityDTO> activities = new ArrayList<>();

        // 1. Lấy Lịch tiêm/khám mới nhất
        List<Vaccin> recentVaccines = vaccineRepository.findRecentVaccines();
        for (Vaccin v : recentVaccines) {
            activities.add(RecentActivityDTO.builder()
                    .activityType("VACCINE") // Loại: Lịch tiêm
                    .customerName(v.getUser() != null ? v.getUser().getFullName() : "Khách vãng lai")
                    .customerAvatar(v.getUser() != null ? v.getUser().getAvatar() : null)
                    .petName(v.getPet().getName())
                    .description("Lịch tiêm: " + v.getVaccineName())
                    .time(v.getCreatedAt()) // Lấy ngày tạo
                    .status(v.getStatus().getLabel())
                    .build());
        }

        // 2. Lấy Đơn hàng mới nhất (BỔ SUNG THEO YÊU CẦU)
        List<Order> recentOrders = orderRepository.findRecentOrders();
        for (Order o : recentOrders) {
            // Lấy tên Pet đầu tiên trong đơn để hiển thị ví dụ
            String petInfo = o.getOrderItems().isEmpty() ? "Không có thú cưng"
                    : o.getOrderItems().iterator().next().getPet().getName();

            if (o.getOrderItems().size() > 1) {
                petInfo += " và " + (o.getOrderItems().size() - 1) + " bé khác";
            }

            activities.add(RecentActivityDTO.builder()
                    .activityType("ORDER") // Loại: Đơn hàng
                    .customerName(o.getUser().getFullName())
                    .customerAvatar(o.getUser().getAvatar())
                    .petName(petInfo)
                    .description("Đơn hàng #" + o.getOrderId() + " - " + String.format("%,.0f đ", o.getTotalAmount()))
                    .time(o.getCreatedAt())
                    .status(o.getStatus().name()) // PENDING, CONFIRMED...
                    .build());
        }

        // 3. Gộp lại, Sắp xếp mới nhất lên đầu, Cắt lấy 10 dòng
        return activities.stream()
                .sorted(Comparator.comparing(RecentActivityDTO::getTime).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }

    // --- API 3: THỐNG KÊ TƯƠNG TÁC (Social Stats) ---
//    @Cacheable(value = "dashboard_social", key = "'social'")
    public SocialStatsDTO getSocialStats() {
        return SocialStatsDTO.builder()
                .totalLikes(wishlistRepository.count())
                .totalComments(reviewRepository.count())
                .topLikedPets(wishlistRepository.findTopFavoritedPets(PageRequest.of(0, 5)).getContent())
                .recentReviews(reviewRepository.findRecentReviews())
                .build();
    }
}