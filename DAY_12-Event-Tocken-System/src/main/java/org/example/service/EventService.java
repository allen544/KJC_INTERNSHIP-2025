package org.example.service;

import org.example.models.Event;
import org.example.repository.EventRepository;

import java.util.List;

public class EventService {
    private final EventRepository repo = new EventRepository();

    public List<Event> getAllEvents() {
        return repo.findAll(); // Implement this in repo
    }
}
