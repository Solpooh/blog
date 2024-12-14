package com.solpooh.boardback.repository.resultSet;

public interface GetCommentListResultSet {
    Integer getCommentNumber();
    String getNickname();
    String getProfileImage();
    String getWriteDatetime();
    String getContent();
    String getUserEmail();
}
