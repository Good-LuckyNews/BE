package com.draconist.goodluckynews.global.exception;

import com.draconist.goodluckynews.global.enums.statuscode.ErrorStatus;
import com.draconist.goodluckynews.global.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ExceptionAdvice {

    // 요청 파라미터 유효성 검사 실패 시 호출
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationExceptions(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new HashMap<>();
        exception.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        ApiResponse<Object> response = ApiResponse
                .onFailure(ErrorStatus._BAD_REQUEST.getCode(), "유효성 검사 실패", errors);
        log.error("Validation error: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // Custom Error Handler
    // GeneralException 및 하위 클래스에 대한 예외 처리
    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<ApiResponse<?>> handleGeneralException(GeneralException exception) {
        ApiResponse<Object> response = ApiResponse
                .onFailure(exception.getErrorCode(), exception.getErrorReason(), null);
        log.error("General exception: {}", exception.getErrorReason());
        return new ResponseEntity<>(response, exception.getHttpStatus());
    }

    //잘못된 타입의 파라미터가 들어왔을 때 처리
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<?>> handleTypeMismatchException(MethodArgumentTypeMismatchException exception) {
        String name = exception.getName(); // 예: "placeId"
        String type = exception.getRequiredType() != null ? exception.getRequiredType().getSimpleName() : "알 수 없음";

        String message = String.format("%s 형식이 올바르지 않습니다. %s 타입이어야 합니다.", name, type);

        log.error("Type mismatch error: {}, parameter: {}, required type: {}", exception.getMessage(), name, type);

        ApiResponse<Object> response = ApiResponse.onFailure(
                "COMMON4001",
                message,
                null
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // 그 외의 모든 예외의 경우
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleException(Exception exception) {
        ApiResponse<Object> response = ApiResponse.onFailure(
                ErrorStatus._INTERNAL_SERVER_ERROR.getCode(),
                "오류가 발생하였습니다. " +
                        "1. 토큰을 삽입했는지 확인 해주세요. " +
                        "2. 토큰의 유효기간을 확인 해주세요.(새로 발급하여 시도해보세요.)" +
                        "문제가 해결되지 않는다면, 관리자에게 문의 해주세요.",
                exception.getMessage());
        log.error("Unhandled exception: {}", exception.getMessage(), exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
