package com.pet.service;

import com.pet.enums.OrderStatus;
import com.pet.modal.response.*;
import com.pet.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
public class DashboardService {

    @Autowired private OrderRepository orderRepository;
    @Autowired private DeliveryRepository deliveryRepository;
    @Autowired private VaccineRepository vaccineRepository;
    @Autowired private PetRepository petRepository;
    @Autowired private PreBookingRepository preBookingRepository;
    //  Thống kê tổng quan (Cache 10 phút)
    @Cacheable(value = "dashboard_general_stats", key = "'stats'")
    public DashboardStatsDTO getGeneralStats() {
        return DashboardStatsDTO.builder()
                .totalRevenue(orderRepository.calculateTotalRevenue())
                .totalSoldPets(orderRepository.countTotalSoldPets())
                .shippingOrders(deliveryRepository.countShippingOrders())
                .scheduledVaccines(vaccineRepository.count()) // Tổng tất cả lịch đã tạo
                .build();
    }

    //  Top 10 bán chạy (Cache 1 giờ vì ít thay đổi nhanh)
    @Cacheable(value = "dashboard_top_selling", key = "'top10'")
    public List<TopSellingPetDTO> getTopSellingPets() {
        return orderRepository.findTopSellingPets(PageRequest.of(0, 10));
    }

    //  Biểu đồ tròn sức khỏe (Cache 5 phút)
    @Cacheable(value = "dashboard_pet_health", key = "'health'")
    public PetHealthStatsDTO getPetHealthStats() {
        LocalDateTime nextWeek = LocalDateTime.now().plusDays(7);

        return PetHealthStatsDTO.builder()
                .healthyPets(petRepository.countHealthyPets())
                .vaccinatedPets(vaccineRepository.countVaccinatedPets())
                .upcomingVaccines(vaccineRepository.countUpcomingVaccines(nextWeek))
                .build();
    }

    @Cacheable(value = "dashboard_top_users", key = "'top5'")
    public List<TopUserDTO> getTopSpendingUsers() {
        // Lấy Top 5
        return orderRepository.findTopSpendingUsers(PageRequest.of(0, 5));
    }

    @Cacheable(value = "dashboard_main_stats", key = "'main-' + (#startDate != null ? #startDate.toString() : 'all') + '-' + (#endDate != null ? #endDate.toString() : 'all')")
    public MainDashboardDTO getMainStats(LocalDate startDate, LocalDate endDate) {

        // 1. Xử lý ngày giờ mặc định
        LocalDateTime startDateTime;
        LocalDateTime endDateTime;

        if (startDate != null) {
            startDateTime = startDate.atStartOfDay(); // 00:00:00
        } else {
            // Mặc định lấy từ xa xưa (nếu muốn xem all time) hoặc đầu tháng này
            startDateTime = LocalDateTime.of(1970, 1, 1, 0, 0);
        }

        if (endDate != null) {
            endDateTime = endDate.atTime(23, 59, 59); // 23:59:59
        } else {
            endDateTime = LocalDateTime.now(); // Đến hiện tại
        }

        // 2. Tính toán các chỉ số cố định (Hôm nay, Tuần này, Tháng này)
        // Những cái này KHÔNG PHỤ THUỘC vào bộ lọc (để hiển thị so sánh)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime startOfWeek = now.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)).with(java.time.LocalTime.MIN);
        LocalDateTime startOfMonth = now.with(java.time.temporal.TemporalAdjusters.firstDayOfMonth()).with(java.time.LocalTime.MIN);

        // 3. Query dữ liệu theo bộ lọc (Filter)
        // Lưu ý: Các trường totalOrders, totalPreBookings... sẽ thay đổi theo bộ lọc ngày

        // SỬA ĐOẠN RETURN NÀY: Thêm hàm safeDouble()
        return MainDashboardDTO.builder()
                .revenueToday(safeDouble(orderRepository.calculateRevenueBetween(startOfDay, now)))
                .revenueThisWeek(safeDouble(orderRepository.calculateRevenueBetween(startOfWeek, now)))
                .revenueThisMonth(safeDouble(orderRepository.calculateRevenueBetween(startOfMonth, now)))

                .totalRevenue(safeDouble(orderRepository.calculateRevenueBetween(startDateTime, endDateTime)))

                .totalOrders(orderRepository.countByCreatedAtBetween(startDateTime, endDateTime))
                .totalPreBookings(preBookingRepository.countByCreatedAtBetween(startDateTime, endDateTime))
                .cancelledOrders(orderRepository.countByStatusAndCreatedAtBetween(OrderStatus.CANCELLED, startDateTime, endDateTime))
                .build();
    }

    // --- Helper function: Chống Null ---
    private Double safeDouble(Double value) {
        return value == null ? 0.0 : value;
    }

//    @Cacheable(value = "dashboard_monthly_chart", key = "#year")
    public List<MonthlyRevenueDTO> getMonthlyRevenueChart(int year) {
        // 1. Lấy dữ liệu thô từ DB
        List<Object[]> rawData = orderRepository.getMonthlyRevenue(year);

        // 2. Map thủ công sang DTO
        // Object[] structure: [0]: month, [1]: year, [2]: revenue, [3]: profit
        return rawData.stream()
                .map(row -> new MonthlyRevenueDTO(
                        ((Number) row[0]).intValue(), // Month
                        ((Number) row[1]).intValue(), // Year
                        ((Number) row[2]).doubleValue(), // Revenue
                        ((Number) row[3]).doubleValue()  // Profit
                ))
                .toList();
    }

    // --- API 3: THỐNG KÊ TRẠNG THÁI ĐƠN HÀNG (Cache 1 phút) ---
    @Cacheable(value = "dashboard_order_status", key = "'status'")
    public OrderStatusStatsDTO getOrderStatusStats() {
        // Cách tối ưu: Dùng 1 câu query Group By trong Repo sẽ nhanh hơn gọi 5 lần count.
        // Nhưng để đơn giản code và dễ hiểu, gọi 5 lần count cũng ổn với lượng data vừa phải.
        return OrderStatusStatsDTO.builder()
                .pending(orderRepository.countByStatus(OrderStatus.PENDING))
                .confirmed(orderRepository.countByStatus(OrderStatus.CONFIRMED))
                .shipped(orderRepository.countByStatus(OrderStatus.SHIPPED))
                .delivered(orderRepository.countByStatus(OrderStatus.DELIVERED))
                .cancelled(orderRepository.countByStatus(OrderStatus.CANCELLED))
                .build();
    }
}