package com.springboot.demo.upload;

public enum UploadBucket {
    COMPANY_LOGO, USER_AVATAR, TEMP, TASK_ATTACHMENT;

    public static UploadBucket from(String s) {
        return switch (s.toLowerCase()) {
            case "company-logo", "company_logo" -> COMPANY_LOGO;
            case "user-avatar", "user_avatar" -> USER_AVATAR;
            case "temp" -> TEMP;
            case "task-attachment", "task_attachment" -> TASK_ATTACHMENT;
            default -> throw new IllegalArgumentException("Unknown bucket: " + s);
        };
    }
}
