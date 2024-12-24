export default interface Board {
    boardNumber: number;
    title: string;
    content: string;
    category: string;
    boardImageList: string[];
    writeDatetime: string;
    writerEmail: string;
    writerNickname: string;
    writerProfileImage: string | null;
}