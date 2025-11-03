import ResponseDto from '../response.dto';
import {BoardListItem} from 'types/interface';
import Pagination from "../../../types/interface/pagination.interface";
import CategoryCountResponseDto from "./category-count.response.dto";

export interface GetLatestBoardList {
    boardList: Pagination<BoardListItem>;
    categoryList: CategoryCountResponseDto[];
}

export type GetLatestBoardListResponseDto = ResponseDto<GetLatestBoardList>;