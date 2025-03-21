package org.example.controller;

import org.example.entity.Image;
import org.example.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Optional;

@RestController
@RequestMapping("/api/images")
public class ImageController {
    private final ImageService imageService;

    @Autowired
    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    // Эндпоинт загрузки изображения (отправляем файл в теле запроса)
    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            Image image = imageService.uploadImage(file);
            return ResponseEntity.ok("Image uploaded successfully with ID: " + image.getId());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error uploading image: " + e.getMessage());
        }
    }

    // Эндпоинт получения изображения по ID (изображение отправляется в теле ответа)
    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable Long id) {
        Optional<Image> imageOpt = imageService.getImage(id);
        if (imageOpt.isPresent()) {
            Image image = imageOpt.get();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + image.getName() + "\"")
                    .contentType(MediaType.parseMediaType(image.getContentType()))
                    .body(image.getData());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Новый эндпоинт подтверждения доставки изображения;
    // событие создаётся только при успешном подтверждении доставки
    @PostMapping("/{id}/confirm")
    public ResponseEntity<?> confirmDelivery(@PathVariable Long id) {
        try {
            imageService.confirmDelivery(id);
            return ResponseEntity.ok("Image delivery confirmed for ID: " + id);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error confirming delivery: " + e.getMessage());
        }
    }
}
