package com.kanaetochi.audio_alchemists.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "\"project_version\"")
@Data
@Builder
@NoArgsConstructor  
@AllArgsConstructor
public class ProjectVersion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(updatable = false, nullable = false)
    private LocalDateTime timeStamp;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private String snapshotData;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "saved_by_user_id", nullable = false)
    private User savedBy;

    @Column // Optional User-provided description for the version
    private String description;

    @PrePersist
    protected void onCreate() {
        timeStamp = LocalDateTime.now();
    }
}