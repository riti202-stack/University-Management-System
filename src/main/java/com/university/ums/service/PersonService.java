package com.university.ums.service;

import com.university.ums.exception.ApiException;
import com.university.ums.model.Person;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

public abstract class PersonService<T extends Person> {

    private final JpaRepository<T, Long> repo;
    private final PasswordEncoder encoder;
    private final UserLookup lookup;
    private final String label;

    protected PersonService(JpaRepository<T, Long> repo, PasswordEncoder encoder, UserLookup lookup, String label) {
        this.repo = repo;
        this.encoder = encoder;
        this.lookup = lookup;
        this.label = label;
    }

    protected abstract void copyExtraFields(T target, T source);

    public List<T> findAll() {
        return repo.findAll();
    }

    public T findById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> ApiException.notFound(label + " " + id + " not found"));
    }

    public T create(T person) {
        checkPassword(person.getPassword());
        if (lookup.exists(person.getUsername())) {
            throw ApiException.conflict("Username already taken");
        }
        person.setId(null);
        person.setPassword(encoder.encode(person.getPassword()));
        return repo.save(person);
    }

    public T update(Long id, T in) {
        T existing = findById(id);
        if (!existing.getUsername().equals(in.getUsername()) && lookup.exists(in.getUsername())) {
            throw ApiException.conflict("Username already taken");
        }
        existing.setName(in.getName());
        existing.setEmail(in.getEmail());
        existing.setUsername(in.getUsername());
        if (in.getPassword() != null && !in.getPassword().isBlank()) {
            checkPassword(in.getPassword());
            existing.setPassword(encoder.encode(in.getPassword()));
        }
        copyExtraFields(existing, in);
        return repo.save(existing);
    }

    public void delete(Long id) {
        repo.delete(findById(id));
    }

    private void checkPassword(String password) {
        if (password == null || password.length() < 6) {
            throw ApiException.badRequest("Password must be at least 6 characters");
        }
    }
}