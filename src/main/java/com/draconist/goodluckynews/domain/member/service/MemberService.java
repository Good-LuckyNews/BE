package com.draconist.goodluckynews.domain.member.service;


import com.draconist.goodluckynews.domain.member.converter.MemberConverter;
import com.draconist.goodluckynews.domain.member.dto.JoinDTO;
import com.draconist.goodluckynews.domain.member.dto.LoginRequestDTO;
import com.draconist.goodluckynews.domain.member.dto.MemberInfoDTO;
import com.draconist.goodluckynews.domain.member.entity.Member;
import com.draconist.goodluckynews.domain.member.repository.MemberRepository;
import com.draconist.goodluckynews.global.awss3.service.AwsS3Service;
import com.draconist.goodluckynews.global.enums.statuscode.ErrorStatus;
import com.draconist.goodluckynews.global.exception.GeneralException;
import com.draconist.goodluckynews.global.jwt.util.JwtUtil;
import com.draconist.goodluckynews.global.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AwsS3Service awsS3Service;
    private final MemberConverter memberConverter;

    // 로그인
    @Transactional(readOnly = true)
    public ResponseEntity<?> login(LoginRequestDTO dto) {
        String email = dto.getEmail();
        String password = dto.getPassword();
        Member member = memberRepository.findMemberByEmail(email)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        // 비밀번호 검증
        if(!passwordEncoder.matches(password, member.getPassword())) {
            throw new GeneralException(ErrorStatus.PASSWORD_NOT_CORRECT);
        }

        return getJwtResponseEntity(member);
    }

    @Transactional
    public ResponseEntity<?> join(MultipartFile image, JoinDTO joinDTO) {

        // 동일 username 사용자 생성 방지
        if (memberRepository.existsMemberByEmail(joinDTO.getEmail())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.onFailure(ErrorStatus._MEMBER_IS_EXISTS, "회원가입에 실패하였습니다."));
        }

        // `MemberConverter`를 사용하여 `Member` 객체 생성
        Member member = memberConverter.toMember(joinDTO);

        memberRepository.save(member);

        // 이미지가 존재하는 경우에만 이미지 업로드 및 설정
        if(image!=null && !image.isEmpty()){
            member.changeProfileImage(awsS3Service.uploadFile(image));
        }

        return getJwtResponseEntity(member);
    }

    // 회원 가입 & 로그인 성공시 JWT 생성 후 반환
    public ResponseEntity<?> getJwtResponseEntity(Member member) {
        String accessToken = jwtUtil.createJwt(member.getEmail(), member.getRole());
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        return ResponseEntity.ok().headers(headers)
                .body(ApiResponse.onSuccess("Bearer " + accessToken));
    }
    //회원 정보 조회
    @Transactional(readOnly = true)
    public ResponseEntity<?> info(String email) {
        Member member= memberRepository.findMemberByEmail(email).orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
        return ResponseEntity.ok().body(ApiResponse.onSuccess(memberConverter.toMemberInfo(member)));
    }

    @Transactional
    public ResponseEntity<?> edit(MultipartFile image, @Valid MemberInfoDTO memberInfoDTO, String email) throws IOException {
        //1. 회원이 존재하는 지 확인
        Member member = memberRepository.findMemberByEmail(email).orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
        //2. 이미지 수정 시도시, aws에서 이미지 삭제
        awsS3Service.deleteFileByUrl(member.getProfileImage());
        //3. 회원정보 수정
        member.changeUserInfo(memberInfoDTO);
        //4. 이미지 수정 시도시, 데베에 이미지 삭제
        if(image!=null && !image.isEmpty()){
            member.changeProfileImage(awsS3Service.uploadFile(image));
        }else{
            member.changeProfileImage(null);
        }
        return ResponseEntity.ok(ApiResponse.onSuccess("회원정보가 수정되었습니다."));
    }
}
