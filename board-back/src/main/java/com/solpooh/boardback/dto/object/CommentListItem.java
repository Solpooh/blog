package com.solpooh.boardback.dto.object;

import com.solpooh.boardback.repository.resultSet.GetCommentListResultSet;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommentListItem {
    private Long commentNumber;
    private String nickname;
    private String profileImage;
    private String writeDatetime;
    private String content;
    private String userEmail;

    public CommentListItem(GetCommentListResultSet resultSet) {
        this.commentNumber = resultSet.getCommentNumber();
        this.nickname = resultSet.getNickname();
        this.profileImage = resultSet.getProfileImage();
        this.writeDatetime = resultSet.getWriteDatetime();
        this.content = resultSet.getContent();
        this.userEmail = resultSet.getUserEmail();
    }
    public static List<CommentListItem> copyList(Page<GetCommentListResultSet> resultSets) {
        List<CommentListItem> list = new ArrayList<>();
        for (GetCommentListResultSet resultSet : resultSets) {
            CommentListItem commentListItem = new CommentListItem(resultSet);
            list.add(commentListItem);
        }
        return list;
    }
}
