package by.radeflex.steamshop.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String title;
    private String description;
    private Integer price;
    private String previewUrl;

    @Builder.Default
    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Account> accounts = new ArrayList<>();
}
