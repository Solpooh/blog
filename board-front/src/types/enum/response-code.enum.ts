enum ResponseCode {
    // HTTP Status 200
    SUCCESS = "SU",

    // HTTP Status 400
    VALIDATION_FAILED = "VF",
    DUPLICATE_EMAIL = "DE",
    DUPLICATE_NICKNAME = "DN",
    DUPLICATE_TEL_NUMBER = "DT",
    NOT_EXISTED_USER = "NU",
    NOT_EXISTED_BOARD = "NB",
    NOT_EXISTED_COMMENT = "NC",
    NOT_EXISTED_CHANNEL = "NEC",
    NOT_EXISTED_VIDEO = "NV",

    // HTTP Status 401
    SIGN_IN_FAIL = "SF",
    AUTHORIZATION_FAIL = "AF",

    // HTTP Status 403
    NO_PERMISSION = "NP",

    // Transcript 관련
    TRANSCRIPT_PROCESSING = "TP",      // HTTP 202 - 처리 중
    TRANSCRIPT_FAILED = "TF",          // HTTP 503 - 일시적 실패
    TRANSCRIPT_UNAVAILABLE = "TU",     // HTTP 404 - 자막 없음
    TRANSCRIPT_RETRY_EXHAUSTED = "TR", // HTTP 410 - 재시도 초과

    // HTTP Status 500
    DATABASE_ERROR = "DBE",
}
export default ResponseCode;