import {VideoListItem} from "../../../types/interface";
import ResponseDto from "../response.dto";

export interface GetTopTrendVideoList {
    videoList: VideoListItem[];
}
export type GetTopTrendVideoListResponseDto = ResponseDto<GetTopTrendVideoList>;