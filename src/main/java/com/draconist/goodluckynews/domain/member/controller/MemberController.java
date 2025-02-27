package com.draconist.goodluckynews.domain.member.controller;

import com.draconist.goodluckynews.domain.member.dto.JoinDTO;
import com.draconist.goodluckynews.domain.member.dto.LoginRequestDTO;
import com.draconist.goodluckynews.domain.member.dto.MemberInfoDTO;
import com.draconist.goodluckynews.domain.member.service.MemberService;
import com.draconist.goodluckynews.global.jwt.dto.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    //회원 로그인

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO loginRequestDTO) {
        return memberService.login(loginRequestDTO);
    }

    //회원 가입

    @PostMapping("/join")
    public ResponseEntity<?> join(
            @RequestParam(value = "image", required = false) MultipartFile image,
            @Valid @ModelAttribute JoinDTO joinDTO) throws IOException {

        return memberService.join(image,joinDTO);
    }

    //회원 정보 조회
    @GetMapping("/info")
    public ResponseEntity<?> info(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return memberService.info(customUserDetails.getEmail());
    }

    //회원 정보 수정

    @PutMapping("/edit")
    public ResponseEntity<?> edit(@AuthenticationPrincipal CustomUserDetails customUserDetails,
       @RequestParam(value= "image", required = false)MultipartFile image
            , @Valid @ModelAttribute MemberInfoDTO memberInfoDTO
       ) throws IOException {
        return memberService.edit(image, memberInfoDTO, customUserDetails.getEmail());
    }
}
