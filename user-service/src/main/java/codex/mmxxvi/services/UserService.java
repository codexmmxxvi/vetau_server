package codex.mmxxvi.services;

import codex.mmxxvi.dto.request.CreateUserRequest;
import codex.mmxxvi.dto.request.LoginRequest;
import codex.mmxxvi.dto.request.PageRequestDto;
import codex.mmxxvi.dto.request.UpdateUserRequest;
import codex.mmxxvi.dto.response.JwtResponse;
import codex.mmxxvi.dto.response.PageResponse;
import codex.mmxxvi.dto.response.UserResponse;

public interface UserService {

    PageResponse<UserResponse> getAllUsers(PageRequestDto pageRequestDto);
    UserResponse registerUser(CreateUserRequest user);
    JwtResponse login(LoginRequest request);
    void delete(String id);
    UserResponse update(String id, UpdateUserRequest request);

}
