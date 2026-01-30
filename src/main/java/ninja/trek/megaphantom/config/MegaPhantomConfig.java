package ninja.trek.megaphantom.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import ninja.trek.megaphantom.MegaPhantom;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class MegaPhantomConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("megaphantom.json");

    private static MegaPhantomConfig INSTANCE = new MegaPhantomConfig();

    public int swoopThreshold = 256;
    public double scale = 3.0;
    public double healthMultiplier = 10.0;
    public double damageMultiplier = 2.0;
    public double reductionPerPhantom = 0.15;
    public double losRange = 48.0;

    public static MegaPhantomConfig get() {
        return INSTANCE;
    }

    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                INSTANCE = GSON.fromJson(reader, MegaPhantomConfig.class);
                if (INSTANCE == null) {
                    INSTANCE = new MegaPhantomConfig();
                }
                MegaPhantom.LOGGER.info("Loaded MegaPhantom config");
            } catch (IOException e) {
                MegaPhantom.LOGGER.error("Failed to load config, using defaults", e);
                INSTANCE = new MegaPhantomConfig();
            }
        } else {
            INSTANCE = new MegaPhantomConfig();
            save();
        }
    }

    public static void save() {
        try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
            GSON.toJson(INSTANCE, writer);
        } catch (IOException e) {
            MegaPhantom.LOGGER.error("Failed to save config", e);
        }
    }
}
