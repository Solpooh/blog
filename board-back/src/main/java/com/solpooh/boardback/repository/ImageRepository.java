package com.solpooh.boardback.repository;

import com.solpooh.boardback.entity.ImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<ImageEntity, Integer> {
    List<ImageEntity> findByBoardNumberAndDeleted(Integer boardNumber, boolean is_deleted);
//    @Transactional
//    void deleteByBoardNumber(Integer boardNumber);

    @Modifying
    @Transactional
    @Query(value =
            "UPDATE image " +
            "SET is_deleted = true " +
            "WHERE board_number = ?1",
            nativeQuery = true
    )
    void imageToDelete(Integer boardNumber);

    @Query(value =
            "SELECT * " +
            "FROM image " +
            "WHERE is_deleted = true",
            nativeQuery = true
    )
    List<ImageEntity> getDeleteImage();
}
