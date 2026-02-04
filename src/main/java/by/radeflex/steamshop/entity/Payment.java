package by.radeflex.steamshop.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Generated;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Payment {
    @Id
    private UUID id;
    @Generated
    @Column(insertable = false, updatable = false)
    private Integer orderId;
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;
    private Double amount;
    private String confirmationUrl;
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
    @Column(insertable = false)
    private LocalDateTime createdAt;
}
