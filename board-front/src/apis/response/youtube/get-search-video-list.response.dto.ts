import ResponseDto from "../response.dto";
import Pagination from "types/interface/pagination.interface";
import {VideoListItem} from "types/interface";

export interface GetSearchVideoList {
    videoList: Pagination<VideoListItem>;
}
export type GetSearchVideoListResponseDto = ResponseDto<GetSearchVideoList>;