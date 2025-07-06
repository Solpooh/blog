import ResponseDto from '../response.dto';
import {BoardListItem} from 'types/interface';
import Pagination from 'types/interface/pagination.interface';

export default interface GetSearchBoardListResponseDto extends ResponseDto {
    pagination: Pagination<BoardListItem>;
}