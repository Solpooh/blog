export default interface BoardListItem {
    boardNumber: number;
    title: string;
    content: string;
    category: string;
    boardTitleImage: string | null;
    favoriteCount: number;
    commentCount: number;
    viewCount: number;
    writeDatetime: string;
    writerNickname: string;
    writerProfileImage: string | null;
}