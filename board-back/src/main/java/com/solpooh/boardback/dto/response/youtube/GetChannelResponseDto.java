package com.solpooh.boardback.dto.response.youtube;

import com.solpooh.boardback.common.ResponseCode;
import com.solpooh.boardback.common.ResponseMessage;
import com.solpooh.boardback.dto.response.ResponseDto;
import com.solpooh.boardback.entity.ChannelEntity;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
public class GetChannelResponseDto extends ResponseDto {
    // 채널 정보
    private final String channelId;
    private final String title;
    private final String thumbnail;

    private GetChannelResponseDto(ChannelEntity channelEntity) {
        super(ResponseCode.SUCCESS, ResponseMessage.SUCCESS);
        this.channelId = channelEntity.getChannelId();
        this.title = channelEntity.getTitle();
        this.thumbnail = channelEntity.getThumbnail();
    }

    public static ResponseEntity<GetChannelResponseDto> success(ChannelEntity channelEntity) {
        GetChannelResponseDto result = new GetChannelResponseDto(channelEntity);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    public static ResponseEntity<ResponseDto> channelNotFound() {
        ResponseDto responseBody = new ResponseDto(ResponseCode.NOT_EXISTED_CHANNEL, ResponseMessage.NOT_EXISTED_CHANNEL);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
    }
}
