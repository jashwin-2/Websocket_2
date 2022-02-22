package com.zoho.vtouch.logging_agent;

public interface WebSocketCallback {
    void onError(Exception ex);
    void onMessageReceived(String message);
}
