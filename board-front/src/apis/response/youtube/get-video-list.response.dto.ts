import ResponseDto from '../response.dto';
import {VideoListItem} from 'types/interface';
import Pagination from 'types/interface/pagination.interface';

export interface GetVideoList {
    videoList: Pagination<VideoListItem>;
}
export type GetVideoListResponseDto = ResponseDto<GetVideoList>;