package com.draconist.goodluckynews.global.enums.statuscode;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum ErrorStatus implements BaseCode {

	// 공통 오류
	_INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러, 관리자에게 문의 바랍니다."),
	_BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON400", "잘못된 요청입니다."),
	_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON401", "인증이 필요합니다."),
	_FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "금지된 요청입니다."),

	// Member Error
	MEMBER_NOT_FOUND(HttpStatus.BAD_REQUEST, "MEMBER4001", "해당하는 사용자를 찾을 수 없습니다."),
	PASSWORD_NOT_CORRECT(HttpStatus.FORBIDDEN, "MEMBER4002", "비밀번호가 일치하지 않습니다."),
	_MEMBER_IS_EXISTS(HttpStatus.FORBIDDEN, "MEMBER4003", "해당하는 사용자가 이미 존재합니다."),

	// Resource Error
	RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "RESOURCE4001", "잘못된 api 요청입니다. " + "요청 형식을 다시 확인해주세요." +
			"반복적인 오류 발생시 관리자에게 문의해주세요."),

	// 로그인 실패 사유
	INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH4001", "아이디 또는 비밀번호가 잘못되었습니다."),

	//s3 에러
	_S3_REMOVE_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "S35004", "S3 파일 삭제 중 오류가 발생하였습니다."),
	// JWT Error
	TOKEN_ERROR(HttpStatus.UNAUTHORIZED, "TOKEN4001", "토큰이 없거나 만료 되었습니다."),
	TOKEN_NO_AUTHORIZATION(HttpStatus.UNAUTHORIZED, "TOKEN4002", "토큰에 권한이 없습니다."),

	//Place(커뮤니티) 에러
	_PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "PLACE4001", "삭제되었거나 존재하지 않는 커뮤니티입니다."),
	_DUPLICATE_PLACE_NAME(HttpStatus.FORBIDDEN, "PLACE4002", "해당하는 이름의 플레이스가 이미 존재합니다. 다른 이름으로 작성해주세요."),

	// Article Error
	_ARTICLE_TITLE_MISSING(HttpStatus.BAD_REQUEST, "ARTICLE4001", "제목을 입력해 주세요."),
	_ARTICLE_CONTENT_MISSING(HttpStatus.BAD_REQUEST, "ARTICLE4002", "내용을 입력해 주세요."),
	_NOT_OWNER_OF_ARTICLE(HttpStatus.FORBIDDEN, "ARTICLE4031", "해당 추억(게시글)의 주인이 아닙니다."),
	_ARTICLE_NOT_FOUND(HttpStatus.NOT_FOUND, "ARTICLE4041", "해당 추억을 찾을 수 없습니다."),

	_CRAWLFAILED(HttpStatus.BAD_REQUEST, "MEMBER4001", "크롤링이 불가능");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;

	// implement of BaseCode
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
