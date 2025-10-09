package lmc.security;

import lmc.user.model.UserRole;
import lmc.user.model.UserStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    private final UserRole role;
    private final String email;
    private final String password;
    private final UserStatus userStatus;

    public CustomUserDetails(UserRole role, String email, String password, UserStatus userStatus) {
        this.role = role;
        this.email = email;
        this.password = password;
        this.userStatus = userStatus;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role.name());
        return List.of(authority);
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.email;
    }
    @Override
    public boolean isEnabled() {
        return userStatus == UserStatus.ACTIVE;
    }
}
