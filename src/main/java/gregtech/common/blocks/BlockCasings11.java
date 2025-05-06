package gregtech.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.Textures;
import gregtech.api.util.GTUtility;
import gregtech.common.misc.GTStructureChannels;

/**
 * The casings are split into separate files because they are registered as regular blocks, and a regular block can have
 * 16 subtypes at most.
 */
public class BlockCasings11 extends BlockCasingsAbstract {

    public BlockCasings11() {
        super(ItemCasings.class, "gt.blockcasings11", MaterialCasings.INSTANCE, 16);

        register(0, ItemList.Casing_Item_Pipe_Tin, "Tin Item Pipe Casing");
        register(1, ItemList.Casing_Item_Pipe_Brass, "Brass Item Pipe Casing");
        register(2, ItemList.Casing_Item_Pipe_Electrum, "Electrum Item Pipe Casing");
        register(3, ItemList.Casing_Item_Pipe_Platinum, "Platinum Item Pipe Casing");
        register(4, ItemList.Casing_Item_Pipe_Osmium, "Osmium Item Pipe Casing");
        register(5, ItemList.Casing_Item_Pipe_Quantium, "Quantium Item Pipe Casing");
        register(6, ItemList.Casing_Item_Pipe_Fluxed_Electrum, "Fluxed Electrum Item Pipe Casing");
        register(7, ItemList.Casing_Item_Pipe_Black_Plutonium, "Black Plutonium Item Pipe Casing");

        register(8, ItemList.Casing_Item_BEC1, "Coherence-preserving Plasma Conduit", "gt.casing11.8.tooltip");
        register(9, ItemList.Casing_Item_BEC2, "Electromagnetically-isolated Casing", "gt.casing11.9.tooltip");
        register(10, ItemList.Casing_Item_BEC3, "Fine-structure Constant Manipulator", "gt.casing11.10.tooltip");

        for (int i = 0; i < 8; i++) {
            GTStructureChannels.ITEM_PIPE_CASING.registerAsIndicator(new ItemStack(this, 1, i), i + 1);
        }
    }

    @Override
    public int getTextureIndex(int aMeta) {
        return (16 << 7) | (aMeta + 64);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
        int meta = world.getBlockMetadata(x, y, z);

        if (meta == 8) {
            return getCTMIcon(world, x, y, z, ForgeDirection.getOrientation(side));
        } else {
            return getIcon(side, meta);
        }
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        return switch (meta) {
            case 1 -> Textures.BlockIcons.MACHINE_CASING_ITEM_PIPE_BRASS.getIcon();
            case 2 -> Textures.BlockIcons.MACHINE_CASING_ITEM_PIPE_ELECTRUM.getIcon();
            case 3 -> Textures.BlockIcons.MACHINE_CASING_ITEM_PIPE_PLATINUM.getIcon();
            case 4 -> Textures.BlockIcons.MACHINE_CASING_ITEM_PIPE_OSMIUM.getIcon();
            case 5 -> Textures.BlockIcons.MACHINE_CASING_ITEM_PIPE_QUANTIUM.getIcon();
            case 6 -> Textures.BlockIcons.MACHINE_CASING_ITEM_PIPE_FLUXED_ELECTRUM.getIcon();
            case 7 -> Textures.BlockIcons.MACHINE_CASING_ITEM_PIPE_BLACK_PLUTONIUM.getIcon();
            case 8 -> Textures.BlockIcons.BEC1.getIcon();
            case 9 -> Textures.BlockIcons.BEC2.getIcon();
            case 10 -> Textures.BlockIcons.BEC3.getIcon();
            default -> Textures.BlockIcons.MACHINE_CASING_ITEM_PIPE_TIN.getIcon();
        };
    }

    private int getCTMPriority(IBlockAccess world, int x, int y, int z, boolean includeSimilar) {
        Block block = world.getBlock(x, y, z);

        if (block != this) return 0;

        int meta = world.getBlockMetadata(x, y, z);

        return switch (meta) {
            case 9 -> includeSimilar ? 1 : 0;
            case 10 -> includeSimilar ? 2 : 0;
            case 8 -> 3;
            default -> 0;
        };
    }

    @SideOnly(Side.CLIENT)
    private IIcon getCTMIcon(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        int x1 = getCTMPriority(world, x + 1, y, z, true), x2 = getCTMPriority(world, x - 1, y, z, true),
            y1 = getCTMPriority(world, x, y + 1, z, side.offsetY != 0),
            y2 = getCTMPriority(world, x, y - 1, z, side.offsetY != 0), z1 = getCTMPriority(world, x, y, z + 1, true),
            z2 = getCTMPriority(world, x, y, z - 1, true);

        return switch (side) {
            case UP, DOWN -> {
                int max = GTUtility.max(x1, x2, z1, z2);

                yield max == x1 || max == x2 ? Textures.BlockIcons.BEC1.getIcon()
                    : Textures.BlockIcons.BEC1_90.getIcon();
            }
            case EAST, WEST -> {
                int max = GTUtility.max(y1, y2, z1, z2);

                yield max == z1 || max == z2 || max == 0 ? Textures.BlockIcons.BEC1.getIcon()
                    : Textures.BlockIcons.BEC1_90.getIcon();
            }
            case NORTH, SOUTH -> {
                int max = GTUtility.max(x1, x2, y1, y2);

                yield max == x1 || max == x2 ? Textures.BlockIcons.BEC1.getIcon()
                    : Textures.BlockIcons.BEC1_90.getIcon();
            }
            default -> Textures.BlockIcons.BEC1.getIcon();
        };
    }
}
