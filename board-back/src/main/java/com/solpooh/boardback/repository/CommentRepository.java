package com.solpooh.boardback.repository;

import com.solpooh.boardback.entity.CommentEntity;
import com.solpooh.boardback.repository.resultSet.GetCommentListResultSet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Integer> {
    Optional<CommentEntity> findByCommentNumber(Long commentNumber);

    boolean existsByCommentNumber(Long commentNumber);
    @Query(value =
            "SELECT " +
            "C.comment_number AS commentNumber, " +
            "U.nickname AS nickname, " +
            "U.profile_image AS profileImage, " +
            "C.write_datetime AS writeDatetime, " +
            "C.content AS content, " +
            "C.user_email AS userEmail " +
            "FROM comment AS C " +
            "INNER JOIN user AS U " +
            "ON C.user_email = U.email " +
            "WHERE C.board_number = ?1 " +
            "ORDER BY C.comment_number ",
            nativeQuery = true
    )
    Page<GetCommentListResultSet> getCommentList(Long boardNumber, Pageable pageable);
    @Transactional
    void deleteByBoardNumber(Long boardNumber);
    // 댓글 삭제
    @Transactional
    void deleteByBoardNumberAndCommentNumber(Long boardNumber, Long commentNumber);
}
