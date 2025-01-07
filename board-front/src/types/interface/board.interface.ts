import ImageUrl from './imageUrl.interface';

export default interface Board {
    boardNumber: number;
    title: string;
    content: string;
    category: string;
    boardImageList: ImageUrl[];
    writeDatetime: string;
    writerEmail: string;
    writerNickname: string;
    writerProfileImage: string | null;
}