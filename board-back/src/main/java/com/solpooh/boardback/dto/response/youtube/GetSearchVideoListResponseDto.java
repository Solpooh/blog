package com.solpooh.boardback.dto.response.youtube;

import com.solpooh.boardback.common.Pagination;
import com.solpooh.boardback.common.ResponseCode;
import com.solpooh.boardback.common.ResponseMessage;
import com.solpooh.boardback.dto.object.VideoListItem;
import com.solpooh.boardback.dto.response.ResponseDto;
import com.solpooh.boardback.entity.VideoEntity;
import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Getter
public class GetSearchVideoListResponseDto extends ResponseDto {
    private Pagination<VideoListItem> pagination;

    private GetSearchVideoListResponseDto(Page<VideoEntity> videoEntities) {
        super(ResponseCode.SUCCESS, ResponseMessage.SUCCESS);
        List<VideoListItem> videoList = VideoListItem.getPagedList(videoEntities);
        this.pagination = Pagination.of(videoEntities, videoList);
    }

    public static ResponseEntity<GetSearchVideoListResponseDto> success(Page<VideoEntity> videoEntities) {
        GetSearchVideoListResponseDto result = new GetSearchVideoListResponseDto(videoEntities);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
    public static ResponseEntity<ResponseDto> videoNotFound() {
        ResponseDto responseBody = new ResponseDto(ResponseCode.NOT_EXISTED_VIDEO, ResponseMessage.NOT_EXISTED_VIDEO);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
    }

}
