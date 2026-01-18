package com.solpooh.boardback.batch;

import lombok.Getter;

@Getter
public enum BatchJobType {
    VIDEO_COLLECT_HOURLY,
    VIDEO_SCORE_UPDATE,
    VIDEO_DATA_UPDATE;
    private String batchJob;
}
