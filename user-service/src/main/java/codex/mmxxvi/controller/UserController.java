package codex.mmxxvi.controller;

import codex.mmxxvi.dto.request.CreateUserRequest;
import codex.mmxxvi.dto.request.LoginRequest;
import codex.mmxxvi.dto.request.PageRequestDto;
import codex.mmxxvi.dto.request.UpdateUserRequest;
import codex.mmxxvi.dto.response.JwtResponse;
import codex.mmxxvi.dto.response.PageResponse;
import codex.mmxxvi.dto.response.UserResponse;
import codex.mmxxvi.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public PageResponse<UserResponse> getUsers(@Valid @ModelAttribute PageRequestDto pageRequestDto) {
        return userService.getAllUsers(pageRequestDto);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse registerUser(@Valid @RequestBody CreateUserRequest user) {
        return userService.registerUser(user);
    }

    @PostMapping("/login")
    public JwtResponse login(@Valid @RequestBody LoginRequest req) {
        return userService.login(req);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/users/{id}")
    public UserResponse update(@PathVariable String id, @Valid @RequestBody UpdateUserRequest request) {
        return userService.update(id, request);
    }

}
