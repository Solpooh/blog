package com.solpooh.boardback.dto.response.youtube;

import com.solpooh.boardback.common.ResponseCode;
import com.solpooh.boardback.common.ResponseMessage;
import com.solpooh.boardback.dto.response.ResponseDto;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
public class PostChannelResponseDto extends ResponseDto {
    private PostChannelResponseDto() {
        super(ResponseCode.SUCCESS, ResponseMessage.SUCCESS);
    }

    public static ResponseEntity<PostChannelResponseDto> success() {
        PostChannelResponseDto result = new PostChannelResponseDto();
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}
