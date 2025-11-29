package com.pet.controller.admin;



import com.pet.entity.User;
import com.pet.modal.request.ReviewReplyRequestDTO;
import com.pet.modal.request.ReviewRequestDTO;
import com.pet.modal.response.ApiResponse;
import com.pet.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/reviews")
public class AdminReviewController {

        @Autowired
        private ReviewService reviewService;

        @GetMapping
        public ResponseEntity<ApiResponse> getReviews(
                @RequestParam(required = false) String petId,
                @RequestParam(required = false) Integer rating,
                @RequestParam(required = false) Boolean isReplied,
                @RequestParam(defaultValue = "0") int page,
                @RequestParam(defaultValue = "10") int size) {

            return ResponseEntity.ok(ApiResponse.builder()
                    .status(200)
                    .data(reviewService.getReviews(petId, rating, isReplied, page, size))
                    .build());
        }



        //  Admin trả lời đánh giá
//        @PutMapping("/{reviewId}/reply")
//        @PreAuthorize("hasRole('ADMIN')")
//        public ResponseEntity<ApiResponse> replyReview(
//                @PathVariable String reviewId,
//                @Valid @RequestBody ReviewReplyRequestDTO request) {
//
//            return ResponseEntity.ok(ApiResponse.builder()
//                    .status(200)
//                    .message("Đã trả lời đánh giá")
//                    .data(reviewService.replyToReview(reviewId, request))
//                    .build());
//        }

        // Admin xem thống kê Dashboard
        @GetMapping("/stats")
        public ResponseEntity<ApiResponse> getStats() {
            return ResponseEntity.ok(ApiResponse.builder()
                    .status(200)
                    .message("Lấy thống kê đánh giá thành công")
                    .data(reviewService.getReviewStats())
                    .build());
        }

    @PutMapping("/{reviewId}/reply")
     @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> updateReply(
            @PathVariable String reviewId,
            @Valid @RequestBody ReviewReplyRequestDTO request) {

        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .message("Cập nhật câu trả lời thành công")
                .data(reviewService.replyToReview(reviewId, request))
                .build());
    }

    //  XÓA REPLY (Chỉ xóa câu trả lời của Admin, giữ nguyên comment của khách)
    @DeleteMapping("/{reviewId}/reply")
     @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> deleteReply(@PathVariable String reviewId) {

        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .message("Đã xóa câu trả lời của Admin")
                .data(reviewService.deleteReply(reviewId)) // Trả về DTO mới nhất để FE cập nhật lại UI
                .build());
    }

        //  Admin xóa đánh giá (Spam/Vi phạm)
        @DeleteMapping("/{reviewId}")
        public ResponseEntity<ApiResponse> deleteReview(@PathVariable String reviewId) {
            reviewService.deleteReview(reviewId);
            return ResponseEntity.ok(ApiResponse.builder()
                    .status(200)
                    .message("Đã xóa đánh giá")
                    .build());
        }


}
