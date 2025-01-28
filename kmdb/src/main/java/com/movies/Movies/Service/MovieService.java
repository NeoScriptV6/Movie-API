package com.movies.Movies.Service;

import com.movies.Movies.Entity.Actor;
import com.movies.Movies.Entity.Genre;
import com.movies.Movies.Entity.Movie;
import com.movies.Movies.dto.ActorDTO;
import com.movies.Movies.dto.MovieDTO;
import com.movies.Movies.exception.ResourceAlreadyExistsException;
import com.movies.Movies.exception.ResourceNotFoundException;
import com.movies.Movies.Repository.ActorRepository;
import com.movies.Movies.Repository.GenreRepository;
import com.movies.Movies.Repository.MovieRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.ArrayList;

/**
 * Service class responsible for managing Movie entities.
 * Provides methods to perform CRUD operations and handle business logic related to movies.
 */
@Service
public class MovieService {

    // Repository for accessing Movie data from the database
    private final MovieRepository movieRepository;
    
    // Repository for accessing Actor data from the database
    private final ActorRepository actorRepository;
    
    // Repository for accessing Genre data from the database
    private final GenreRepository genreRepository;

    /**
     * Constructor for MovieService.
     * Injects the required repositories for Movie, Actor, and Genre entities.
     *
     * @param movieRepository   the repository for Movie entities
     * @param actorRepository   the repository for Actor entities
     * @param genreRepository   the repository for Genre entities
     */
    public MovieService(MovieRepository movieRepository, ActorRepository actorRepository, GenreRepository genreRepository) {
        this.movieRepository = movieRepository;
        this.actorRepository = actorRepository;
        this.genreRepository = genreRepository;
    }

    /**
     * Creates a new movie along with its associated actors and genres.
     * Validates to ensure the movie does not already exist with the same details.
     *
     * @param movieDTO the data transfer object containing movie details
     * @return the saved Movie entity
     * @throws ResourceNotFoundException if a movie with the same details already exists
     */
    @Transactional
    public Movie createMovieWithActors(MovieDTO movieDTO) {
        // Convert MovieDTO to Movie entity
        Movie movie = mapToEntity(movieDTO);

        // Retrieve and set the list of Actor entities based on provided actor IDs
        List<Actor> actors = actorRepository.findAllById(movieDTO.getActorIds());
        movie.setActors(actors);

        // Retrieve and set the list of Genre entities based on provided genre IDs
        List<Genre> genres = genreRepository.findAllById(movieDTO.getGenreIds());
        movie.setGenres(genres);

        // Check if a movie with the same title, release year, and duration already exists
        if (movieRepository.findByTitleContainingIgnoreCase(movie.getTitle()).stream()
                .anyMatch(existingMovie -> existingMovie.getReleaseYear().equals(movie.getReleaseYear()) 
                        && existingMovie.getDuration().equals(movie.getDuration()))) {
            // If such a movie exists, throw an exception to prevent duplication
            throw new ResourceAlreadyExistsException(HttpStatus.BAD_REQUEST, "Movie already exists with the same details.");
        }

        // Save the new Movie entity to the repository (database) and return it
        return movieRepository.save(movie);
    }

