package com.stacknote.back.domain.file.service;

import com.stacknote.back.domain.file.exception.FileStorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * 파일 저장소 서비스
 * 실제 파일 시스템과의 상호작용을 담당
 */
@Slf4j
@Service
public class FileStorageService {

    private final Path rootLocation;

    public FileStorageService(@Value("${file.upload.path:files}") String uploadPath) {
        this.rootLocation = Paths.get(uploadPath).toAbsolutePath().normalize();
        initializeStorage();
    }

    /**
     * 저장소 초기화
     */
    private void initializeStorage() {
        try {
            if (!Files.exists(rootLocation)) {
                Files.createDirectories(rootLocation);
                log.info("파일 저장소 디렉토리 생성: {}", rootLocation);
            }
        } catch (IOException e) {
            throw new FileStorageException("파일 저장소를 초기화할 수 없습니다.", e);
        }
    }

    /**
     * 파일 저장
     */
    public void storeFile(MultipartFile file, String relativePath) {
        try {
            if (file.isEmpty()) {
                throw new FileStorageException("빈 파일은 저장할 수 없습니다.");
            }

            // 상대 경로에서 절대 경로 생성
            Path targetLocation = rootLocation.resolve(relativePath.startsWith("/") ?
                    relativePath.substring(1) : relativePath).normalize();

            // 디렉토리가 존재하지 않으면 생성
            Path parentDir = targetLocation.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
                log.debug("디렉토리 생성: {}", parentDir);
            }

            // 파일 저장
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
                log.debug("파일 저장 완료: {}", targetLocation);
            }

        } catch (IOException e) {
            log.error("파일 저장 실패: {}", relativePath, e);
            throw new FileStorageException("파일을 저장할 수 없습니다: " + file.getOriginalFilename(), e);
        }
    }

    /**
     * 파일 로드
     */
    public Resource loadFileAsResource(String relativePath) {
        try {
            Path filePath = rootLocation.resolve(relativePath.startsWith("/") ?
                    relativePath.substring(1) : relativePath).normalize();

            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new FileStorageException("파일을 찾을 수 없습니다: " + relativePath);
            }
        } catch (MalformedURLException e) {
            log.error("파일 로드 실패: {}", relativePath, e);
            throw new FileStorageException("파일을 로드할 수 없습니다: " + relativePath, e);
        }
    }

    /**
     * 파일 삭제
     */
    public void deleteFile(String relativePath) {
        try {
            Path filePath = rootLocation.resolve(relativePath.startsWith("/") ?
                    relativePath.substring(1) : relativePath).normalize();

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.debug("파일 삭제 완료: {}", filePath);

                // 빈 디렉토리 정리
                cleanupEmptyDirectories(filePath.getParent());
            } else {
                log.warn("삭제할 파일이 존재하지 않음: {}", filePath);
            }
        } catch (IOException e) {
            log.error("파일 삭제 실패: {}", relativePath, e);
            throw new FileStorageException("파일을 삭제할 수 없습니다: " + relativePath, e);
        }
    }

    /**
     * 파일 존재 여부 확인
     */
    public boolean fileExists(String relativePath) {
        try {
            Path filePath = rootLocation.resolve(relativePath.startsWith("/") ?
                    relativePath.substring(1) : relativePath).normalize();
            return Files.exists(filePath);
        } catch (Exception e) {
            log.error("파일 존재 확인 실패: {}", relativePath, e);
            return false;
        }
    }

    /**
     * 파일 크기 조회
     */
    public long getFileSize(String relativePath) {
        try {
            Path filePath = rootLocation.resolve(relativePath.startsWith("/") ?
                    relativePath.substring(1) : relativePath).normalize();

            if (Files.exists(filePath)) {
                return Files.size(filePath);
            } else {
                throw new FileStorageException("파일을 찾을 수 없습니다: " + relativePath);
            }
        } catch (IOException e) {
            log.error("파일 크기 조회 실패: {}", relativePath, e);
            throw new FileStorageException("파일 크기를 조회할 수 없습니다: " + relativePath, e);
        }
    }

    /**
     * 디렉토리 생성
     */
    public void createDirectory(String relativePath) {
        try {
            Path dirPath = rootLocation.resolve(relativePath.startsWith("/") ?
                    relativePath.substring(1) : relativePath).normalize();

            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
                log.debug("디렉토리 생성: {}", dirPath);
            }
        } catch (IOException e) {
            log.error("디렉토리 생성 실패: {}", relativePath, e);
            throw new FileStorageException("디렉토리를 생성할 수 없습니다: " + relativePath, e);
        }
    }

    /**
     * 디렉토리 삭제 (비어있는 경우만)
     */
    public void deleteDirectoryIfEmpty(String relativePath) {
        try {
            Path dirPath = rootLocation.resolve(relativePath.startsWith("/") ?
                    relativePath.substring(1) : relativePath).normalize();

            if (Files.exists(dirPath) && Files.isDirectory(dirPath)) {
                try {
                    if (Files.list(dirPath).findAny().isEmpty()) {
                        Files.delete(dirPath);
                        log.debug("빈 디렉토리 삭제: {}", dirPath);
                    }
                } catch (IOException listException) {
                    log.warn("빈 디렉토리 삭제 실패: {}, 오류: {}", relativePath, listException.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("디렉토리 삭제 실패: {}", relativePath, e);
            throw new FileStorageException("디렉토리를 삭제할 수 없습니다: " + relativePath, e);
        }
    }

    /**
     * 빈 디렉토리 정리 (재귀적)
     */
    private void cleanupEmptyDirectories(Path directory) {
        if (directory == null || !Files.exists(directory) || directory.equals(rootLocation)) {
            return;
        }

        try {
            if (Files.isDirectory(directory) && Files.list(directory).findAny().isEmpty()) {
                Files.delete(directory);
                log.debug("빈 디렉토리 정리: {}", directory);

                // 상위 디렉토리도 확인
                cleanupEmptyDirectories(directory.getParent());
            }
        } catch (IOException e) {
            log.debug("디렉토리 정리 중 오류 (무시): {}", directory);
        }
    }

    /**
     * 저장소 루트 경로 반환
     */
    public Path getRootLocation() {
        return rootLocation;
    }
}