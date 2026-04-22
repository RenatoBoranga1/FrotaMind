package com.example.arlacontrole.export;

import com.example.arlacontrole.data.local.RefuelEntity;

import java.util.ArrayList;
import java.util.List;

public class ReportDataset {
    public String systemName;
    public String generatedAtIso;
    public ReportFilter filter;
    public ReportSummary summary;
    public List<RefuelEntity> records = new ArrayList<>();
}
