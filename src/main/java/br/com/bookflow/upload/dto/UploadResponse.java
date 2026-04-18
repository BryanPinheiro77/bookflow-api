package br.com.bookflow.upload.dto;

public record UploadResponse(
        String fileName,
        String fileUrl
) {
}