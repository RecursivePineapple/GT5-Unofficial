package gregtech.common.items.matterManipulator;

import java.lang.ref.WeakReference;
import java.util.Map;

import org.spongepowered.libraries.com.google.common.collect.MapMaker;

import gregtech.GTMod;
import gregtech.common.items.matterManipulator.BlockAnalyzer.RegionAnalysis;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

public class TileAnalysisIO {
    
    public static final int MAGIC_HEADER = 5594;

    public static int counter = 0;
    public static final long FIELD_CONNECTIONS = 0b1 << counter++;
    public static final long FIELD_GT_COLOUR = 0b1 << counter++;
    public static final long FIELD_GT_FRONT = 0b1 << counter++;
    public static final long FIELD_GT_MAIN_FACING = 0b1 << counter++;
    public static final long FIELD_GT_FLAGS = 0b1 << counter++;
    public static final long FIELD_GT_FACING = 0b1 << counter++;
    public static final long FIELD_COVERS = 0b1 << counter++;
    public static final long FIELD_STRONG_REDSTONE = 0b1 << counter++;
    public static final long FIELD_GT_CUSTOM_NAME = 0b1 << counter++;
    public static final long FIELD_GT_GHOST_CIRCUIT = 0b1 << counter++;
    public static final long FIELD_GT_ITEM_LOCK = 0b1 << counter++;
    public static final long FIELD_GT_FLUID_LOCK = 0b1 << counter++;
    public static final long FIELD_GT_MODE = 0b1 << counter++;
    public static final long FIELD_GT_DATA = 0b1 << counter++;
    public static final long FIELD_AE_COLOUR = 0b1 << counter++;
    public static final long FIELD_AE_UP = 0b1 << counter++;
    public static final long FIELD_AE_FORWARD = 0b1 << counter++;
    public static final long FIELD_AE_CONFIG = 0b1 << counter++;
    public static final long FIELD_AE_UPGRADES = 0b1 << counter++;
    public static final long FIELD_AE_CUSTOM_NAME = 0b1 << counter++;
    public static final long FIELD_AE_PARTS = 0b1 << counter++;
    public static final long FIELD_AE_CELLS = 0b1 << counter++;
    public static final long FIELD_INVENTORY = 0b1 << counter++;

    public static final byte STACK_META = 0b001;
    public static final byte STACK_AMOUNT = 0b010;
    public static final byte STACK_NBT = 0b100;

    public static final byte INV_FUZZY = 0b001;

    public static final byte CELL_UPGRADES     = 0b0000_0001;
    public static final byte CELL_CONFIG       = 0b0000_0010;
    public static final byte CELL_OREDICT      = 0b0000_0100;
    public static final byte CELL_FUZZY_MODE   = 0b0011_1000;
    public static final byte CELL_IGNORE       = 0 << 3;
    public static final byte CELL_PERCENT_25   = 1 << 3;
    public static final byte CELL_PERCENT_50   = 2 << 3;
    public static final byte CELL_PERCENT_75   = 3 << 3;
    public static final byte CELL_PERCENT_99   = 4 << 3;

    public static int counter2 = 0;
    public static final byte PART_SETTINGS = (byte) (0b1 << counter2++);
    public static final byte PART_CUSTOM_NAME = (byte) (0b1 << counter2++);
    public static final byte PART_UPGRADES = (byte) (0b1 << counter2++);
    public static final byte PART_CONFIG = (byte) (0b1 << counter2++);
    public static final byte PART_OREDICT = (byte) (0b1 << counter2++);
    public static final byte PART_P2P_OUTPUT = (byte) (0b1 << counter2++);
    public static final byte PART_P2P_FREQ = (byte) (0b1 << counter2++);

    private static final Map<EntityPlayer, TileAnalysisLoader> UPLOADS = new MapMaker().weakKeys().makeMap();

    public static int MAX_UPLOAD_SIZE = 10_000_000;

    public static void startUpload(EntityPlayerMP sender) {
        UPLOADS.put(sender, new TileAnalysisLoader());
    }

    public static void addChunk(EntityPlayerMP sender, byte[] data) {
        TileAnalysisLoader loader = UPLOADS.get(sender);
        if (loader != null) {
            loader.addChunk(data);

            if (loader.getStoredSize() > MAX_UPLOAD_SIZE && !MinecraftServer.getServer().getConfigurationManager().func_152596_g(sender.getGameProfile())) {
                UPLOADS.remove(sender);
                GTMod.GT_FML_LOGGER.warn("Kicking player because they sent too many SchematicUpload packets.");
                sender.playerNetServerHandler.kickPlayerFromServer("Schematic was too large.");
                return;
            }
        }
    }

    public static void finishUpload(EntityPlayerMP sender) {
        TileAnalysisLoader loader = UPLOADS.get(sender);
        if (loader != null) {
            if (loader.getStoredSize() > MAX_UPLOAD_SIZE && !MinecraftServer.getServer().getConfigurationManager().func_152596_g(sender.getGameProfile())) {
                UPLOADS.remove(sender);
                GTMod.GT_FML_LOGGER.warn("Kicking player because they sent too many SchematicUpload packets.");
                sender.playerNetServerHandler.kickPlayerFromServer("Schematic was too large.");
                return;
            }

            try {
                RegionAnalysis analysis = loader.load();
            } catch (IOException e) {
                GTMod.GT_FML_LOGGER.warn("Kicking player because they tried to upload an invalid schematic", e);
                sender.playerNetServerHandler.kickPlayerFromServer("Invalid schematic data.");
                return;
            }
        }
    }
}
