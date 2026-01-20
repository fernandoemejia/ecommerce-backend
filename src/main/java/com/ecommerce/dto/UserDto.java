package com.ecommerce.dto;

import com.ecommerce.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record UserDto(
        Long id,
        String username,
        String email,
        String firstName,
        String lastName,
        String phone,
        String address,
        User.Role role,
        boolean enabled,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static UserDto fromEntity(User user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.getAddress(),
                user.getRole(),
                user.isEnabled(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    public record CreateRequest(
            @NotBlank @Size(min = 3, max = 50) String username,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8) String password,
            String firstName,
            String lastName,
            String phone,
            String address
    ) {
        public User toEntity() {
            User user = new User(username, email, password);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setPhone(phone);
            user.setAddress(address);
            return user;
        }
    }

    public record UpdateRequest(
            String firstName,
            String lastName,
            String phone,
            String address
    ) {}

    public record LoginRequest(
            @NotBlank String email,
            @NotBlank String password
    ) {}

    public record LoginResponse(
            String token,
            String tokenType,
            UserDto user
    ) {
        public LoginResponse(String token, UserDto user) {
            this(token, "Bearer", user);
        }
    }

    public record ChangePasswordRequest(
            @NotBlank String currentPassword,
            @NotBlank @Size(min = 8) String newPassword
    ) {}
}
