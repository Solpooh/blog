package com.solpooh.boardback.repository.resultSet;

public interface GetBoardDetailResultSet {
    Long getBoardNumber();
    String getTitle();
    String getContent();
    String getCategory();
    String getWriteDatetime();
    String getWriterEmail();
    String getWriterNickname();
    String getWriterProfileImage();
}
