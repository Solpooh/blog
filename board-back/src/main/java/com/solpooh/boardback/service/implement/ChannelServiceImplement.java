package com.solpooh.boardback.service.implement;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.solpooh.boardback.common.ResponseApi;
import com.solpooh.boardback.converter.YoutubeConverter;
import com.solpooh.boardback.dto.response.youtube.GetChannelResponse;
import com.solpooh.boardback.dto.response.youtube.PostChannelResponse;
import com.solpooh.boardback.entity.ChannelEntity;
import com.solpooh.boardback.exception.CustomException;
import com.solpooh.boardback.provider.ChannelProvider;
import com.solpooh.boardback.repository.ChannelRepository;
import com.solpooh.boardback.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ChannelServiceImplement implements ChannelService {
    @Value("${youtube.api.key}")
    private String apiKey;
    private final YouTube youtube;
    private final ChannelRepository channelRepository;

    // GET: 특정 채널 정보 가져오기
    // 단순 channelId로 ChannelEntity 정보 반환
    @Override
    public GetChannelResponse getChannel(String channelId) {
        ChannelEntity channelEntity = channelRepository.findByChannelId(channelId)
                .orElseThrow(() -> new CustomException(ResponseApi.NOT_EXISTED_CHANNEL));

        return YoutubeConverter.toResponse(channelEntity);
    }

    // Youtube API 비동기 I/O로 전환 후 성능향상
    @Override
    public PostChannelResponse postChannel() {

        Set<String> channelIds = channelRepository.findAllIds();
        // 이미 DB에 존재하는 채널은 제외(API 호출 최소화)
        List<String> newChannelIds = ChannelProvider.getChannelIds().stream()
                .filter(channelId -> !channelIds.contains(channelId))
                .toList();

        newChannelIds.stream()
                .map(this::fetchChannelFromYoutube)
                .flatMap(Optional::stream)
                .map(YoutubeConverter::toChannelEntity)
                .forEach(channelRepository::save);

        return new PostChannelResponse();
    }

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
}
