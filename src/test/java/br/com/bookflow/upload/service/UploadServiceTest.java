package br.com.bookflow.upload.service;

import br.com.bookflow.exception.RegraDeNegocioException;
import br.com.bookflow.upload.dto.UploadResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class UploadServiceTest {

    @TempDir
    Path tempDir;

    private UploadService uploadService;

    @BeforeEach
    void setUp() {
        uploadService = new UploadService(tempDir.toString());
    }

    @Test
    void deveFazerUploadDeCapaComSucesso() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "capa.jpg",
                "image/jpeg",
                "conteudo fake".getBytes()
        );

        UploadResponse response = uploadService.uploadCapaLivro(file);

        assertNotNull(response);
        assertNotNull(response.fileName());
        assertNotNull(response.fileUrl());

        assertTrue(response.fileName().endsWith(".jpg"));
        assertTrue(response.fileUrl().startsWith("/uploads/capas/"));

        Path pastaCapas = tempDir.resolve("capas");
        Path arquivoSalvo = pastaCapas.resolve(response.fileName());

        assertTrue(Files.exists(arquivoSalvo));
    }

    @Test
    void deveLancarExcecaoQuandoArquivoForNulo() {
        RegraDeNegocioException exception = assertThrows(
                RegraDeNegocioException.class,
                () -> uploadService.uploadCapaLivro(null)
        );

        assertEquals("O arquivo de imagem é obrigatório.", exception.getMessage());
    }

    @Test
    void deveLancarExcecaoQuandoArquivoEstiverVazio() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "capa.jpg",
                "image/jpeg",
                new byte[0]
        );

        RegraDeNegocioException exception = assertThrows(
                RegraDeNegocioException.class,
                () -> uploadService.uploadCapaLivro(file)
        );

        assertEquals("O arquivo de imagem é obrigatório.", exception.getMessage());
    }

    @Test
    void deveLancarExcecaoQuandoTipoDeArquivoForInvalido() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "arquivo.pdf",
                "application/pdf",
                "conteudo fake".getBytes()
        );

        RegraDeNegocioException exception = assertThrows(
                RegraDeNegocioException.class,
                () -> uploadService.uploadCapaLivro(file)
        );

        assertEquals("Apenas arquivos JPG, JPEG e PNG são permitidos.", exception.getMessage());
    }

    @Test
    void deveLancarExcecaoQuandoArquivoNaoTiverExtensaoValida() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "arquivo_sem_extensao",
                "image/jpeg",
                "conteudo fake".getBytes()
        );

        RegraDeNegocioException exception = assertThrows(
                RegraDeNegocioException.class,
                () -> uploadService.uploadCapaLivro(file)
        );

        assertEquals("Arquivo com extensão inválida.", exception.getMessage());
    }
}