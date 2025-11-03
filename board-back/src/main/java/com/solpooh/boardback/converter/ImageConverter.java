package com.solpooh.boardback.converter;

import com.solpooh.boardback.dto.request.board.PatchBoardRequest;
import com.solpooh.boardback.dto.request.board.PostBoardRequest;
import com.solpooh.boardback.entity.ImageEntity;

import java.util.List;

public class ImageConverter {
    // BoardRequest 통합하자
    private ImageConverter() {}
    public static List<ImageEntity> toEntity(PostBoardRequest dto, Long boardNumber) {
        return dto.boardImageList()
                .stream()
                .map(image -> ImageEntity.builder()
                        .image(image)
                        .boardNumber(boardNumber)
                        .build()
                )
                .toList();
    }
    public static List<ImageEntity> toEntity(PatchBoardRequest dto, Long boardNumber) {
        return dto.boardImageList()
                .stream()
                .map(image -> ImageEntity.builder()
                        .image(image)
                        .boardNumber(boardNumber)
                        .build()
                )
                .toList();
    }
}
