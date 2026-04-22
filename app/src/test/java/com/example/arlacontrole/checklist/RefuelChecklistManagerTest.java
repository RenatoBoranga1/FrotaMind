package com.example.arlacontrole.checklist;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class RefuelChecklistManagerTest {

    @Test
    public void applyEvidenceState_marksAutomaticEvidenceItem() {
        List<ChecklistItemState> items = Arrays.asList(
            new ChecklistItemState("item_a", "Item A", true, false),
            new ChecklistItemState("evidencia", "Foto obrigatoria", false, true)
        );

        RefuelChecklistManager.applyEvidenceState(items, true);

        assertTrue(items.get(1).checked);
    }

    @Test
    public void isComplete_returnsFalseWhenItemIsUnchecked() {
        List<ChecklistItemState> items = Arrays.asList(
            new ChecklistItemState("a", "A", true, false),
            new ChecklistItemState("b", "B", false, false)
        );

        assertFalse(RefuelChecklistManager.isComplete(items));
    }
}
