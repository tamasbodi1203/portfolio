package hu.portfoliotracker.Configuration;

import hu.portfoliotracker.Model.Role;
import hu.portfoliotracker.Model.User;
import hu.portfoliotracker.Repository.RoleRepository;
import hu.portfoliotracker.Repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Slf4j
public class UserDataSetup implements ApplicationListener<ContextRefreshedEvent> {

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private PasswordEncoder passwordEncoder;

    boolean alreadySetup = false;

    @Autowired
    public UserDataSetup(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (alreadySetup) {
            return;
        }

        val adminRole = createRoleIfNotExists("ADMIN");
        val userRole = createRoleIfNotExists("USER");

        if (userRepository.findByEmail("test@test.com") == null) {
            val user = new User();
            user.setIsEnabled(true);
            user.setRoles(List.of(adminRole));
            user.setPassword(passwordEncoder.encode("asdqwe"));
            user.setUsername("test");
            user.setEmail("test@test.com");

            userRepository.save(user);

            alreadySetup = true;
            log.info("test user saved");
        }

    }

    @Transactional
    Role createRoleIfNotExists(String name) {
        Role role = roleRepository.findByName(name);

        if (role == null) {
            role = new Role(name);
            roleRepository.save(role);
        }

        return role;
    }

}