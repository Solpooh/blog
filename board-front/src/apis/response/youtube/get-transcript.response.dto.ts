import ResponseDto from "../response.dto";

export interface GetTranscriptVideo {
    transcript: string;
}
export type GetTranscriptResponseDto = ResponseDto<GetTranscriptVideo>