package com.ecommerce.user_service.kafka;

public final class KafkaTopics {

    private KafkaTopics() {}

    public static final String USER_REGISTERED      = "user.registered";
    public static final String USER_UPDATED         = "user.updated";
    public static final String USER_DELETED         = "user.deleted";
    public static final String USER_STATUS_CHANGED  = "user.status-changed";
    public static final String USER_PASSWORD_CHANGED = "user.password-changed";
    public static final String USER_LOGIN_SUCCESS   = "user.login-success";
    public static final String USER_LOGIN_FAILED    = "user.login-failed";
    public static final String USER_ROLE_ASSIGNED   = "user.role-assigned";
}