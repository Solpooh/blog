package com.solpooh.boardback.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "image")
@Table(name = "image")
public class ImageEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int sequence;
    private Integer boardNumber;
    private String image;
    @Column(name = "is_deleted")
    private boolean deleted;

    public ImageEntity(int boardNumber, String image) {
        this.boardNumber = boardNumber;
        this.image = image;
    }
}
