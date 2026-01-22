package com.solpooh.boardback.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ResponseApi implements ResponseApiInterface {
    // HTTP Status 200
    SUCCESS("SU", "Success", HttpStatus.OK),

    // HTTP Status 400
    VALIDATION_FAILED("VF", "검증에 실패했습니다. 요청 값을 확인하세요", HttpStatus.BAD_REQUEST),
    DUPLICATE_EMAIL("DE", "중복된 이메일입니다.", HttpStatus.BAD_REQUEST),
    DUPLICATE_NICKNAME("DN", "중복된 닉네임입니다.", HttpStatus.BAD_REQUEST),
    DUPLICATE_TEL_NUMBER("DT", "중복된 전화번호입니다.", HttpStatus.BAD_REQUEST),
    NOT_EXISTED_USER("NU", "존재하지 않은 유저입니다.", HttpStatus.BAD_REQUEST),
    NOT_EXISTED_BOARD("NB", "존재하지 않은 게시물입니다.", HttpStatus.BAD_REQUEST),
    NOT_EXISTED_COMMENT("NC", "존재하지 않은 댓글입니다.", HttpStatus.BAD_REQUEST),
    NOT_EXISTED_CHANNEL("NEC", "존재하지 않은 채널입니다.", HttpStatus.BAD_REQUEST),
    NOT_EXISTED_VIDEO("NV", "존재하지 않은 비디오입니다.", HttpStatus.BAD_REQUEST),

    // Transcript 관련
    TRANSCRIPT_PROCESSING("TP", "자막이 현재 처리 중입니다. 잠시 후 다시 시도해주세요.", HttpStatus.CONFLICT),
    TRANSCRIPT_FAILED("TF", "자막 처리에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

    // HTTP Status 401
    SIGN_IN_FAIL("SF", "로그인 정보가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED),

    // HTTP Status 403
    NO_PERMISSION("NP", "권한이 없습니다.", HttpStatus.FORBIDDEN),

    // HTTP Status 500
    DATABASE_ERROR("DBE", "데이터베이스/서버 오류입니다.", HttpStatus.INTERNAL_SERVER_ERROR);
    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
