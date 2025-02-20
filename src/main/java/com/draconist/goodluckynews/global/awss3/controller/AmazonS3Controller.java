package com.draconist.goodluckynews.global.awss3.controller;

import com.draconist.goodluckynews.global.awss3.service.AwsS3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/file")
public class AmazonS3Controller {

    private final AwsS3Service awsS3Service;

    // 파일 업로드
    @PostMapping
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile multipartFile) {
        String fileUrl = awsS3Service.uploadFile(multipartFile);
        return ResponseEntity.ok(fileUrl);
    }

    // 파일 삭제
    @DeleteMapping
    public ResponseEntity<String> deleteFile(@RequestParam String fileName) {
        awsS3Service.deleteFile(fileName);
        return ResponseEntity.ok(fileName);
    }

    // 파일 URL 조회
    @GetMapping("/url")
    public ResponseEntity<String> getFileUrl(@RequestParam String fileName) {
        String fileUrl = awsS3Service.getFileUrl(fileName);
        return ResponseEntity.ok(fileUrl);
    }
}
