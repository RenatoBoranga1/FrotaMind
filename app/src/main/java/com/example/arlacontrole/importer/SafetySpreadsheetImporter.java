package com.example.arlacontrole.importer;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SafetySpreadsheetImporter {

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_DATE = "data";
    public static final String COLUMN_PLATE = "identificador/placa";
    public static final String COLUMN_FLEET = "frota";
    public static final String COLUMN_SEVERITY = "criticidade";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_QUANTITY = "quantidade";
    public static final String COLUMN_DRIVER = "motorista";
    public static final String COLUMN_LOCATION = "localidade";
    public static final String COLUMN_EVENT_DETAIL = "tipo de evento";
    public static final String COLUMN_NAME = "nome";
    public static final String COLUMN_DESCRIPTION = "descricao";
    public static final String COLUMN_REASON = "motivo";
    public static final String COLUMN_LAST_NOTE = "ultima observacao";
    public static final String COLUMN_HAS_EVIDENCE = "possui evidencia?";

    private static final Set<String> REQUIRED_COLUMNS;
    private static final Map<String, String> HEADER_ALIASES = buildHeaderAliases();

    static {
        LinkedHashSet<String> required = new LinkedHashSet<>();
        required.add(COLUMN_DATE);
        required.add(COLUMN_PLATE);
        required.add(COLUMN_DRIVER);
        required.add(COLUMN_LOCATION);
        required.add(COLUMN_EVENT_DETAIL);
        required.add(COLUMN_SEVERITY);
        required.add(COLUMN_STATUS);
        required.add(COLUMN_QUANTITY);
        REQUIRED_COLUMNS = Collections.unmodifiableSet(required);
    }

    public SpreadsheetData read(InputStream inputStream, String fileName) throws IOException {
        List<List<String>> rows = isCsv(fileName) ? readCsv(inputStream) : readXlsx(inputStream);
        return mapRows(rows, fileName);
    }

    private SpreadsheetData mapRows(List<List<String>> rows, String fileName) {
        if (rows == null || rows.isEmpty()) {
            throw new IllegalArgumentException("A planilha nao possui linhas para importacao.");
        }

        int headerRowIndex = -1;
        List<String> normalizedHeaders = new ArrayList<>();
        for (int index = 0; index < rows.size(); index++) {
            List<String> row = rows.get(index);
            if (isEmptyRow(row)) {
                continue;
            }
            headerRowIndex = index;
            for (String header : row) {
                normalizedHeaders.add(resolveCanonicalHeader(normalizeHeader(header)));
            }
            break;
        }

        if (headerRowIndex < 0) {
            throw new IllegalArgumentException("Nao foi possivel localizar o cabecalho da planilha.");
        }

        List<String> missingColumns = new ArrayList<>();
        for (String required : REQUIRED_COLUMNS) {
            if (!normalizedHeaders.contains(required)) {
                missingColumns.add(required);
            }
        }
        if (missingColumns.size() == REQUIRED_COLUMNS.size()) {
            throw new IllegalArgumentException("Cabecalho invalido. Use a mesma estrutura do arquivo basedadosseguranca.xlsx.");
        }
        if (!missingColumns.isEmpty()) {
            throw new IllegalArgumentException("Colunas obrigatorias ausentes: " + String.join(", ", missingColumns));
        }

        SpreadsheetData data = new SpreadsheetData();
        data.fileName = fileName == null ? "" : fileName;
        for (int index = headerRowIndex + 1; index < rows.size(); index++) {
            List<String> row = rows.get(index);
            if (isEmptyRow(row)) {
                continue;
            }
            SpreadsheetRow item = new SpreadsheetRow();
            item.rowNumber = index + 1;
            for (int columnIndex = 0; columnIndex < normalizedHeaders.size(); columnIndex++) {
                String header = normalizedHeaders.get(columnIndex);
                if (header.isEmpty()) {
                    continue;
                }
                String value = columnIndex < row.size() ? safe(row.get(columnIndex)) : "";
                item.values.put(header, value);
            }
            data.rows.add(item);
        }
        return data;
    }

    private boolean isCsv(String fileName) {
        return fileName != null && fileName.toLowerCase(Locale.ROOT).endsWith(".csv");
    }

    private List<List<String>> readCsv(InputStream inputStream) throws IOException {
        List<List<String>> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                rows.add(parseDelimitedLine(line));
            }
        }
        return rows;
    }

    private List<String> parseDelimitedLine(String line) {
        List<String> values = new ArrayList<>();
        if (line == null) {
            values.add("");
            return values;
        }
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int index = 0; index < line.length(); index++) {
            char value = line.charAt(index);
            if (value == '"') {
                if (quoted && index + 1 < line.length() && line.charAt(index + 1) == '"') {
                    current.append('"');
                    index++;
                } else {
                    quoted = !quoted;
                }
            } else if (value == ';' && !quoted) {
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(value);
            }
        }
        values.add(current.toString());
        return values;
    }

    private List<List<String>> readXlsx(InputStream inputStream) throws IOException {
        Map<String, byte[]> entries = unzipEntries(inputStream);
        List<String> sharedStrings = parseSharedStrings(entries.get("xl/sharedStrings.xml"));
        String sheetEntry = resolveFirstSheet(entries);
        if (sheetEntry == null) {
            throw new IllegalArgumentException("Nenhuma aba valida foi encontrada na planilha Excel.");
        }
        return parseWorksheet(entries.get(sheetEntry), sharedStrings);
    }

    private Map<String, byte[]> unzipEntries(InputStream inputStream) throws IOException {
        Map<String, byte[]> entries = new LinkedHashMap<>();
        try (ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
            ZipEntry entry;
            byte[] buffer = new byte[4096];
            while ((entry = zipInputStream.getNextEntry()) != null) {
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                int count;
                while ((count = zipInputStream.read(buffer)) != -1) {
                    output.write(buffer, 0, count);
                }
                entries.put(entry.getName(), output.toByteArray());
                zipInputStream.closeEntry();
            }
        }
        return entries;
    }

    private List<String> parseSharedStrings(byte[] content) throws IOException {
        List<String> values = new ArrayList<>();
        if (content == null || content.length == 0) {
            return values;
        }
        Document document = parseXml(content);
        NodeList items = document.getElementsByTagName("si");
        for (int index = 0; index < items.getLength(); index++) {
            Element element = (Element) items.item(index);
            NodeList texts = element.getElementsByTagName("t");
            StringBuilder builder = new StringBuilder();
            for (int textIndex = 0; textIndex < texts.getLength(); textIndex++) {
                builder.append(texts.item(textIndex).getTextContent());
            }
            values.add(builder.toString());
        }
        return values;
    }

    private String resolveFirstSheet(Map<String, byte[]> entries) {
        List<String> sheetNames = new ArrayList<>();
        for (String name : entries.keySet()) {
            if (name.startsWith("xl/worksheets/") && name.endsWith(".xml")) {
                sheetNames.add(name);
            }
        }
        Collections.sort(sheetNames);
        return sheetNames.isEmpty() ? null : sheetNames.get(0);
    }

    private List<List<String>> parseWorksheet(byte[] content, List<String> sharedStrings) throws IOException {
        Document document = parseXml(content);
        NodeList rowNodes = document.getElementsByTagName("row");
        List<List<String>> rows = new ArrayList<>();
        for (int rowIndex = 0; rowIndex < rowNodes.getLength(); rowIndex++) {
            Element rowElement = (Element) rowNodes.item(rowIndex);
            NodeList cellNodes = rowElement.getElementsByTagName("c");
            TreeMap<Integer, String> values = new TreeMap<>();
            int lastColumn = -1;
            for (int cellIndex = 0; cellIndex < cellNodes.getLength(); cellIndex++) {
                Element cell = (Element) cellNodes.item(cellIndex);
                int column = resolveColumnIndex(cell.getAttribute("r"));
                lastColumn = Math.max(lastColumn, column);
                values.put(column, resolveCellValue(cell, sharedStrings));
            }
            List<String> row = new ArrayList<>();
            for (int column = 0; column <= Math.max(lastColumn, 0); column++) {
                row.add(values.getOrDefault(column, ""));
            }
            rows.add(row);
        }
        return rows;
    }

    private String resolveCellValue(Element cell, List<String> sharedStrings) {
        String type = cell.getAttribute("t");
        if ("inlineStr".equals(type)) {
            NodeList texts = cell.getElementsByTagName("t");
            return texts.getLength() == 0 ? "" : safe(texts.item(0).getTextContent());
        }

        NodeList valueNodes = cell.getElementsByTagName("v");
        if (valueNodes.getLength() == 0) {
            return "";
        }
        String raw = safe(valueNodes.item(0).getTextContent());
        if ("s".equals(type)) {
            try {
                int index = Integer.parseInt(raw);
                return index >= 0 && index < sharedStrings.size() ? sharedStrings.get(index) : "";
            } catch (NumberFormatException ignored) {
                return "";
            }
        }
        return raw;
    }

    private Document parseXml(byte[] content) throws IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            return factory.newDocumentBuilder().parse(new ByteArrayInputStream(content));
        } catch (Exception exception) {
            throw new IOException("Nao foi possivel ler a estrutura da planilha Excel.", exception);
        }
    }

    private int resolveColumnIndex(String reference) {
        if (reference == null || reference.trim().isEmpty()) {
            return 0;
        }
        int value = 0;
        for (int index = 0; index < reference.length(); index++) {
            char current = Character.toUpperCase(reference.charAt(index));
            if (!Character.isLetter(current)) {
                break;
            }
            value = (value * 26) + (current - 'A' + 1);
        }
        return Math.max(0, value - 1);
    }

    private boolean isEmptyRow(List<String> row) {
        if (row == null || row.isEmpty()) {
            return true;
        }
        for (String value : row) {
            if (!safe(value).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private String normalizeHeader(String value) {
        String normalized = safe(value)
            .replace('\uFEFF', ' ')
            .replace('\u00A0', ' ');
        normalized = Normalizer.normalize(normalized, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .toLowerCase(Locale.ROOT)
            .replaceAll("\\s+", " ")
            .trim();
        return normalized;
    }

    private String resolveCanonicalHeader(String normalizedHeader) {
        if (normalizedHeader == null || normalizedHeader.trim().isEmpty()) {
            return "";
        }
        return HEADER_ALIASES.getOrDefault(normalizedHeader, normalizedHeader);
    }

    private static Map<String, String> buildHeaderAliases() {
        Map<String, String> aliases = new HashMap<>();
        registerAlias(aliases, COLUMN_ID, "id", "codigo", "codigo do evento", "id do evento");
        registerAlias(aliases, COLUMN_DATE, "data", "data/hora", "data hora", "data e hora", "data do evento", "horario", "hora");
        registerAlias(aliases, COLUMN_PLATE, "identificador/placa", "placa", "veiculo", "veiculo/placa", "placa cavalo", "identificador");
        registerAlias(aliases, COLUMN_FLEET, "frota", "cod frota", "codigo da frota");
        registerAlias(aliases, COLUMN_SEVERITY, "criticidade", "severidade", "gravidade", "nivel");
        registerAlias(aliases, COLUMN_STATUS, "status", "situacao");
        registerAlias(aliases, COLUMN_QUANTITY, "quantidade", "qtd", "ocorrencias", "total de ocorrencias");
        registerAlias(aliases, COLUMN_DRIVER, "motorista", "condutor", "nome do motorista");
        registerAlias(aliases, COLUMN_LOCATION, "localidade", "local", "cidade", "endereco");
        registerAlias(aliases, COLUMN_EVENT_DETAIL, "tipo de evento", "evento", "evento do motorista", "descricao do evento", "tipo");
        registerAlias(aliases, COLUMN_NAME, "nome", "titulo");
        registerAlias(aliases, COLUMN_DESCRIPTION, "descricao", "descricao resumida");
        registerAlias(aliases, COLUMN_REASON, "motivo", "causa", "causa provavel");
        registerAlias(aliases, COLUMN_LAST_NOTE, "ultima observacao", "observacao", "observacoes", "nota");
        registerAlias(aliases, COLUMN_HAS_EVIDENCE, "possui evidencia?", "evidencia", "possui evidencia", "anexo");
        return aliases;
    }

    private static void registerAlias(Map<String, String> aliases, String canonical, String... values) {
        for (String value : values) {
            aliases.put(value, canonical);
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    public static class SpreadsheetData {
        public String fileName = "";
        public final List<SpreadsheetRow> rows = new ArrayList<>();
    }

    public static class SpreadsheetRow {
        public int rowNumber;
        public final Map<String, String> values = new LinkedHashMap<>();

        public String get(String key) {
            return values.getOrDefault(key, "");
        }
    }
}
