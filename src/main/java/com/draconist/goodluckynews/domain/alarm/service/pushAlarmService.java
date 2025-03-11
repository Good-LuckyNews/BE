package com.draconist.goodluckynews.domain.alarm.service;

import com.draconist.goodluckynews.domain.alarm.dto.PushAlarmDTO;
import com.draconist.goodluckynews.domain.alarm.entity.PushAlarm;
import com.draconist.goodluckynews.domain.alarm.repository.PushAlarmRepository;
import com.draconist.goodluckynews.global.firebase.FCMService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PushAlarmService {
    private final PushAlarmRepository pushAlarmRepository;
    private final FCMService fcmService;

    // 📌 1. 사용자의 모든 알람 조회
    public ResponseEntity<?> getUserPushAlarms(Long userId) {
        List<PushAlarmDTO> alarms = pushAlarmRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(alarm -> PushAlarmDTO.builder()
                        .pushAlarmId(alarm.getId())
                        .userId(alarm.getUserId())
                        .notificationType(alarm.getNotificationType())
                        .content(alarm.getContent())
                        .url(alarm.getUrl())
                        .isRead(alarm.getIsRead())
                        .createdAt(alarm.getCreatedAt())
                        .scheduledTime(alarm.getScheduledTime())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(alarms);
    }

    // 📌 2. 특정 알람 읽음 처리
    public ResponseEntity<?> markAlarmAsRead(Long alarmId, Long userId) {
        PushAlarm alarm = pushAlarmRepository.findById(alarmId)
                .orElseThrow(() -> new RuntimeException("알람을 찾을 수 없습니다."));

        if (!alarm.getUserId().equals(userId)) {
            return ResponseEntity.status(403).body("권한이 없습니다.");
        }

        alarm.setIsRead(true);
        pushAlarmRepository.save(alarm);
        return ResponseEntity.ok("알람이 읽음 처리되었습니다.");
    }

    // 📌 3. 새 알람 생성 및 FCM 푸시 전송
    public ResponseEntity<?> createPushAlarm(Long userId, PushAlarmRequest request) {
        PushAlarm alarm = PushAlarm.builder()
                .userId(userId)
                .notificationType(request.getNotificationType())
                .content(request.getContent())
                .url(request.getUrl())
                .isRead(false)
                .build();
        pushAlarmRepository.save(alarm);

        // FCM 푸시 알람 전송
        String fcmToken = "사용자의_FCM_토큰"; // DB에서 조회 필요
        fcmService.sendPushNotification(fcmToken, "새로운 알람", request.getContent());

        return ResponseEntity.ok("알람이 생성되었습니다.");
    }

    // 📌 4. 특정 알람 삭제
    public ResponseEntity<?> deleteAlarm(Long alarmId, Long userId) {
        PushAlarm alarm = pushAlarmRepository.findById(alarmId)
                .orElseThrow(() -> new RuntimeException("알람을 찾을 수 없습니다."));

        if (!alarm.getUserId().equals(userId)) {
            return ResponseEntity.status(403).body("권한이 없습니다.");
        }

        pushAlarmRepository.delete(alarm);
        return ResponseEntity.ok("알람이 삭제되었습니다.");
    }

    // 📌 5. 사용자의 모든 알람 삭제
    public ResponseEntity<?> deleteAllAlarms(Long userId) {
        pushAlarmRepository.deleteByUserId(userId);
        return ResponseEntity.ok("모든 알람이 삭제되었습니다.");
    }
}
