package com.example.arlacontrole.export.excel;

import com.example.arlacontrole.data.local.RefuelEntity;
import com.example.arlacontrole.export.ReportDataset;
import com.example.arlacontrole.export.ReportFilter;
import com.example.arlacontrole.utils.FormatUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ExcelReportGenerator {

    public void generate(OutputStream outputStream, ReportDataset dataset) throws IOException {
        try (ZipOutputStream zip = new ZipOutputStream(outputStream, StandardCharsets.UTF_8)) {
            writeEntry(zip, "[Content_Types].xml", buildContentTypes());
            writeEntry(zip, "_rels/.rels", buildRootRels());
            writeEntry(zip, "docProps/app.xml", buildAppProps());
            writeEntry(zip, "docProps/core.xml", buildCoreProps(dataset));
            writeEntry(zip, "xl/workbook.xml", buildWorkbook());
            writeEntry(zip, "xl/_rels/workbook.xml.rels", buildWorkbookRels());
            writeEntry(zip, "xl/styles.xml", buildStyles());
            writeEntry(zip, "xl/worksheets/sheet1.xml", buildDataSheet(dataset));
            writeEntry(zip, "xl/worksheets/sheet2.xml", buildSummarySheet(dataset));
        }
    }

    private void writeEntry(ZipOutputStream zip, String name, String content) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(content.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private String buildContentTypes() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">" +
            "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>" +
            "<Default Extension=\"xml\" ContentType=\"application/xml\"/>" +
            "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>" +
            "<Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>" +
            "<Override PartName=\"/xl/worksheets/sheet2.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>" +
            "<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>" +
            "<Override PartName=\"/docProps/core.xml\" ContentType=\"application/vnd.openxmlformats-package.core-properties+xml\"/>" +
            "<Override PartName=\"/docProps/app.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.extended-properties+xml\"/>" +
            "</Types>";
    }

    private String buildRootRels() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">" +
            "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>" +
            "<Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties\" Target=\"docProps/core.xml\"/>" +
            "<Relationship Id=\"rId3\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties\" Target=\"docProps/app.xml\"/>" +
            "</Relationships>";
    }

    private String buildAppProps() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<Properties xmlns=\"http://schemas.openxmlformats.org/officeDocument/2006/extended-properties\" " +
            "xmlns:vt=\"http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes\">" +
            "<Application>FrotaMind</Application>" +
            "</Properties>";
    }

    private String buildCoreProps(ReportDataset dataset) {
        String created = xml(dataset.generatedAtIso);
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<cp:coreProperties xmlns:cp=\"http://schemas.openxmlformats.org/package/2006/metadata/core-properties\" " +
            "xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:dcterms=\"http://purl.org/dc/terms/\" " +
            "xmlns:dcmitype=\"http://purl.org/dc/dcmitype/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
            "<dc:title>Relatorio de abastecimentos</dc:title>" +
            "<dc:creator>" + xml(dataset.systemName) + "</dc:creator>" +
            "<cp:lastModifiedBy>" + xml(dataset.systemName) + "</cp:lastModifiedBy>" +
            "<dcterms:created xsi:type=\"dcterms:W3CDTF\">" + created + "</dcterms:created>" +
            "<dcterms:modified xsi:type=\"dcterms:W3CDTF\">" + created + "</dcterms:modified>" +
            "</cp:coreProperties>";
    }

    private String buildWorkbook() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" " +
            "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">" +
            "<sheets>" +
            "<sheet name=\"Lancamentos\" sheetId=\"1\" r:id=\"rId1\"/>" +
            "<sheet name=\"Resumo\" sheetId=\"2\" r:id=\"rId2\"/>" +
            "</sheets>" +
            "</workbook>";
    }

    private String buildWorkbookRels() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">" +
            "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/>" +
            "<Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet2.xml\"/>" +
            "<Relationship Id=\"rId3\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/>" +
            "</Relationships>";
    }

    private String buildStyles() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">" +
            "<fonts count=\"3\">" +
            "<font><sz val=\"11\"/><name val=\"Calibri\"/></font>" +
            "<font><b/><sz val=\"11\"/><name val=\"Calibri\"/></font>" +
            "<font><b/><sz val=\"14\"/><name val=\"Calibri\"/></font>" +
            "</fonts>" +
            "<fills count=\"3\">" +
            "<fill><patternFill patternType=\"none\"/></fill>" +
            "<fill><patternFill patternType=\"gray125\"/></fill>" +
            "<fill><patternFill patternType=\"solid\"><fgColor rgb=\"FFDCE8F2\"/><bgColor indexed=\"64\"/></patternFill></fill>" +
            "</fills>" +
            "<borders count=\"1\"><border><left/><right/><top/><bottom/><diagonal/></border></borders>" +
            "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>" +
            "<cellXfs count=\"4\">" +
            "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyAlignment=\"1\"><alignment vertical=\"center\"/></xf>" +
            "<xf numFmtId=\"0\" fontId=\"2\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyAlignment=\"1\"><alignment horizontal=\"center\" vertical=\"center\"/></xf>" +
            "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"2\" borderId=\"0\" xfId=\"0\" applyAlignment=\"1\"><alignment horizontal=\"center\" vertical=\"center\"/></xf>" +
            "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyAlignment=\"1\"><alignment vertical=\"center\"/></xf>" +
            "</cellXfs>" +
            "</styleSheet>";
    }

    private String buildDataSheet(ReportDataset dataset) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        xml.append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">");
        xml.append("<cols>");
        xml.append(col(1, 18)).append(col(2, 12)).append(col(3, 18)).append(col(4, 22)).append(col(5, 12));
        xml.append(col(6, 14)).append(col(7, 24)).append(col(8, 12)).append(col(9, 36)).append(col(10, 16));
        xml.append("</cols><sheetData>");
        int row = 1;
        xml.append(rowStart(row)).append(stringCell("A", row, "Relatorio de abastecimentos", 1)).append(rowEnd());
        row++;
        xml.append(rowStart(row)).append(stringCell("A", row, dataset.systemName, 0)).append(rowEnd());
        row++;
        xml.append(rowStart(row)).append(stringCell("A", row, "Gerado em: " + FormatUtils.formatDateTime(dataset.generatedAtIso), 0)).append(rowEnd());
        row++;
        xml.append(rowStart(row)).append(stringCell("A", row, "Periodo: " + buildPeriod(dataset.filter), 0)).append(rowEnd());
        row++;
        xml.append(rowStart(row)).append(stringCell("A", row, "Filtros: " + buildFilters(dataset.filter), 0)).append(rowEnd());
        row++;
        xml.append(rowStart(row));
        xml.append(stringCell("A", row, "Data/Hora", 2)).append(stringCell("B", row, "Tipo", 2));
        xml.append(stringCell("C", row, "Veiculo", 2)).append(stringCell("D", row, "Motorista", 2));
        xml.append(stringCell("E", row, "Litros", 2)).append(stringCell("F", row, "Odometro", 2));
        xml.append(stringCell("G", row, "Local/Posto", 2)).append(stringCell("H", row, "Status", 2));
        xml.append(stringCell("I", row, "Observacao", 2)).append(stringCell("J", row, "Sincronizado", 2));
        xml.append(rowEnd());
        row++;
        for (RefuelEntity entity : dataset.records) {
            xml.append(rowStart(row));
            xml.append(stringCell("A", row, FormatUtils.formatDateTime(entity.suppliedAtIso), 0));
            xml.append(stringCell("B", row, entity.fuelType, 0));
            xml.append(stringCell("C", row, entity.vehiclePlate + " / " + entity.vehicleFleetCode, 0));
            xml.append(stringCell("D", row, entity.driverName, 0));
            xml.append(numberCell("E", row, entity.liters));
            xml.append(numberCell("F", row, entity.odometerKm));
            xml.append(stringCell("G", row, entity.locationName, 0));
            xml.append(stringCell("H", row, entity.statusLevel, 0));
            xml.append(stringCell("I", row, entity.notes == null || entity.notes.trim().isEmpty() ? "Sem observacao" : entity.notes, 0));
            xml.append(stringCell("J", row, "SYNCED".equals(entity.syncStatus) ? "Sim" : "Nao", 0));
            xml.append(rowEnd());
            row++;
        }
        xml.append("</sheetData>");
        xml.append("<mergeCells count=\"5\"><mergeCell ref=\"A1:J1\"/><mergeCell ref=\"A2:J2\"/><mergeCell ref=\"A3:J3\"/><mergeCell ref=\"A4:J4\"/><mergeCell ref=\"A5:J5\"/></mergeCells>");
        xml.append("</worksheet>");
        return xml.toString();
    }

    private String buildSummarySheet(ReportDataset dataset) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        xml.append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">");
        xml.append("<cols>").append(col(1, 28)).append(col(2, 22)).append("</cols><sheetData>");
        int row = 1;
        xml.append(rowStart(row)).append(stringCell("A", row, "Resumo executivo", 1)).append(rowEnd());
        row++;
        xml.append(rowStart(row)).append(stringCell("A", row, "Sistema", 3)).append(stringCell("B", row, dataset.systemName, 0)).append(rowEnd());
        row++;
        xml.append(rowStart(row)).append(stringCell("A", row, "Gerado em", 3)).append(stringCell("B", row, FormatUtils.formatDateTime(dataset.generatedAtIso), 0)).append(rowEnd());
        row++;
        xml.append(rowStart(row)).append(stringCell("A", row, "Periodo", 3)).append(stringCell("B", row, buildPeriod(dataset.filter), 0)).append(rowEnd());
        row++;
        xml.append(rowStart(row)).append(stringCell("A", row, "Filtros", 3)).append(stringCell("B", row, buildFilters(dataset.filter), 0)).append(rowEnd());
        row += 2;
        xml.append(rowStart(row)).append(stringCell("A", row, "Total ARLA", 3)).append(numberCell("B", row, dataset.summary.totalArlaLiters)).append(rowEnd());
        row++;
        xml.append(rowStart(row)).append(stringCell("A", row, "Total DIESEL", 3)).append(numberCell("B", row, dataset.summary.totalDieselLiters)).append(rowEnd());
        row++;
        xml.append(rowStart(row)).append(stringCell("A", row, "Registros", 3)).append(numberCell("B", row, dataset.summary.totalRecords)).append(rowEnd());
        row++;
        xml.append(rowStart(row)).append(stringCell("A", row, "Alertas", 3)).append(numberCell("B", row, dataset.summary.alertCount)).append(rowEnd());
        row++;
        xml.append(rowStart(row)).append(stringCell("A", row, "Atencao", 3)).append(numberCell("B", row, dataset.summary.attentionCount)).append(rowEnd());
        row++;
        xml.append(rowStart(row)).append(stringCell("A", row, "Veiculos distintos", 3)).append(numberCell("B", row, dataset.summary.distinctVehicles)).append(rowEnd());
        row++;
        xml.append(rowStart(row)).append(stringCell("A", row, "Media por veiculo", 3)).append(numberCell("B", row, dataset.summary.averageLitersPerVehicle)).append(rowEnd());
        xml.append("</sheetData><mergeCells count=\"1\"><mergeCell ref=\"A1:B1\"/></mergeCells></worksheet>");
        return xml.toString();
    }

    private String buildPeriod(ReportFilter filter) {
        if (filter.startDate == null && filter.endDate == null) {
            return "Todo o periodo";
        }
        String start = filter.startDate == null ? "--" : filter.startDate.toString();
        String end = filter.endDate == null ? "--" : filter.endDate.toString();
        return start + " a " + end;
    }

    private String buildFilters(ReportFilter filter) {
        StringBuilder builder = new StringBuilder();
        builder.append(filter.fuelType == null || filter.fuelType.isEmpty() ? "Todos os tipos" : filter.fuelType);
        if (filter.vehiclePlate != null && !filter.vehiclePlate.trim().isEmpty()) {
            builder.append(" | Veiculo: ").append(filter.vehiclePlate.trim().toUpperCase());
        }
        if (filter.driverName != null && !filter.driverName.trim().isEmpty()) {
            builder.append(" | Motorista: ").append(filter.driverName.trim());
        }
        if (filter.statusLevel != null && !filter.statusLevel.trim().isEmpty()) {
            builder.append(" | Status: ").append(filter.statusLevel.trim());
        }
        if (ReportFilter.SYNC_SYNCED.equals(filter.syncFilter)) {
            builder.append(" | Somente sincronizados");
        } else if (ReportFilter.SYNC_PENDING.equals(filter.syncFilter)) {
            builder.append(" | Pendentes/falha");
        } else {
            builder.append(" | Todos os registros");
        }
        return builder.toString();
    }

    private String rowStart(int row) {
        return "<row r=\"" + row + "\">";
    }

    private String rowEnd() {
        return "</row>";
    }

    private String col(int index, int width) {
        return "<col min=\"" + index + "\" max=\"" + index + "\" width=\"" + width + "\" customWidth=\"1\"/>";
    }

    private String stringCell(String column, int row, String value, int style) {
        return "<c r=\"" + column + row + "\" s=\"" + style + "\" t=\"inlineStr\"><is><t>" + xml(value) + "</t></is></c>";
    }

    private String numberCell(String column, int row, double value) {
        return "<c r=\"" + column + row + "\"><v>" + value + "</v></c>";
    }

    private String xml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;");
    }
}
