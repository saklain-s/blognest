package com.blognest.userservice.service;

import com.blognest.common.dto.ApiResponse;
import com.blognest.common.security.JwtTokenProvider;
import com.blognest.userservice.dto.AuthRequest;
import com.blognest.userservice.dto.AuthResponse;
import com.blognest.userservice.dto.UserRegistrationRequest;
import com.blognest.userservice.dto.UserUpdateRequest;
import com.blognest.userservice.entity.User;
import com.blognest.userservice.exception.UserAlreadyExistsException;
import com.blognest.userservice.exception.UserNotFoundException;
import com.blognest.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private AuthRequest authRequest;
    private UserRegistrationRequest registrationRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .firstName("Test")
                .lastName("User")
                .role(User.Role.USER)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();

        authRequest = new AuthRequest("testuser", "password");
        registrationRequest = UserRegistrationRequest.builder()
                .username("newuser")
                .email("new@example.com")
                .password("password")
                .firstName("New")
                .lastName("User")
                .build();

        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void authenticate_Success() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("jwt-token");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        ApiResponse<AuthResponse> response = userService.authenticate(authRequest);

        // Assert
        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertEquals("Authentication successful", response.getMessage());
        assertNotNull(response.getData());
        assertEquals("jwt-token", response.getData().getToken());
        assertEquals("testuser", response.getData().getUsername());
        assertEquals("test@example.com", response.getData().getEmail());
        assertEquals("USER", response.getData().getRole());

        verify(userRepository).save(any(User.class));
    }

    @Test
    void authenticate_Failure() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Authentication failed"));

        // Act
        ApiResponse<AuthResponse> response = userService.authenticate(authRequest);

        // Assert
        assertNotNull(response);
        assertEquals("ERROR", response.getStatus());
        assertEquals("Invalid username or password", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void registerUser_Success() {
        // Arrange
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        ApiResponse<User> response = userService.registerUser(registrationRequest);

        // Assert
        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertEquals("User registered successfully", response.getMessage());
        assertNotNull(response.getData());
        assertEquals("testuser", response.getData().getUsername());

        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_UsernameAlreadyExists() {
        // Arrange
        when(userRepository.existsByUsername("newuser")).thenReturn(true);

        // Act & Assert
        assertThrows(UserAlreadyExistsException.class, () -> {
            userService.registerUser(registrationRequest);
        });
    }

    @Test
    void registerUser_EmailAlreadyExists() {
        // Arrange
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(true);

        // Act & Assert
        assertThrows(UserAlreadyExistsException.class, () -> {
            userService.registerUser(registrationRequest);
        });
    }

    @Test
    void getUserById_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        ApiResponse<User> response = userService.getUserById(1L);

        // Assert
        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertNotNull(response.getData());
        assertEquals(1L, response.getData().getId());
    }

    @Test
    void getUserById_NotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> {
            userService.getUserById(1L);
        });
    }

    @Test
    void getUserByUsername_Success() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        ApiResponse<User> response = userService.getUserByUsername("testuser");

        // Assert
        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertNotNull(response.getData());
        assertEquals("testuser", response.getData().getUsername());
    }

    @Test
    void getUserByUsername_NotFound() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> {
            userService.getUserByUsername("testuser");
        });
    }

    @Test
    void getAllUsers_Success() {
        // Arrange
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findAll()).thenReturn(users);

        // Act
        ApiResponse<List<User>> response = userService.getAllUsers();

        // Assert
        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().size());
    }

    @Test
    void updateUser_Success() {
        // Arrange
        UserUpdateRequest updateRequest = UserUpdateRequest.builder()
                .firstName("Updated")
                .lastName("Name")
                .email("updated@example.com")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("updated@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        ApiResponse<User> response = userService.updateUser(1L, updateRequest);

        // Assert
        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertEquals("User updated successfully", response.getMessage());
    }

    @Test
    void updateUser_NotFound() {
        // Arrange
        UserUpdateRequest updateRequest = UserUpdateRequest.builder()
                .firstName("Updated")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> {
            userService.updateUser(1L, updateRequest);
        });
    }

    @Test
    void deleteUser_Success() {
        // Arrange
        when(userRepository.existsById(1L)).thenReturn(true);

        // Act
        ApiResponse<Void> response = userService.deleteUser(1L);

        // Assert
        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertEquals("User deleted successfully", response.getMessage());

        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_NotFound() {
        // Arrange
        when(userRepository.existsById(1L)).thenReturn(false);

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> {
            userService.deleteUser(1L);
        });
    }

    @Test
    void searchUsers_Success() {
        // Arrange
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findByKeyword("test")).thenReturn(users);

        // Act
        ApiResponse<List<User>> response = userService.searchUsers("test");

        // Assert
        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().size());
    }

    @Test
    void getUsersByRole_Success() {
        // Arrange
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findByRole(User.Role.USER)).thenReturn(users);

        // Act
        ApiResponse<List<User>> response = userService.getUsersByRole(User.Role.USER);

        // Assert
        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().size());
    }

    @Test
    void getUsersCountByRole_Success() {
        // Arrange
        when(userRepository.countByRole(User.Role.USER)).thenReturn(5L);

        // Act
        ApiResponse<Long> response = userService.getUsersCountByRole(User.Role.USER);

        // Assert
        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertEquals(5L, response.getData());
    }

    @Test
    void getActiveUsers_Success() {
        // Arrange
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findByEnabled(true)).thenReturn(users);

        // Act
        ApiResponse<List<User>> response = userService.getActiveUsers();

        // Assert
        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().size());
    }

    @Test
    void getUsersCreatedAfter_Success() {
        // Arrange
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findUsersCreatedAfter(startDate)).thenReturn(users);

        // Act
        ApiResponse<List<User>> response = userService.getUsersCreatedAfter(startDate);

        // Assert
        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().size());
    }

    @Test
    void validateToken_Success() {
        // Arrange
        String token = "valid-token";
        String username = "testuser";
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);

        // Act
        boolean result = userService.validateToken(token, username);

        // Assert
        assertTrue(result);
    }

    @Test
    void validateToken_Failure() {
        // Arrange
        String token = "invalid-token";
        String username = "testuser";
        when(jwtTokenProvider.validateToken(token)).thenReturn(false);

        // Act
        boolean result = userService.validateToken(token, username);

        // Assert
        assertFalse(result);
    }

    @Test
    void getUsernameFromToken_Success() {
        // Arrange
        String token = "valid-token";
        when(jwtTokenProvider.getUsernameFromToken(token)).thenReturn("testuser");

        // Act
        String result = userService.getUsernameFromToken(token);

        // Assert
        assertEquals("testuser", result);
    }
} 