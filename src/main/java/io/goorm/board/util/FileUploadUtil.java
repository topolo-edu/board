package io.goorm.board.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 파일 업로드 유틸리티 클래스
 */
@Slf4j
@Component
public class FileUploadUtil {

    @Value("${app.upload.root:/src/main/resources/static/uploads}")
    private String uploadRoot;

    private static final String PRODUCT_PATH = "products";

    /**
     * 상품 이미지 업로드
     */
    public String uploadProductImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }

        // 파일 확장자 검증
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !isImageFile(originalFilename)) {
            throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");
        }

        // 파일명 생성
        String filename = generateFilename(originalFilename);

        // 저장 경로 생성
        Path uploadPath = Paths.get(uploadRoot, PRODUCT_PATH);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 파일 저장
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath);

        log.info("Product image uploaded: {}", filePath);

        // 웹 접근 경로 반환
        return "/uploads/" + PRODUCT_PATH + "/" + filename;
    }

    /**
     * 파일 삭제
     */
    public void deleteFile(String fileUrl) throws IOException {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }

        // URL에서 실제 파일 경로 추출
        String relativePath = fileUrl.replace("/uploads/", "");
        Path filePath = Paths.get(uploadRoot, relativePath);

        if (Files.exists(filePath)) {
            Files.delete(filePath);
            log.info("File deleted: {}", filePath);
        }
    }

    /**
     * 이미지 파일 여부 확인
     */
    private boolean isImageFile(String filename) {
        String extension = getFileExtension(filename).toLowerCase();
        return extension.equals("jpg") || extension.equals("jpeg") ||
               extension.equals("png") || extension.equals("gif") ||
               extension.equals("webp");
    }

    /**
     * 파일명 생성
     */
    private String generateFilename(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);

        return String.format("product_%s_%s.%s", timestamp, uuid, extension);
    }

    /**
     * 파일 확장자 추출
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex > 0 ? filename.substring(lastDotIndex + 1) : "";
    }
}