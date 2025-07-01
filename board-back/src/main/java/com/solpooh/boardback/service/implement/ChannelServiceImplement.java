package com.solpooh.boardback.service.implement;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.ChannelSnippet;
import com.solpooh.boardback.entity.ChannelEntity;
import com.solpooh.boardback.provider.ChannelList;
import com.solpooh.boardback.repository.ChannelRepository;
import com.solpooh.boardback.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Profile("dev")
@RequiredArgsConstructor
public class ChannelServiceImplement implements ChannelService {
    private final ChannelRepository channelRepository;
    private final YouTube youtube;
    @Value("${youtube.api.key}")
    private String apiKey;

    // GET: 특정 채널 정보 가져오기
    // 단순 channelId로 ChannelEntity 정보 반환
    @Override
    public ChannelEntity getChannel(String channelId) {
        return channelRepository.findById(channelId)
                .orElseThrow(() -> new IllegalArgumentException("채널이 존재하지 않습니다: " + channelId));
    }

    // POST: 채널 정보 저장하기
    // 1. 하드 코딩한 channelList에서 channelId 추출 (중복 체크)
    // 2. 해당 id로 API 요청
    // 3. 반환 응답 필드를 DB에 저장
        @Override
        public ResponseEntity<String> postChannel() {
            for (String channelId : ChannelList.CHANNEL_IDS) {
                if (!channelRepository.existsById(channelId)) {
                    try {
                        YouTube.Channels.List request = youtube.channels()
                                .list("snippet")
                                .setId(channelId)
                                .setKey(apiKey);

                        ChannelListResponse response = request.execute();
                        // 응답 잘됐는지 확인하기
                        if (!response.getItems().isEmpty()) {
                            Channel channel = response.getItems().get(0);
                            ChannelSnippet snippet = channel.getSnippet();

                            // ChannelEntity 생성자(dto)를 이용한 초기화로 리팩토링하기
                            ChannelEntity channelEntity = new ChannelEntity();
                            channelEntity.setChannelId(channelId);
                            channelEntity.setTitle(snippet.getTitle());
                            channelEntity.setThumbnail(snippet.getThumbnails().getDefault().getUrl());
                            channelEntity.setCustomUrl(snippet.getCustomUrl() != null ? snippet.getCustomUrl() : "unknown");
                            channelEntity.setLang("ko");
                            channelEntity.setCategory("dev");

                            channelRepository.save(channelEntity);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            return ResponseEntity.ok("저장 완료");
        }}
