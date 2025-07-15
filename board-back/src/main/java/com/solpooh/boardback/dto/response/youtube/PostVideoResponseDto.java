package com.solpooh.boardback.dto.response.youtube;

import com.solpooh.boardback.common.ResponseCode;
import com.solpooh.boardback.common.ResponseMessage;
import com.solpooh.boardback.dto.response.ResponseDto;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
public class PostVideoResponseDto extends ResponseDto {
    private PostVideoResponseDto() {
        super(ResponseCode.SUCCESS, ResponseMessage.SUCCESS);
    }
    public static ResponseEntity<PostVideoResponseDto> success() {
        PostVideoResponseDto result = new PostVideoResponseDto();
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}
