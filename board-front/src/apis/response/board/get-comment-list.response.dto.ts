import ResponseDto from '../response.dto';
import {CommentListItem} from 'types/interface';
import Pagination from 'types/interface/pagination.interface';

export interface GetCommentList{
    commentList: Pagination<CommentListItem>;
}
export type GetCommentListResponseDto = ResponseDto<GetCommentList>;