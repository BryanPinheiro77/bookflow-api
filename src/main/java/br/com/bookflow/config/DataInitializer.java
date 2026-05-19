package br.com.bookflow.config;

import br.com.bookflow.emprestimo.entity.Emprestimo;
import br.com.bookflow.emprestimo.entity.EmprestimoStatus;
import br.com.bookflow.emprestimo.repository.EmprestimoRepository;
import br.com.bookflow.livro.entity.Livro;
import br.com.bookflow.livro.entity.LivroStatus;
import br.com.bookflow.livro.repository.LivroRepository;
import br.com.bookflow.usuario.entity.Role;
import br.com.bookflow.usuario.entity.Usuario;
import br.com.bookflow.usuario.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final LivroRepository livroRepository;
    private final EmprestimoRepository emprestimoRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepository,
                           LivroRepository livroRepository,
                           EmprestimoRepository emprestimoRepository,
                           PasswordEncoder passwordEncoder) {

        this.usuarioRepository = usuarioRepository;
        this.livroRepository = livroRepository;
        this.emprestimoRepository = emprestimoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        if (usuarioRepository.count() > 0 || livroRepository.count() > 0) {
            return;
        }

        Usuario admin1 = criarAdmin(
                "Biblioteca Central",
                "biblioteca1@email.com"
        );

        Usuario admin2 = criarAdmin(
                "Biblioteca Zona Sul",
                "biblioteca2@bookflow.com"
        );

        Usuario usuario1 = criarUsuario(
                "João Silva",
                "usuario1@email.com"
        );

        Usuario usuario2 = criarUsuario(
                "Maria Oliveira",
                "usuario2@email.com"
        );

        /*
         * =========================
         * BIBLIOTECA 1
         * =========================
         */

        Livro cleanCode1 = livroRepository.save(
                criarLivro(
                        "Clean Code",
                        "Robert C. Martin",
                        "Tecnologia",
                        8,
                        7,
                        "12.90",
                        "1.50",
                        "https://m.media-amazon.com/images/I/41xShlnTZTL._SY445_SX342_.jpg",
                        admin1
                )
        );

        Livro hobbit = livroRepository.save(
                criarLivro(
                        "O Hobbit",
                        "J. R. R. Tolkien",
                        "Fantasia",
                        5,
                        5,
                        "8.90",
                        "1.20",
                        "https://m.media-amazon.com/images/I/91M9xPIf10L._SY466_.jpg",
                        admin1
                )
        );

        Livro domCasmurro = livroRepository.save(
                criarLivro(
                        "Dom Casmurro",
                        "Machado de Assis",
                        "Romance",
                        10,
                        9,
                        "4.50",
                        "0.80",
                        "https://m.media-amazon.com/images/I/61Z2bMhGicL._SY466_.jpg",
                        admin1
                )
        );

        Livro harryPotter = livroRepository.save(
                criarLivro(
                        "Harry Potter e a Pedra Filosofal",
                        "J. K. Rowling",
                        "Fantasia",
                        7,
                        7,
                        "9.90",
                        "1.30",
                        "https://m.media-amazon.com/images/I/81ibfYk4qmL._SY466_.jpg",
                        admin1
                )
        );

        Livro pequenoPrincipe1 = livroRepository.save(
                criarLivro(
                        "O Pequeno Príncipe",
                        "Antoine de Saint-Exupéry",
                        "Fábula",
                        4,
                        4,
                        "5.90",
                        "1.00",
                        "https://m.media-amazon.com/images/I/71XSRR7q0xL.jpg",
                        admin1
                )
        );

        /*
         * =========================
         * BIBLIOTECA 2
         * =========================
         */

        Livro cleanCode2 = livroRepository.save(
                criarLivro(
                        "Clean Code",
                        "Robert C. Martin",
                        "Tecnologia",
                        5,
                        5,
                        "13.90",
                        "1.80",
                        "https://m.media-amazon.com/images/I/41xShlnTZTL._SY445_SX342_.jpg",
                        admin2
                )
        );

        Livro pequenoPrincipe2 = livroRepository.save(
                criarLivro(
                        "O Pequeno Príncipe",
                        "Antoine de Saint-Exupéry",
                        "Fábula",
                        6,
                        6,
                        "6.90",
                        "1.00",
                        "https://m.media-amazon.com/images/I/71XSRR7q0xL.jpg",
                        admin2
                )
        );

        Livro ddd = livroRepository.save(
                criarLivro(
                        "Domain-Driven Design",
                        "Eric Evans",
                        "Tecnologia",
                        4,
                        4,
                        "15.90",
                        "2.00",
                        "https://m.media-amazon.com/images/I/51OWGtzQLLL._SY445_SX342_.jpg",
                        admin2
                )
        );

        Livro javaEfetivo = livroRepository.save(
                criarLivro(
                        "Java Efetivo",
                        "Joshua Bloch",
                        "Tecnologia",
                        8,
                        8,
                        "11.90",
                        "1.40",
                        "https://m.media-amazon.com/images/I/71lbdk3iBVL.jpg",
                        admin2
                )
        );

        Livro livro1984 = livroRepository.save(
                criarLivro(
                        "1984",
                        "George Orwell",
                        "Ficção",
                        10,
                        10,
                        "7.90",
                        "1.10",
                        "https://m.media-amazon.com/images/I/71kxa1-0mfL.jpg",
                        admin2
                )
        );

        /*
         * =========================
         * EMPRÉSTIMOS
         * =========================
         */

        // Usuário 1
        // Possui Clean Code da biblioteca 1
        // Isso demonstra que ele NÃO pode pegar o mesmo livro na biblioteca 2

        emprestimoRepository.save(
                Emprestimo.builder()
                        .usuario(usuario1)
                        .livro(cleanCode1)
                        .dataEmprestimo(LocalDate.now().minusDays(5))
                        .dataPrevistaDevolucao(LocalDate.now().plusDays(25))
                        .valorEmprestimo(cleanCode1.getValorEmprestimo())
                        .status(EmprestimoStatus.ATIVO)
                        .build()
        );

        // Usuário 2
        // Empréstimo dentro do prazo

        emprestimoRepository.save(
                Emprestimo.builder()
                        .usuario(usuario2)
                        .livro(hobbit)
                        .dataEmprestimo(LocalDate.now().minusDays(7))
                        .dataPrevistaDevolucao(LocalDate.now().plusDays(23))
                        .valorEmprestimo(hobbit.getValorEmprestimo())
                        .status(EmprestimoStatus.ATIVO)
                        .build()
        );

        // Usuário 2
        // Empréstimo atrasado para demonstrar multa

        emprestimoRepository.save(
                Emprestimo.builder()
                        .usuario(usuario2)
                        .livro(domCasmurro)
                        .dataEmprestimo(LocalDate.now().minusDays(40))
                        .dataPrevistaDevolucao(LocalDate.now().minusDays(10))
                        .valorEmprestimo(domCasmurro.getValorEmprestimo())
                        .status(EmprestimoStatus.ATIVO)
                        .build()
        );

        System.out.println("DADOS INICIAIS CRIADOS COM SUCESSO");
    }

    private Usuario criarAdmin(String nome, String email) {

        Usuario admin = Usuario.builder()
                .nome(nome)
                .email(email)
                .senha(passwordEncoder.encode("123456"))
                .role(Role.ADMIN)
                .build();

        return usuarioRepository.save(admin);
    }

    private Usuario criarUsuario(String nome, String email) {

        Usuario usuario = Usuario.builder()
                .nome(nome)
                .email(email)
                .senha(passwordEncoder.encode("123456"))
                .role(Role.USUARIO)
                .build();

        return usuarioRepository.save(usuario);
    }

    private Livro criarLivro(String titulo,
                             String autor,
                             String categoria,
                             Integer quantidadeTotal,
                             Integer quantidadeDisponivel,
                             String valorEmprestimo,
                             String valorMultaDiaria,
                             String capaUrl,
                             Usuario admin) {

        Livro livro = Livro.builder()
                .titulo(titulo)
                .autor(autor)
                .categoria(categoria)
                .quantidadeTotal(quantidadeTotal)
                .quantidadeDisponivel(quantidadeDisponivel)
                .valorEmprestimo(new BigDecimal(valorEmprestimo))
                .valorMultaDiaria(new BigDecimal(valorMultaDiaria))
                .capaUrl(capaUrl)
                .admin(admin)
                .build();

        if (quantidadeDisponivel == 0) {
            livro.setStatus(LivroStatus.INDISPONIVEL);
        } else {
            livro.setStatus(LivroStatus.DISPONIVEL);
        }

        return livro;
    }
}