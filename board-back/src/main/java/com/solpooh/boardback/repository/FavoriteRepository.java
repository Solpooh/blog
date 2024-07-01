package com.solpooh.boardback.repository;

import com.solpooh.boardback.entity.FavoriteEntity;
import com.solpooh.boardback.entity.primaryKey.FavoritePk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FavoriteRepository extends JpaRepository<FavoriteEntity, FavoritePk> {
    FavoriteEntity findByBoardNumberAndUserEmail(Integer boardNumber, String userEmail);
}
