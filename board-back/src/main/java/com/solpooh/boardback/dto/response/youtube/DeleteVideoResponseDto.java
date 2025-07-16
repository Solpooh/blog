package com.solpooh.boardback.dto.response.youtube;

import com.solpooh.boardback.common.ResponseCode;
import com.solpooh.boardback.common.ResponseMessage;
import com.solpooh.boardback.dto.response.ResponseDto;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
public class DeleteVideoResponseDto extends ResponseDto {
    private DeleteVideoResponseDto() {
        super(ResponseCode.SUCCESS, ResponseMessage.SUCCESS);
    }
    public static ResponseEntity<DeleteVideoResponseDto> success() {
        DeleteVideoResponseDto result = new DeleteVideoResponseDto();
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    public static ResponseEntity<ResponseDto> noExistVideo() {
        ResponseDto result = new ResponseDto(ResponseCode.NOT_EXISTED_VIDEO, ResponseMessage.NOT_EXISTED_VIDEO);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }
}
