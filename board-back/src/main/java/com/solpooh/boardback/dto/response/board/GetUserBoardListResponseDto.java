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
//public class GetUserBoardListResponseDto extends ResponseDto {
//    private List<BoardListItem> userBoardList;
//
//    private GetUserBoardListResponseDto(List<BoardListViewEntity> boardListViewEntities) {
//        super(ResponseCode.SUCCESS, ResponseMessage.SUCCESS);
//        this.userBoardList = BoardListItem.getList(boardListViewEntities);
//    }
//
//    public static ResponseEntity<GetUserBoardListResponseDto> success(List<BoardListViewEntity> boardListViewEntities) {
//        GetUserBoardListResponseDto result = new GetUserBoardListResponseDto(boardListViewEntities);
//        return ResponseEntity.status(HttpStatus.OK).body(result);
//    }
//
//    public static ResponseEntity<ResponseDto> noExistUser() {
//        ResponseDto result = new ResponseDto(ResponseCode.NOT_EXISTED_USER, ResponseMessage.NOT_EXISTED_USER);
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
//    }
//}
