package by.radeflex.steamshop.props;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "shop")
public class ShopProperties {
    private int id;
    private String token;
    private String returnUrl;
}
