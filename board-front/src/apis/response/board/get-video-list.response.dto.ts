import ResponseDto from "../response.dto";
import {VideoListItem} from "types/interface";

export default interface GetVideoListResponseDto extends ResponseDto {
    videoList: VideoListItem[];
}