package com.solpooh.boardback.dto.response;

import com.solpooh.boardback.common.ResponseApiInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResponseDto<T> {
    private String code;
    private String message;
    private T data;

    public static <T> ResponseDto<T> of(ResponseApiInterface status, T data) {
        return new ResponseDto<>(status.getCode(), status.getMessage(), data);
    }
    public static <T> ResponseDto<T> of(ResponseApiInterface status) {
        return of(status, null);
    }
}