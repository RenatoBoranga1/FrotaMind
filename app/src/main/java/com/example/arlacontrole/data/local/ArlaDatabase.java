package com.example.arlacontrole.data.local;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(
    entities = {
        DriverEntity.class,
        VehicleEntity.class,
        RefuelEntity.class,
        SyncQueueEntity.class,
        SafetyEventEntity.class,
        OdometerCalibrationEntity.class,
        OdometerCalibrationMediaEntity.class
    },
    version = 10,
    exportSchema = false
)
public abstract class ArlaDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "arla_controle.db";
    private static volatile ArlaDatabase INSTANCE;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(2);

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE vehicles ADD COLUMN fleet_code TEXT NOT NULL DEFAULT ''");
            database.execSQL("UPDATE vehicles SET fleet_code = 'FROTA 201' WHERE plate = 'BRA2E19' AND fleet_code = ''");
            database.execSQL("UPDATE vehicles SET fleet_code = 'FROTA 114' WHERE plate = 'QTM8A44' AND fleet_code = ''");
            database.execSQL("UPDATE vehicles SET fleet_code = 'FROTA 332' WHERE plate = 'FLE7K21' AND fleet_code = ''");
            database.execSQL("UPDATE vehicles SET fleet_code = 'FROTA 417' WHERE plate = 'RIT5B93' AND fleet_code = ''");
            database.execSQL("ALTER TABLE refuels ADD COLUMN vehicle_fleet_code TEXT NOT NULL DEFAULT ''");
            database.execSQL(
                "UPDATE refuels SET vehicle_fleet_code = COALESCE((SELECT fleet_code FROM vehicles WHERE vehicles.plate = refuels.vehicle_plate LIMIT 1), '') WHERE vehicle_fleet_code = ''"
            );
        }
    };

    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE vehicles ADD COLUMN expected_diesel_fill_min_liters REAL NOT NULL DEFAULT 180");
            database.execSQL("ALTER TABLE vehicles ADD COLUMN expected_diesel_fill_max_liters REAL NOT NULL DEFAULT 520");
            database.execSQL("ALTER TABLE vehicles ADD COLUMN expected_diesel_km_per_liter_min REAL NOT NULL DEFAULT 1.8");
            database.execSQL("ALTER TABLE vehicles ADD COLUMN expected_diesel_km_per_liter_max REAL NOT NULL DEFAULT 3.2");
            database.execSQL("ALTER TABLE refuels ADD COLUMN fuel_type TEXT NOT NULL DEFAULT 'ARLA'");
            database.execSQL("ALTER TABLE refuels ADD COLUMN km_per_liter REAL");
        }
    };

    private static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE refuels ADD COLUMN evidence_photo_path TEXT NOT NULL DEFAULT ''");
            database.execSQL("ALTER TABLE refuels ADD COLUMN evidence_category TEXT NOT NULL DEFAULT ''");
        }
    };

    private static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE refuels ADD COLUMN checklist_payload TEXT NOT NULL DEFAULT ''");
            database.execSQL("ALTER TABLE refuels ADD COLUMN checklist_completed_at_iso TEXT NOT NULL DEFAULT ''");
        }
    };

    private static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE refuels ADD COLUMN total_amount REAL");
            database.execSQL("ALTER TABLE refuels ADD COLUMN data_entry_mode TEXT NOT NULL DEFAULT 'MANUAL'");
            database.execSQL("ALTER TABLE refuels ADD COLUMN ocr_status TEXT NOT NULL DEFAULT ''");
            database.execSQL("ALTER TABLE refuels ADD COLUMN ocr_raw_text TEXT NOT NULL DEFAULT ''");
            database.execSQL("ALTER TABLE refuels ADD COLUMN ocr_metadata_json TEXT NOT NULL DEFAULT ''");
        }
    };

    private static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS `safety_events` (" +
                    "`local_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`client_record_id` TEXT NOT NULL, " +
                    "`remote_id` INTEGER, " +
                    "`event_type` TEXT NOT NULL, " +
                    "`occurred_at_iso` TEXT NOT NULL, " +
                    "`vehicle_plate` TEXT NOT NULL, " +
                    "`vehicle_fleet_code` TEXT NOT NULL, " +
                    "`vehicle_model` TEXT NOT NULL, " +
                    "`driver_id` INTEGER NOT NULL, " +
                    "`driver_name` TEXT NOT NULL, " +
                    "`location_name` TEXT NOT NULL, " +
                    "`description` TEXT NOT NULL, " +
                    "`severity` TEXT NOT NULL, " +
                    "`probable_cause` TEXT NOT NULL, " +
                    "`notes` TEXT NOT NULL, " +
                    "`evidence_photo_path` TEXT NOT NULL, " +
                    "`evidence_category` TEXT NOT NULL, " +
                    "`analysis_status` TEXT NOT NULL, " +
                    "`synced` INTEGER NOT NULL, " +
                    "`sync_error` TEXT NOT NULL, " +
                    "`created_at` INTEGER NOT NULL, " +
                    "`updated_at` INTEGER NOT NULL)"
            );
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_safety_events_client_record_id` ON `safety_events` (`client_record_id`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_safety_events_vehicle_plate` ON `safety_events` (`vehicle_plate`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_safety_events_driver_id` ON `safety_events` (`driver_id`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_safety_events_analysis_status` ON `safety_events` (`analysis_status`)");
        }
    };

    private static final Migration MIGRATION_7_8 = new Migration(7, 8) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE safety_events ADD COLUMN occurrence_count INTEGER NOT NULL DEFAULT 1");
        }
    };

    private static final Migration MIGRATION_8_9 = new Migration(8, 9) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE refuels ADD COLUMN odometer_initial_km INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE refuels ADD COLUMN odometer_final_km INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE refuels ADD COLUMN calculated_arla_control_quantity REAL NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE refuels ADD COLUMN expected_initial_odometer_km INTEGER");
            database.execSQL("ALTER TABLE refuels ADD COLUMN odometer_divergence_km INTEGER");
            database.execSQL("UPDATE refuels SET odometer_final_km = odometer_km WHERE odometer_final_km = 0");
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS `odometer_calibrations` (" +
                    "`local_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`vehicle_plate` TEXT NOT NULL, " +
                    "`vehicle_fleet_code` TEXT NOT NULL, " +
                    "`vehicle_model` TEXT NOT NULL, " +
                    "`calibration_at_iso` TEXT NOT NULL, " +
                    "`odometer_km` INTEGER NOT NULL, " +
                    "`notes` TEXT NOT NULL, " +
                    "`registered_by_name` TEXT NOT NULL, " +
                    "`registered_at_iso` TEXT NOT NULL, " +
                    "`sync_status` TEXT NOT NULL, " +
                    "`sync_error` TEXT NOT NULL, " +
                    "`created_at` INTEGER NOT NULL, " +
                    "`synced_at` INTEGER)"
            );
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_odometer_calibrations_vehicle_plate` ON `odometer_calibrations` (`vehicle_plate`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_odometer_calibrations_calibration_at_iso` ON `odometer_calibrations` (`calibration_at_iso`)");
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS `odometer_calibration_media` (" +
                    "`local_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`calibration_local_id` INTEGER NOT NULL, " +
                    "`media_type` TEXT NOT NULL, " +
                    "`file_path` TEXT NOT NULL, " +
                    "`created_at` INTEGER NOT NULL)"
            );
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_odometer_calibration_media_calibration_local_id` ON `odometer_calibration_media` (`calibration_local_id`)");
        }
    };

    private static final Migration MIGRATION_9_10 = new Migration(9, 10) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE refuels ADD COLUMN price_per_liter REAL NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE refuels ADD COLUMN cost_per_km REAL NOT NULL DEFAULT 0");
            database.execSQL(
                "UPDATE refuels SET price_per_liter = CASE " +
                    "WHEN total_amount IS NOT NULL AND liters > 0 THEN total_amount / liters " +
                    "ELSE 0 END"
            );
            database.execSQL(
                "UPDATE refuels SET cost_per_km = CASE " +
                    "WHEN total_amount IS NOT NULL AND odometer_final_km > odometer_initial_km THEN total_amount / (odometer_final_km - odometer_initial_km) " +
                    "ELSE 0 END"
            );
        }
    };

    public abstract DriverDao driverDao();

    public abstract VehicleDao vehicleDao();

    public abstract RefuelDao refuelDao();

    public abstract SyncQueueDao syncQueueDao();

    public abstract SafetyEventDao safetyEventDao();

    public abstract OdometerCalibrationDao odometerCalibrationDao();

    public abstract OdometerCalibrationMediaDao odometerCalibrationMediaDao();

    public static ArlaDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (ArlaDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), ArlaDatabase.class, DATABASE_NAME)
                        .addMigrations(
                            MIGRATION_1_2,
                            MIGRATION_2_3,
                            MIGRATION_3_4,
                            MIGRATION_4_5,
                            MIGRATION_5_6,
                            MIGRATION_6_7,
                            MIGRATION_7_8,
                            MIGRATION_8_9,
                            MIGRATION_9_10
                        )
                        .fallbackToDestructiveMigration()
                        .build();
                }
            }
        }
        return INSTANCE;
    }

    public void seedDefaults() {
        databaseWriteExecutor.execute(() -> {
            if (driverDao().countDrivers() == 0) {
                driverDao().insertAll(defaultDrivers());
            }
            if (vehicleDao().countVehicles() == 0) {
                vehicleDao().insertAll(defaultVehicles());
            }
        });
    }

    private List<DriverEntity> defaultDrivers() {
        long now = System.currentTimeMillis();
        List<DriverEntity> drivers = new ArrayList<>();
        drivers.add(new DriverEntity("Carlos Mendes", true, now));
        drivers.add(new DriverEntity("Joao Pereira", true, now));
        drivers.add(new DriverEntity("Marina Souza", true, now));
        drivers.add(new DriverEntity("Rafael Lima", true, now));
        return drivers;
    }

    private List<VehicleEntity> defaultVehicles() {
        long now = System.currentTimeMillis();
        List<VehicleEntity> vehicles = new ArrayList<>();
        vehicles.add(new VehicleEntity("BRA2E19", "FROTA 201", "VW Meteor 29.530", "Longa distancia", 32.0, 50.0, 2.8, 4.1, 250.0, 480.0, 1.9, 2.7, now));
        vehicles.add(new VehicleEntity("QTM8A44", "FROTA 114", "Mercedes Actros 2651", "Distribuicao urbana", 24.0, 38.0, 3.2, 4.8, 120.0, 260.0, 2.3, 3.3, now));
        vehicles.add(new VehicleEntity("FLE7K21", "FROTA 332", "Scania R450", "Frigorificado", 38.0, 56.0, 3.8, 5.4, 280.0, 520.0, 1.8, 2.6, now));
        vehicles.add(new VehicleEntity("RIT5B93", "FROTA 417", "DAF XF 530", "Transferencia", 30.0, 46.0, 2.9, 4.2, 240.0, 450.0, 2.0, 2.9, now));
        return vehicles;
    }
}
