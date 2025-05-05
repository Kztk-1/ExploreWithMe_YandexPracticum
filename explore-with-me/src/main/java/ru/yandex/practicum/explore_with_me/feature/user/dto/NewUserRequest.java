package ru.yandex.practicum.explore_with_me.feature.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.explore_with_me.config.Config;

@Data
@Builder
@AllArgsConstructor
public class NewUserRequest {
    @NotBlank(message = "Name cannot be blank")
    @Size(min = 2, max = Config.USER_NAME_MAX_LENGTH, message = "Name must be between 2 and 250 characters")
    private String name;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    @Size(max = Config.USER_EMAIL_MAX_LENGTH, message = "Email must be up to 254 characters")
    private String email;
}
