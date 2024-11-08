package gregtech.common.items.matterManipulator;

import cpw.mods.fml.common.network.ByteBufUtils;
import gregtech.common.items.matterManipulator.BlockAnalyzer.RegionAnalysis;
import gregtech.common.items.matterManipulator.NBTState.PendingBlock;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import static gregtech.common.items.matterManipulator.TileAnalysisIO.*;

import java.util.Arrays;

public class TileAnalysisWriter {
    
    private final Object2IntMap<String> stringPool = new Object2IntLinkedOpenHashMap<>();
    private ByteBuf buffer, out;
    private byte[] data = new byte[1024];

    private void getData() {
        out = Unpooled.buffer();

        out.writeShort((short) MAGIC_HEADER);
        ByteBufUtils.writeVarInt(out, 0, 4);
        out.writeBytes(buffer);
    }

    public void tick() {
        if (out == null) {
            getData();
            out.resetReaderIndex();
            Messages.SchematicUploadStart.sendToServer();
        }

        int toRead = Math.min(out.readableBytes(), data.length);

        if (toRead > 0) {
            out.readBytes(data, 0, toRead);
            Messages.SchematicUpload.sendToServer(Arrays.copyOf(data, toRead));

            if (toRead < data.length) {
                Messages.SchematicUploadFinish.sendToServer();
            }
        }
    }

    public void write(RegionAnalysis analysis) {
        writeInt(analysis.deltas.x);
        writeInt(analysis.deltas.y);
        writeInt(analysis.deltas.z);

        writeVarInt(analysis.blocks.size());

        for (PendingBlock block : analysis.blocks) {
            writeVarInt(block.worldId);
            writeInt(block.x);
            writeInt(block.y);
            writeInt(block.z);

            writeString(block.blockId.modId);
            writeString(block.blockId.name);

            writeVarInt(block.metadata);

            writeTileAnalysis(block.tileData);
        }
    }

    private void writeByte(int x) {
        buffer.writeByte((byte) x);
    }

    private void writeShort(int x) {
        buffer.writeShort((short) x);
    }

    private void writeInt(int x) {
        buffer.writeInt(x);
    }

    private void writeVarInt(int x) {
        ByteBufUtils.writeVarInt(buffer, x, 4);
    }

    private void writeString(String s) {
        if (stringPool.containsKey(s)) {
            writeInt(stringPool.getInt(s));
        } else {
            int x = stringPool.size();
            stringPool.put(s, x);
            writeInt(x);
        }
    }

