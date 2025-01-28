package com.movies.Movies.Repository;

import com.movies.Movies.Entity.Actor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActorRepository extends JpaRepository<Actor, Long> {
    @Query("SELECT a FROM Actor a WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Actor> findByNameContainingIgnoreCase(@Param("name") String name);

    @Query("SELECT a FROM Actor a WHERE a.name = :name AND a.birthDate = :birthDate")
    Optional<Actor> findByNameAndBirthDate(@Param("name") String name, @Param("birthDate") String birthDate);

    
}
