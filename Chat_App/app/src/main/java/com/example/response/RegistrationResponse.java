package com.example;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RegistrationResponse {

    @SerializedName("response")
    @Expose
    public String response;
    @SerializedName("nickname")
    @Expose
    public String nickname;

}
