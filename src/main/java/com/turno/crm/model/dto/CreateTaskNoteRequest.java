package com.turno.crm.model.dto;

import com.turno.crm.model.enums.NoteType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateTaskNoteRequest {

    @NotBlank
    private String content;

    @NotNull
    private NoteType noteType;

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public NoteType getNoteType() { return noteType; }
    public void setNoteType(NoteType noteType) { this.noteType = noteType; }
}
