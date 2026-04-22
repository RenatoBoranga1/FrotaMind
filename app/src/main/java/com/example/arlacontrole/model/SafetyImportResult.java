package com.example.arlacontrole.model;

import java.util.ArrayList;
import java.util.List;

public class SafetyImportResult {
    public String fileName = "";
    public int rowsRead;
    public int rowsImported;
    public int skippedRows;
    public int representedOccurrences;
    public int createdDrivers;
    public int createdVehicles;
    public final List<String> warnings = new ArrayList<>();
}
