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

	// ✅ 내가 만든 플레이스 조회 성공 응답 추가
	_PLACE_MYLIST_SUCCESS(HttpStatus.OK, "PLACE207", "내가 만든 플레이스 조회 성공"),


	// 플레이스 수정 성공 응답 추가
	_PLACE_UPDATED(HttpStatus.OK, "PLACE200", "플레이스 수정 성공"),

	// ✅ 북마크 추가/삭제(토글) 성공 응답 추가
	_BOOKMARK_UPDATED(HttpStatus.OK, "BOOKMARK200", "북마크 상태가 업데이트되었습니다."),

	// ✅ 게시글(희소식) 관련 응답 추가
	POST_CREATED(HttpStatus.CREATED, "POST201", "게시글이 성공적으로 생성되었습니다."),
	POST_DELETED(HttpStatus.OK, "POST202", "게시글이 성공적으로 삭제되었습니다."),
	POST_UPDATED(HttpStatus.OK, "POST203", "게시글이 성공적으로 수정되었습니다."),
	POST_LIKE_SUCCESS(HttpStatus.OK, "POST204", "게시글 좋아요 처리가 완료되었습니다."),
	POST_LIST_SUCCESS(HttpStatus.OK, "POST205", "게시글 목록이 성공적으로 조회되었습니다."),
	POST_DETAIL_SUCCESS(HttpStatus.OK, "POST206", "게시글 상세 정보가 성공적으로 조회되었습니다."),

	// ✅ 댓글 관련 응답 추가
	COMMENT_CREATED(HttpStatus.CREATED, "COMMENT201", "댓글이 성공적으로 생성되었습니다."),
	COMMENT_DELETED(HttpStatus.OK, "COMMENT202", "댓글이 성공적으로 삭제되었습니다."),
	COMMENT_UPDATED(HttpStatus.OK, "COMMENT203", "댓글이 성공적으로 수정되었습니다."),
	COMMENT_LIKE_SUCCESS(HttpStatus.OK, "COMMENT204", "댓글 좋아요 토글이 완료되었습니다."),
	COMMENT_LIKE_ADDED(HttpStatus.OK,"COMMENT204", "댓글 좋아요 완료"),
	COMMENT_LIKE_REMOVED(HttpStatus.OK,"COMMENT204", "댓글 좋아요 취소"),
	COMMENT_LIST_SUCCESS(HttpStatus.OK, "COMMENT205", "댓글 목록이 성공적으로 조회되었습니다."),
	COMMENT_DETAIL_SUCCESS(HttpStatus.OK, "COMMENT206", "댓글 상세 정보가 성공적으로 조회되었습니다."),
	COMMENT_REPLIES_FOUND(HttpStatus.OK, "COMMENT207", "대댓글 목록을 성공적으로 가져왔습니다."),
	NO_REPLIES_FOUND(HttpStatus.OK, "COMMENT208", "해당 댓글에 달린 댓글이 없습니다" ),
	COMMENT_REPLIES_CREATED(HttpStatus.CREATED, "COMMENT209", "대댓글이 성공적으로 생성되었습니다.");


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
