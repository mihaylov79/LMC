package lmc.user.service;

import lmc.user.model.User;
import lmc.user.model.UserStatus;
import lmc.user.repository.UserRepository;
import lmc.web.dto.newUserRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User getUserById(UUID userId){
        return userRepository.findById(userId)
                .orElseThrow(()->new RuntimeException("Потребител с идентификатор : [%s] не беше открит"
                        .formatted(userId)));
    }

    public User getUserByEmail(String email){
        return userRepository.findByEmail(email)
                .orElseThrow(()->new RuntimeException("Потребител с мейл: %s не беще открит"
                        .formatted(email)));
    }

    public User addNewUser(newUserRequest request){
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());

        if (existingUser.isPresent()){
            throw new RuntimeException("Потребител с мейл: %s вече съществува!".formatted(request.getEmail()));
        }

        User newUser = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .userRole(request.getRole())
                .status(UserStatus.ACTIVE)
                .build();

        return userRepository.save(newUser);

        //TODO паролата трябва да се криптира и да се добавят още проверки!
    }
}
