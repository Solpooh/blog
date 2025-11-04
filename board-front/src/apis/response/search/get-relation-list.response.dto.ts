import ResponseDto from '../response.dto';

export interface GetRelationList {
    relativeWordList: string[];
}

export type GetRelationListResponseDto = ResponseDto<GetRelationList>;