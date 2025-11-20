import {VideoListItem} from "../../../types/interface";
import ResponseDto from "../response.dto";

export interface GetShortsVideoList {
    videoList: VideoListItem[];
}
export type GetShortsVideoListResponseDto = ResponseDto<GetShortsVideoList>