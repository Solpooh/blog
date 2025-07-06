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
public class GetLatestBoardListResponseDto extends ResponseDto {
    private Pagination<BoardListItem> pagination;
    private List<CategoryResponseDto> categoryCounts;

    private GetLatestBoardListResponseDto(Page<BoardListViewEntity> boardEntities, List<CategoryResponseDto> categoryCounts) {
        super(ResponseCode.SUCCESS, ResponseMessage.SUCCESS);
        List<BoardListItem> latestList = BoardListItem.getList(boardEntities);
        this.pagination = Pagination.of(boardEntities, latestList);
        this.categoryCounts = categoryCounts;
    }

    public static ResponseEntity<GetLatestBoardListResponseDto> success(Page<BoardListViewEntity> boardEntities, List<CategoryResponseDto> categoryCounts) {
        GetLatestBoardListResponseDto result = new GetLatestBoardListResponseDto(boardEntities, categoryCounts);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}
