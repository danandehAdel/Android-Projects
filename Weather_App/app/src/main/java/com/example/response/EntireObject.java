package com.example.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class EntireObject {

        @SerializedName("response")
        @Expose
        public HTTPResponse response;

}
