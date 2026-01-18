package com.solpooh.boardback.service.youtube;

import com.solpooh.boardback.agent.SummaryAgent;
import com.solpooh.boardback.converter.TranscriptConverter;
import com.solpooh.boardback.dto.response.youtube.GetTranscriptResponse;
import com.solpooh.boardback.entity.VideoEntity;
import com.solpooh.boardback.fetcher.TranscriptFetcher;
import com.solpooh.boardback.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TranscriptService {
    private final TranscriptFetcher transcriptFetcher;
    private final VideoRepository videoRepository;
    private final SummaryAgent summaryAgent;

    @Transactional
    public GetTranscriptResponse getTranscript(String videoId) {
        // 1. videoId 기준으로 transcript 컬럼만 조회
        Optional<String> existingTranscript =
                videoRepository.findTranscriptByVideoId(videoId);
        // 2. transcript 존재 → 즉시 반환
        if (existingTranscript.isPresent()) {
            return new GetTranscriptResponse(existingTranscript.get());
        }

        // 3. 없으면 VideoEntity 조회 (쓰기 목적)
        VideoEntity videoEntity = videoRepository.findByVideoId(videoId)
                .orElseThrow(() -> new IllegalStateException("Video not found"));
        try {
            // 4. yt-dlp 실행 → 자막 파싱
            Path path = transcriptFetcher.fetchTranscriptJson(videoId);
            String rawTranscript = TranscriptConverter.parseTranscript(path);

            // 5. AI 요약 실행
            String summarizedTranscript = summaryAgent.summarize(rawTranscript);

            // 6. VideoEntity.transcript 업데이트 + 저장
            videoEntity.setTranscript(summarizedTranscript);
            videoRepository.save(videoEntity);

            return new GetTranscriptResponse(summarizedTranscript);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}
