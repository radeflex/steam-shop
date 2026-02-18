package by.radeflex.steamshop.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String title;
    private String text;
    @Enumerated(EnumType.STRING)
    private NotificationType type;
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;
    @OneToOne(fetch = FetchType.LAZY)
    private Payment payment;
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;
    @OneToOne
    @JoinColumn(name = "created_by")
    private User createdBy;
    @Column(insertable = false)
    private LocalDateTime createdAt;
}
