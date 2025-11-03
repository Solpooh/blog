package com.solpooh.boardback.repository;

import com.solpooh.boardback.entity.ChannelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChannelRepository extends JpaRepository<ChannelEntity, String> {
    Optional<ChannelEntity> findByChannelId(String channelId);
}
