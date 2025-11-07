package by.radeflex.steamshop.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String username;
    private String password;
    private String email;
    @Enumerated(EnumType.STRING)
    private UserRole role;
    @Column(insertable = false)
    private Integer points;
    @Column(insertable = false)
    private Integer balance;
    @Column(insertable = false)
    private LocalDateTime createdAt;

    @Builder.Default
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, orphanRemoval = true)
    private List<UserProduct> cart = new ArrayList<>();
}
