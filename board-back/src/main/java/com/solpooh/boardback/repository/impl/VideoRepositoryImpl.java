package com.solpooh.boardback.repository.impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.solpooh.boardback.entity.QChannelEntity;
import com.solpooh.boardback.entity.QVideoEntity;
import com.solpooh.boardback.entity.VideoEntity;
import com.solpooh.boardback.repository.ifs.VideoRepositoryIf;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class VideoRepositoryImpl implements VideoRepositoryIf {
    private final JPAQueryFactory queryFactory;
    public VideoRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }
    @Override
    public List<VideoEntity> findVideosByCategoryAndLang(String category, String lang) {
        QVideoEntity video = QVideoEntity.videoEntity;
        QChannelEntity channel = QChannelEntity.channelEntity;

        return queryFactory
                .selectFrom(video)
                .join(video.channel, channel).fetchJoin()
                .where(
                        channel.category.eq(category),
                        channel.lang.eq(lang)
                )
                .orderBy(video.publishedAt.desc())
                .limit(40)
                .fetch();
    }
}
