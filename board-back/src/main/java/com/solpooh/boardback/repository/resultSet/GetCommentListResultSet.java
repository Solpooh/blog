package com.solpooh.boardback.repository.resultSet;

public interface GetCommentListResultSet {
    Long getCommentNumber();
    String getNickname();
    String getProfileImage();
    String getWriteDatetime();
    String getContent();
    String getUserEmail();
}
