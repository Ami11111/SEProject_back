package com.library.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.library.backend.entity.PM_Paper;
import com.sun.istack.NotNull;
import lombok.Data;
import lombok.NonNull;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Map;

@Data
public class PaperDTO {
    @NotNull
    @JsonProperty("DOI")
    private String DOI;
    private String title;
    private ArrayList<String> firstAuthor;
    private ArrayList<String> secondAuthor;
    private ArrayList<String> thirdAuthor;
    @JsonProperty("CCF")
    private String CCF;
    private String status;
    private String recommend;
    ArrayList<Map<String, String>> additional;
    private String url;

    public PaperDTO() {
    }
}