package com.movies.Movies.Controller;

import com.movies.Movies.dto.ActorDTO;

import jakarta.validation.Valid;

import com.movies.Movies.Service.ActorService;
import org.springframework.validation.BindingResult;  // NEW: for handling validation errors
import org.springframework.validation.FieldError;  // NEW: to extract field errors
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/actors")
public class ActorController {

    private final ActorService actorService;
    

    public ActorController(ActorService actorService) {
        this.actorService = actorService;
    }

    
    @GetMapping
    public ResponseEntity<Page<ActorDTO>> getAllActors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ActorDTO> actorPage = actorService.getAllActors(pageable);
        return ResponseEntity.ok(actorPage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActorDTO> getActorById(@PathVariable Long id) {
        return ResponseEntity.ok(actorService.getActorById(id));
    }

    @GetMapping(params = "name")
    public List<ActorDTO> getActorsByName(@RequestParam String name) {
        return actorService.getActorsByName(name);
    }

    @PostMapping
    public ResponseEntity<Object> createActor(@Valid @RequestBody ActorDTO actorDTO, BindingResult bindingResult) {  // NEW: binding result added
        if (bindingResult.hasErrors()) {  // NEW: check for validation errors
            // Collect validation error messages
            StringBuilder errorMessage = new StringBuilder("Validation failed: ");
            for (FieldError fieldError : bindingResult.getFieldErrors()) {  // NEW: loop through field errors
                errorMessage.append(fieldError.getField())  // NEW: get the field name
                            .append(" - ")
                            .append(fieldError.getDefaultMessage())  // NEW: get the error message
                            .append("; ");
            }
            // Return a 400 Bad Request with the error message
            return new ResponseEntity<>(errorMessage.toString(), HttpStatus.BAD_REQUEST);  // NEW: returning 400 with error details
        }

        ActorDTO createdActor = actorService.createActor(actorDTO);
        return new ResponseEntity<>(createdActor, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ActorDTO> updateActor(@PathVariable Long id, @RequestBody ActorDTO actorDTO) {
        return ResponseEntity.ok(actorService.updateActor(id, actorDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteActor(@PathVariable Long id, @RequestParam(defaultValue = "false") boolean force) {
        actorService.deleteActor(id, force);
        return ResponseEntity.noContent().build();
    }
}
