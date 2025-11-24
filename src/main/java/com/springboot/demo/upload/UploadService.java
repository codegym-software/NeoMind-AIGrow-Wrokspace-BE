package com.springboot.demo.upload;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class UploadService {
    private final StorageProperties props;

    private static final Set<String> ALLOWED_TYPES = Set.of(
            MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE, "image/webp"
    );
    private static final Pattern SAFE = Pattern.compile("[^a-zA-Z0-9._-]");

    public UploadService(StorageProperties props) {
        this.props = props;
    }

    public StoredFile store(MultipartFile file, UploadBucket bucket) throws IOException {
        if (file.isEmpty()) throw new IllegalArgumentException("Empty file");
        String contentType = safeContentType(file);
        
        // For task attachments, allow all file types
        if (bucket == UploadBucket.TASK_ATTACHMENT) {
            return storeAnyFile(file, bucket, contentType);
        }
        
        // For other buckets (avatars, logos), only allow images
        if (!ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Unsupported content type: " + contentType);
        }

        String ext = switch (contentType) {
            case MediaType.IMAGE_JPEG_VALUE -> ".jpg";
            case MediaType.IMAGE_PNG_VALUE -> ".png";
            case "image/webp" -> ".webp";
            default -> getExtensionFromOriginal(file.getOriginalFilename()); // fallback
        };

        String original = StringUtils.hasText(file.getOriginalFilename())
                ? Path.of(file.getOriginalFilename()).getFileName().toString()
                : "upload";
        String baseName = SAFE.matcher(original.toLowerCase().replace(' ', '-')).replaceAll("");
        if (baseName.isBlank()) baseName = "upload";
        // companies.id(from db when edit comapany) + time (ms) + base_name
        String newName = Instant.now().toEpochMilli() + "-" + UUID.randomUUID() + "-" + baseName;
        if (ext != null && !newName.endsWith(ext)) newName += ext;

        Path dstDir = Path.of(props.getBaseDir(), props.folderFor(bucket));
        Files.createDirectories(dstDir);

        Path target = dstDir.resolve(newName);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        String publicUrl = "/files/" + props.folderFor(bucket) + "/" + newName;
        return new StoredFile(newName, contentType, file.getSize(), publicUrl, target.toAbsolutePath().toString());
    }
    
    // Store any file type (for task attachments)
    private StoredFile storeAnyFile(MultipartFile file, UploadBucket bucket, String contentType) throws IOException {
        String original = StringUtils.hasText(file.getOriginalFilename())
                ? Path.of(file.getOriginalFilename()).getFileName().toString()
                : "upload";
        
        // Keep original extension
        String ext = getExtensionFromOriginal(original);
        String baseName = SAFE.matcher(original.toLowerCase().replace(' ', '-')).replaceAll("");
        if (baseName.isBlank()) baseName = "upload";
        
        String newName = Instant.now().toEpochMilli() + "-" + UUID.randomUUID() + "-" + baseName;
        if (ext != null && !ext.isEmpty() && !newName.endsWith(ext)) {
            newName += ext;
        }

        Path dstDir = Path.of(props.getBaseDir(), props.folderFor(bucket));
        Files.createDirectories(dstDir);

        Path target = dstDir.resolve(newName);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        String publicUrl = "/files/" + props.folderFor(bucket) + "/" + newName;
        return new StoredFile(newName, contentType, file.getSize(), publicUrl, target.toAbsolutePath().toString());
    }

    private String safeContentType(MultipartFile file) {
        String t = file.getContentType();
        return (t != null) ? t.toLowerCase() : "application/octet-stream";
    }

    private String getExtensionFromOriginal(String original) {
        if (!StringUtils.hasText(original)) return "";
        String name = Path.of(original).getFileName().toString();
        int dot = name.lastIndexOf('.');
        return dot > -1 ? name.substring(dot).toLowerCase() : "";
    }

    public record StoredFile(String filename, String contentType, long size, String url, String path) {
    }
}
