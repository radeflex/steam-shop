package by.radeflex.steamshop.dto;

public interface UserInfo {
    String username();
    String password();
    String email();

    // for mapstruct
    default String getUsername() {
        return username();
    }

    default String getPassword() {
        return password();
    }

    default String getEmail() {
        return email();
    }
}
