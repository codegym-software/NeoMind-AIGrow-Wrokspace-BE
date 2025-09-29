package com.springboot.demo.upload;

public enum UploadBucket {
    COMPANY_LOGO, USER_AVATAR, TEMP;

    public static UploadBucket from(String s) {
        return switch (s.toLowerCase()) {
            case "company-logo", "company_logo" -> COMPANY_LOGO;
            case "user-avatar", "user_avatar" -> USER_AVATAR;
            case "temp" -> TEMP;
            default -> throw new IllegalArgumentException("Unknown bucket: " + s);
        };
    }
}
