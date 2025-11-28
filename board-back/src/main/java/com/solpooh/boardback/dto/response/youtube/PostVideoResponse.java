package com.solpooh.boardback.dto.response.youtube;

import java.util.List;

public record PostVideoResponse(
        List<String> videoIds
){}