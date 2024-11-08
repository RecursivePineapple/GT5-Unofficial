package gregtech.common.items.matterManipulator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.joml.Vector3i;

import com.gtnewhorizon.structurelib.alignment.enumerable.ExtendedFacing;

import appeng.api.util.AEColor;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;
import gregtech.common.items.matterManipulator.BlockAnalyzer.RegionAnalysis;
import gregtech.common.items.matterManipulator.NBTState.PendingBlock;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import static gregtech.common.items.matterManipulator.TileAnalysisIO.*;

public class TileAnalysisLoader {
    
    private final List<byte[]> chunks = new ArrayList<>();

    private ByteBuf buffer;
    private final List<String> stringPool = new ArrayList<>();

    public void addChunk(byte[] data) {
        chunks.add(data);
    }

    public int getStoredSize() {
        int byteCount = 0;

        for (byte[] chunk : chunks) byteCount += chunk.length;

        return byteCount;
    }

    public RegionAnalysis load() throws IOException {
        byte[] data = new byte[getStoredSize()];

        int cursor = 0;
        for (byte[] chunk : chunks) {
            System.arraycopy(chunk, 0, data, cursor, chunk.length);
            cursor += chunk.length;
        }

        GZIPInputStream stream = new GZIPInputStream(new ByteArrayInputStream(data));

        buffer = Unpooled.buffer();

        data = new byte[4096];

        while(stream.available() > 0) {
            int read = stream.read(data);
            buffer.writeBytes(data, 0, read);
        }

        if (buffer.readUnsignedShort() != MAGIC_HEADER) {
            throw new IOException("Data was not a region analysis");
        }

        int version = readVarInt();

        switch (version) {
            case 0: {
                return loadV0();
            }
            default: {
                throw new IOException("Version was " + version);
            }
        }
    }

    private RegionAnalysis loadV0() {
        int stringCount = readVarInt();

        for (int i = 0; i < stringCount; i++) {
            stringPool.add(ByteBufUtils.readUTF8String(buffer));
        }

        RegionAnalysis analysis = new RegionAnalysis();
        analysis.deltas = new Vector3i(readInt(), readInt(), readInt());

        int blockCount = readVarInt();

        for (int i = 0; i < blockCount; i++) {
            int worldId = readVarInt();
            int x = readInt();
            int y = readInt();
            int z = readInt();

            String mod = readString(), blockName = readString();

            int metadata = readVarInt();

            PendingBlock block = new PendingBlock(worldId, x, y, z, GameRegistry.findBlock(mod, blockName), metadata);
            block.tileData = readTileAnalysisResultV0();
            analysis.blocks.add(block);
        }

        return analysis;
    }

    private String readString() {
        return stringPool.get(readVarInt());
    }

    private byte readByte() {
        return buffer.readByte();
    }

    private int readInt() {
        return buffer.readInt();
    }

    private int readVarInt() {
        return ByteBufUtils.readVarInt(buffer, 4);
    }

    private TileAnalysisResult readTileAnalysisResultV0() {
        long presentFields = buffer.readLong();

        if (presentFields == 0) return null;

        TileAnalysisResult result = new TileAnalysisResult();

        if ((presentFields & FIELD_CONNECTIONS) != 0) {
            result.mConnections = buffer.readByte();
        }
        
        if ((presentFields & FIELD_GT_COLOUR) != 0) {
            result.mGTColour = buffer.readByte();
        }
        
        if ((presentFields & FIELD_GT_FRONT) != 0) {
            result.mGTFront = ForgeDirection.VALID_DIRECTIONS[buffer.readByte()];
        }
        
        if ((presentFields & FIELD_GT_MAIN_FACING) != 0) {
            result.mGTMainFacing = ForgeDirection.VALID_DIRECTIONS[buffer.readByte()];
        }

        if ((presentFields & FIELD_GT_FLAGS) != 0) {
            result.mGTFlags = buffer.readShort();
        }

        if ((presentFields & FIELD_GT_FACING) != 0) {
            result.mGTFacing = ExtendedFacing.VALUES[buffer.readByte()];
        }

        if ((presentFields & FIELD_COVERS) != 0) {
            byte presentCovers = buffer.readByte();

            result.mCovers = new CoverData[6];

            for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
                if ((presentCovers & dir.flag) != 0) {
                    result.mCovers[dir.ordinal()] = new CoverData(readStack(), readTag().getTag("x"), readVarInt());
                }
            }
        }

        if ((presentFields & FIELD_STRONG_REDSTONE) != 0) {
            result.mStrongRedstone = buffer.readByte();
        }

        if ((presentFields & FIELD_GT_CUSTOM_NAME) != 0) {
            result.mGTCustomName = readString();
        }

        if ((presentFields & FIELD_GT_GHOST_CIRCUIT) != 0) {
            result.mGTGhostCircuit = buffer.readByte();
        }

        if ((presentFields & FIELD_GT_ITEM_LOCK) != 0) {
            result.mGTItemLock = readStack();
        }

