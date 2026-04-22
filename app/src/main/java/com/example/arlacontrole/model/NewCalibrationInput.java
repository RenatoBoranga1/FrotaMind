package com.example.arlacontrole.model;

import java.util.ArrayList;
import java.util.List;

public class NewCalibrationInput {
    public String vehiclePlate;
    public String calibrationAtIso;
    public int odometerKm;
    public String notes;
    public String registeredByName;
    public final List<String> photoPaths = new ArrayList<>();
    public final List<String> videoPaths = new ArrayList<>();
}
