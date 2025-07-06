package com.solpooh.boardback.dto.response.board;

import com.solpooh.boardback.common.Pagination;
import com.solpooh.boardback.common.ResponseCode;
import com.solpooh.boardback.common.ResponseMessage;
import com.solpooh.boardback.dto.object.BoardListItem;
import com.solpooh.boardback.dto.object.CommentListItem;
import com.solpooh.boardback.dto.response.ResponseDto;
import com.solpooh.boardback.repository.resultSet.GetCommentListResultSet;
import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Getter
public class GetCommentListResponseDto extends ResponseDto {
    private Pagination<CommentListItem> pagination;

    private GetCommentListResponseDto(Page<GetCommentListResultSet> resultSets) {
        super(ResponseCode.SUCCESS, ResponseMessage.SUCCESS);
        List<CommentListItem> commentList = CommentListItem.copyList(resultSets);
        this.pagination = Pagination.of(resultSets, commentList);
    }

    public static ResponseEntity<GetCommentListResponseDto> success(Page<GetCommentListResultSet> resultSets) {
        GetCommentListResponseDto result = new GetCommentListResponseDto(resultSets);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    public static ResponseEntity<ResponseDto> noExistBoard() {
        ResponseDto result = new ResponseDto(ResponseCode.NOT_EXISTED_BOARD, ResponseMessage.NOT_EXISTED_BOARD);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }
}
