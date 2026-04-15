package codex.mmxxvi.services.impl;

import codex.mmxxvi.dto.request.CreateUserRequest;
import codex.mmxxvi.dto.request.LoginRequest;
import codex.mmxxvi.dto.request.PageRequestDto;
import codex.mmxxvi.dto.request.UpdateUserRequest;
import codex.mmxxvi.dto.response.JwtResponse;
import codex.mmxxvi.dto.response.PageResponse;
import codex.mmxxvi.dto.response.UserResponse;
import codex.mmxxvi.entity.User;
import codex.mmxxvi.repository.UserRepository;
import codex.mmxxvi.services.JwtService;
import codex.mmxxvi.services.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.UUID;
import java.util.Set;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
public class UserServiceImpl implements UserService {
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("id", "username", "email", "role");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    private UserResponse convertDTO(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }


    @Override
    public Mono<PageResponse<UserResponse>> getAllUsers(PageRequestDto pageRequestDto) {
        return Mono.fromCallable(() -> {
                    validateSortField(pageRequestDto.getSortBy());
                    Pageable pageable = pageRequestDto.getPageable();
                    Page<User> userPage = userRepository.findAll(pageable);

                    return PageResponse.<UserResponse>builder()
                            .content(userPage.getContent().stream()
                                    .map(this::convertDTO)
                                    .toList())
                            .pageNo(userPage.getNumber())
                            .pageSize(userPage.getSize())
                            .totalElements(userPage.getTotalElements())
                            .totalPage(userPage.getTotalPages())
                            .last(userPage.isLast())
                            .build();
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<UserResponse> registerUser(CreateUserRequest user) {
        return Mono.fromCallable(() -> {
                    ensureUniqueEmail(user.getEmail());
                    ensureUniqueUsername(user.getUsername());

                    User u = new User();
                    u.setEmail(user.getEmail());
                    u.setUsername(user.getUsername());
                    u.setPassword(passwordEncoder.encode(user.getPassword()));
                    u.setRole(user.getRole());
                    return convertDTO(userRepository.save(u));
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<JwtResponse> login(LoginRequest request) {
        return Mono.fromCallable(() -> {
                    User user = userRepository.findByEmail(request.getEmail())
                            .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Invalid email or password"));

                    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                        throw new ResponseStatusException(UNAUTHORIZED, "Invalid email or password");
                    }

                    return JwtResponse.builder()
                            .accessToken(jwtService.generateAccessToken(user))
                            .refreshToken(jwtService.generateRefreshToken(user))
                            .user(convertDTO(user))
                            .build();
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Void> delete(String id) {
        return Mono.fromRunnable(() -> {
                    UUID userId = parseUserId(id);
                    if (!userRepository.existsById(userId)) {
                        throw new ResponseStatusException(NOT_FOUND, "User not found");
                    }
                    userRepository.deleteById(userId);
                })
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    @Override
    public Mono<UserResponse> update(String id, UpdateUserRequest request) {
        return Mono.fromCallable(() -> {
                    User user = userRepository.findById(parseUserId(id))
                            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));

                    if (request.getUsername() != null) {
                        validateTextField(request.getUsername(), "username");
                        if (!request.getUsername().equals(user.getUsername())) {
                            ensureUniqueUsername(request.getUsername());
                            user.setUsername(request.getUsername());
                        }
                    }

                    if (request.getEmail() != null) {
                        validateTextField(request.getEmail(), "email");
                        if (!request.getEmail().equals(user.getEmail())) {
                            ensureUniqueEmail(request.getEmail());
                            user.setEmail(request.getEmail());
                        }
                    }

                    if (request.getPassword() != null) {
                        validateTextField(request.getPassword(), "password");
                        user.setPassword(passwordEncoder.encode(request.getPassword()));
                    }

                    if (request.getRole() != null) {
                        user.setRole(request.getRole());
                    }

                    return convertDTO(userRepository.save(user));
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    private void validateSortField(String sortBy) {
        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new ResponseStatusException(BAD_REQUEST, "sortBy must be one of: " + ALLOWED_SORT_FIELDS);
        }
    }

    private void ensureUniqueEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(CONFLICT, "Email already exists");
        }
    }

    private void ensureUniqueUsername(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new ResponseStatusException(CONFLICT, "Username already exists");
        }
    }

    private void validateTextField(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new ResponseStatusException(BAD_REQUEST, fieldName + " must not be blank");
        }
    }

    private UUID parseUserId(String id) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(BAD_REQUEST, "Invalid user id");
        }
    }
}
