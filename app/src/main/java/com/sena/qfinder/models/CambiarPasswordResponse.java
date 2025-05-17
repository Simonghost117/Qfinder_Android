package com.sena.qfinder.models;

import com.google.gson.annotations.SerializedName;

public class CambiarPasswordResponse {
    @SerializedName("message")
    private String message;

    @SerializedName("success")
    private boolean success;

    public String getMessage() {
        return message;
    }

    public boolean isSuccess() {
        return success;
    }
}