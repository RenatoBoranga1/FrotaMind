package com.example.arlacontrole.export.pdf;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;

import com.example.arlacontrole.R;
import com.example.arlacontrole.data.local.RefuelEntity;
import com.example.arlacontrole.export.ReportDataset;
import com.example.arlacontrole.export.ReportFilter;
import com.example.arlacontrole.export.ReportSummary;
import com.example.arlacontrole.utils.FormatUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class PdfReportGenerator {

    private static final int PAGE_WIDTH = 842;
    private static final int PAGE_HEIGHT = 595;
    private static final int MARGIN = 28;
    private static final int FOOTER_HEIGHT = 24;

    public void generate(Context context, OutputStream outputStream, ReportDataset dataset) throws IOException {
        PdfDocument document = new PdfDocument();

        Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setColor(Color.WHITE);
        titlePaint.setTextSize(18f);
        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        Paint headerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        headerPaint.setColor(Color.parseColor("#12344D"));
        headerPaint.setTextSize(11f);
        headerPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        Paint bodyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bodyPaint.setColor(Color.parseColor("#243746"));
        bodyPaint.setTextSize(9.5f);

        Paint smallPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        smallPaint.setColor(Color.parseColor("#5A6B7B"));
        smallPaint.setTextSize(9f);

        int pageNumber = 1;
        PdfDocument.Page page = document.startPage(new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create());
        Canvas canvas = page.getCanvas();
        float y = drawPageTop(canvas, backgroundPaint, titlePaint, bodyPaint, smallPaint, context, dataset, pageNumber);
        y = drawSummary(canvas, backgroundPaint, headerPaint, smallPaint, dataset.summary, y);
        y = drawTableHeader(canvas, backgroundPaint, headerPaint, y);

        for (int index = 0; index < dataset.records.size(); index++) {
            if (y + 20 > PAGE_HEIGHT - MARGIN - FOOTER_HEIGHT) {
                drawFooter(canvas, smallPaint, pageNumber);
                document.finishPage(page);
                pageNumber++;
                page = document.startPage(new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create());
                canvas = page.getCanvas();
                y = drawPageTop(canvas, backgroundPaint, titlePaint, bodyPaint, smallPaint, context, dataset, pageNumber);
                y = drawTableHeader(canvas, backgroundPaint, headerPaint, y);
            }
            y = drawRow(canvas, backgroundPaint, bodyPaint, context, dataset.records.get(index), y, index % 2 == 0);
        }

        drawFooter(canvas, smallPaint, pageNumber);
        document.finishPage(page);
        document.writeTo(outputStream);
        document.close();
    }

    private float drawPageTop(
        Canvas canvas,
        Paint backgroundPaint,
        Paint titlePaint,
        Paint bodyPaint,
        Paint smallPaint,
        Context context,
        ReportDataset dataset,
        int pageNumber
    ) {
        backgroundPaint.setColor(Color.parseColor("#12344D"));
        canvas.drawRoundRect(new RectF(MARGIN, MARGIN, PAGE_WIDTH - MARGIN, MARGIN + 54), 18f, 18f, backgroundPaint);
        canvas.drawText(context.getString(R.string.export_report_title), MARGIN + 18, MARGIN + 24, titlePaint);
        canvas.drawText(dataset.systemName, MARGIN + 18, MARGIN + 42, bodyPaint);

        float y = MARGIN + 72;
        canvas.drawText(context.getString(R.string.export_generated_at, FormatUtils.formatDateTime(dataset.generatedAtIso)), MARGIN, y, smallPaint);
        y += 14;
        canvas.drawText(context.getString(R.string.export_period_label, buildPeriodLabel(context, dataset.filter)), MARGIN, y, smallPaint);
        y += 14;
        for (String line : wrapText(context.getString(R.string.export_filters_label, buildFilterLabel(context, dataset.filter)), smallPaint, PAGE_WIDTH - (MARGIN * 2))) {
            canvas.drawText(line, MARGIN, y, smallPaint);
            y += 12;
        }
        canvas.drawText("Pagina " + pageNumber, PAGE_WIDTH - MARGIN - 42, MARGIN + 42, smallPaint);
        return y + 8;
    }

    private float drawSummary(Canvas canvas, Paint backgroundPaint, Paint headerPaint, Paint smallPaint, ReportSummary summary, float startY) {
        float cardWidth = (PAGE_WIDTH - (MARGIN * 2) - 24) / 4f;
        String[] titles = {"ARLA", "DIESEL", "REGISTROS", "ALERTAS"};
        String[] values = {
            FormatUtils.formatLiters(summary.totalArlaLiters),
            FormatUtils.formatLiters(summary.totalDieselLiters),
            String.valueOf(summary.totalRecords),
            String.valueOf(summary.alertCount)
        };

        for (int index = 0; index < titles.length; index++) {
            float left = MARGIN + (index * (cardWidth + 8));
            backgroundPaint.setColor(Color.parseColor("#F4F7FA"));
            canvas.drawRoundRect(new RectF(left, startY, left + cardWidth, startY + 48), 14f, 14f, backgroundPaint);
            canvas.drawText(titles[index], left + 12, startY + 18, smallPaint);
            canvas.drawText(values[index], left + 12, startY + 36, headerPaint);
        }
        canvas.drawText("Media por veiculo: " + FormatUtils.formatLiters(summary.averageLitersPerVehicle), MARGIN, startY + 66, smallPaint);
        return startY + 86;
    }

    private float drawTableHeader(Canvas canvas, Paint backgroundPaint, Paint headerPaint, float startY) {
        backgroundPaint.setColor(Color.parseColor("#E9EEF3"));
        canvas.drawRoundRect(new RectF(MARGIN, startY, PAGE_WIDTH - MARGIN, startY + 22), 8f, 8f, backgroundPaint);
        float[] columns = columnStarts();
        String[] headers = {"Data/Hora", "Tipo", "Veiculo", "Motorista", "Litros", "Odom.", "Local/Posto", "Status", "Observacao"};
        for (int index = 0; index < headers.length; index++) {
            canvas.drawText(headers[index], columns[index], startY + 15, headerPaint);
        }
        return startY + 28;
    }

    private float drawRow(Canvas canvas, Paint backgroundPaint, Paint bodyPaint, Context context, RefuelEntity entity, float startY, boolean striped) {
        if (striped) {
            backgroundPaint.setColor(Color.parseColor("#FAFBFC"));
            canvas.drawRoundRect(new RectF(MARGIN, startY - 12, PAGE_WIDTH - MARGIN, startY + 8), 6f, 6f, backgroundPaint);
        }
        float[] columns = columnStarts();
        String[] values = {
            truncate(FormatUtils.formatDateTime(entity.suppliedAtIso), bodyPaint, 86),
            truncate(FormatUtils.formatFuelType(context, entity.fuelType), bodyPaint, 46),
            truncate(entity.vehiclePlate + " / " + entity.vehicleFleetCode, bodyPaint, 86),
            truncate(entity.driverName, bodyPaint, 84),
            truncate(FormatUtils.formatLiters(entity.liters), bodyPaint, 48),
            truncate(FormatUtils.formatKilometers(entity.odometerKm), bodyPaint, 58),
            truncate(entity.locationName, bodyPaint, 116),
            truncate(FormatUtils.formatStatus(context, entity.statusLevel), bodyPaint, 56),
            truncate(entity.notes == null || entity.notes.trim().isEmpty() ? "Sem observacao" : entity.notes, bodyPaint, 190)
        };
        for (int index = 0; index < values.length; index++) {
            canvas.drawText(values[index], columns[index], startY, bodyPaint);
        }
        return startY + 20;
    }

    private void drawFooter(Canvas canvas, Paint smallPaint, int pageNumber) {
        canvas.drawText("Pagina " + pageNumber, PAGE_WIDTH - MARGIN - 42, PAGE_HEIGHT - MARGIN, smallPaint);
    }

    private float[] columnStarts() {
        return new float[] {MARGIN + 6, 122, 178, 268, 362, 420, 480, 606, 674};
    }

    private String buildPeriodLabel(Context context, ReportFilter filter) {
        if (filter.startDate == null && filter.endDate == null) {
            return context.getString(R.string.export_all_period);
        }
        String start = filter.startDate == null ? "--" : filter.startDate.toString();
        String end = filter.endDate == null ? "--" : filter.endDate.toString();
        return start + " a " + end;
    }

    private String buildFilterLabel(Context context, ReportFilter filter) {
        List<String> parts = new ArrayList<>();
        parts.add(filter.fuelType == null || filter.fuelType.isEmpty() ? context.getString(R.string.fuel_filter_all) : filter.fuelType);
        if (filter.vehiclePlate != null && !filter.vehiclePlate.trim().isEmpty()) {
            parts.add("Veiculo: " + filter.vehiclePlate.trim().toUpperCase());
        }
        if (filter.driverName != null && !filter.driverName.trim().isEmpty()) {
            parts.add("Motorista: " + filter.driverName.trim());
        }
        if (filter.statusLevel != null && !filter.statusLevel.trim().isEmpty()) {
            parts.add("Status: " + filter.statusLevel.trim());
        }
        if (ReportFilter.SYNC_SYNCED.equals(filter.syncFilter)) {
            parts.add("Sincronizacao: sincronizados");
        } else if (ReportFilter.SYNC_PENDING.equals(filter.syncFilter)) {
            parts.add("Sincronizacao: pendentes");
        } else {
            parts.add("Sincronizacao: todos");
        }
        return String.join(" | ", parts);
    }

    private List<String> wrapText(String text, Paint paint, int maxWidth) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.trim().isEmpty()) {
            lines.add("");
            return lines;
        }
        String[] words = text.split("\\s+");
        StringBuilder current = new StringBuilder();
        for (String word : words) {
            String candidate = current.length() == 0 ? word : current + " " + word;
            if (paint.measureText(candidate) <= maxWidth) {
                current.setLength(0);
                current.append(candidate);
            } else {
                if (current.length() > 0) {
                    lines.add(current.toString());
                    current.setLength(0);
                }
                current.append(word);
            }
        }
        if (current.length() > 0) {
            lines.add(current.toString());
        }
        return lines;
    }

    private String truncate(String value, Paint paint, int maxWidth) {
        String safe = value == null ? "" : value.trim();
        if (safe.isEmpty()) {
            return "-";
        }
        if (paint.measureText(safe) <= maxWidth) {
            return safe;
        }
        String ellipsis = "...";
        while (safe.length() > 1 && paint.measureText(safe + ellipsis) > maxWidth) {
            safe = safe.substring(0, safe.length() - 1);
        }
        return safe + ellipsis;
    }
}
