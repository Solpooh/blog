package com.solpooh.boardback;

import com.solpooh.boardback.entity.BoardEntity;
import com.solpooh.boardback.entity.UserEntity;
import com.solpooh.boardback.repository.BoardRepository;
import com.solpooh.boardback.repository.UserRepository;
import com.solpooh.boardback.service.BoardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.services.s3.S3AsyncClient;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SpringBootTest
@ActiveProfiles("test")
public class FavoriteConcurrencyTest {
    @Autowired
    private BoardService boardService;
    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private UserRepository userRepository;
    @MockBean
    private S3AsyncClient s3AsyncClient;
    @BeforeEach
    // given
    void setUp() {
        // 회원 10명 미리 저장
        List<UserEntity> users = IntStream.range(0, 10)
                .mapToObj(i -> UserEntity.builder()
                        .email("user" + i + "@test.com")
                        .password("password")
                        .nickname("닉네임" + i)
                        .build())
                .collect(Collectors.toList());

        userRepository.saveAll(users);

        // 테스트용 게시글 하나 생성
        BoardEntity board = BoardEntity.builder()
                .title("테스트 게시글")
                .content("내용")
                .category("카테고리")
                .writerEmail("hsw9420@naver.com")
                .build();

        boardRepository.save(board);
        System.out.println("테스트 사전 데이터 주입 끝!!");
    }

    @Test
    void testOptimisticLock() throws InterruptedException {
        Long boardNumber = 1L;
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final String email = "user" + i + "@test.com";

            executorService.submit(() -> {
                try {
                    boardService.putFavorite(boardNumber, email);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        BoardEntity board = boardRepository.findByBoardNumber(boardNumber).get();
        System.out.println("최종 좋아요 수 = " + board.getFavoriteCount());
    }
}
