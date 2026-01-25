CREATE TABLE `user`
(
    `email`          VARCHAR(50)  NOT NULL COMMENT '사용자 이메일',
    `password`       VARCHAR(100) NOT NULL COMMENT '사용자 비밀번호',
    `nickname`       VARCHAR(20)  NOT NULL UNIQUE COMMENT '사용자 닉네임',
    `tel_number`     VARCHAR(15)  NOT NULL UNIQUE COMMENT '사용자 휴대폰 번호',
    `address`        TEXT         NOT NULL COMMENT '사용자 주소',
    `address_detail` TEXT         NULL COMMENT '사용자 상세 주소',
    `profile_image`  TEXT         NULL COMMENT '사용자 프로필 사진'
);

CREATE TABLE `board`
(
    `board_number`   INT         NOT NULL AUTO_INCREMENT COMMENT '게시물 번호' PRIMARY KEY,
    `title`          TEXT        NOT NULL COMMENT '게시물 제목',
    `content`        TEXT        NOT NULL COMMENT '게시물 내용',
    `category`       TEXT        NOT NULL COMMENT '게시물 유형',
    `write_datetime` DATETIME    NOT NULL COMMENT '게시물 작성 날짜',
    `favorite_count` INT         NOT NULL DEFAULT 0 COMMENT '게시물 좋아요 수',
    `comment_count`  INT         NOT NULL DEFAULT 0 COMMENT '게시물 댓글 수',
    `view_count`     INT         NOT NULL DEFAULT 0 COMMENT '게시물 조회 수',
    `writer_email`   VARCHAR(50) NOT NULL COMMENT '게시물 작성자 이메일'
);

CREATE TABLE `image`
(
    `board_number` INT  NOT NULL COMMENT '게시물 번호',
    `image`        TEXT NOT NULL COMMENT '게시물 이미지 URL'
);

CREATE TABLE `favorite`
(
    `user_email`   VARCHAR(50) NOT NULL COMMENT '사용자 이메일',
    `board_number` INT         NOT NULL COMMENT '게시물 번호'
);

CREATE TABLE `comment`
(
    `comment_number` INT         NOT NULL COMMENT '댓글 번호',
    `user_email`     VARCHAR(50) NOT NULL COMMENT '사용자 이메일',
    `board_number`   INT         NOT NULL COMMENT '게시물 번호',
    `content`        TEXT        NOT NULL COMMENT '댓글 내용',
    `write_datetime` DATETIME    NOT NULL COMMENT '작성 날짜 및 시간'
);

CREATE TABLE `search_log`
(
    `sequence`      INT     NOT NULL AUTO_INCREMENT COMMENT '시퀀스' PRIMARY KEY,
    `search_word`   TEXT    NOT NULL COMMENT '검색어',
    `relation_word` TEXT    NULL COMMENT '관련 검색어',
    `relation`      BOOLEAN NOT NULL COMMENT '관련 검색어 여부'
);

ALTER TABLE `user`
    ADD CONSTRAINT `PK_USER` PRIMARY KEY (
                                          `email`
        );

ALTER TABLE `board`
    ADD CONSTRAINT `PK_BOARD` PRIMARY KEY (
                                           `board_number`
        );

ALTER TABLE `favorite`
    ADD CONSTRAINT `PK_FAVORITE` PRIMARY KEY (
                                              `user_email`,
                                              `board_number`
        );

ALTER TABLE `comment`
    ADD CONSTRAINT `PK_COMMENT` PRIMARY KEY (
                                             `comment_number`
        );

ALTER TABLE `search_log`
    ADD CONSTRAINT `PK_SEARCH_LOG` PRIMARY KEY (
                                                `sequence`
        );

ALTER TABLE `board`
    ADD CONSTRAINT `FK_user_TO_board_1` FOREIGN KEY (
                                                     `writer_email`
        )
        REFERENCES `user` (
                           `email`
            );

ALTER TABLE `image`
    ADD CONSTRAINT `FK_board_TO_image_1` FOREIGN KEY (
                                                      `board_number`
        )
        REFERENCES `board` (
                            `board_number`
            );

ALTER TABLE `favorite`
    ADD CONSTRAINT `FK_user_TO_favorite_1` FOREIGN KEY (
                                                        `user_email`
        )
        REFERENCES `user` (
                           `email`
            );

