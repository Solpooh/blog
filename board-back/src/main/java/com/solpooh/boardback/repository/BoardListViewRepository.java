package com.solpooh.boardback.repository;

import com.solpooh.boardback.entity.BoardListViewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardListViewRepository extends JpaRepository<BoardListViewEntity, Integer> {
    // 전체 게시물 조회
    List<BoardListViewEntity> findByOrderByWriteDatetimeDesc();
    // 유형별 게시물 조회
    List<BoardListViewEntity> findByCategoryOrderByWriteDatetimeDesc(String category);
    List<BoardListViewEntity> findTop3ByWriteDatetimeGreaterThanOrderByFavoriteCountDescCommentCountDescViewCountDescWriteDatetimeDesc(String writeDatetime);
    List<BoardListViewEntity> findByTitleContainsOrContentContainsOrderByWriteDatetimeDesc(String title, String content);
    List<BoardListViewEntity> findByWriterEmailOrderByWriteDatetimeDesc(String writerEmail);
}
