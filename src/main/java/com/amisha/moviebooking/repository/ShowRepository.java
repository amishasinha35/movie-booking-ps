package com.amisha.moviebooking.repository;

import com.amisha.moviebooking.entity.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ShowRepository extends JpaRepository<Show, Long> {

    List<Show> findByTheatreIdAndShowTimeBetween(Long theatreId, LocalDateTime start, LocalDateTime end);

    List<Show> findByMovieNameContainingIgnoreCase(String movieName);

    @Query("SELECT s FROM Show s WHERE LOWER(s.movieName) LIKE LOWER(CONCAT('%', :movieName, '%')) " +
           "AND LOWER(s.theatre.city) = LOWER(:city) " +
           "AND s.showTime >= :start AND s.showTime < :end")
    List<Show> findByMovieNameAndCityAndDate(
            @Param("movieName") String movieName,
            @Param("city") String city,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}