        if ((presentFields & FIELD_GT_FLUID_LOCK) != 0) {
            result.mGTFluidLock = readString();
        }

        if ((presentFields & FIELD_GT_MODE) != 0) {
            result.mGTMode = readVarInt();
        }

        if ((presentFields & FIELD_GT_DATA) != 0) {
            result.mGTData = readTag();
        }

        if ((presentFields & FIELD_AE_COLOUR) != 0) {
            result.mAEColour = AEColor.VALID_COLORS.get(buffer.readByte());
        }

        if ((presentFields & FIELD_AE_UP) != 0) {
            result.mAEUp = ForgeDirection.VALID_DIRECTIONS[buffer.readByte()];
        }

        if ((presentFields & FIELD_AE_FORWARD) != 0) {
            result.mAEForward = ForgeDirection.VALID_DIRECTIONS[buffer.readByte()];
        }

        if ((presentFields & FIELD_AE_CONFIG) != 0) {
            result.mAEConfig = readTag();
        }

        if ((presentFields & FIELD_AE_UPGRADES) != 0) {
            result.mAEUpgrades = readStackArray();
        }

        if ((presentFields & FIELD_AE_CUSTOM_NAME) != 0) {
            result.mAECustomName = readString();
        }

        if ((presentFields & FIELD_AE_PARTS) != 0) {
            int count = readByte();

            result.mAEParts = new AEPartData[7];

            for (int i = 0; i < count; i++) {
                int slot = readByte();
                result.mAEParts[slot] = readPart();
            }
        }

        if ((presentFields & FIELD_AE_CELLS) != 0) {
            result.mAECells = readInventory();
        }

        if ((presentFields & FIELD_INVENTORY) != 0) {
            result.mInventory = readInventory();
        }

        return result;
    }

    private PortableItemStack readStack() {
        PortableItemStack stack = new PortableItemStack();

        byte mask = readByte();

        stack.item = new UniqueIdentifier(readString() + ":" + readString());

        if ((mask & STACK_META) != 0) stack.metadata = readVarInt();
        if ((mask & STACK_AMOUNT) != 0) stack.amount = readVarInt();
        if ((mask & STACK_NBT) != 0) stack.nbt = readTag();

        return stack;
    }

    private PortableItemStack[] readStackArray() {
        int arraySize = readVarInt();
        int count = readVarInt();

        PortableItemStack[] stacks = new PortableItemStack[arraySize];

        for (int i = 0; i < count; i++) {
            int slot = readVarInt();
            stacks[slot] = readStack();
        }

        return stacks;
    }

    private NBTTagCompound readTag() {
        return ByteBufUtils.readTag(buffer);
    }

    private InventoryAnalysis readInventory() {
        byte flags = readByte();

        InventoryAnalysis inventory = new InventoryAnalysis();
        inventory.mFuzzy = (flags & INV_FUZZY) != 0;

        int invSize = readVarInt();
        int count = readVarInt();

        inventory.mItems = new IItemProvider[invSize];

        for (int i = 0; i < count; i++) {
            int slot = readVarInt();
            byte itemType = readByte();

            switch (itemType) {
                case 0: {
                    inventory.mItems[slot] = readStack();
                }
                case 1: {
                    AECellItemProvider cell = new AECellItemProvider();
                    byte mask = readByte();

                    cell.mCell = readStack();
                    if ((mask & CELL_UPGRADES) != 0) cell.mUpgrades = readStackArray();
                    if ((mask & CELL_CONFIG) != 0) cell.mConfig = readStackArray();
                    if ((mask & CELL_OREDICT) != 0) cell.mOreDict = readString();
                    cell.mFuzzyMode = switch (mask & CELL_FUZZY_MODE) {
                        default -> 0;
                        case CELL_IGNORE -> 0;
                        case CELL_PERCENT_25 -> 1;
                        case CELL_PERCENT_50 -> 2;
                        case CELL_PERCENT_75 -> 3;
                        case CELL_PERCENT_99 -> 4;
                    };

                    inventory.mItems[slot] = cell;
                }
            }
        }

        return inventory;
    }

    private AEPartData readPart() {
        AEPartData part = new AEPartData();

        int flags = readByte();

        part.mPart = readStack();

        if ((flags & PART_SETTINGS) != 0) {
            part.mSettingsName = readString();
            part.mSettings = readTag();
        }

        if ((flags & PART_CUSTOM_NAME) != 0) {
            part.mCustomName = readString();
        }

        if ((flags & PART_UPGRADES) != 0) {
            part.mAEUpgrades = readStackArray();
        }

        if ((flags & PART_CONFIG) != 0) {
            part.mConfig = readInventory();
        }

        if ((flags & PART_OREDICT) != 0) {
            part.mOreDict = readString();
        }

        if ((flags & PART_P2P_FREQ) != 0) {
            part.mP2POutput = (flags & PART_P2P_OUTPUT) != 0;
            part.mP2PFreq = buffer.readLong();
        }

        return part;
    }
}
