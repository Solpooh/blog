import ResponseDto from '../response.dto';
import {BoardListItem} from 'types/interface';
import Pagination from 'types/interface/pagination.interface';

export interface GetUserBoardList {
    userBoardList: Pagination<BoardListItem>;
}

export type GetUserBoardListResponseDto = ResponseDto<GetUserBoardList>;