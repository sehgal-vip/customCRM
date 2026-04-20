package com.turno.crm.model.entity;

import com.turno.crm.model.enums.NoteType;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "task_notes")
public class TaskNote extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "note_type", nullable = false, columnDefinition = "note_type")
    private NoteType noteType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    public Task getTask() { return task; }
    public void setTask(Task task) { this.task = task; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public NoteType getNoteType() { return noteType; }
    public void setNoteType(NoteType noteType) { this.noteType = noteType; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
}
