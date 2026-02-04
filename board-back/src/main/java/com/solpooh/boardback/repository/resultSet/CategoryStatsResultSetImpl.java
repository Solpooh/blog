package com.solpooh.boardback.repository.resultSet;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CategoryStatsResultSetImpl implements CategoryStatsResultSet {
    private final String mainCategory;
    private final String subCategory;
    private final Long count;
}
