import ResponseDto from "../response.dto";

export interface SignInResponse {
    token: string;
    expirationTime: string;
    role: string;
}
export type SignInResponseDto = ResponseDto<SignInResponse>;