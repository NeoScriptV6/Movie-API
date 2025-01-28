package com.movies.Movies.Service;

import com.movies.Movies.dto.ActorDTO;
import com.movies.Movies.Entity.Actor;
import com.movies.Movies.exception.ResourceAlreadyExistsException;
import com.movies.Movies.exception.ResourceNotFoundException;
import com.movies.Movies.Repository.ActorRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class responsible for managing Actor entities.
 * Provides methods to perform CRUD operations on actors.
 */
@Service
public class ActorService {

    /**
     * Repository interface for Actor entities, providing data access methods.
     * This is used to interact with the database for Actor-related operations.
     */
    private final ActorRepository actorRepository;

    /**
     * Constructor for ActorService, injecting the ActorRepository dependency.
     * Ensures that ActorService has access to the necessary data access methods.
     *
     * @param actorRepository the repository for Actor entities
     */
    public ActorService(ActorRepository actorRepository) {
        this.actorRepository = actorRepository;
    }

    /**
     * Retrieves a list of all actors from the repository.
     * Logs each actor's name and birth date to the console for debugging or informational purposes.
     *
     * @return a list of ActorDTO objects representing all actors
     */
    @Transactional(readOnly = true)
    public Page<ActorDTO> getAllActors(Pageable pageable) {
        validatePagination(pageable);
        return actorRepository.findAll(pageable)
                                .map(this::mapToDTO);
    }

    private void validatePagination(Pageable pageable) {
        if (pageable.getPageNumber() < 0) {
            throw new IllegalArgumentException("Invalid page parameters: page number can't be < 0");
        }
        if (pageable.getPageSize() > 100) {
            throw new IllegalArgumentException("Invalid pagination parameters: page size must be <= 100");
        }
        if (pageable.getPageSize() < 1){
            throw new IllegalArgumentException("Invalid pagination parameters: Page size must be 1 to 100");
        }
    }

    /**
     * Retrieves an actor by their unique identifier.
     * If the actor is not found, throws a ResourceNotFoundException with a relevant message.
     *
     * @param id the unique identifier of the actor
     * @return an ActorDTO representing the found actor
     * @throws ResourceNotFoundException if no actor is found with the given id
     */
    public ActorDTO getActorById(Long id) {
        // Fetch the actor by id from the repository or throw exception if not found
        Actor actor = actorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(HttpStatus.NOT_FOUND, "Actor not found with id: " + id));

