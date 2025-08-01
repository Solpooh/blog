-- 회원가입
INSERT INTO user VALUES ('email@email.com', 'P!ssw0rd', 'nickname', '01012345678', '부산광역시', '롯데백화점', null, 1);

-- 로그인
SELECT * FROM user WHERE email = 'email@email.com';

-- 게시물 작성
INSERT INTO board (title, content, category, write_datetime, favorite_count, comment_count, view_count, writer_email)
VALUES ('제목입니다', '내용입니다', 'java', '2024-06-10 13:08', 0, 0, 0, 'email@email.com');

INSERT INTO image VALUES (1, 'url',1, 0);

-- 댓글 작성
INSERT INTO comment (content, write_datetime, user_email, board_number)
VALUES ('반갑습니다', '2024-06-10 13:08', 'email@email.com', 1);
UPDATE board SET comment_count = comment_count + 1 WHERE board_number = 1;

-- 좋아요 누르기
INSERT INTO favorite VALUES ('email@email.com', 1);
UPDATE board SET favorite_count = favorite_count + 1 WHERE board_number = 1;

-- 좋아요 삭제
DELETE FROM favorite WHERE user_email = 'email@email.com' AND board_number = 1;
UPDATE board SET favorite_count = favorite_count - 1 WHERE board_number = 1;

-- 게시물 수정
UPDATE board SET title = '수정 제목입니다', content = '수정 내용입니다', category = 'spring' WHERE board_number = 1;
DELETE FROM image WHERE board_number = 1;
INSERT INTO image VALUES (1, 'url', 1, 0);

-- 댓글 수정
UPDATE comment SET content = '수정 내용입니다'
WHERE board_number = 1 AND comment_number = 1;

-- 댓글 삭제
DELETE FROM comment
WHERE board_number = 1 AND comment_number = 1;
UPDATE board SET comment_count = comment_count - 1 WHERE board_number = 1;

-- 게시물 삭제
DELETE FROM image WHERE board_number = 1;
DELETE FROM comment WHERE board_number = 1;
DELETE FROM favorite WHERE board_number = 1;
DELETE FROM board WHERE board_number = 1;

-- 상세 게시물 불러오기
SELECT
    B.board_number AS board_number,
    B.title AS title,
    B.content AS content,
    B.category AS category,
    B.write_datetime AS write_datetime,
    B.writer_email AS writer_email,
    U.nickname AS nickname,
    U.profile_image AS profile_image
FROM board AS B
         INNER JOIN user AS U
                    ON B.writer_email = U.email
WHERE board_number = 1;

SELECT image
FROM image
WHERE board_number = 1 AND is_deleted = false;

-- 좋아요 리스트 가져오기
SELECT
    U.email AS email,
    U.nickname AS nickname,
    U.profile_image
FROM favorite AS F
         INNER JOIN user AS U
                    ON F.user_email = U.email
WHERE F.board_number = 1;

-- 댓글 리스트 가져오기
SELECT
    C.comment_number AS commentNumber,
    U.nickname AS nickname,
    U.profile_image AS profile_image,
    C.write_datetime AS write_datetime,
    C.content AS content,
    C.user_email AS userEmail
FROM comment AS C
         INNER JOIN user AS U
                    ON C.user_email = U.email
WHERE C.board_number = 1
ORDER BY write_datetime DESC;
LIMIT 0, 5;

-- 최신 게시물 리스트 불러오기
SELECT *
FROM board_list_view
ORDER BY write_datetime DESC
LIMIT 0, 5;

-- 유형별 게시물 리스트 불러오기
SELECT *
FROM board_list_view
WHERE category = 'java'
ORDER BY write_datetime DESC
LIMIT 0, 5;

-- 검색 게시물 리스트 불러오기
SELECT *
FROM board_list_view
WHERE title LIKE '%수정%' OR content LIKE '%수정%'
ORDER BY write_datetime DESC
LIMIT 0, 5;

-- 주간 상위 3개 게시물
SELECT *
FROM board_list_view
WHERE write_datetime BETWEEN '2024-06-03 13:08' AND '2024-06-10 13:08'
ORDER BY favorite_count DESC, comment_count DESC, view_count DESC, write_datetime DESC
LIMIT 3;

-- 특정 유저 게시물 리스트 불러오기
SELECT *
FROM board_list_view
WHERE writer_email = 'email@email.com'
ORDER BY write_datetime DESC;
LIMIT 0, 5;

-- 인기 검색어 리스트
SELECT search_word, count(search_word) AS count
FROM search_log
WHERE relation IS FALSE
GROUP BY search_word
ORDER BY count DESC
LIMIT 15;

-- 관련 검색어 리스트
SELECT relation_word, count(relation_word) AS count
FROM search_log
WHERE search_word = '검색어'
GROUP BY relation_word
ORDER BY count DESC
LIMIT 15;

-- 유저 정보 불러오기 / 로그인 유저 정보 불러오기
SELECT *
FROM user
WHERE email = 'email@email.com';

-- 닉네임 수정
UPDATE user SET nickname = '수정 닉네임' WHERE email = 'email@email.com';

-- 프로필 이미지 수정
UPDATE user SET profile_image = 'url2' WHERE email = 'email@email.com';

-- 최신 비디오 리스트 불러오기
SELECT c.thumbnail, c.title, v.thumbnail, v.title, v.published_at
FROM channel c
INNER JOIN video v ON c.channel_id = v.channel_id
WHERE c.category = 'dev' and  c.lang = 'ko'
ORDER BY v.published_at DESC;

-- 최신 비디오 리스트 불러오기(With 페이징)
SELECT c.thumbnail, c.title, v.thumbnail, v.title, v.published_at
FROM channel c
INNER JOIN video v ON c.channel_id = v.channel_id
WHERE c.category = 'dev' and  c.lang = 'ko'
ORDER BY v.published_at DESC
LIMIT 0, 20;