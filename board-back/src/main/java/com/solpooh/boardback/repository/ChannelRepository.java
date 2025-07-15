package com.solpooh.boardback.repository;

import com.solpooh.boardback.entity.ChannelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChannelRepository extends JpaRepository<ChannelEntity, String> {
    ChannelEntity findByChannelId(String channelId);
}
