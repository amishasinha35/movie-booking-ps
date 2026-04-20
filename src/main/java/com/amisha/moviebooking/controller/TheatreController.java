package com.amisha.moviebooking.controller;

import com.amisha.moviebooking.entity.Theatre;
import com.amisha.moviebooking.repository.TheatreRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/theatres")
@Tag(name = "Theatres", description = "Browse theatres by city")
public class TheatreController {

    private final TheatreRepository theatreRepository;

    public TheatreController(TheatreRepository theatreRepository) {
        this.theatreRepository = theatreRepository;
    }

    @GetMapping
    @Operation(summary = "List theatres, optionally filter by city")
    public ResponseEntity<List<Theatre>> getTheatres(@RequestParam(required = false) String city) {
        if (city != null && !city.isBlank()) {
            return ResponseEntity.ok(theatreRepository.findByCityIgnoreCase(city));
        }
        return ResponseEntity.ok(theatreRepository.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get theatre by ID")
    public ResponseEntity<Theatre> getTheatre(@PathVariable Long id) {
        return theatreRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
