package com.turno.crm.model.dto;

public class CsvImportResponse {

    private int importedCount;
    private int skippedCount;

    public CsvImportResponse() {}

    public CsvImportResponse(int importedCount, int skippedCount) {
        this.importedCount = importedCount;
        this.skippedCount = skippedCount;
    }

    public int getImportedCount() { return importedCount; }
    public void setImportedCount(int importedCount) { this.importedCount = importedCount; }

    public int getSkippedCount() { return skippedCount; }
    public void setSkippedCount(int skippedCount) { this.skippedCount = skippedCount; }
}
