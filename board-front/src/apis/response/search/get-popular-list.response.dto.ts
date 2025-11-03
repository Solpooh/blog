import ResponseDto from '../response.dto';

export interface GetPopularList {
    popularWordList: string[];
}
export type GetPopularListResponseDto = ResponseDto<GetPopularList>;