ALTER TABLE `favorite`
    ADD CONSTRAINT `FK_board_TO_favorite_1` FOREIGN KEY (
                                                         `board_number`
        )
        REFERENCES `board` (
                            `board_number`
            );

ALTER TABLE `comment`
    ADD CONSTRAINT `FK_user_TO_comment_1` FOREIGN KEY (
                                                       `user_email`
        )
        REFERENCES `user` (
                           `email`
            );

ALTER TABLE `comment`
    ADD CONSTRAINT `FK_board_TO_comment_1` FOREIGN KEY (
                                                        `board_number`
        )
        REFERENCES `board` (
                            `board_number`
            );

CREATE USER 'developer'@'%' IDENTIFIED BY 'P!ssw0rd';
GRANT SELECT, UPDATE, DELETE, INSERT
    ON devhub.*
    TO 'developer'@'%';

CREATE VIEW board_list_view AS
SELECT B.board_number   AS board_number,
       B.title          AS title,
       B.content        AS content,
       B.category       AS category,
       I.image          AS title_image,
       B.favorite_count AS favorite_count,
       B.comment_count  AS comment_count,
       B.view_count     AS view_count,
       B.write_datetime AS write_datetime,
       B.writer_email   AS writer_email,
       U.nickname       AS writer_nickname,
       U.profile_image  AS writer_profile_image
FROM board AS B
         INNER JOIN user AS U
                    ON B.writer_email = U.email
         LEFT JOIN (SELECT board_number, ANY_VALUE(image) AS image
                    FROM image
                    WHERE is_deleted = 0
                    GROUP BY board_number) AS I
                   ON B.board_number = I.board_number;

-- transcript 테이블 생성
CREATE TABLE IF NOT EXISTS `transcript`
(
    `video_id`              VARCHAR(20) NOT NULL COMMENT 'YouTube 영상 ID (PK, 동시성 제어 키)',
    `summarized_transcript` TEXT        NULL COMMENT 'AI 요약된 자막 (사용자에게 제공되는 최종 데이터)',
    `status`                VARCHAR(20) NOT NULL COMMENT '처리 상태 (PROCESSING, COMPLETED, FAILED)',
    `error_message`         TEXT        NULL COMMENT '실패 시 에러 메시지',
    `started_at`            DATETIME    NULL COMMENT '처리 시작 시간',
    `completed_at`          DATETIME    NULL COMMENT '처리 완료 시간',
    `retry_count`           INT         NOT NULL DEFAULT 0 COMMENT '재시도 횟수',
    `created_at`            DATETIME    NOT NULL COMMENT '레코드 생성 시간',
    `updated_at`            DATETIME    NOT NULL COMMENT '레코드 수정 시간',

    PRIMARY KEY (`video_id`),

    -- video 테이블과 FK 관계 (video 삭제 시 transcript도 삭제)
    CONSTRAINT `fk_transcript_video`
        FOREIGN KEY (`video_id`)
            REFERENCES `video` (`video_id`)
            ON DELETE CASCADE,

    -- 성능 최적화 인덱스
    INDEX `idx_status` (`status`),
    INDEX `idx_completed_at` (`completed_at`)

) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT ='영상 자막 처리 테이블 (동시성 제어 및 AI 요약 저장)';

-- batch_history 테이블 생성
CREATE TABLE IF NOT EXISTS `batch_history`
(
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '배치 이력 ID',
    `job_name`        VARCHAR(50)  NOT NULL COMMENT '배치 작업명 (VIDEO_COLLECT, VIDEO_DATA_UPDATE, VIDEO_SCORE_UPDATE, IMAGE_CLEANUP)',
    `status`          VARCHAR(20)  NOT NULL COMMENT '실행 상태 (RUNNING, SUCCESS, FAILED)',
    `started_at`      DATETIME     NOT NULL COMMENT '실행 시작 시간',
    `finished_at`     DATETIME     NULL COMMENT '실행 종료 시간',
    `processed_count` INT          NULL COMMENT '처리된 건수',
    `duration_ms`     BIGINT       NULL COMMENT '실행 소요 시간 (밀리초)',
    `error_message`   VARCHAR(500) NULL COMMENT '실패 시 에러 메시지',

    PRIMARY KEY (`id`),

    INDEX `idx_job_started` (`job_name`, `started_at` DESC),
    INDEX `idx_status` (`status`)

) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT ='배치 작업 실행 이력 테이블';