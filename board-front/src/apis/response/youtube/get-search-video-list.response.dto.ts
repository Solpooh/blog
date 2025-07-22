import ResponseDto from "../response.dto";
import Pagination from "types/interface/pagination.interface";
import {VideoListItem} from "types/interface";

export default interface GetSearchVideoListResponseDto extends ResponseDto {
    pagination: Pagination<VideoListItem>;
}