package com.solpooh.boardback.repository;

import com.solpooh.boardback.entity.BoardEntity;
import com.solpooh.boardback.entity.CommentEntity;
import com.solpooh.boardback.repository.resultSet.GetCommentListResultSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Integer> {
    CommentEntity findByCommentNumber(Integer commentNumber);
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
            "ORDER BY C.comment_number DESC",
            nativeQuery = true
    )
    List<GetCommentListResultSet> getCommentList(Integer boardNumber);
    @Transactional
    void deleteByBoardNumber(Integer boardNumber);

    // 댓글 삭제
    @Transactional
    void deleteByBoardNumberAndCommentNumber(Integer boardNumber, Integer commentNumber);
}
