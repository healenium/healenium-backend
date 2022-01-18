package com.epam.healenium.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

@Slf4j
@RestController
@RequestMapping("/screenshots")
@RequiredArgsConstructor
public class ImageController {

    @GetMapping(value = "/{uid}/{name}", produces = MediaType.IMAGE_PNG_VALUE)
    public @ResponseBody
    byte[] getImageWithMediaType(@PathVariable String uid, @PathVariable String name) throws IOException {
        String rootDir = Paths.get("").toAbsolutePath().toString();
        String path = Paths.get(rootDir, "/screenshots/" + uid + "/" + name).toString();
        try (InputStream inputStream = new FileInputStream(path)) {
            return IOUtils.toByteArray(inputStream);
        }
    }

}