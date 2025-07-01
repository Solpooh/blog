package com.solpooh.boardback.dto.response.board;

import com.solpooh.boardback.common.ResponseCode;
import com.solpooh.boardback.common.ResponseMessage;
import com.solpooh.boardback.dto.object.VideoListItem;
import com.solpooh.boardback.dto.response.ResponseDto;
import com.solpooh.boardback.entity.VideoEntity;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Getter
public class GetVideoListResponseDto extends ResponseDto {
    private List<VideoListItem> videoList;

    private GetVideoListResponseDto(List<VideoEntity> videoEntities) {
        super(ResponseCode.SUCCESS, ResponseMessage.SUCCESS);
        this.videoList = VideoListItem.getList(videoEntities);
    }
    public static ResponseEntity<GetVideoListResponseDto> success(List<VideoEntity> videoEntities) {
        GetVideoListResponseDto result = new GetVideoListResponseDto(videoEntities);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}