        // Convert the Actor entity to ActorDTO and return
        return mapToDTO(actor);
    }

    /**
     * Searches for actors by their name, ignoring case.
     * Useful for implementing search functionality where users can find actors by partial or full names.
     *
     * @param name the name or partial name of the actor(s) to search for
     * @return a list of ActorDTOs matching the search criteria
     */
    public List<ActorDTO> getActorsByName(String name) {
        System.out.println("Searching for actors with name containing: " + name);
        List<ActorDTO> actors = actorRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        System.out.println("Found actors: " + actors);
        return actors;
    }

    /**
     * Creates a new actor in the repository.
     * Before creation, it checks if an actor with the same name and birth date already exists to prevent duplicates.
     *
     * @param actorDTO the ActorDTO containing the details of the actor to create
     * @return an ActorDTO representing the newly created actor
     * @throws ResourceNotFoundException if an actor with the same name and birth date already exists
     * @throws IllegalArgumentException if the actor's name or birth date is null or invalid
     */
    public ActorDTO createActor(ActorDTO actorDTO) {
        // Validate the actor's name to ensure it is not null or empty
        if (actorDTO.getName() == null || actorDTO.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("The actor's name cannot be null or blank.");
        }

        // Validate the actor's birth date to ensure it is not null
        if (actorDTO.getBirthDate() == null) {
            throw new IllegalArgumentException("The actor's birth date cannot be null.");
        }

        // Check if an actor with the same name and birth date already exists
        if (actorRepository.findByNameAndBirthDate(actorDTO.getName(), actorDTO.getBirthDate().toString()).isPresent()) {
            // If such an actor exists, throw an exception to prevent duplicate entries
            throw new ResourceAlreadyExistsException(HttpStatus.BAD_REQUEST, "An actor with the same name and birthdate already exists");
        }

        // Convert ActorDTO to Actor entity for persistence
        Actor actor = mapToEntity(actorDTO);

        // Save the new actor to the repository (database)
        actor = actorRepository.save(actor);

        // Convert the saved Actor entity back to ActorDTO to return to the caller
        return mapToDTO(actor);
    }

    /**
     * Updates an existing actor's details.
     * Fetches the actor by id, validates the input, updates the fields, and saves the changes.
     *
     * @param id the unique identifier of the actor to update
     * @param actorDTO the ActorDTO containing the updated actor details
     * @return an ActorDTO representing the updated actor
     * @throws ResourceNotFoundException if no actor is found with the given id
     * @throws IllegalArgumentException if the updated actor's name or birth date is null or invalid
     */
    public ActorDTO updateActor(Long id, ActorDTO actorDTO) {
        // Fetch the actor by id from the repository or throw exception if not found
        Actor actor = actorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(HttpStatus.NOT_FOUND, "Actor not found with id: " + id));
        
        // Partially update the actor's details based on the provided ActorDTO
        if (actorDTO.getName() != null && !actorDTO.getName().trim().isEmpty()) {
            actor.setName(actorDTO.getName());
        }
        if (actorDTO.getBirthDate() != null) {
            actor.setBirthDate(actorDTO.getBirthDate().format(DateTimeFormatter.ISO_DATE));
        }

        // Save the updated actor to the repository (database)
        actor = actorRepository.save(actor);

        // Convert the updated Actor entity to ActorDTO and return
        return mapToDTO(actor);
    }

    /**
     * Deletes an actor from the repository.
     * If 'force' is true, removes the actor from all associated movies before deletion.
     * If 'force' is false, only deletes the actor if they are not associated with any movies to maintain referential integrity.
     *
     * @param id the unique identifier of the actor to delete
     * @param force flag indicating whether to force deletion even if associated with movies
     * @throws ResourceNotFoundException if the actor is associated with movies and 'force' is false,
     *                                     or if no actor is found with the given id
     */
    public void deleteActor(Long id, boolean force) {
        // Fetch the actor by id from the repository or throw exception if not found
        Actor actor = actorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(HttpStatus.NOT_FOUND, "Actor not found with id: " + id));
        
        if (!force) {
            // If not forcing deletion, check if actor is associated with any movies
            if (!actor.getMovies().isEmpty()) {
                // If the actor is associated with movies, prevent deletion to avoid orphan records
                throw new ResourceAlreadyExistsException(HttpStatus.BAD_REQUEST, "Actor is associated with movies and cannot be deleted.");
            }
        } else {
            // If forcing deletion, remove the actor from all associated movies to maintain data integrity
            actor.getMovies().forEach(movie -> movie.getActors().remove(actor));
        }
        
        // Delete the actor from the repository (database)
        actorRepository.delete(actor);
    }

    /**
     * Converts an Actor entity to its corresponding ActorDTO.
     * This method is useful for transferring data without exposing the entity directly.
     *
     * @param actor the Actor entity to map
     * @return the mapped ActorDTO containing actor details
     */
    private ActorDTO mapToDTO(Actor actor) {
        // Create a new ActorDTO instance to hold the actor's data
        ActorDTO dto = new ActorDTO();
        
        // Set the ID from the Actor entity
        dto.setId(actor.getId());
        
        // Set the name from the Actor entity
        dto.setName(actor.getName());
        
        // Parse the birthDate string from the Actor entity to LocalDate and set in the DTO
        dto.setBirthDate(LocalDate.parse(actor.getBirthDate(), DateTimeFormatter.ISO_DATE));
        
        // Return the populated ActorDTO
        return dto;
    }

    /**
     * Converts an ActorDTO to its corresponding Actor entity.
     * This method is useful for persisting data received from external sources.
     *
     * @param actorDTO the ActorDTO to map
     * @return the mapped Actor entity with actor details
     */
    private Actor mapToEntity(ActorDTO actorDTO) {
        // Create a new Actor entity instance to hold the actor's data
        Actor actor = new Actor();
        
        // Set the ID from the ActorDTO (useful for updates; typically null for new entries)
        actor.setId(actorDTO.getId());
        
        // Set the name from the ActorDTO
        actor.setName(actorDTO.getName());
        
        // Format the LocalDate birthDate from the DTO to String in ISO_DATE format and set in the entity
        actor.setBirthDate(actorDTO.getBirthDate().format(DateTimeFormatter.ISO_DATE));
        
        // Return the populated Actor entity
        return actor;
    }
}
