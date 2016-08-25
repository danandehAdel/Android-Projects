package com.example.response;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class HTTPResponse {
    @SerializedName("conditions")
    @Expose
    public Conditions conditions;
    @SerializedName("result")
    @Expose
    public String result;
}
