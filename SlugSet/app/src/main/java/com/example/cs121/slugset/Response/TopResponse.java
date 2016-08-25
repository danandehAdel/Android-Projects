package com.example.cs121.slugset.Response;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


//Generated ("org.jsonschema2pojo")
public class TopResponse {

    @SerializedName("gameID")
    @Expose
    public Integer gameID;
    @SerializedName("found_sets")
    @Expose
    public List<String> foundSets = new ArrayList<String>();
    @SerializedName("response")
    @Expose
    public String response;
    @SerializedName("shuffled_deck")
    @Expose
    public List<ShuffledDeck> shuffledDeck = new ArrayList<ShuffledDeck>();
    @SerializedName("countsets")
    @Expose
    public Integer countsets;

}