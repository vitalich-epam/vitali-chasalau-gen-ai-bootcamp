package com.epam.training.gen.ai.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "content")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContentEntity {

    @Id
    private String id;
    @Column
    private String content;
    @Column
    private Integer index;
    @ManyToOne
    @JoinColumn(name = "file", nullable = false)
    private FileEntity file;

}
