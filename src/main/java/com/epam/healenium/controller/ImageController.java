package com.epam.healenium.controller;

import com.epam.healenium.repository.ReportRepository;
import com.epam.healenium.tenant.TenantTxFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Serves report screenshots. On Pro, {@code uid} must be a report visible under the
 * current RLS tenant (via {@link TenantTxFacade}).
 */
@Slf4j
@RestController
@RequestMapping("/screenshots")
@RequiredArgsConstructor
public class ImageController {

    private final TenantTxFacade tenantTx;
    private final ReportRepository reportRepository;

    @GetMapping(value = "/{uid}/{name}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getImageWithMediaType(@PathVariable String uid,
                                                        @PathVariable String name) {
        if (!isSafePathSegment(uid) || !isSafePathSegment(name)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid screenshot path");
        }

        boolean allowed = tenantTx.required(() -> reportRepository.findById(uid).isPresent());
        if (!allowed) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Screenshot not found");
        }

        Path root = Paths.get("").toAbsolutePath().normalize();
        Path screenshotsRoot = root.resolve("screenshots").normalize();
        Path file = screenshotsRoot.resolve(uid).resolve(name).normalize();
        if (!file.startsWith(screenshotsRoot) || !Files.isRegularFile(file)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Screenshot not found");
        }

        try (InputStream inputStream = new FileInputStream(file.toFile())) {
            return ResponseEntity.ok(IOUtils.toByteArray(inputStream));
        } catch (IOException e) {
            log.warn("[Image] Error reading screenshot {}/{}: {}", uid, name, e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Screenshot not found");
        }
    }

    private static boolean isSafePathSegment(String segment) {
        return segment != null
                && !segment.isBlank()
                && !segment.contains("..")
                && !segment.contains("/")
                && !segment.contains("\\");
    }
}
