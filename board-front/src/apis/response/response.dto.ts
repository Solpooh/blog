import { ResponseCode } from 'types/enum';
export default interface ResponseDto<T = any> {
    code: ResponseCode;
    message: string;
    data: T;
}