package com.blognest.userservice.dto;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {
    
    private String firstName;
    
    private String lastName;
    
    @Email(message = "Email should be valid")
    private String email;
} 