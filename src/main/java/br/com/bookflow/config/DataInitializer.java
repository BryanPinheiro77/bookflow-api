package br.com.bookflow.config;

import br.com.bookflow.usuario.entity.Role;
import br.com.bookflow.usuario.entity.Usuario;
import br.com.bookflow.usuario.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepository,
                           PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        if (!usuarioRepository.existsByEmail("admin@email.com")) {

            Usuario admin = Usuario.builder()
                    .nome("Administrador")
                    .email("admin@email.com")
                    .senha(passwordEncoder.encode("123456"))
                    .role(Role.ADMIN)
                    .build();

            usuarioRepository.save(admin);

            System.out.println("ADMIN PADRÃO CRIADO");
        }

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
}