package com.solpooh.boardback.service.implement;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.solpooh.boardback.cache.CacheService;
import com.solpooh.boardback.common.ResponseApi;
import com.solpooh.boardback.converter.YoutubeConverter;
import com.solpooh.boardback.dto.common.ChannelResponse;
import com.solpooh.boardback.dto.request.channel.PostChannelRequest;
import com.solpooh.boardback.dto.response.youtube.DeleteChannelResponse;
import com.solpooh.boardback.dto.response.youtube.GetChannelListResponse;
import com.solpooh.boardback.dto.response.youtube.PostChannelResponse;
import com.solpooh.boardback.elasticsearch.VideoIndexService;
import com.solpooh.boardback.entity.ChannelEntity;
import com.solpooh.boardback.exception.CustomException;
import com.solpooh.boardback.provider.ChannelProvider;
import com.solpooh.boardback.repository.ChannelRepository;
import com.solpooh.boardback.repository.VideoRepository;
import com.solpooh.boardback.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChannelServiceImplement implements ChannelService {
    @Value("${youtube.api.key}")
    private String apiKey;
    private final YouTube youtube;
    private final ChannelRepository channelRepository;
    private final VideoRepository videoRepository;
    private final CacheService cacheService;
    private final VideoIndexService videoIndexService;

    private Optional<Channel> fetchChannelFromYoutube(String channelId) {
        try {

            YouTube.Channels.List request = youtube.channels()
                    .list("snippet")
                    .setId(channelId)
                    .setKey(apiKey);

            ChannelListResponse response = request.execute();

            if (response.getItems().isEmpty()) return Optional.empty();
            return Optional.of(response.getItems().get(0));

        } catch (IOException e) {
            return Optional.empty();
        }
    }

    // ===== Admin 채널 관리 =====

    @Override
    public GetChannelListResponse getChannelList() {
        List<ChannelEntity> channelEntities = channelRepository.findAll();

        List<ChannelResponse> channelList = channelEntities.stream()
                .map(YoutubeConverter::toResponse)
                .toList();

        return new GetChannelListResponse(channelList);
    }

    @Override
    public PostChannelResponse addChannel(PostChannelRequest request) {
        // channelId 필수
        if (request.channelId() == null || request.channelId().isBlank()) {
            throw new CustomException(ResponseApi.VALIDATION_FAILED);
        }

        // 이미 존재하는 채널인지 확인
        if (channelRepository.existsById(request.channelId())) {
            throw new CustomException(ResponseApi.VALIDATION_FAILED);
        }

        Channel channel = fetchChannelFromYoutube(request.channelId())
                .orElseThrow(() -> new CustomException(ResponseApi.NOT_EXISTED_CHANNEL));

        ChannelEntity entity = YoutubeConverter.toChannelEntity(channel);
        channelRepository.save(entity);

        return new PostChannelResponse();
    }

    @Override
    @Transactional
    public DeleteChannelResponse deleteChannel(String channelId) {
        // 채널 존재 여부 확인
        if (!channelRepository.existsById(channelId)) {
            throw new CustomException(ResponseApi.NOT_EXISTED_CHANNEL);
        }

        // 채널에 속한 비디오 ID 목록 조회 (Cache/ES 삭제용)
        List<String> videoIds = videoRepository.findVideoIdsByChannelId(channelId);
        int deletedVideoCount = videoIds.size();

        // 비디오 DB 삭제
        videoRepository.deleteAllByChannel_ChannelId(channelId);

        // 채널 DB 삭제
        channelRepository.deleteById(channelId);

        // Cache에서 삭제
        cacheService.removeAll(videoIds);

        // Elasticsearch에서 삭제
        videoIndexService.deleteVideos(videoIds);

        return new DeleteChannelResponse(channelId, deletedVideoCount);
    }
}
