package com.university.ums.controller;

import com.university.ums.model.Person;
import com.university.ums.service.PersonService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

public abstract class PersonController<T extends Person> {

    private final PersonService<T> service;

    protected PersonController(PersonService<T> service) {
        this.service = service;
    }

    @GetMapping
    public List<T> all() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public T one(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<T> create(@Valid @RequestBody T body) {
        T saved = service.create(body);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location).body(saved);
    }

    @PutMapping("/{id}")
    public T update(@PathVariable Long id, @Valid @RequestBody T body) {
        return service.update(id, body);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}