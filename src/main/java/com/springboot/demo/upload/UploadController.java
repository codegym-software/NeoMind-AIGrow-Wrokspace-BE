package com.springboot.demo.upload;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/uploads")
public class UploadController {

    private final UploadService service;

    public UploadController(UploadService service) {
        this.service = service;
    }

    // POST /api/uploads/company-logo  (form-data: file=<blob>)
    // POST /api/uploads/user-avatar
    // POST /api/uploads/temp
    @PostMapping("/{bucket}")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    public Map<String, Object> upload(@PathVariable String bucket, @RequestParam("file") MultipartFile file)
            throws IOException {

        UploadBucket b = UploadBucket.from(bucket);
        var stored = service.store(file, b);

        return Map.of(
                "filename", stored.filename(),
                "contentType", stored.contentType(),
                "size", stored.size(),
                "url", stored.url()
        );
    }
}
