package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "image_events")
public class ImageEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long imageId; // Идентификатор изображения

    private String eventType; // Например: IMAGE_UPLOADED, IMAGE_SENT

    @Lob
    private String eventData; // Дополнительные данные события (JSON)

    private LocalDateTime timestamp;
}
