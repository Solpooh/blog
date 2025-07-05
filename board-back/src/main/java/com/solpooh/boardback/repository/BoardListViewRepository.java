package com.solpooh.boardback.repository;

import com.solpooh.boardback.entity.BoardListViewEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardListViewRepository extends JpaRepository<BoardListViewEntity, Integer> {
    // 전체 게시물 조회
    Page<BoardListViewEntity> findByOrderByWriteDatetimeDesc(Pageable pageable);
    // 유형별 게시물 조회
    Page<BoardListViewEntity> findByCategoryOrderByWriteDatetimeDesc(String category, Pageable pageable);
    // TOP3 게시물 조회
    List<BoardListViewEntity> findTop3ByWriteDatetimeGreaterThanOrderByFavoriteCountDescCommentCountDescViewCountDescWriteDatetimeDesc(String writeDatetime);
    // 검색 게시물 조회
    List<BoardListViewEntity> findByTitleContainsOrContentContainsOrderByWriteDatetimeDesc(String title, String content);

    // 작성자 게시물 조회
    List<BoardListViewEntity> findByWriterEmailOrderByWriteDatetimeDesc(String writerEmail);
}
