package com.example.local_server;


import android.content.res.AssetManager;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.local_server.model.SessionDetails;
import com.example.local_server.model.JsonData;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class WebSocketHandler {
    AssetManager mAssets;
    public SessionDetails sessionDetails;
    public WebSocketCallback callback;
    private OutputStream out;
    public ArrayBlockingQueue<String> messages = new ArrayBlockingQueue<>(20);

    public void setCallback(WebSocketCallback callback) {
        this.callback = callback;
    }

    public Boolean isClientConnected = false;


    public WebSocketHandler(AssetManager mAssets, SessionDetails data) {
        this.mAssets = mAssets;
        this.sessionDetails = data;
    }

    public void handle(@NonNull Socket client) {
        try {

            InputStream in = client.getInputStream();
            out = client.getOutputStream();
            Scanner s = new Scanner(in, "UTF-8");

            try {
                String data = s.useDelimiter("\\r\\n\\r\\n").next();
                Matcher get = Pattern.compile("^GET").matcher(data);


                if (get.find()) {
                    Matcher match = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(data);
                    match.find();
                    String key = SHA1(match.group(1) + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11");

                    byte[] response = ("HTTP/1.1 101 Switching Protocols\r\n"
                            + "Connection: Upgrade\r\n"
                            + "Upgrade: websocket\r\n"
                            + "Sec-WebSocket-Accept: "
                            + key
                            + "\r\n\r\n").getBytes(StandardCharsets.UTF_8);

                    out.write(response, 0, response.length);
                    JsonData data1 = new JsonData(JsonData.INITIAL_DATA,0,new Gson().toJson(this.sessionDetails));
                    out.write(encode(new Gson().toJson(data1)));
                    isClientConnected = true;
                    activateWriter();
                    printInputStream(in);


                }
            } finally {
                Log.d("WebSocket", "Closed");
                isClientConnected = false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            callback.onError(e);
        } finally {
            Log.d("WebSocket", "Closed");        }


    }

    private void printInputStream(InputStream inputStream) throws IOException {
        byte[] b = new byte[8000];//incoming buffer
        byte[] message = null;//buffer to assemble message in
        byte[] masks = new byte[4];
        boolean isSplit = false;//has a message been split over a read
        int length = 0; //length of message
        int totalRead = 0; //total read in message so far
        while (true) {
            int len = 0;//length of bytes read from socket
            try {
                len = inputStream.read(b);
            } catch (IOException e) {
                break;
            }
            if (len != -1) {
                boolean more = false;
                int totalLength = 0;
                do {
                    int j = 0;
                    int i = 0;
                    if (!isSplit) {
                        byte rLength = 0;
                        int rMaskIndex = 2;
                        int rDataStart = 0;
                        // b[0] assuming text
                        byte data = b[1];
                        byte op = (byte) 127;
                        rLength = (byte) (data & op);
                        length = (int) rLength;
                        if (rLength == (byte) 126) {
                            rMaskIndex = 4;
                            length = ((int) b[2]) & 0xff << 8;
                            length += ((int) b[3]) & 0xff;
                        } else if (rLength == (byte) 127)
                            rMaskIndex = 10;
                        for (i = rMaskIndex; i < (rMaskIndex + 4); i++) {
                            masks[j] = b[i];
                            j++;
                        }

                        rDataStart = rMaskIndex + 4;

                        message = new byte[length];
                        totalLength = length + rDataStart;
                        for (i = rDataStart, totalRead = 0; i < len && i < totalLength; i++, totalRead++) {
                            message[totalRead] = (byte) (b[i] ^ masks[totalRead % 4]);
                        }

                    } else {
                        for (i = 0; i < len && totalRead < length; i++, totalRead++) {
                            message[totalRead] = (byte) (b[i] ^ masks[totalRead % 4]);
                        }
                        totalLength = i;
                    }


                    if (totalRead < length) {
                        isSplit = true;
                    } else {
                        isSplit = false;
                        callback.onMessageReceived(new String(message));
                        b = new byte[8000];
                    }

                    if (totalLength < len) {
                        more = true;
                        for (i = totalLength, j = 0; i < len; i++, j++)
                            b[j] = b[i];
                        len = len - totalLength;
                    } else
                        more = false;
                } while (more);
            } else
                break;
        }

    }


    public byte[] encode(@NonNull String mess) throws IOException {
        byte[] rawData = mess.getBytes();

        int frameCount = 0;
        byte[] frame = new byte[10];

        frame[0] = (byte) 129;

        if (rawData.length <= 125) {
            frame[1] = (byte) rawData.length;
            frameCount = 2;
        } else if (rawData.length >= 126 && rawData.length <= 65535) {
            frame[1] = (byte) 126;
            int len = rawData.length;
            frame[2] = (byte) ((len >> 8) & (byte) 255);
            frame[3] = (byte) (len & (byte) 255);
            frameCount = 4;
        } else {
            frame[1] = (byte) 127;
            long len = rawData.length; //note an int is not big enough in java
            frame[2] = (byte) ((len >> 56) & (byte) 255);
            frame[3] = (byte) ((len >> 48) & (byte) 255);
            frame[4] = (byte) ((len >> 40) & (byte) 255);
            frame[5] = (byte) ((len >> 32) & (byte) 255);
            frame[6] = (byte) ((len >> 24) & (byte) 255);
            frame[7] = (byte) ((len >> 16) & (byte) 255);
            frame[8] = (byte) ((len >> 8) & (byte) 255);
            frame[9] = (byte) (len & (byte) 255);
            frameCount = 10;
        }

        int bLength = frameCount + rawData.length;

        byte[] reply = new byte[bLength];

        int bLim = 0;
        for (int i = 0; i < frameCount; i++) {
            reply[bLim] = frame[i];
            bLim++;
        }
        for (int i = 0; i < rawData.length; i++) {
            reply[bLim] = rawData[i];
            bLim++;
        }

        return reply;
    }

    public void activateWriter(){

        new Thread(() -> {
            try {
                while (isClientConnected) {
                    try {
                            out.write(encode(messages.take()));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        callback.onError(e);
                    }
                }


            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();

    }

    private static String SHA1(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(text.getBytes(StandardCharsets.ISO_8859_1), 0, text.length());
            byte[] sha1hash = md.digest();
            return android.util.Base64.encodeToString(sha1hash, android.util.Base64.NO_WRAP);
        } catch (Exception ex) {
            return null;
        }
    }

}
