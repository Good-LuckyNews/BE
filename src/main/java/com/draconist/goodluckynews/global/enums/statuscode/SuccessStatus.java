package com.draconist.goodluckynews.global.enums.statuscode;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum SuccessStatus implements BaseCode {

	// common
	_OK(HttpStatus.OK, "COMMON200", "성공입니다."),

	// 플레이스 
	_PLACE_CREATED(HttpStatus.CREATED, "COMMON201", "플레이스가 성공적으로 생성되었습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;

	@Override
	public String getCode() {
		return code;
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public HttpStatus getHttpStatus() {
		return httpStatus;
	}

	@Override
	public Integer getStatusValue() {
		return httpStatus.value();
	}
}
