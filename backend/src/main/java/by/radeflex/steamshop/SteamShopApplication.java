package by.radeflex.steamshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SteamShopApplication {
    public static void main(String[] args) {
        SpringApplication.run(SteamShopApplication.class, args);
    }
}