    /**
     * Retrieves a movie by its unique identifier.
     *
     * @param id the unique identifier of the movie to retrieve
     * @return the Movie entity if found
     * @throws ResourceNotFoundException if no movie is found with the given ID
     */
    @Transactional(readOnly = true)
    public Movie getMovieById(Long id) {
        // Attempt to find the movie by ID; throw an exception if not found
        return movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(HttpStatus.NOT_FOUND, "Movie not found"));
    }

    /**
     * Retrieves a list of all movies in the repository.
     *
     * @return a list of MovieDTOs representing all movies
     */
    @Transactional(readOnly = true)
    public Page<MovieDTO> getAllMovies(Pageable pageable) {
        validatePagination(pageable);
                return movieRepository.findAll(pageable)
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
     * Retrieves movies that belong to a specific genre.
     *
     * @param genreId the unique identifier of the genre
     * @return a list of MovieDTOs associated with the specified genre
     * @throws ResourceNotFoundException if the genre with the given ID is not found
     */
    @Transactional(readOnly = true)
    public List<MovieDTO> getMoviesByGenre(Long genreId) {
        // Attempt to find the genre by ID; throw an exception if not found
        Genre genre = genreRepository.findById(genreId)
                .orElseThrow(() -> new ResourceNotFoundException(HttpStatus.NOT_FOUND, "Genre not found"));
        
        // Fetch all movies associated with the found genre, map them to DTOs, and collect into a list
        return genre.getMovies().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves movies released in a specific year.
     *
     * @param releaseYear the year of release to filter movies by
     * @return a list of MovieDTOs released in the specified year
     */
    @Transactional(readOnly = true)
    public List<MovieDTO> getMoviesByReleaseYear(int releaseYear) {
        // Find movies by release year, map them to DTOs, and collect into a list
        return movieRepository.findByReleaseYear(releaseYear).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all actors associated with a specific movie.
     *
     * @param movieId the unique identifier of the movie
     * @return a list of ActorDTOs representing actors in the specified movie
     * @throws ResourceNotFoundException if the movie with the given ID is not found
     */
    @Transactional(readOnly = true)
    public List<ActorDTO> getActorsByMovieId(Long movieId) {
        // Attempt to find the movie by ID; throw an exception if not found
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException(HttpStatus.NOT_FOUND, "Movie not found"));

        // Fetch all actors associated with the movie, map them to DTOs, and collect into a list
        return movie.getActors().stream()
                .map(this::mapActorToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all movies associated with a specific actor.
     *
     * @param actorId the unique identifier of the actor
     * @return a list of MovieDTOs representing movies the specified actor has acted in
     * @throws ResourceNotFoundException if the actor with the given ID is not found
     */
    @Transactional(readOnly = true)
    public List<MovieDTO> getMoviesByActorId(Long actorId) {
        // Attempt to find the actor by ID; throw an exception if not found
        Actor actor = actorRepository.findById(actorId)
                .orElseThrow(() -> new ResourceNotFoundException(HttpStatus.NOT_FOUND, "Actor not found with id: " + actorId));

        // Fetch all movies associated with the actor, map them to DTOs, and collect into a list
        return actor.getMovies().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Maps an Actor entity to its corresponding ActorDTO.
     *
     * @param actor the Actor entity to map
     * @return the mapped ActorDTO
     */
    private ActorDTO mapActorToDTO(Actor actor) {
        ActorDTO dto = new ActorDTO();
        dto.setId(actor.getId());
        dto.setName(actor.getName());
        dto.setBirthDate(LocalDate.parse(actor.getBirthDate()));
        return dto;
    }
    
    /**
     * Deletes a movie by its unique identifier.
     * Handles forced and non-forced deletion based on associations with actors and genres.
     *
     * @param id    the unique identifier of the movie to delete
     * @param force flag indicating whether to force deletion even if associated with actors or genres
     * @throws ResourceNotFoundException if the movie cannot be deleted due to existing associations and force is false
     */
    @Transactional
    public void deleteMovieById(Long id, boolean force) {
        // Retrieve the movie by ID; throws exception if not found
        Movie movie = getMovieById(id);
        
        // Get associated actors and genres of the movie
        List<Actor> actors = movie.getActors();
        List<Genre> genres = movie.getGenres();
    
        // If not forcing deletion and the movie has associated actors or genres, prevent deletion
        if (!force && (!actors.isEmpty() || !genres.isEmpty())) {
            throw new ResourceAlreadyExistsException(HttpStatus.BAD_REQUEST,
                    "Oops, you cannot delete '" + movie.getTitle() + "' because it is associated with "
                            + actors.size() + " actor(s) and " + genres.size() + " genre(s).");
        }
    
        if (force) {
            // If forcing deletion, remove the movie from each associated actor's movie list
            for (Actor actor : actors) {
                actor.getMovies().remove(movie);
                actorRepository.save(actor);
            }
            // Remove the movie from each associated genre's movie list
            for (Genre genre : genres) {
                genre.getMovies().remove(movie);
                genreRepository.save(genre);
            }
        }
    
        // Delete the movie from the repository (database)
        movieRepository.deleteById(id);
    }
    
    /**
     * Converts a MovieDTO to its corresponding Movie entity.
     *
     * @param movieDTO the MovieDTO to map
     * @return the mapped Movie entity
     */
    public Movie mapToEntity(MovieDTO movieDTO) {
        Movie movie = new Movie();
        movie.setId(movieDTO.getId());
        movie.setTitle(movieDTO.getTitle());
        movie.setReleaseYear(movieDTO.getReleaseYear());
        movie.setDuration(movieDTO.getDuration());
        return movie;
    }
    
    /**
     * Updates an existing movie's details.
     * Handles updating basic details as well as associations with actors and genres.
     *
     * @param id       the unique identifier of the movie to update
     * @param movieDTO the MovieDTO containing updated movie details
     * @return the updated Movie entity
     */
    @Transactional
    public Movie updateMovie(Long id, MovieDTO movieDTO) {
        // Retrieve the existing movie by ID; throws exception if not found
        Movie existingMovie = getMovieById(id);

        // Log the incoming movieDTO
        System.out.println("Updating Movie with ID: " + id + " using DTO: " + movieDTO);

        // Check for null movieDTO
        if (movieDTO == null) {
            throw new IllegalArgumentException("MovieDTO cannot be null");
        }

        // Update basic movie details only if they are provided
        if (movieDTO.getTitle() != null) {
            existingMovie.setTitle(movieDTO.getTitle());
        }
        if (movieDTO.getReleaseYear() != null) {
            existingMovie.setReleaseYear(movieDTO.getReleaseYear());
        }
        if (movieDTO.getDuration() != null) {
            existingMovie.setDuration(movieDTO.getDuration());
        }

        // Update the list of associated actors only if actor IDs are provided
        if (movieDTO.getActorIds() != null) {
            if (!movieDTO.getActorIds().isEmpty()) {
                List<Actor> actors = actorRepository.findAllById(movieDTO.getActorIds());
                existingMovie.setActors(actors);
            } else {
                // Clear existing actors if no actor IDs are provided
                existingMovie.setActors(new ArrayList<>());
            }
        }

        // Update the list of associated genres only if genre IDs are provided
        if (movieDTO.getGenreIds() != null) {
            if (!movieDTO.getGenreIds().isEmpty()) {
                List<Genre> genres = genreRepository.findAllById(movieDTO.getGenreIds());
                existingMovie.setGenres(genres);
            } else {
                // Clear existing genres if no genre IDs are provided
                existingMovie.setGenres(new ArrayList<>());
            }
        }

        // Log the updated movie details
        System.out.println("Updated Movie: " + existingMovie);

        // Check for null fields in the existingMovie before saving
        if (existingMovie.getTitle() == null || existingMovie.getReleaseYear() == null || existingMovie.getDuration() == null) {
            throw new IllegalArgumentException("Movie fields cannot be null before saving.");
        }

        // Save the updated movie entity to the repository (database) and return it
        try {
            return movieRepository.save(existingMovie);
        } catch (Exception e) {
            System.err.println("Error saving updated movie: " + e.getMessage());
            throw e; // Rethrow the exception after logging
        }
    }

    
    /**
     * Maps a Movie entity to its corresponding MovieDTO.
     *
     * @param movie the Movie entity to map
     * @return the mapped MovieDTO
     */
    public MovieDTO mapToDTO(Movie movie) {
        MovieDTO dto = new MovieDTO();
        dto.setId(movie.getId());
        dto.setTitle(movie.getTitle());
        dto.setReleaseYear(movie.getReleaseYear());
        dto.setDuration(movie.getDuration());
    
        // If the movie has associated actors, set their names and IDs in the DTO
        if (!movie.getActors().isEmpty()) {
            List<String> actorNames = movie.getActors().stream()
                    .map(Actor::getName)
                    .collect(Collectors.toList());
            dto.setActors(Optional.of(actorNames));
    
            List<Long> actorIds = movie.getActors().stream()
                    .map(Actor::getId)
                    .collect(Collectors.toList());
            dto.setActorIds(actorIds);
        } else {
            // If no actors are associated, set Optional.empty() and null for IDs
            dto.setActors(Optional.empty());
            dto.setActorIds(null);
        }
    
        // If the movie has associated genres, set their names and IDs in the DTO
        if (!movie.getGenres().isEmpty()) {
            List<String> genreNames = movie.getGenres().stream()
                    .map(Genre::getName)
                    .collect(Collectors.toList());
            dto.setGenres(Optional.of(genreNames));
    
            List<Long> genreIds = movie.getGenres().stream()
                    .map(Genre::getId)
                    .collect(Collectors.toList());
            dto.setGenreIds(genreIds);
        } else {
            // If no genres are associated, set Optional.empty() and null for IDs
            dto.setGenres(Optional.empty());
            dto.setGenreIds(null);
        }
    
        return dto;
    }

    /**
     * Searches for movies by title, allowing for case-insensitive and partial matches.
     *
     * @param title the title or partial title to search for
     * @return a list of MovieDTOs matching the search criteria
     */
    @Transactional(readOnly = true)
    public List<MovieDTO> searchMoviesByTitle(String title) {
        // Fetch movies with titles containing the search term, ignoring case
        return movieRepository.findByTitleContainingIgnoreCase(title).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

 
}
