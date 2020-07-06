package com.teevid.sample;

import com.teevid.sdk.constant.CameraProvider;

public interface UserPreferences {

    String getServer();

    void setServer(String server);

    String getRoomId();

    void setRoomId(String roomId);

    String getUsername();

    void setUsername(String username);

    @CameraProvider
    int getCamera();

    void setCamera(@CameraProvider int defaultCamera);
}