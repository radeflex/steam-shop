package by.radeflex.steamshop.configuration;

import by.radeflex.steamshop.props.ShopProperties;
import lombok.RequiredArgsConstructor;
import me.dynomake.yookassa.Yookassa;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ShopConfig {
    private final ShopProperties shopProperties;

    @Bean
    public Yookassa yookassa() {
        return Yookassa.initialize(
                shopProperties.getId(),
                shopProperties.getToken());
    }
}
