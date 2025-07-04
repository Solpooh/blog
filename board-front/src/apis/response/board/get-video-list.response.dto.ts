import ResponseDto from '../response.dto';
import {VideoListItem} from 'types/interface';
import Pagination from 'types/interface/pagination.interface';

export default interface GetVideoListResponseDto extends ResponseDto {
    pagination: Pagination<VideoListItem>;
}