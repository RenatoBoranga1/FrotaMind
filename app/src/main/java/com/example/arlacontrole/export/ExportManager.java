package com.example.arlacontrole.export;

import android.content.Context;
import android.os.Environment;

import androidx.core.content.FileProvider;

import com.example.arlacontrole.R;
import com.example.arlacontrole.data.local.ArlaDatabase;
import com.example.arlacontrole.data.local.RefuelEntity;
import com.example.arlacontrole.export.excel.ExcelReportGenerator;
import com.example.arlacontrole.export.pdf.PdfReportGenerator;
import com.example.arlacontrole.model.UserRole;
import com.example.arlacontrole.utils.AppPreferences;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class ExportManager {

    private final Context appContext;
    private final ArlaDatabase database;
    private final AppPreferences preferences;
    private final ReportDataBuilder dataBuilder;
    private final PdfReportGenerator pdfReportGenerator;
    private final ExcelReportGenerator excelReportGenerator;

    public ExportManager(Context context) {
        appContext = context.getApplicationContext();
        database = ArlaDatabase.getInstance(appContext);
        preferences = new AppPreferences(appContext);
        dataBuilder = new ReportDataBuilder();
        pdfReportGenerator = new PdfReportGenerator();
        excelReportGenerator = new ExcelReportGenerator();
    }

    public ExportedReport generateReport(ReportFilter filter) throws Exception {
        ensureExportAllowed();
        validateFilter(filter);

        List<RefuelEntity> records = database.refuelDao().getAllSync();
        ReportDataset dataset = dataBuilder.build(
            appContext.getString(R.string.app_name),
            LocalDateTime.now().withSecond(0).withNano(0).toString(),
            filter,
            records
        );
        if (dataset.records.isEmpty()) {
            throw new IllegalStateException(appContext.getString(R.string.export_empty));
        }

        File reportFile = buildReportFile(filter.format);
        try (OutputStream outputStream = new FileOutputStream(reportFile)) {
            if (ReportFormat.isPdf(filter.format)) {
                pdfReportGenerator.generate(appContext, outputStream, dataset);
            } else {
                excelReportGenerator.generate(outputStream, dataset);
            }
        }

        ExportedReport exportedReport = new ExportedReport();
        exportedReport.file = reportFile;
        exportedReport.fileName = reportFile.getName();
        exportedReport.mimeType = ReportFormat.isPdf(filter.format)
            ? "application/pdf"
            : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        exportedReport.uri = FileProvider.getUriForFile(
            appContext,
            appContext.getPackageName() + ".fileprovider",
            reportFile
        );
        return exportedReport;
    }

    private void ensureExportAllowed() {
        String role = preferences.getSession().role;
        if (!UserRole.canExportReports(role)) {
            throw new SecurityException(appContext.getString(R.string.export_permission_denied));
        }
    }

    private void validateFilter(ReportFilter filter) {
        if (filter == null) {
            throw new IllegalArgumentException(appContext.getString(R.string.export_invalid_filter));
        }
        if (filter.startDate != null && filter.endDate != null && filter.startDate.isAfter(filter.endDate)) {
            throw new IllegalArgumentException(appContext.getString(R.string.export_invalid_period));
        }
        if (!ReportFormat.isPdf(filter.format) && !ReportFormat.isXlsx(filter.format)) {
            throw new IllegalArgumentException(appContext.getString(R.string.export_invalid_format));
        }
    }

    private File buildReportFile(String format) {
        String extension = ReportFormat.isPdf(format) ? ".pdf" : ".xlsx";
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmm", Locale.US));
        String fileName = "relatorio_abastecimento_" + timestamp + extension;

        File baseDir = appContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if (baseDir == null) {
            baseDir = new File(appContext.getCacheDir(), "reports");
        } else {
            baseDir = new File(baseDir, "reports");
        }
        if (!baseDir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            baseDir.mkdirs();
        }
        return new File(baseDir, fileName);
    }
}
