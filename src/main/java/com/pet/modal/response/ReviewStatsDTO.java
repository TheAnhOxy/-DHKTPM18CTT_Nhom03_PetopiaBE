package com.pet.modal.response;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class ReviewStatsDTO {
    private long totalReviews;       // Tổng số đánh giá
    private double averageRating;    // Điểm trung bình
    private long repliedCount;       // Số đã trả lời
    private long unrepliedCount;     // Số chưa trả lời
    private Map<Integer, Long> starCounts; // Chi tiết từng sao: {5: 10, 4: 2, ...}
}