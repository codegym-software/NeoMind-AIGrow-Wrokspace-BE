package com.springboot.demo.upload;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConfigurationProperties(prefix = "file.storage")
public class StorageProperties {
    private String baseDir;
    private Map<String, String> buckets; // key: company-logo, user-avatar, temp

    public String getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    public Map<String, String> getBuckets() {
        return buckets;
    }

    public void setBuckets(Map<String, String> buckets) {
        this.buckets = buckets;
    }

    public String folderFor(UploadBucket bucket) {
        return buckets.get(switch (bucket) {
            case COMPANY_LOGO -> "company-logo";
            case USER_AVATAR -> "user-avatar";
            case TEMP -> "temp";
            case TASK_ATTACHMENT -> "task-attachment";
        });
    }
}
