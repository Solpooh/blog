import ResponseDto from '../response.dto';
import {CommentListItem} from 'types/interface';
import Pagination from 'types/interface/pagination.interface';

export default interface GetCommentListResponseDto extends ResponseDto {
    pagination: Pagination<CommentListItem>;
}