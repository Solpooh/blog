package com.solpooh.boardback.repository.impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.solpooh.boardback.entity.QChannelEntity;
import com.solpooh.boardback.entity.QVideoEntity;
import com.solpooh.boardback.entity.VideoEntity;
import com.solpooh.boardback.repository.ifs.VideoRepositoryIf;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class VideoRepositoryImpl implements VideoRepositoryIf {
    private final JPAQueryFactory queryFactory;
    public VideoRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    QVideoEntity video = QVideoEntity.videoEntity;
    QChannelEntity channel = QChannelEntity.channelEntity;

    @Override
    public Page<VideoEntity> getLatestVideo(Pageable pageable, String lang) {

        List<VideoEntity> videoList = queryFactory
                .selectFrom(video)
                .join(video.channel, channel).fetchJoin()
                .where(
                        channel.lang.eq(lang)
                )
                .orderBy(video.publishedAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .select(video.count())
                .from(video)
                .fetchOne();


        return new PageImpl<>(videoList, pageable, total);
    }

    @Override
    public List<VideoEntity> getHotVideoList() {
        return queryFactory
                .selectFrom(video)
                .join(video.channel, channel).fetchJoin()
                .orderBy(video.trendScore.desc())
                .limit(16)
                .fetch();
    }

    @Override
    public List<VideoEntity> getTopViewVideoList() {
        return queryFactory
                .selectFrom(video)
                .join(video.channel, channel).fetchJoin()
                .orderBy(video.viewCount.desc())
                .limit(16)
                .fetch();
    }

    @Override
    public List<VideoEntity> getShortsVideoList() {
        return queryFactory
                .selectFrom(video)
                .join(video.channel, channel).fetchJoin()
                .where(video.isShort.isTrue())
                .orderBy(video.publishedAt.desc())
                .limit(16)
                .fetch();
    }

    @Override
    public Page<VideoEntity> getAllVideos(Pageable pageable) {
        List<VideoEntity> videoList = queryFactory
                .selectFrom(video)
                .join(video.channel, channel).fetchJoin()
                .orderBy(video.publishedAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long totalCount = queryFactory
                .select(video.count())
                .from(video)
                .fetchOne();

        return new PageImpl<>(videoList, pageable, totalCount != null ? totalCount : 0L);
    }

    @Override
    public Page<VideoEntity> searchVideosByChannelTitle(String channelTitle, Pageable pageable) {
        List<VideoEntity> videoList = queryFactory
                .selectFrom(video)
                .join(video.channel, channel).fetchJoin()
                .where(channel.title.containsIgnoreCase(channelTitle))
                .orderBy(video.publishedAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long totalCount = queryFactory
                .select(video.count())
                .from(video)
                .join(video.channel, channel)
                .where(channel.title.containsIgnoreCase(channelTitle))
                .fetchOne();

        return new PageImpl<>(videoList, pageable, totalCount != null ? totalCount : 0L);
    }
}
