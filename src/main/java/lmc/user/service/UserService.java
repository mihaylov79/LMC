package lmc.user.service;

import lmc.security.CustomUserDetails;
import lmc.user.model.User;
import lmc.user.model.UserStatus;
import lmc.user.repository.UserRepository;
import lmc.web.dto.NewPasswordRequest;
import lmc.web.dto.NewUserRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {

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

    public User addNewUser(NewUserRequest request){
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

    public User changePassword(NewPasswordRequest request) {

        if (!request.getPassword().equals(request.getConfirmPassword())){
            throw new RuntimeException("Потвърждението на паролата не съвпада!");
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = getUserByEmail(email);

        currentUser = currentUser.toBuilder()
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        return userRepository.save(currentUser);

    }

    public User resetUserPassword(UUID userId, NewPasswordRequest request){
        User user = getUserById(userId);

        if (!request.getPassword().equals(request.getConfirmPassword())){
            throw new RuntimeException("Потвърждението на паролата не съвпада!");
        }

        user = user.toBuilder()
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        return userRepository.save(user);

    }

    public List<User> getAllActiveUsers(){
        return userRepository.findAllByStatus(UserStatus.ACTIVE, Sort.by("firstName", "lastName"));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username).orElseThrow(()->
                new UsernameNotFoundException("Потребител с мейл: %s не съществува".formatted(username)));

        return new CustomUserDetails (
                                    user.getUserRole(),
                                    user.getEmail(),
                                    user.getPassword(),
                                    user.getStatus(),
                                    user.getId()
        );



    }
}
