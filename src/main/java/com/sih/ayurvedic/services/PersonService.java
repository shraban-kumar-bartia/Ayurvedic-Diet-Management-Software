package com.sih.ayurvedic.services;

import com.sih.ayurvedic.dto.LoginDto;
import com.sih.ayurvedic.dto.RegisterDto;
import com.sih.ayurvedic.dto.UpdateDto;
import com.sih.ayurvedic.models.Person;
import com.sih.ayurvedic.repositories.PersonRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PersonService implements UserDetailsService {

    private final PersonRepository personRepository;
    private final PasswordEncoder passwordEncoder;

    public PersonService(PersonRepository personRepository, PasswordEncoder passwordEncoder) {
        this.personRepository = personRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Person person = personRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        return org.springframework.security.core.userdetails.User.builder()
                .username(person.getEmail())
                .password(person.getPassword())
                .roles("USER")
                .build();
    }

    public Map<String, Object> registerUser(RegisterDto registerDto) {
        Map<String, Object> response = new HashMap<>();
        if (personRepository.findByEmail(registerDto.getEmail()).isPresent()) {
            response.put("error", "Email already exists");
            return response;
        }
        if (registerDto.getUsername() != null && personRepository.findByUsername(registerDto.getUsername()).isPresent()) {
            response.put("error", "Username already exists");
            return response;
        }
        Person person = new Person();
        person.setFirstname(registerDto.getFirstname());
        person.setLastname(registerDto.getLastname());
        person.setUsername(registerDto.getUsername());
        person.setEmail(registerDto.getEmail());
        person.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        person.setPhoneNumber(registerDto.getPhoneNumber());
        person.setCreatedAt(new Date());
        personRepository.save(person);

        response.put("message", "User registered successfully");
        response.put("user", person);
        return response;
    }

    public Map<String, Object> authenticateUser(LoginDto loginDto) {
        Map<String, Object> response = new HashMap<>();
        Optional<Person> personOptional = personRepository.findByEmail(loginDto.getEmail());

        if (personOptional.isEmpty() || !passwordEncoder.matches(loginDto.getPassword(), personOptional.get().getPassword())) {
            response.put("error", "Invalid email or password");
            return response;
        }

        Person person = personOptional.get();
        person.setLastLogin(new Date());
        personRepository.save(person);

        response.put("message", "Login successful");
        response.put("user", person);
        return response;
    }

    public Map<String, Object> updateUser(String username, UpdateDto updateDto) {
        Map<String, Object> response = new HashMap<>();
        Optional<Person> optionalPerson = personRepository.findByUsername(username);

        if (optionalPerson.isEmpty()) {
            response.put("error", "User not found");
            return response;
        }

        Person person = optionalPerson.get();
        if (updateDto.getFirstname() != null) person.setFirstname(updateDto.getFirstname());
        if (updateDto.getLastname() != null) person.setLastname(updateDto.getLastname());
        if (updateDto.getEmail() != null) person.setEmail(updateDto.getEmail());
        if (updateDto.getPassword() != null) person.setPassword(passwordEncoder.encode(updateDto.getPassword()));
        if (updateDto.getPhoneNumber() != null) person.setPhoneNumber(updateDto.getPhoneNumber());

        person.setUpdateAt(new Date());
        personRepository.save(person);

        response.put("message", "User updated successfully");
        response.put("user", person);
        return response;
    }

    // ✅ Fully working Google login/signup
    public Person createOrGetGoogleUser(String email, String firstname, String lastname) {
        Optional<Person> existingUser = personRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            Person person = existingUser.get();
            person.setLastLogin(new Date());
            return personRepository.save(person);
        } else {
            Person newUser = new Person();
            newUser.setEmail(email);
            newUser.setFirstname(firstname);
            newUser.setLastname(lastname);
            newUser.setCreatedAt(new Date());
            newUser.setLastLogin(new Date());
            newUser.setUsername("user" + new Random().nextInt(10000));
            return personRepository.save(newUser);
        }
    }
}
