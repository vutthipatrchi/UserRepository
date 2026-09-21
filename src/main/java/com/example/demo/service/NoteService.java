package com.example.demo.service;

import com.example.demo.dto.NoteRequest;
import com.example.demo.dto.NoteResponse;
import com.example.demo.entity.Note;
import com.example.demo.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository noteRepository;

    public NoteResponse create(NoteRequest request) {
        Note note = Note.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .build();
        return toResponse(noteRepository.save(note));
    }

    public List<NoteResponse> findAll() {
        return noteRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public NoteResponse update(Long id, NoteRequest request) {
        Note note = findById(id);
        note.setTitle(request.getTitle());
        note.setContent(request.getContent());
        return toResponse(noteRepository.save(note));
    }

    public void delete(Long id) {
        noteRepository.delete(findById(id));
    }

    public NoteResponse attachFileUrl(Long id, String url) {
        Note note = findById(id);
        note.setImageUrl(url);
        return toResponse(noteRepository.save(note));
    }

    private Note findById(Long id) {
        return noteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found"));
    }

    private NoteResponse toResponse(Note note) {
        return NoteResponse.builder()
                .id(note.getId())
                .title(note.getTitle())
                .content(note.getContent())
                .imageUrl(note.getImageUrl())
                .build();
    }
}
