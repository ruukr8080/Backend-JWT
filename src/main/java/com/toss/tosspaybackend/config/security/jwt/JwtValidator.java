package com.toss.tosspaybackend.config.security.jwt;

import com.toss.tosspaybackend.config.security.SecurityProperties;
import com.toss.tosspaybackend.config.security.jwt.enums.TokenStatus;
import com.toss.tosspaybackend.config.security.jwt.enums.TokenType;
import com.toss.tosspaybackend.domain.member.entity.Member;
import com.toss.tosspaybackend.domain.member.repository.MemberRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtValidator {
    private final Key key;
    private final MemberRepository memberRepository;
    private final SecurityProperties securityProperties;

    public TokenAuthentication getAuthentication(String accessToken, String refreshToken) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        Claims refreshTokenClaims = null;
        try {
            refreshTokenClaims = getTokenBodyClaims(refreshToken, TokenType.REFRESH_TOKEN);
            Claims accessTokenClaims = getTokenBodyClaims(accessToken, TokenType.ACCESS_TOKEN);

            if (!accessTokenClaims.get("id", Long.class).equals(refreshTokenClaims.get("id", Long.class))) {
                throw new AccessDeniedException("Invalid Token");
            }

            return TokenAuthentication.builder()
                    .authentication(new UsernamePasswordAuthenticationToken(memberRepository.findById(accessTokenClaims.get("id", Long.class)), "", authorities))
                    .tokenStatus(TokenStatus.VALID)
                    .build();
        } catch (CustomJwtException e) {
            // Refresh Token 재발급
            if (e.getCause() instanceof RefreshTokenHalfExpiredException hex) {
                Member member = memberRepository.findById(hex.getMemberId())
                        .orElseThrow(() -> new AccessDeniedException("Invalid Token"));

                return TokenAuthentication.builder()
                        .authentication(new UsernamePasswordAuthenticationToken(member, "", authorities))
                        .tokenStatus(TokenStatus.REFRESH_TOKEN_REGENERATION)
                        .build();
            }

            JwtException je = (JwtException) e.getCause();
            // Access Token 재발급
            if (e.getTokenType().equals(TokenType.ACCESS_TOKEN) &&
                    getTokenStatus(je, TokenType.ACCESS_TOKEN).equals(TokenStatus.ACCESS_TOKEN_REGENERATION)) {

                Member member = memberRepository.findById(refreshTokenClaims.get("id", Long.class))
                        .orElseThrow(() -> new AccessDeniedException("Invalid Token"));

                return TokenAuthentication.builder()
                        .authentication(new UsernamePasswordAuthenticationToken(member, "", authorities))
                        .tokenStatus(TokenStatus.ACCESS_TOKEN_REGENERATION)
                        .build();
            }
            handleTokenStatus(getTokenStatus(je, e.getTokenType()));
        } catch (Exception e) {
            log.error("JWT Exception", e);
        }

        return null;
    }

    private Claims getTokenBodyClaims(String token, TokenType tokenType) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            if (tokenType.equals(TokenType.REFRESH_TOKEN)) {
                Date expiration = claims.getExpiration();
                long halfTimeInMs = securityProperties.getRefreshTokenValidationMillisecond() / 2;
                Date now = new Date();

                if (now.after(new Date(expiration.getTime() - halfTimeInMs))) {
                    throw new RefreshTokenHalfExpiredException(claims.get("id", Long.class));
                }
            }
            return claims;
        } catch (JwtException e) {
            throw new CustomJwtException(tokenType, e);
        }
    }

    private TokenStatus getTokenStatus(JwtException e, TokenType tokenType) {
        if (tokenType == TokenType.ACCESS_TOKEN && e instanceof ExpiredJwtException) {
            return TokenStatus.ACCESS_TOKEN_REGENERATION;
        }

        if (e instanceof ExpiredJwtException) {
            return TokenStatus.EXPIRED;
        } else if (e instanceof MalformedJwtException || e instanceof SignatureException) {
            // TODO: 위변조 Check 후 차단 로직 구현 and Log 생성
            return TokenStatus.FORGED;
        } else {
            // TODO: Token이 잘못 생성된 경우나 위변조의 경우가 있어 Log생성
            return TokenStatus.INVALID;
        }
    }

    private void handleTokenStatus(TokenStatus status) {
        switch (status) {
            case EXPIRED -> throw new AccessDeniedException("Expired Token");
            case FORGED -> throw new AccessDeniedException("Forged Token");
            case INVALID -> throw new AccessDeniedException("Invalid Token");
        }
    }
}
