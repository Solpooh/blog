//package com.solpooh.boardback.dto.response.board;
//
//import com.solpooh.boardback.common.ResponseCode;
//import com.solpooh.boardback.common.ResponseMessage;
//import com.solpooh.boardback.dto.object.BoardListItem;
//import com.solpooh.boardback.dto.response.ResponseDto;
//import com.solpooh.boardback.entity.BoardListViewEntity;
//import lombok.Getter;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//
//import java.util.List;
//
//@Getter
//public class GetSearchBoardListResponseDto extends ResponseDto {
//    private List<BoardListItem> searchList;
//
//    private GetSearchBoardListResponseDto(List<BoardListViewEntity> boardListViewEntities) {
//        super(ResponseCode.SUCCESS, ResponseMessage.SUCCESS);
//        this.searchList = BoardListItem.getList(boardListViewEntities);
//    }
//
//    public static ResponseEntity<GetSearchBoardListResponseDto> success(List<BoardListViewEntity> boardListViewEntities) {
//        GetSearchBoardListResponseDto result = new GetSearchBoardListResponseDto(boardListViewEntities);
//        return ResponseEntity.status(HttpStatus.OK).body(result);
//    }
//}
