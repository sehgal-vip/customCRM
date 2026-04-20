package com.turno.crm.model.dto;

import java.util.List;
import java.util.Map;

public class CsvRowResult {

    private int rowNumber;
    private boolean valid;
    private List<String> errors;
    private Map<String, String> data;

    public int getRowNumber() { return rowNumber; }
    public void setRowNumber(int rowNumber) { this.rowNumber = rowNumber; }

    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }

    public List<String> getErrors() { return errors; }
    public void setErrors(List<String> errors) { this.errors = errors; }

    public Map<String, String> getData() { return data; }
    public void setData(Map<String, String> data) { this.data = data; }
}
