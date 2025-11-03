import ResponseDto from '../response.dto';
import {BoardListItem} from 'types/interface';
import Pagination from 'types/interface/pagination.interface';

export interface GetSearchBoardList {
    searchList: Pagination<BoardListItem>;
}
export type GetSearchBoardListResponseDto = ResponseDto<GetSearchBoardList>;