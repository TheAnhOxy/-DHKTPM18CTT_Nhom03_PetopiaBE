package com.pet.entity;


import lombok.*;
import com.pet.enums.PetFurType;
import com.pet.enums.PetGender;
import com.pet.enums.PetStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "pets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Pet {
    @Id
    @EqualsAndHashCode.Include
    @Column(name = "pet_id", length = 10, nullable = false)
    private String petId;

    @Column(name = "name", nullable = false)
    private String name;

    @Lob
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "age")
    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", columnDefinition = "ENUM('MALE','FEMALE') DEFAULT 'MALE'")
    private PetGender gender;

    @Column(name = "price", nullable = false)
    private Double price;

    @Column(name = "discountPrice")
    private Double discountPrice;

    @Column(name = "health_status")
    private String healthStatus;

    @Lob
    @Column(name = "vaccination_history", columnDefinition = "TEXT")
    private String vaccinationHistory;

    @Column(name = "stock_quantity", columnDefinition = "INT DEFAULT 1")
    private Integer stockQuantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "ENUM('AVAILABLE','SOLD','DRAFT') DEFAULT 'DRAFT'")
    private PetStatus status;

    @Column(name = "video_url")
    private String videoUrl;

    @Column(name = "weight")
    private Double weight;

    @Column(name = "height")
    private Double height;

    @Column(name = "color")
    private String color;

    @Enumerated(EnumType.STRING)
    @Column(name = "fur_type", columnDefinition = "ENUM('SHORT','LONG','CURLY','NONE','OTHER') DEFAULT 'OTHER'")
    private PetFurType furType;

    @CreationTimestamp
    @Column(name = "created_at", columnDefinition = "DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6)", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "pet", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<PetImage> images = new HashSet<>();

    @OneToMany(mappedBy = "pet", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<Wishlist> wishlists;

    @OneToMany(mappedBy = "pet", cascade = {CascadeType.MERGE, CascadeType.PERSIST}, orphanRemoval = false, fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<OrderItem> orderItems;

    @OneToMany(mappedBy = "pet", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<PreBooking> preBookings;

    @OneToMany(mappedBy = "pet", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<Review> reviews;

    @OneToMany(mappedBy = "pet", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<Vaccin> vaccines;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "owner_id")
//    private User owner;
}
