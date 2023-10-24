package be.markus.syngestbel.security.auth;

public record AuthRecord(
        String username,
        String password,
        boolean withRefreshToken,
        String grantType,
        String refreshToken
) {
}
