package com.solpooh.boardback.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

/**
 * 신규 영상 수집 완료 시 발행되는 이벤트
 * Transcript 비동기 처리를 트리거함
 */
@Getter
public class NewVideosCollectedEvent extends ApplicationEvent {
    private final List<String> videoIds;

    public NewVideosCollectedEvent(Object source, List<String> videoIds) {
        super(source);
        this.videoIds = videoIds;
    }
}
