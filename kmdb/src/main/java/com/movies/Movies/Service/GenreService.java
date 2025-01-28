package com.movies.Movies.Service;

import com.movies.Movies.dto.GenreDTO;
import com.movies.Movies.Entity.Genre;
import com.movies.Movies.Entity.Movie;
import com.movies.Movies.exception.ResourceAlreadyExistsException;
import com.movies.Movies.exception.ResourceNotFoundException;
import com.movies.Movies.Repository.GenreRepository;
import com.movies.Movies.Repository.MovieRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * Service class responsible for managing Genre entities.
 * Provides methods to perform CRUD operations on genres.
 */
@Service
public class GenreService {

    private final GenreRepository genreRepository;
    private final MovieRepository movieRepository;

    /**
     * Constructor for GenreService, injecting the GenreRepository and MovieRepository dependencies.
     * Ensures that GenreService has access to the necessary data access methods.
     *
     * @param genreRepository the repository for Genre entities
     * @param movieRepository the repository for Movie entities
     */
    public GenreService(GenreRepository genreRepository, MovieRepository movieRepository) {
        this.genreRepository = genreRepository;
        this.movieRepository = movieRepository;
    }

    /**
     * Retrieves a list of all genres from the repository.
     * Converts each Genre entity to a GenreDTO for data transfer.
     * @param pageable 
     *
     * @return a list of GenreDTO objects representing all genres
     */
   
    public Page<GenreDTO> getAllGenres(Pageable pageable) {
        validatePagination(pageable);
        return genreRepository.findAll(pageable)
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
     * Retrieves a specific genre by its unique identifier.
     *
     * @param id the unique identifier of the genre to retrieve
     * @return a GenreDTO representing the requested genre
     * @throws ResourceNotFoundException if no genre is found with the given id
     */
    public GenreDTO getGenreById(Long id) {
        // Fetch the genre by id or throw an exception if not found
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(HttpStatus.NOT_FOUND, "Genre not found with id: " + id));
        // Convert the Genre entity to GenreDTO and return
        return mapToDTO(genre);
    }

    /**
     * Creates a new genre in the repository.
     * Validates the input to ensure the genre name is not null or empty.
     *
     * @param genreDTO the GenreDTO containing details of the genre to create
     * @return a GenreDTO representing the newly created genre
     * @throws IllegalArgumentException if the genre name is null or empty
     */
    public GenreDTO createGenre(GenreDTO genreDTO) {
        // Validate that the genre name is not null or empty
        if (genreDTO.getName() == null || genreDTO.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Genre name cannot be empty or blank.");
        }
        // Convert GenreDTO to Genre entity for persistence
        Genre genre = mapToEntity(genreDTO);
        // Save the new genre to the repository (database)
        genre = genreRepository.save(genre);
        // Convert the saved Genre entity back to GenreDTO and return
        return mapToDTO(genre);
    }

    /**
     * Updates an existing genre's details.
     * Validates the input and ensures the genre exists before updating.
     *
     * @param id the unique identifier of the genre to update
     * @param genreDTO the GenreDTO containing updated genre details
     * @return a GenreDTO representing the updated genre
     * @throws IllegalArgumentException if the genre name is null or empty
     * @throws ResourceNotFoundException if no genre is found with the given id
     */
    public GenreDTO updateGenre(Long id, GenreDTO genreDTO) {
        // Validate that the genre name is not null or empty
        if (genreDTO.getName() == null || genreDTO.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Genre name cannot be empty or blank.");
        }
        // Fetch the existing genre by id or throw an exception if not found
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(HttpStatus.NOT_FOUND, "Genre not found with id: " + id));
        // Update the genre's name with the new value from GenreDTO
        genre.setName(genreDTO.getName());
        // Save the updated genre to the repository (database)
        genre = genreRepository.save(genre);
        // Convert the updated Genre entity back to GenreDTO and return
        return mapToDTO(genre);
    }

    /**
     * Deletes a genre from the repository.
     * Handles both forced and non-forced deletion based on associated movies.
     *
     * @param id the unique identifier of the genre to delete
     * @param force flag indicating whether to force deletion even if associated with movies
     * @throws ResourceNotFoundException if the genre is associated with movies and 'force' is false,
     *                                     or if no genre is found with the given id
     */
    public void deleteGenre(Long id, boolean force) {
        // Fetch the genre by id from the repository or throw exception if not found
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(HttpStatus.NOT_FOUND, "Genre not found with id: " + id));

        if (!force && !genre.getMovies().isEmpty()) {
            // If not forcing deletion and genre is associated with movies, prevent deletion
            throw new ResourceAlreadyExistsException(HttpStatus.BAD_REQUEST,
                    "Cannot delete genre '" + genre.getName() + "' because it is associated with " + genre.getMovies().size() + " movie(s).");
        }

        if (force) {
            // If forcing deletion, remove the genre from all associated movies to maintain data integrity
            for (Movie movie : genre.getMovies()) {
                // Remove the genre from the movie's genre list
                movie.getGenres().remove(genre);
                // Save the updated movie to persist the changes
                movieRepository.save(movie);
            }
        }

        // Delete the genre from the repository (database)
        genreRepository.delete(genre);
    }

    /**
     * Converts a Genre entity to its corresponding GenreDTO.
     * This method is useful for transferring data without exposing the entity directly.
     *
     * @param genre the Genre entity to map
     * @return the mapped GenreDTO containing genre details
     */
    private GenreDTO mapToDTO(Genre genre) {
        // Create a new GenreDTO instance to hold the genre's data
        GenreDTO dto = new GenreDTO();
        // Set the ID from the Genre entity
        dto.setId(genre.getId());
        // Set the name from the Genre entity
        dto.setName(genre.getName());
        // Return the populated GenreDTO
        return dto;
    }

    /**
     * Converts a GenreDTO to its corresponding Genre entity.
     * This method is useful for persisting data received from external sources.
     *
     * @param dto the GenreDTO to map
     * @return the mapped Genre entity with genre details
     */
    private Genre mapToEntity(GenreDTO dto) {
        // Create a new Genre entity instance to hold the genre's data
        Genre genre = new Genre();
        // Set the name from the GenreDTO
        genre.setName(dto.getName());
        // Return the populated Genre entity
        return genre;
    }

}
