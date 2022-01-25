package com.example.local_server;

public interface WebSocketCallback {
    void onError(Exception ex);
    void onMessageReceived(String message);
}