    private void writeTileAnalysis(TileAnalysisResult result) {
        if (result == null || !result.doesAnything()) {
            buffer.writeLong(0);
            return;
        }

        long flags = 0
            | (result.mConnections != 0 ? FIELD_CONNECTIONS : 0)
            | (result.mGTColour != -1 ? FIELD_GT_COLOUR : 0)
            | (result.mGTFront != null ? FIELD_GT_FRONT : 0)
            | (result.mGTMainFacing != null ? FIELD_GT_MAIN_FACING : 0)
            | (result.mGTFlags != 0 ? FIELD_GT_FLAGS : 0)
            | (result.mGTFacing != null ? FIELD_GT_FACING : 0)
            | (result.mCovers != null ? FIELD_COVERS : 0)
            | (result.mStrongRedstone != 0 ? FIELD_STRONG_REDSTONE : 0)
            | (result.mGTCustomName != null ? FIELD_GT_CUSTOM_NAME : 0)
            | (result.mGTGhostCircuit != 0 ? FIELD_GT_GHOST_CIRCUIT : 0)
            | (result.mGTItemLock != null ? FIELD_GT_ITEM_LOCK : 0)
            | (result.mGTFluidLock != null ? FIELD_GT_FLUID_LOCK : 0)
            | (result.mGTMode != 0 ? FIELD_GT_MODE : 0)
            | (result.mGTData != null ? FIELD_GT_DATA : 0)
            | (result.mAEColour != null ? FIELD_AE_COLOUR : 0)
            | (result.mAEUp != null ? FIELD_AE_UP : 0)
            | (result.mAEForward != null ? FIELD_AE_FORWARD : 0)
            | (result.mAEConfig != null ? FIELD_AE_CONFIG : 0)
            | (result.mAEUpgrades != null ? FIELD_AE_UPGRADES : 0)
            | (result.mAECustomName != null ? FIELD_AE_CUSTOM_NAME : 0)
            | (result.mAEParts != null ? FIELD_AE_PARTS : 0)
            | (result.mAECells != null ? FIELD_AE_CELLS : 0)
            | (result.mInventory != null ? FIELD_INVENTORY : 0);

        buffer.writeLong(flags);

        if (result.mConnections != 0) {
            writeByte(result.mConnections);
        }

        if (result.mGTColour != -1) {
            writeByte(result.mGTColour);
        }

        if (result.mGTFront != null) {
            writeByte((byte) result.mGTFront.ordinal());
        }

        if (result.mGTMainFacing != null) {
            writeByte((byte) result.mGTMainFacing.ordinal());
        }

        if (result.mGTFlags != 0) {
            writeShort(result.mGTFlags);
        }

        if (result.mGTFacing != null) {
            writeByte((byte) result.mGTFacing.ordinal());
        }

        if (result.mCovers != null) {
            byte presentCovers = 0;

            for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
                if (result.mCovers[dir.ordinal()] != null) {
                    presentCovers |= dir.flag;
                }
            }

            writeByte(presentCovers);

            for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
                CoverData cover = result.mCovers[dir.ordinal()];
                if (cover != null) {
                    writeStack(cover.cover);
                    NBTTagCompound tag = new NBTTagCompound();
                    tag.setTag("x", cover.coverData);
                    writeTag(tag);
                    writeVarInt(cover.tickRateAddition == null ? 0 : cover.tickRateAddition);
                }
            }
        }

        if (result.mStrongRedstone != 0) {
            writeByte(result.mStrongRedstone);
        }

        if (result.mGTCustomName != null) {
            writeString(result.mGTCustomName);
        }

        if (result.mGTGhostCircuit != 0) {
            writeByte(result.mGTGhostCircuit);
        }

        if (result.mGTItemLock != null) {
            writeStack(result.mGTItemLock);
        }

        if (result.mGTFluidLock != null) {
            writeString(result.mGTFluidLock);
        }

        if (result.mGTMode != 0) {
            writeVarInt(result.mGTMode);
        }

        if (result.mGTData != null) {
            writeTag(result.mGTData);
        }

        if (result.mAEColour != null) {
            writeByte((byte) result.mAEColour.ordinal());
        }

        if (result.mAEUp != null) {
            writeByte((byte) result.mAEUp.ordinal());
        }

        if (result.mAEForward != null) {
            writeByte((byte) result.mAEForward.ordinal());
        }

        if (result.mAEConfig != null) {
            writeTag(result.mAEConfig);
        }

        if (result.mAEUpgrades != null) {
            writeStackArray(result.mAEUpgrades);
        }

        if (result.mAECustomName != null) {
            writeString(result.mAECustomName);
        }

        if (result.mAEParts != null) {
            int count = 0;

            for (int i = 0; i < result.mAEParts.length; i++) {
                if (result.mAEParts[i] != null) {
                    count++;
                }
            }

            writeByte(count);
            
            for (int i = 0; i < result.mAEParts.length; i++) {
                AEPartData part = result.mAEParts[i];

                if (part != null) {
                    writeByte(i);
                    writePart(part);
                }
            }
        }

        if (result.mAECells != null) {
            writeInventory(result.mAECells);
        }

        if (result.mInventory != null) {
            writeInventory(result.mInventory);
        }
    }

    private void writeStack(PortableItemStack stack) {
        byte flags = (byte) (0
            | (stack.metadata != null ? STACK_META : 0)
            | (stack.amount != null ? STACK_AMOUNT : 0)
            | (stack.nbt != null ? STACK_NBT : 0));

        writeByte(flags);
    }

    private void writeTag(NBTTagCompound tag) {
        ByteBufUtils.writeTag(buffer, tag);
    }

    private void writeStackArray(PortableItemStack[] stacks) {
        writeVarInt(stacks.length);

        int count = 0;
        for (int i = 0; i < stacks.length; i++) {
            if (stacks[i] != null) {
                count++;
            }
        }

        writeVarInt(count);

        for (int i = 0; i < stacks.length; i++) {
            PortableItemStack stack = stacks[i];

            if (stack != null) {
                writeVarInt(i);
                writeStack(stacks[i]);
            }
        }
    }

    private void writeInventory(InventoryAnalysis inventory) {
        writeByte(inventory.mFuzzy ? INV_FUZZY : 0);

        int count = 0;

        for (int i = 0; i < inventory.mItems.length; i++) {
            if (inventory.mItems[i] != null) {
                count++;
            }
        }

        writeVarInt(inventory.mItems.length);
        writeVarInt(count);

        for (int i = 0; i < inventory.mItems.length; i++) {
            IItemProvider item = inventory.mItems[i];

            if (item != null) {
                writeVarInt(i);

                if (item instanceof PortableItemStack stack) {
                    writeByte(0);
                    writeStack(stack);
                } else if (item instanceof AECellItemProvider cell) {
                    int flags = 0
                        | (cell.mUpgrades != null ? CELL_UPGRADES : 0)
                        | (cell.mConfig != null ? CELL_CONFIG : 0)
                        | (cell.mOreDict != null ? CELL_OREDICT : 0)
                        | switch (cell.mFuzzyMode) {
                            default -> 0;
                            case 0 -> CELL_IGNORE;
                            case 1 -> CELL_PERCENT_25;
                            case 2 -> CELL_PERCENT_50;
                            case 3 -> CELL_PERCENT_75;
                            case 4 -> CELL_PERCENT_99;
                        };
                    
                    writeByte(flags);
                    writeStack(cell.mCell);

                    if (cell.mUpgrades != null) {
                        writeStackArray(cell.mUpgrades);
                    }

                    if (cell.mConfig != null) {
                        writeStackArray(cell.mConfig);
                    }

                    if (cell.mOreDict != null) {
                        writeString(cell.mOreDict);
                    }
                }
            }
        }
    }

    private void writePart(AEPartData part) {
        int flags = 0
            | (part.mSettingsName != null && part.mSettings != null ? PART_SETTINGS : 0)
            | (part.mCustomName != null ? PART_CUSTOM_NAME : 0)
            | (part.mAEUpgrades != null ? PART_UPGRADES : 0)
            | (part.mConfig != null ? PART_CONFIG : 0)
            | (part.mOreDict != null ? PART_OREDICT : 0)
            | (part.mP2POutput != null && part.mP2PFreq != null ? PART_P2P_FREQ : 0)
            | (part.mP2POutput != null && part.mP2POutput == true ? PART_P2P_OUTPUT : 0);

        writeByte(flags);

        if (part.mSettingsName != null && part.mSettings != null) {
            writeString(part.mSettingsName);
            writeTag(part.mSettings);
        }

        if (part.mCustomName != null) {
            writeString(part.mCustomName);
        }

        if (part.mAEUpgrades != null) {
            writeStackArray(part.mAEUpgrades);
        }

        if (part.mConfig != null) {
            writeInventory(part.mConfig);
        }

        if (part.mOreDict != null) {
            writeString(part.mOreDict);
        }

        if (part.mP2POutput != null && part.mP2PFreq != null) {
            buffer.writeLong(part.mP2PFreq);
        }
    }
}
