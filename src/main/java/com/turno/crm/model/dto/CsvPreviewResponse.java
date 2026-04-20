package com.turno.crm.model.dto;

import java.util.List;

public class CsvPreviewResponse {

    private int totalRows;
    private int validRows;
    private int invalidRows;
    private List<CsvRowResult> rows;

    public int getTotalRows() { return totalRows; }
    public void setTotalRows(int totalRows) { this.totalRows = totalRows; }

    public int getValidRows() { return validRows; }
    public void setValidRows(int validRows) { this.validRows = validRows; }

    public int getInvalidRows() { return invalidRows; }
    public void setInvalidRows(int invalidRows) { this.invalidRows = invalidRows; }

    public List<CsvRowResult> getRows() { return rows; }
    public void setRows(List<CsvRowResult> rows) { this.rows = rows; }
}
