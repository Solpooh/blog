package com.solpooh.boardback.service.implement;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.ChannelSnippet;
import com.solpooh.boardback.dto.response.ResponseDto;
import com.solpooh.boardback.dto.response.youtube.GetChannelResponseDto;
import com.solpooh.boardback.dto.response.youtube.PostChannelResponseDto;
import com.solpooh.boardback.entity.ChannelEntity;
import com.solpooh.boardback.provider.ChannelProvider;
import com.solpooh.boardback.repository.ChannelRepository;
import com.solpooh.boardback.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
    public ResponseEntity<? super GetChannelResponseDto> getChannel(String channelId) {
        ChannelEntity channelEntity;

        try {
            channelEntity = channelRepository.findByChannelId(channelId);
            if (channelEntity == null) return GetChannelResponseDto.channelNotFound();

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.databaseError();
        }

        return GetChannelResponseDto.success(channelEntity);
    }

    @Override
    public ResponseEntity<? super PostChannelResponseDto> postChannel() {
        try {

            for (String channelId : ChannelProvider.CHANNEL_IDS) {
                if (!channelRepository.existsById(channelId)) {
                    YouTube.Channels.List request = youtube.channels()
                            .list("snippet")
                            .setId(channelId)
                            .setKey(apiKey);

                    ChannelListResponse response = request.execute();

                    if (!response.getItems().isEmpty()) {
                        Channel channel = response.getItems().get(0);
                        ChannelSnippet snippet = channel.getSnippet();

                        ChannelEntity channelEntity = new ChannelEntity();
                        channelEntity.setChannelId(channelId);
                        channelEntity.setTitle(snippet.getTitle());
                        channelEntity.setThumbnail(snippet.getThumbnails().getDefault().getUrl());
                        channelEntity.setCustomUrl(snippet.getCustomUrl() != null ? snippet.getCustomUrl() : "unknown");
                        channelEntity.setLang("ko");
                        channelEntity.setCategory("dev");

                        channelRepository.save(channelEntity);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.databaseError();
        }

        return PostChannelResponseDto.success();
    }
}
