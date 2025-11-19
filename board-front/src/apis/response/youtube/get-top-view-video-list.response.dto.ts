import {VideoListItem} from "../../../types/interface";
import ResponseDto from "../response.dto";

export interface GetTopViewVideoList {
    videoList: VideoListItem[];
}
export type GetTopViewVideoListResponseDto = ResponseDto<GetTopViewVideoList>