package com.toss.tosspaybackend.domain.member.controller;

import com.toss.tosspaybackend.config.security.jwt.JwtToken;
import com.toss.tosspaybackend.domain.member.dto.*;
import com.toss.tosspaybackend.domain.member.enums.Role;
import com.toss.tosspaybackend.domain.member.service.MemberService;
import com.toss.tosspaybackend.domain.member.service.RoleService;
import com.toss.tosspaybackend.global.Response;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
public class MemberController {

    private final MemberService memberService;
    private final RoleService roleService;

    @PostMapping("/auth/register")
    public Response<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        return memberService.register(request);
    }

    @PostMapping("/auth")
    public Response<String> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        return memberService.login(request, httpRequest, httpResponse);
    }

    @PostMapping("/auth/existence-check")
    public Response<String> existenceCheck(@Valid @RequestBody ExistenceCheckRequest request, HttpServletResponse response) {
        return memberService.existenceCheck(request, response);
    }

    @PreAuthorize("hasAuthority(@roleService.getRoleUser())")
    @PostMapping("/password-check")
    public Response<String> passwordCheck(@Valid @RequestBody PasswordCheckRequest request) {
        return memberService.passwordCheck(request);
    }
}