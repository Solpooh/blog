import ResponseDto from '../response.dto';
import {BoardListItem} from 'types/interface';
import Pagination from "../../../types/interface/pagination.interface";
import CategoryCountResponseDto from "./category-count.response.dto";

export default interface GetLatestBoardListResponseDto extends ResponseDto {
    pagination: Pagination<BoardListItem>;
    categoryCounts: CategoryCountResponseDto[];
}