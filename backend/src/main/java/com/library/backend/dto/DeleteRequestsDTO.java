package com.library.backend.dto;

import lombok.Data;

@Data
public class DeleteRequestsDTO {
    private String id;
    private String doi;

    public DeleteRequestsDTO(String id, String doi) {
        this.id = id;
        this.doi = doi;
    }
}
