package codex.mmxxvi.services;

public interface JwtService {
    public String generateAccessToken(String username);
    public String generateRefreshToken(String username);
    public String extractUsername(String token);
    public String extractTokenType(String token);
    public boolean isValidToken(String token, String username);
    public boolean isRefreshToken(String token);
}
