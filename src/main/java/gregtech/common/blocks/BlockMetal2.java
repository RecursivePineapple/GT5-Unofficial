package gregtech.common.blocks;

import static gregtech.api.enums.Mods.GregTech;
import static gregtech.api.enums.Mods.NotEnoughItems;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.GregTechAPI;
import gregtech.api.enums.IMaterial;
import gregtech.api.enums.MaterialBuilder2.BlockTexture;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.enums.TextureSet;
import gregtech.api.items.GTGenericBlock;
import gregtech.api.util.GTLanguageManager;
import gregtech.api.util.GTOreDictUnificator;
import gregtech.common.render.GTRendererBlock;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockMetal2 extends GTGenericBlock {
    
    public static Int2ObjectArrayMap<BlockMetal2> BLOCK_SERIES = new Int2ObjectArrayMap<>();

    private final int IDOffset;
    private Int2ObjectArrayMap<IMaterial> materials = new Int2ObjectArrayMap<>();
    private Int2ObjectArrayMap<Object> textures = new Int2ObjectArrayMap<>();
    private Int2ObjectArrayMap<IIcon> icons = new Int2ObjectArrayMap<>();

    public BlockMetal2(int series) {
        super(ItemStorage.class, "gt.blockmetal2." + series, Material.iron);

        setStepSound(soundTypeMetal);
        setCreativeTab(GregTechAPI.TAB_GREGTECH_MATERIALS);

        this.IDOffset = series * Short.MAX_VALUE;
    }

    public static class BlockAutogenTexture {
        public final TextureSet textureSet;
        public final BlockTexture texture;

        public BlockAutogenTexture(TextureSet textureSet, BlockTexture texture) {
            this.textureSet = textureSet;
            this.texture = texture;
        }

        public ResourceLocation getBaseTexture() {
            return texture.getBaseTexture(textureSet);
        }
    }

    public static void register(IMaterial material, ResourceLocation customBlockTexture) {
        if (GregTechAPI.sPreloadFinished) {
            throw new IllegalStateException("Cannot register blocks after preload has finished");
        }

        int series = material.getID() / Short.MAX_VALUE;

        if (!BLOCK_SERIES.containsKey(series)) {
            BLOCK_SERIES.put(series, new BlockMetal2(series));
        }

        BLOCK_SERIES.get(series).register2(material, customBlockTexture);
    }

    public static void register(IMaterial material, TextureSet textureSet, BlockTexture texture) {
        if (GregTechAPI.sPreloadFinished) {
            throw new IllegalStateException("Cannot register blocks after preload has finished");
        }

        int series = material.getID() / Short.MAX_VALUE;

        if (!BLOCK_SERIES.containsKey(series)) {
            BLOCK_SERIES.put(series, new BlockMetal2(series));
        }

        BLOCK_SERIES.get(series).register2(material, textureSet, texture);
    }

    public void register2(IMaterial material, ResourceLocation customBlockTexture) {
        registerImpl(material, customBlockTexture);
    }

    public void register2(IMaterial material, TextureSet textureSet, BlockTexture texture) {
        registerImpl(material, new BlockAutogenTexture(textureSet, texture));
    }

    private void registerImpl(IMaterial material, Object texture) {
        int meta = material.getID() - IDOffset;

        if (meta < 0 || meta > Short.MAX_VALUE) {
            throw new IllegalArgumentException("cannot register material " + material + " to BlockMetal2 " +
                this + " because the blocks ID range is " + IDOffset + " to " + (IDOffset + Short.MAX_VALUE));
        }

        materials.put(meta, material);
        textures.put(meta, texture);

        GTLanguageManager.addStringLocalization(
            getUnlocalizedName() + "." + meta + ".name",
            "Block of " + material.getDefaultLocalName());
        GTOreDictUnificator.registerOre(OrePrefixes.block.name() + material.getName(), new ItemStack(this, 1, meta));
    }

    public void onInit() {
        if (NotEnoughItems.isModLoaded()) {
            for (int i = 0; i < Short.MAX_VALUE; i++) {
                if (!materials.containsKey(i)) {
                    codechicken.nei.api.API.hideItem(new ItemStack(this, 1, i));
                }
            }
        }
    }

    @Override
    public IIcon getIcon(int ordinalSide, int aMeta) {
        return icons.get(aMeta);
    }

    @Override
    public int getRenderColor(int meta) {
        IMaterial material = materials.get(meta);

        if (material == null) {
            // hot pink
            return 0xF204AB;
        }

        short[] rgba = material.getRGBA();

        return ((rgba[0] & 0xFF) << 16) | ((rgba[1] & 0xFF) << 8) | (rgba[2] & 0xFF);
    }

    @Override
    public int colorMultiplier(IBlockAccess worldIn, int x, int y, int z) {
        return getRenderColor(worldIn.getBlockMetadata(x, y, z));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean addDestroyEffects(World world, int x, int y, int z, int meta, EffectRenderer effectRenderer) {
        GTRendererBlock.addDestroyEffects(effectRenderer, this, world, x, y, z);
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister aIconRegister) {
        Object2ObjectArrayMap<ResourceLocation, BufferedImage> sharedBaseTextureCache = new Object2ObjectArrayMap<>();

        icons.clear();

        for (var e : textures.int2ObjectEntrySet()) {
            if (e.getValue() instanceof ResourceLocation resource) {
                icons.put(e.getIntKey(), aIconRegister.registerIcon(resource.toString()));
            } else if (e.getValue() instanceof BlockAutogenTexture texture) {
                String textureName = GregTech.resourceDomain + "-autogen:" + this.getUnlocalizedName() + ".virtual-texture." + e.getIntKey();

                TextureAtlasSprite sprite = new TextureAtlasSprite(textureName) {
                    
                    private Object2ObjectArrayMap<ResourceLocation, BufferedImage> baseCache = sharedBaseTextureCache;

                    @Override
                    public boolean hasCustomLoader(net.minecraft.client.resources.IResourceManager manager, ResourceLocation location) {
                        return true;
                    }

                    @Override
                    public boolean load(IResourceManager manager, ResourceLocation location) {
                        BufferedImage base = baseCache.computeIfAbsent(texture.getBaseTexture(), x -> loadBase((ResourceLocation)x));
                        baseCache = null;
        
                        BufferedImage out = new BufferedImage(base.getWidth(), base.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
                        base.copyData(out.getRaster());
        
                        int[] argb = new int[out.getWidth() * out.getHeight()];
                        out.getRGB(0, 0, out.getWidth(), out.getHeight(), argb, 0, out.getWidth());
        
                        short[] rgba = materials.get(e.getIntKey()).getRGBA();

                        for (int i = 0; i < argb.length; i++) {
                            int pixel = argb[i];
                            int a = ((pixel >> 24) & 0xFF) * rgba[3] / 255;
                            int r = ((pixel >> 16) & 0xFF) * rgba[0] / 255;
                            int g = ((pixel >> 8) & 0xFF) * rgba[1] / 255;
                            int b = (pixel & 0xFF) * rgba[2] / 255;
        
                            argb[i] = (a << 24) | (r << 16) | (g << 8) | b;
                        }
        
                        out.setRGB(0, 0, out.getWidth(), out.getHeight(), argb, 0, out.getWidth());
        
                        GameSettings settings = Minecraft.getMinecraft().gameSettings;
                        BufferedImage[] images = new BufferedImage[settings.mipmapLevels + 1];
                        images[0] = out;

                        loadSprite(images, null, settings.anisotropicFiltering > 1);

                        return false;
                    }
                };

                icons.put(e.getIntKey(), sprite);
                ((TextureMap) aIconRegister).setTextureEntry(textureName, sprite);
            } else {
                throw new IllegalStateException("Bad block texture: " + e.getValue());
            }
        }
    }

    private static BufferedImage loadBase(ResourceLocation baseTexture) {
        try {
            IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(baseTexture);
    
            return ImageIO.read(resource.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onBlockAdded(World aWorld, int aX, int aY, int aZ) {
        if (GregTechAPI.isMachineBlock(this, aWorld.getBlockMetadata(aX, aY, aZ))) {
            GregTechAPI.causeMachineUpdate(aWorld, aX, aY, aZ);
        }
    }

    @Override
    public void breakBlock(World aWorld, int aX, int aY, int aZ, Block aBlock, int aMetaData) {
        if (GregTechAPI.isMachineBlock(this, aMetaData)) {
            GregTechAPI.causeMachineUpdate(aWorld, aX, aY, aZ);
        }
    }

    @Override
    public String getHarvestTool(int aMeta) {
        return "pickaxe";
    }

    @Override
    public boolean isBeaconBase(IBlockAccess worldObj, int x, int y, int z, int beaconX, int beaconY, int beaconZ) {
        return true;
    }

    @Override
    public int getHarvestLevel(int aMeta) {
        return 1;
    }

    @Override
    public float getBlockHardness(World aWorld, int aX, int aY, int aZ) {
        return Blocks.iron_block.getBlockHardness(aWorld, aX, aY, aZ);
    }

    @Override
    public float getExplosionResistance(Entity aTNT) {
        return Blocks.iron_block.getExplosionResistance(aTNT);
    }

    @Override
    public String getUnlocalizedName() {
        return this.mUnlocalizedName;
    }

    @Override
    public String getLocalizedName() {
        return StatCollector.translateToLocal(this.mUnlocalizedName + ".name");
    }

    @Override
    public boolean canBeReplacedByLeaves(IBlockAccess aWorld, int aX, int aY, int aZ) {
        return false;
    }

    @Override
    public boolean isNormalCube(IBlockAccess aWorld, int aX, int aY, int aZ) {
        return true;
    }

    @Override
    public boolean canCreatureSpawn(EnumCreatureType type, IBlockAccess world, int x, int y, int z) {
        return true;
    }

    @Override
    public int damageDropped(int metadata) {
        return metadata;
    }

    @Override
    public int getDamageValue(World aWorld, int aX, int aY, int aZ) {
        return aWorld.getBlockMetadata(aX, aY, aZ);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubBlocks(Item aItem, CreativeTabs aCreativeTab, List<ItemStack> aList) {
        for (int meta : materials.keySet()) {
            aList.add(new ItemStack(aItem, 1, meta));
        }
    }
}
