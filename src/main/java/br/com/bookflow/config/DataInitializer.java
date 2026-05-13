package br.com.bookflow.config;

import br.com.bookflow.livro.entity.Livro;
import br.com.bookflow.livro.entity.LivroStatus;
import br.com.bookflow.livro.repository.LivroRepository;
import br.com.bookflow.usuario.entity.Role;
import br.com.bookflow.usuario.entity.Usuario;
import br.com.bookflow.usuario.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final LivroRepository livroRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepository,
                           LivroRepository livroRepository,
                           PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.livroRepository = livroRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        Usuario admin = criarAdminPadrao();
        criarUsuarioPadrao();
        criarLivrosPadrao(admin);
    }

    private Usuario criarAdminPadrao() {
        return usuarioRepository.findByEmail("admin@email.com")
                .orElseGet(() -> {
                    Usuario admin = Usuario.builder()
                            .nome("Administrador")
                            .email("admin@email.com")
                            .senha(passwordEncoder.encode("123456"))
                            .role(Role.ADMIN)
                            .build();

                    System.out.println("ADMIN PADRÃO CRIADO");
                    return usuarioRepository.save(admin);
                });
    }

    private void criarUsuarioPadrao() {
        if (!usuarioRepository.existsByEmail("usuario@email.com")) {
            Usuario usuario = Usuario.builder()
                    .nome("Usuário")
                    .email("usuario@email.com")
                    .senha(passwordEncoder.encode("123456"))
                    .role(Role.USUARIO)
                    .build();

            usuarioRepository.save(usuario);
            System.out.println("USUÁRIO PADRÃO CRIADO");
        }
    }

    private void criarLivrosPadrao(Usuario admin) {
        if (livroRepository.count() > 0) {
            return;
        }

        livroRepository.save(Livro.builder()
                .titulo("Dom Casmurro")
                .autor("Machado de Assis")
                .categoria("Romance")
                .status(LivroStatus.DISPONIVEL)
                .capaUrl("https://m.media-amazon.com/images/I/61Z2bMhGicL._SY466_.jpg")
                .admin(admin)
                .build());

        livroRepository.save(Livro.builder()
                .titulo("O Pequeno Príncipe")
                .autor("Antoine de Saint-Exupéry")
                .categoria("Fábula")
                .status(LivroStatus.DISPONIVEL)
                .capaUrl("https://m.media-amazon.com/images/I/81tmcZcW4QL._SY466_.jpg")
                .admin(admin)
                .build());

        livroRepository.save(Livro.builder()
                .titulo("Clean Code")
                .autor("Robert C. Martin")
                .categoria("Tecnologia")
                .status(LivroStatus.DISPONIVEL)
                .capaUrl("https://m.media-amazon.com/images/I/41xShlnTZTL._SY445_SX342_.jpg")
                .admin(admin)
                .build());

        livroRepository.save(Livro.builder()
                .titulo("Harry Potter e a Pedra Filosofal")
                .autor("J. K. Rowling")
                .categoria("Fantasia")
                .status(LivroStatus.DISPONIVEL)
                .capaUrl("https://m.media-amazon.com/images/I/81ibfYk4qmL._SY466_.jpg")
                .admin(admin)
                .build());

        livroRepository.save(Livro.builder()
                .titulo("O Hobbit")
                .autor("J. R. R. Tolkien")
                .categoria("Fantasia")
                .status(LivroStatus.DISPONIVEL)
                .capaUrl("https://m.media-amazon.com/images/I/91M9xPIf10L._SY466_.jpg")
                .admin(admin)
                .build());

        System.out.println("LIVROS PADRÃO CRIADOS");
    }
}