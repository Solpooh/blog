export default interface CommentListItem {
    commentNumber: number;
    nickname: string;
    profileImage: string | null;
    writeDatetime: string;
    content: string;
    userEmail: string;
}