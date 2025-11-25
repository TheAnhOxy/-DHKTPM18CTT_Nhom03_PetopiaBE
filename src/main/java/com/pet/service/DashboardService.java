package com.pet.service;

import com.pet.modal.response.DashboardStatsDTO;
import com.pet.modal.response.PetHealthStatsDTO;
import com.pet.modal.response.TopSellingPetDTO;
import com.pet.repository.DeliveryRepository;
import com.pet.repository.OrderRepository;
import com.pet.repository.PetRepository;
import com.pet.repository.VaccineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DashboardService {

    @Autowired private OrderRepository orderRepository;
    @Autowired private DeliveryRepository deliveryRepository;
    @Autowired private VaccineRepository vaccineRepository;
    @Autowired private PetRepository petRepository;

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
}