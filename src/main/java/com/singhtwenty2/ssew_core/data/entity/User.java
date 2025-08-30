package com.singhtwenty2.ssew_core.data.entity;

import com.singhtwenty2.ssew_core.data.enums.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_username", columnList = "username", unique = true),
                @Index(name = "idx_email", columnList = "email"),
                @Index(name = "idx_phone_number", columnList = "phone_number", unique = true)
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity implements UserDetails {

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "mobile_number", nullable = false, unique = true, length = 15)
    private String mobileNumber;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role = UserRole.USER;

    @Column(name = "is_email_verified", nullable = false)
    private Boolean isEmailVerified = false;

    @Column(name = "is_mobile_verified", nullable = false)
    private Boolean isMobileVerified = false;

    @Column(name = "is_account_locked", nullable = false)
    private Boolean isAccountLocked = false;

    @Column(name = "failed_login_attempts", nullable = false)
    private Integer failedLoginAttempts = 0;

    @Column(name = "last_login_time")
    private LocalDateTime lastLoginTime;

    @Column(name = "last_password_change")
    private LocalDateTime lastPasswordChange;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return mobileNumber;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !isAccountLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        if (lastPasswordChange == null) {
            return true;
        }
        return lastPasswordChange.isAfter(LocalDateTime.now().minusDays(90));
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }

    public void setPassword(String newPasswordHash) {
        this.password = newPasswordHash;
        this.lastPasswordChange = LocalDateTime.now();
        this.failedLoginAttempts = 0;
    }

    public void recordSuccessfulLogin() {
        this.lastLoginTime = LocalDateTime.now();
    }

    public void recordFailedLogin() {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= 15) {
            this.isAccountLocked = true;
        }
    }

    public boolean isFullyVerified() {
        return isEmailVerified && isMobileVerified;
    }

    public boolean canLogin() {
        // Uncomment the below line if you want to enforce email or mobile verification for login
        // As per CLIENT'S requirement, commenting it out
        // return isActive && !isAccountLocked && (isEmailVerified || isMobileVerified);
        return isActive && !isAccountLocked;
    }
}