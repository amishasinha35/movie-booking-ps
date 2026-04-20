package com.amisha.moviebooking.repository;

import com.amisha.moviebooking.entity.Seat;
import com.amisha.moviebooking.enums.SeatStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByShowIdAndStatus(Long showId, SeatStatus status);

    int countByShowId(Long showId);

    // PESSIMISTIC_WRITE lock to prevent double-booking under concurrent requests
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Seat s WHERE s.id IN :ids")
    List<Seat> findByIdsWithLock(@Param("ids") List<Long> ids);
}
