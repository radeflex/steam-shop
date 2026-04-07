package by.radeflex.steamshop.http.controller;

import by.radeflex.steamshop.dto.LoginDto;
import by.radeflex.steamshop.dto.UserCreateDto;
import by.radeflex.steamshop.dto.response.JwtResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

@Tag(name = "Auth API", description = "API для регистрации и входа в систему")
public interface AuthController {
    @Operation(
            summary = "Регистрация нового пользователя",
            description = "Создаёт нового пользователя, возвращает JWT-токен и устанавливает cookie")
    @ApiResponse(responseCode = "200", description = "Пользователь успешно зарегистрирован")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации данных")
    ResponseEntity<JwtResponse> register(HttpServletResponse resp, UserCreateDto userCreateDto,
                                                BindingResult bindingResult);

    @Operation(
            summary = "Вход в систему",
            description = "Аутентифицирует пользователя по логину и паролю, возвращает JWT-токен и устанавливает cookie")
    @ApiResponse(responseCode = "200", description = "Успешный вход")
    @ApiResponse(responseCode = "401", description = "Неверные учётные данные")
    ResponseEntity<JwtResponse> login(HttpServletResponse resp,
                                             LoginDto loginDto);
}
