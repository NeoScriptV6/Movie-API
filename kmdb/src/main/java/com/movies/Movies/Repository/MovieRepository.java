package com.movies.Movies.Repository;

import com.movies.Movies.Entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    // Paginated query to find movies by genre
    @Query("SELECT m FROM Movie m JOIN m.genres g WHERE g.id = :genreId")
    Page<Movie> findByGenre(@Param("genreId") Long genreId, Pageable pageable);

    // Paginated query to find movies by release year
    @Query("SELECT m FROM Movie m WHERE m.releaseYear = :releaseYear")
    Page<Movie> findByReleaseYear(@Param("releaseYear") Integer releaseYear, Pageable pageable);

    // Paginated query to find movies by genre and release year
    @Query("SELECT m FROM Movie m JOIN m.genres g WHERE g.id = :genreId AND m.releaseYear = :releaseYear")
    Page<Movie> findByGenreAndReleaseYear(@Param("genreId") Long genreId, @Param("releaseYear") Integer releaseYear, Pageable pageable);

    // Check existence of a movie by title, release year, and duration
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM Movie m WHERE m.title = :title AND m.releaseYear = :releaseYear AND m.duration = :duration")
    boolean existsByTitleAndReleaseYearAndDuration(@Param("title") String title, @Param("releaseYear") Integer releaseYear, @Param("duration") Integer duration);

    // Non-paginated query to find movies by release year
    @Query("SELECT m FROM Movie m WHERE m.releaseYear = :releaseYear")
    List<Movie> findByReleaseYear(@Param("releaseYear") int releaseYear);

    // Non-paginated, case-insensitive search by title
    @Query("SELECT m FROM Movie m WHERE LOWER(m.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    List<Movie> findByTitleContainingIgnoreCase(@Param("title") String title);
}
