package com.solpooh.boardback.dto.response.board;

import com.solpooh.boardback.common.Pagination;
import com.solpooh.boardback.common.ResponseCode;
import com.solpooh.boardback.common.ResponseMessage;
import com.solpooh.boardback.dto.object.BoardListItem;
import com.solpooh.boardback.dto.response.ResponseDto;
import com.solpooh.boardback.entity.BoardListViewEntity;
import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Getter
public class GetUserBoardListResponseDto extends ResponseDto {
    private Pagination<BoardListItem> pagination;

    private GetUserBoardListResponseDto(Page<BoardListViewEntity> boardListViewEntities) {
        super(ResponseCode.SUCCESS, ResponseMessage.SUCCESS);
        List<BoardListItem> userBoardList = BoardListItem.getList(boardListViewEntities);
        this.pagination = Pagination.of(boardListViewEntities, userBoardList);
    }

    public static ResponseEntity<GetUserBoardListResponseDto> success(Page<BoardListViewEntity> boardListViewEntities) {
        GetUserBoardListResponseDto result = new GetUserBoardListResponseDto(boardListViewEntities);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    public static ResponseEntity<ResponseDto> noExistUser() {
        ResponseDto result = new ResponseDto(ResponseCode.NOT_EXISTED_USER, ResponseMessage.NOT_EXISTED_USER);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }
}
