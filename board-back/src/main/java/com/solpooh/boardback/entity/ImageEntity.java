package com.solpooh.boardback.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "image")
@Table(name = "image")
public class ImageEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sequence;
    private Long boardNumber;
    private String image;
    @Column(name = "is_deleted")
    private boolean deleted;
}
