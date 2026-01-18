package com.solpooh.boardback.batch;

import lombok.Getter;

@Getter
public enum BatchStatus {
    READY,
    RUNNING,
    SUCCESS,
    FAILED;
    private String batchStatus;
}
