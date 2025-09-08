package com.sih.ayurvedic.controllers;


import com.sih.ayurvedic.dto.LoginDto;
import com.sih.ayurvedic.dto.RegisterDto;
import com.sih.ayurvedic.dto.UpdateDto;
import com.sih.ayurvedic.models.Person;
import com.sih.ayurvedic.repositories.PersonRepository;
import com.sih.ayurvedic.services.JwtService;
import com.sih.ayurvedic.services.PersonService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin("http://localhost:3000")
public class AuthController {

    private final PersonService personService;
    private final PersonRepository personRepository;
    private final JwtService jwtService;

    public AuthController(PersonService personService, PersonRepository personRepository, JwtService jwtService) {
        this.personService = personService;
        this.personRepository = personRepository;
        this.jwtService = jwtService;
    }

    // Manual registration
    @PostMapping("/register")
    public ResponseEntity<Object> register(@Valid @RequestBody RegisterDto registerDto, BindingResult result) {
        if (result.hasErrors()) {
            var errorMap = result.getAllErrors().stream()
                    .collect(Collectors.toMap(
                            error -> ((FieldError) error).getField(),
                            ObjectError::getDefaultMessage
                    ));
            return ResponseEntity.badRequest().body(errorMap);
        }

        Map<String, Object> response = personService.registerUser(registerDto);
        if (response.containsKey("error")) {
            return ResponseEntity.badRequest().body(response);
        }

        Integer id = (Integer) response.get("id");
        String token = jwtService.generateSignedToken(registerDto.getEmail(), id);
        response.put("token", token);
        return ResponseEntity.ok(response);
    }

    // Manual login
    @PostMapping("/login")
    public ResponseEntity<Object> login(@Valid @RequestBody LoginDto loginDto, BindingResult result) {
        if (result.hasErrors()) {
            var errorMap = result.getAllErrors().stream()
                    .collect(Collectors.toMap(
                            error -> ((FieldError) error).getField(),
                            ObjectError::getDefaultMessage
                    ));
            return ResponseEntity.badRequest().body(errorMap);
        }

        Map<String, Object> response = personService.authenticateUser(loginDto);
        if (response.containsKey("error")) {
            return ResponseEntity.badRequest().body(response);
        }

        Integer id = (Integer) response.get("id");
        String token = jwtService.generateSignedToken(loginDto.getEmail(), id);
        response.put("token", token);
        return ResponseEntity.ok(response);
    }

    // OAuth2 (Google) success handler — defaultSuccessUrl redirects here
    @GetMapping("/loginSuccess")
    public ResponseEntity<Object> loginSuccess(Authentication authentication) {
        Map<String, Object> resp = new HashMap<>();

        if (authentication != null && authentication.getPrincipal() instanceof OAuth2User oauthUser) {
            String email = oauthUser.getAttribute("email");
            String givenName = oauthUser.getAttribute("given_name");
            String familyName = oauthUser.getAttribute("family_name");
            if (givenName == null && oauthUser.getAttribute("name") != null) {
                String[] parts = oauthUser.getAttribute("name").toString().split(" ", 2);
                givenName = parts[0];
                familyName = parts.length > 1 ? parts[1] : "";
            }

            Person p = personService.createOrGetGoogleUser(email, givenName, familyName);
            String token = jwtService.generateSignedToken(email, p.getId());

            resp.put("id", p.getId());
            resp.put("email", p.getEmail());
            resp.put("name", p.getFirstname() + " " + p.getLastname());
            resp.put("token", token);
            resp.put("message", "Google login/signup successful");
            return ResponseEntity.ok(resp);
        }

        resp.put("error", "OAuth2 authentication failed");
        return ResponseEntity.status(401).body(resp);
    }

    // Update user
    @PutMapping("/update/{username}")
    public ResponseEntity<Map<String, Object>> updateUser(@PathVariable String username, @RequestBody UpdateDto updateDto) {
        Map<String, Object> response = personService.updateUser(username, updateDto);
        if (response.containsKey("error")) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    // Simple endpoint - get user by id (protected)
    @GetMapping("/{id}")
    public ResponseEntity<Object> person(@PathVariable Integer id, Authentication auth) {
        var response = new HashMap<String, Object>();
        Optional<Person> personOptional = personRepository.findById(id);
        if (personOptional.isPresent()) {
            Person person = personOptional.get();
            response.put("Id", person.getId());
            response.put("Authorities", auth == null ? null : auth.getAuthorities());
            response.put("Person", person);
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
