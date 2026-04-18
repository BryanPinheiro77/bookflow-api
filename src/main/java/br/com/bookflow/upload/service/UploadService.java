package br.com.bookflow.upload.service;

import br.com.bookflow.exception.RegraDeNegocioException;
import br.com.bookflow.upload.dto.UploadResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class UploadService {

    private final Path uploadRootPath;

    public UploadService(@Value("${app.upload.dir}") String uploadDir) {
        this.uploadRootPath = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    public UploadResponse uploadCapaLivro(MultipartFile file) {
        validarArquivo(file);

        try {
            Path pastaCapas = uploadRootPath.resolve("capas");
            Files.createDirectories(pastaCapas);

            String originalFilename = file.getOriginalFilename();
            String extensao = extrairExtensao(originalFilename);
            String novoNomeArquivo = UUID.randomUUID() + "." + extensao;

            Path destino = pastaCapas.resolve(novoNomeArquivo);
            file.transferTo(destino.toFile());

            String fileUrl = "/uploads/capas/" + novoNomeArquivo;

            return new UploadResponse(novoNomeArquivo, fileUrl);

        } catch (IOException e) {
            throw new RegraDeNegocioException("Erro ao salvar arquivo de imagem.");
        }
    }

    public void removerArquivo(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return;
        }

        if (!fileUrl.startsWith("/uploads/")) {
            return;
        }

        try {
            String caminhoRelativo = fileUrl.replaceFirst("/uploads/", "");
            Path arquivo = uploadRootPath.resolve(caminhoRelativo).normalize();
            Files.deleteIfExists(arquivo);
        } catch (IOException e) {
            throw new RegraDeNegocioException("Erro ao remover arquivo de imagem.");
        }
    }

    private void validarArquivo(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RegraDeNegocioException("O arquivo de imagem é obrigatório.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !isImagemValida(contentType)) {
            throw new RegraDeNegocioException("Apenas arquivos JPG, JPEG e PNG são permitidos.");
        }
    }

    private boolean isImagemValida(String contentType) {
        return contentType.equals("image/jpeg")
                || contentType.equals("image/jpg")
                || contentType.equals("image/png");
    }

    private String extrairExtensao(String nomeArquivo) {
        if (nomeArquivo == null || !nomeArquivo.contains(".")) {
            throw new RegraDeNegocioException("Arquivo com extensão inválida.");
        }

        return nomeArquivo.substring(nomeArquivo.lastIndexOf('.') + 1);
    }
}