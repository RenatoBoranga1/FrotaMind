package com.example.arlacontrole.export;

import java.time.LocalDate;

public class ReportFilter {

    public static final String STATUS_ALL = "";
    public static final String SYNC_ALL = "ALL";
    public static final String SYNC_SYNCED = "SYNCED";
    public static final String SYNC_PENDING = "PENDING";

    public String format = ReportFormat.PDF;
    public String fuelType = "";
    public String vehiclePlate = "";
    public String driverName = "";
    public String statusLevel = STATUS_ALL;
    public String syncFilter = SYNC_ALL;
    public LocalDate startDate;
    public LocalDate endDate;
}
