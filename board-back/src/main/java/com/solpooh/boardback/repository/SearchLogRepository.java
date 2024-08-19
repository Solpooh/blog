package com.solpooh.boardback.repository;

import com.solpooh.boardback.entity.SearchLogEntity;
import com.solpooh.boardback.repository.resultSet.GetPopularListResultSet;
import com.solpooh.boardback.repository.resultSet.GetRelationListResultSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SearchLogRepository extends JpaRepository<SearchLogEntity, Integer> {
    @Query(value =
    "SELECT search_word as searchWord, count(search_word) AS count " +
    "FROM search_log " +
    "WHERE relation IS FALSE " +
    "GROUP BY search_word " +
    "ORDER BY count DESC " +
    "LIMIT 15 ",
    nativeQuery = true
    )
    List<GetPopularListResultSet> getPopularList();

    @Query(value =
    "SELECT relation_word as searchWord, count(relation_word) AS count " +
    "FROM search_log " +
    "WHERE search_word = ?1 " +
    "AND relation_word IS NOT NULL " +
    "GROUP BY relation_word " +
    "ORDER BY count DESC " +
    "LIMIT 15 ",
    nativeQuery = true
    )
    List<GetRelationListResultSet> getRelationList(String searchWord);

    void deleteByCreatedAtBefore(LocalDateTime dateTime);
}
