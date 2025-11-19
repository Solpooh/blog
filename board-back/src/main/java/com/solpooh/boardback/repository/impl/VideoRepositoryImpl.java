package com.solpooh.boardback.repository.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.solpooh.boardback.entity.QChannelEntity;
import com.solpooh.boardback.entity.QVideoEntity;
import com.solpooh.boardback.entity.VideoEntity;
import com.solpooh.boardback.repository.ifs.VideoRepositoryIf;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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
    public Page<VideoEntity> getLatestVideo(Pageable pageable, String category, String lang) {

        List<VideoEntity> videoList = queryFactory
                .selectFrom(video)
                .join(video.channel, channel).fetchJoin()
                .where(
                        channel.category.eq(category),
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
    public Page<VideoEntity> getSearchVideoList(String searchWord, String type, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();
        switch (type.toLowerCase()) {
            case "channel" -> builder.and(channel.title.containsIgnoreCase(searchWord));
            case "title" -> builder.and(video.title.containsIgnoreCase(searchWord));
            default -> throw new IllegalArgumentException("지원하지 않는 검색 타입입니다.");
        }

        List<VideoEntity> videoList = queryFactory
                .selectFrom(video)
                .join(video.channel, channel).fetchJoin()
                .where(builder)
                .orderBy(video.publishedAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .select(video.count())
                .from(video)
                .join(video.channel, channel)
                .where(builder)
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
}
