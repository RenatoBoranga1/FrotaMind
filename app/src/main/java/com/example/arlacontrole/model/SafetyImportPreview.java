package com.example.arlacontrole.model;

import java.util.ArrayList;
import java.util.List;

public class SafetyImportPreview {
    public String fileName = "";
    public int totalRows;
    public int validRows;
    public int invalidRows;
    public int suspectRows;
    public int duplicateRows;
    public int uniqueDrivers;
    public int uniqueVehicles;
    public String periodStartIso = "";
    public String periodEndIso = "";
    public final List<SafetyImportPreviewRow> rows = new ArrayList<>();
    public final List<String> warnings = new ArrayList<>();
}
