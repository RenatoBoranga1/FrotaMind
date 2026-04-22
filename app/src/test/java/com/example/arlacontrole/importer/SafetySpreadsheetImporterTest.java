package com.example.arlacontrole.importer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

public class SafetySpreadsheetImporterTest {

    @Test
    public void read_shouldParseCsvWithExpectedSchema() throws Exception {
        String csv =
            "Id;Data;Identificador/Placa;Frota;Criticidade;Status;QUANTIDADE;Motorista;Localidade;Tipo de Evento;Nome;Motivo;Ultima Observacao;Possui evidencia?\n" +
            "evt-1;13/04/2025 21:47;SFF4I73;8013481;Grave;Finalizado;5;ANTONIO MARCOS;Santa Cruz;Aceleracao;Excesso de velocidade;Violacao valida;Tratativa iniciada;Sim\n";

        SafetySpreadsheetImporter importer = new SafetySpreadsheetImporter();
        SafetySpreadsheetImporter.SpreadsheetData data = importer.read(
            new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)),
            "basedadosseguranca.csv"
        );

        assertEquals(1, data.rows.size());
        assertEquals("evt-1", data.rows.get(0).get(SafetySpreadsheetImporter.COLUMN_ID));
        assertEquals("SFF4I73", data.rows.get(0).get(SafetySpreadsheetImporter.COLUMN_PLATE));
        assertEquals("5", data.rows.get(0).get(SafetySpreadsheetImporter.COLUMN_QUANTITY));
        assertEquals("Excesso de velocidade", data.rows.get(0).get(SafetySpreadsheetImporter.COLUMN_NAME));
    }

    @Test
    public void read_shouldFailWhenRequiredColumnsAreMissing() {
        String csv = "Motorista;Localidade\nJoao;Base\n";
        SafetySpreadsheetImporter importer = new SafetySpreadsheetImporter();

        try {
            importer.read(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)), "invalido.csv");
        } catch (Exception exception) {
            assertTrue(exception.getMessage().contains("Cabecalho invalido") || exception.getMessage().contains("Colunas obrigatorias"));
            return;
        }

        throw new AssertionError("Era esperado erro por colunas obrigatorias ausentes.");
    }

    @Test
    public void read_shouldAcceptMaxtrackStyleAliases() throws Exception {
        String csv =
            "Data/Hora;Placa;Condutor;Local;Evento do Motorista;Severidade;Status;Qtd;Observacoes\n" +
            "13/04/2025 21:47;SFF4I73;ANTONIO MARCOS;Santa Cruz;Excesso de velocidade;Grave;Finalizado;3;Telemetria Maxtrack\n";

        SafetySpreadsheetImporter importer = new SafetySpreadsheetImporter();
        SafetySpreadsheetImporter.SpreadsheetData data = importer.read(
            new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)),
            "maxtrack-eventos.csv"
        );

        assertEquals(1, data.rows.size());
        assertEquals("SFF4I73", data.rows.get(0).get(SafetySpreadsheetImporter.COLUMN_PLATE));
        assertEquals("ANTONIO MARCOS", data.rows.get(0).get(SafetySpreadsheetImporter.COLUMN_DRIVER));
        assertEquals("Excesso de velocidade", data.rows.get(0).get(SafetySpreadsheetImporter.COLUMN_EVENT_DETAIL));
        assertEquals("3", data.rows.get(0).get(SafetySpreadsheetImporter.COLUMN_QUANTITY));
    }
}
