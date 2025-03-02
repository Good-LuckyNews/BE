package com.draconist.goodluckynews.global.enums.statuscode;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum SuccessStatus implements BaseCode {

	// 공통
	_OK(HttpStatus.OK, "COMMON200", "성공입니다."),

	// 플레이스 관련
	_PLACE_CREATED(HttpStatus.CREATED, "COMMON201", "플레이스가 성공적으로 생성되었습니다."),
	_PLACE_DELETED(HttpStatus.OK, "COMMON202", "플레이스가 성공적으로 삭제되었습니다."),

	// 페이지네이션 관련
	_PLACE_PAGINATION_SUCCESS(HttpStatus.OK, "PAGE200", "플레이스 목록이 성공적으로 조회되었습니다."),

	// 특정 플레이스 조회 성공 응답 추가
	_PLACE_DETAIL_SUCCESS(HttpStatus.OK, "PLACE200", "플레이스 상세 정보 조회 성공"),

	// 플레이스 수정 성공 응답 추가
	_PLACE_UPDATED(HttpStatus.OK, "PLACE200", "플레이스 수정 성공");


	private final HttpStatus httpStatus;
	private final String code;
	private final String message;

	@Override
	public String getCode() { return code; }

	@Override
	public String getMessage() { return message; }

	@Override
	public HttpStatus getHttpStatus() { return httpStatus; }

	@Override
	public Integer getStatusValue() { return httpStatus.value(); }
}
