package com.turno.crm.model.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class CsvImportRequest {

    @NotEmpty(message = "Selected rows must not be empty")
    private List<Integer> selectedRows;

    public List<Integer> getSelectedRows() { return selectedRows; }
    public void setSelectedRows(List<Integer> selectedRows) { this.selectedRows = selectedRows; }
}
