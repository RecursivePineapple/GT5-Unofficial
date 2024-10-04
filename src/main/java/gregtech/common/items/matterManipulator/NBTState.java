package gregtech.common.items.matterManipulator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.ForgeDirection;

import org.joml.Vector3i;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import appeng.api.AEApi;
import appeng.api.config.SecurityPermissions;
import appeng.api.features.ILocatable;
import appeng.api.implementations.tiles.IWirelessAccessPoint;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.DimensionalCoord;
import appeng.tile.misc.TileSecurity;
import appeng.tile.networking.TileWireless;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;
import gregtech.api.util.GTUtility;
import gregtech.common.items.matterManipulator.BlockAnalyzer.RegionAnalysis;

class NBTState {

    static final Gson GSON = new GsonBuilder().create();

    public NBTState.Config config = new NBTState.Config();

    public String encKey;
    public double charge;

    public transient TileSecurity securityTerminal;
    public transient IGridNode gridNode;
    public transient IGrid grid;
    public transient IStorageGrid storageGrid;
    public transient IMEMonitor<IAEItemStack> itemStorage;

    public static NBTState load(NBTTagCompound tag) {
        NBTState state = GSON.fromJson(toJsonObject(tag), NBTState.class);

        if (state == null) state = new NBTState();
        if (state.config == null) state.config = new NBTState.Config();

        return state;
    }

    public NBTTagCompound save() {
        return (NBTTagCompound) toNbt(GSON.toJsonTree(this));
    }

    public boolean hasMEConnection() {
        return encKey != null && securityTerminal != null
            && gridNode != null
            && grid != null
            && storageGrid != null
            && itemStorage != null;
    }

    public boolean connectToMESystem() {
        grid = null;
        storageGrid = null;
        itemStorage = null;

        if (encKey == null) return false;

        long addr = 0;

        try {
            addr = Long.parseLong(encKey, 16);
        } catch (NumberFormatException e) {
            return false;
        }

        ILocatable grid = AEApi.instance()
            .registries()
            .locatable()
            .getLocatableBy(addr);

        if (grid instanceof TileSecurity security) {
            this.securityTerminal = security;
            this.gridNode = security.getGridNode(ForgeDirection.UNKNOWN);
            if (this.gridNode != null) {
                this.grid = this.gridNode.getGrid();
                this.storageGrid = this.grid.getCache(IStorageGrid.class);
                if (this.storageGrid != null) {
                    this.itemStorage = this.storageGrid.getItemInventory();
                }
            }
        }

        return hasMEConnection();
    }

    private transient IWirelessAccessPoint prevAccessPoint;

    public boolean canInteractWithAE(EntityPlayer player) {
        if (grid == null) {
            return false;
        }

        IEnergyGrid eg = grid.getCache(IEnergyGrid.class);
        if (!eg.isNetworkPowered()) {
            return false;
        }

        ISecurityGrid sec = grid.getCache(ISecurityGrid.class);
        if (!sec.hasPermission(player, SecurityPermissions.EXTRACT)
            || !sec.hasPermission(player, SecurityPermissions.INJECT)) {
            return false;
        }

        if (checkAEDistance(player, prevAccessPoint)) {
            return true;
        }

        for (IGridNode node : grid.getMachines(TileWireless.class)) {
            if (checkAEDistance(player, (IWirelessAccessPoint) node.getMachine())) {
                prevAccessPoint = (IWirelessAccessPoint) node.getMachine();
                return true;
            }
        }

        prevAccessPoint = null;

        return false;
    }

    private boolean checkAEDistance(EntityPlayer player, IWirelessAccessPoint accessPoint) {
        if (accessPoint != null && accessPoint.getGrid() == grid && accessPoint.isActive()) {
            DimensionalCoord coord = accessPoint.getLocation();

            if (coord.getWorld().provider.dimensionId != player.worldObj.provider.dimensionId) {
                return false;
            }

            double distance = player.getDistanceSq(coord.x, coord.y, coord.z);

            return Math.pow(accessPoint.getRange(), 2) >= distance;
        } else {
            return false;
        }
    }

    // #region Pending blocks

    public List<PendingBlock> getPendingBlocks() {
        switch (config.placeMode) {
            case COPYING: {
                Location coordA = config.coordA;
                Location coordB = config.coordB;
                Location coordC = config.coordC;

                if (!Location.areCompatible(coordA, coordB, coordC)) {
                    return new ArrayList<>();
                }

                RegionAnalysis analysis = BlockAnalyzer.analyzeRegion(coordA.getWorld(), coordA, coordB);

                for (PendingBlock block : analysis.blocks) {
                    block.x += coordC.x;
                    block.y += coordC.y;
                    block.z += coordC.z;
                }

                return analysis.blocks;
            }
            case DEBUGGING: {
                throw new IllegalStateException();
            }
            case GEOMETRY: {
                return getGeomPendingBlocks();
            }
            case MOVING: {
                throw new IllegalStateException();
            }
            default: {
                throw new AssertionError();
            }
        }
    }

    private List<PendingBlock> getGeomPendingBlocks() {
        ArrayList<PendingBlock> pending = new ArrayList<>();

        if (config.coordA == null || config.coordB == null) {
            return pending;
        }

        int x1 = config.coordA.x;
        int y1 = config.coordA.y;
        int z1 = config.coordA.z;
        int x2 = config.coordB.x;
        int y2 = config.coordB.y;
        int z2 = config.coordB.z;

        int minX = Math.min(x1, x2);
        int minY = Math.min(y1, y2);
        int minZ = Math.min(z1, z2);
        int maxX = Math.max(x1, x2);
        int maxY = Math.max(y1, y2);
        int maxZ = Math.max(z1, z2);

        switch (config.shape) {
            case LINE: {
                iterateLine(pending, x1, y1, z1, x2, y2, z2);
                break;
            }
            case CUBE: {
                iterateCube(pending, minX, minY, minZ, maxX, maxY, maxZ);
                break;
            }
            case SPHERE: {
                iterateSphere(pending, minX, minY, minZ, maxX, maxY, maxZ);
                break;
            }
            default: {
                break;
            }
        }

        return pending;
    }

    private void iterateLine(ArrayList<PendingBlock> pending, int x1, int y1, int z1, int x2, int y2, int z2) {
        ItemStack edges = config.getEdges();

        int dx = Math.abs(x1 - x2), dy = Math.abs(y1 - y2), dz = Math.abs(z1 - z2);
        int sx = x1 < x2 ? 1 : -1, sy = y1 < y2 ? 1 : -1, sz = z1 < z2 ? 1 : -1;

        pending.add(new PendingBlock(config.coordA.worldId, x1, y1, z1, edges));

        if (dx >= dy && dx >= dz) {
            int p1 = 2 * dy - dx;
            int p2 = 2 * dz - dx;

            while (x1 != x2) {
                x1 += sx;

                if (p1 >= 0) {
                    y1 += sy;
                    p1 -= 2 * dx;
                }
                if (p2 >= 0) {
                    z1 += sz;
                    p2 -= 2 * dx;
                }

                p1 += 2 * dy;
                p2 += 2 * dz;

                pending.add(new PendingBlock(config.coordA.worldId, x1, y1, z1, edges));
            }
        } else if (dy >= dx && dy >= dz) {
            int p1 = 2 * dx - dy;
            int p2 = 2 * dz - dy;

            while (y1 != y2) {
                y1 += sy;

                if (p1 >= 0) {
                    x1 += sx;
                    p1 -= 2 * dy;
                }
                if (p2 >= 0) {
                    z1 += sz;
                    p2 -= 2 * dy;
                }

                p1 += 2 * dx;
                p2 += 2 * dz;

                pending.add(new PendingBlock(config.coordA.worldId, x1, y1, z1, edges));
            }
        } else {
            int p1 = 2 * dy - dz;
            int p2 = 2 * dx - dz;

            while (z1 != z2) {
                z1 += sz;

                if (p1 >= 0) {
                    y1 += sy;
                    p1 -= 2 * dz;
                }
                if (p2 >= 0) {
                    x1 += sx;
                    p2 -= 2 * dz;
                }

                p1 += 2 * dy;
                p2 += 2 * dx;

                pending.add(new PendingBlock(config.coordA.worldId, x1, y1, z1, edges));
            }
        }
    }

    private void iterateCube(ArrayList<PendingBlock> pending, int minX, int minY, int minZ, int maxX, int maxY,
        int maxZ) {
        ItemStack corners = config.getCorners();
        ItemStack edges = config.getEdges();
        ItemStack faces = config.getFaces();
        ItemStack volumes = config.getVolumes();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    int insideCount = 0;

                    if (x > minX && x < maxX) insideCount++;
                    if (y > minY && y < maxY) insideCount++;
                    if (z > minZ && z < maxZ) insideCount++;

                    ItemStack selection = switch (insideCount) {
                        case 0 -> corners;
                        case 1 -> edges;
                        case 2 -> faces;
                        case 3 -> volumes;
                        default -> null;
                    };

                    pending.add(new PendingBlock(config.coordA.worldId, x, y, z, selection, insideCount, insideCount));
                }
            }
        }
    }

    private void iterateSphere(ArrayList<PendingBlock> pending, int minX, int minY, int minZ, int maxX, int maxY,
        int maxZ) {
        ItemStack faces = config.getFaces();
        ItemStack volumes = config.getVolumes();

        int sx = maxX - minX + 1;
        int sy = maxY - minY + 1;
        int sz = maxZ - minZ + 1;

        double rx = sx / 2.0;
        double ry = sy / 2.0;
        double rz = sz / 2.0;

        boolean[][][] present = new boolean[sx + 2][sy + 2][sz + 2];

        for (int x = 0; x < sx; x++) {
            for (int y = 0; y < sy; y++) {
                for (int z = 0; z < sz; z++) {
                    // the ternaries here check whether the given axis is 1, in which case this is a circle and not a
                    // sphere
                    // spotless:off
                    double distance = Math.sqrt(
                        (rx > 1 ? Math.pow((x - rx + 0.5) / rx, 2.0) : 0) +
                        (ry > 1 ? Math.pow((y - ry + 0.5) / ry, 2.0) : 0) +
                        (rz > 1 ? Math.pow((z - rz + 0.5) / rz, 2.0) : 0)
                    );
                    // spotless:on

                    if (distance <= 1) {
                        PendingBlock block = new PendingBlock(
                            config.coordA.worldId,
                            x + minX,
                            y + minY,
                            z + minZ,
                            volumes,
                            1,
                            1);

                        present[x + 1][y + 1][z + 1] = true;
                        pending.add(block);
                    }
                }
            }
        }

        ArrayList<ForgeDirection> directions = new ArrayList<>();

        if (rx > 1) {
            directions.add(ForgeDirection.EAST);
            directions.add(ForgeDirection.WEST);
        }

        if (ry > 1) {
            directions.add(ForgeDirection.UP);
            directions.add(ForgeDirection.DOWN);
        }

        if (rz > 1) {
            directions.add(ForgeDirection.NORTH);
            directions.add(ForgeDirection.SOUTH);
        }

        for (PendingBlock block : pending) {
            for (ForgeDirection dir : directions) {
                if (!present[block.x - minX + 1 + dir.offsetX][block.y - minY + 1 + dir.offsetY][block.z - minZ
                    + 1
                    + dir.offsetZ]) {
                    block.setBlock(faces);
                    block.buildOrder = 0;
                    block.renderOrder = 0;
                    break;
                }
            }
        }
    }

    // #endregion

    static class Config {

        public PendingAction action;
        public CoordMode coordMode = CoordMode.SET_INTERLEAVED;
        public BlockSelectMode blockSelectMode = BlockSelectMode.ALL;
        public BlockRemoveMode removeMode = BlockRemoveMode.NONE;
        public PlaceMode placeMode = PlaceMode.GEOMETRY;
        public Shape shape = Shape.LINE;

        public Location coordA, coordB, coordC;
        public Vector3i coordAOffset, coordBOffset, coordCOffset;

        public JsonElement corners, edges, faces, volumes;

        private static JsonElement saveStack(ItemStack stack) {
            if (stack == null || stack.getItem() == null || !(stack.getItem() instanceof ItemBlock)) {
                stack = null;
            }

            return stack == null ? null : toJsonObject(stack.writeToNBT(new NBTTagCompound()));
        }

        private static ItemStack loadStack(JsonElement stack) {
            return stack == null ? null : ItemStack.loadItemStackFromNBT((NBTTagCompound) toNbt(stack));
        }

        public void setCorners(ItemStack corners) {
            this.corners = saveStack(corners);
        }

        public ItemStack getCorners() {
            return loadStack(corners);
        }

        public void setEdges(ItemStack edges) {
            this.edges = saveStack(edges);
        }

        public ItemStack getEdges() {
            return loadStack(edges);
        }

        public void setFaces(ItemStack faces) {
            this.faces = saveStack(faces);
        }

        public ItemStack getFaces() {
            return loadStack(faces);
        }

        public void setVolumes(ItemStack volumes) {
            this.volumes = saveStack(volumes);
        }

        public ItemStack getVolumes() {
            return loadStack(volumes);
        }

        public static MovingObjectPosition getHitResult(EntityPlayer player) {
            double reachDistance = player instanceof EntityPlayerMP mp
                ? mp.theItemInWorldManager.getBlockReachDistance()
                : GTUtility.getClientReachDistance();

            Vec3 posVec = player.getPosition(0)
                .addVector(0, player.getEyeHeight(), 0);

            Vec3 lookVec = player.getLook(0);

            Vec3 modifiedPosVec = posVec.addVector(
                lookVec.xCoord * reachDistance,
                lookVec.yCoord * reachDistance,
                lookVec.zCoord * reachDistance);

            return player.worldObj.rayTraceBlocks(posVec, modifiedPosVec);
        }

        public static Vector3i getLookingAtLocation(EntityPlayer player) {
            double reachDistance = player instanceof EntityPlayerMP mp
                ? mp.theItemInWorldManager.getBlockReachDistance()
                : GTUtility.getClientReachDistance();

            Vec3 posVec = player.getPosition(0);

            Vec3 lookVec = player.getLook(0);

            Vec3 modifiedPosVec = posVec.addVector(
                lookVec.xCoord * reachDistance,
                lookVec.yCoord * reachDistance,
                lookVec.zCoord * reachDistance);

            MovingObjectPosition hit = player.worldObj.rayTraceBlocks(posVec, modifiedPosVec);

            Vector3i target;

            if (hit != null && hit.typeOfHit == MovingObjectType.BLOCK) {
                target = new Vector3i(hit.blockX, hit.blockY, hit.blockZ);

                if (!player.isSneaking()) {
                    ForgeDirection dir = ForgeDirection.getOrientation(hit.sideHit);
                    target.add(dir.offsetX, dir.offsetY, dir.offsetZ);
                }
            } else {
                target = new Vector3i(
                    MathHelper.floor_double(modifiedPosVec.xCoord),
                    MathHelper.floor_double(modifiedPosVec.yCoord),
                    MathHelper.floor_double(modifiedPosVec.zCoord));
            }

            return target;
        }

        public static Vector3i getRegionDeltas(Location a, Location b) {
            if (a == null || b == null || a.worldId != b.worldId) return null;

            int x1 = a.x;
            int y1 = a.y;
            int z1 = a.z;
            int x2 = b.x;
            int y2 = b.y;
            int z2 = b.z;

            int minX = Math.min(x1, x2);
            int minY = Math.min(y1, y2);
            int minZ = Math.min(z1, z2);
            int maxX = Math.max(x1, x2);
            int maxY = Math.max(y1, y2);
            int maxZ = Math.max(z1, z2);

            int dX = (maxX - minX) * (minX < x1 ? -1 : 1);
            int dY = (maxY - minY) * (minY < y1 ? -1 : 1);
            int dZ = (maxZ - minZ) * (minZ < z1 ? -1 : 1);

            return new Vector3i(dX, dY, dZ);
        }

        public static AxisAlignedBB getBoundingBox(Location l, Vector3i deltas) {
            int minX = Math.min(l.x, l.x + deltas.x);
            int minY = Math.min(l.y, l.y + deltas.y);
            int minZ = Math.min(l.z, l.z + deltas.z);
            int maxX = Math.max(l.x, l.x + deltas.x) + 1;
            int maxY = Math.max(l.y, l.y + deltas.y) + 1;
            int maxZ = Math.max(l.z, l.z + deltas.z) + 1;

            return AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
        }

        public static List<Vector3i> getBlocksInBB(Location l, Vector3i deltas) {
            int minX = Math.min(l.x, l.x + deltas.x);
            int minY = Math.min(l.y, l.y + deltas.y);
            int minZ = Math.min(l.z, l.z + deltas.z);
            int maxX = Math.max(l.x, l.x + deltas.x) + 1;
            int maxY = Math.max(l.y, l.y + deltas.y) + 1;
            int maxZ = Math.max(l.z, l.z + deltas.z) + 1;

            int dX = maxX - minX;
            int dY = maxY - minY;
            int dZ = maxZ - minZ;

            List<Vector3i> blocks = new ArrayList<>();

            for (int y = 0; y < dY; y++) {
                for (int z = 0; z < dZ; z++) {
                    for (int x = 0; x < dX; x++) {
                        blocks.add(new Vector3i(minX + x, minY + y, minZ + z));
                    }
                }
            }

            return blocks;
        }

        public Location getCoordA(EntityPlayer player) {
            if (coordAOffset == null) {
                return coordA;
            } else {
                Vector3i lookingAt = getLookingAtLocation(player);
                lookingAt.add(coordAOffset);

                return new Location(player.worldObj, lookingAt);
            }
        }

        public Location getCoordB(EntityPlayer player) {
            if (coordBOffset == null) {
                return coordB;
            } else {
                Vector3i lookingAt = getLookingAtLocation(player);
                lookingAt.add(coordBOffset);

                return new Location(player.worldObj, lookingAt);
            }
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((action == null) ? 0 : action.hashCode());
            result = prime * result + ((coordMode == null) ? 0 : coordMode.hashCode());
            result = prime * result + ((blockSelectMode == null) ? 0 : blockSelectMode.hashCode());
            result = prime * result + ((removeMode == null) ? 0 : removeMode.hashCode());
            result = prime * result + ((placeMode == null) ? 0 : placeMode.hashCode());
            result = prime * result + ((shape == null) ? 0 : shape.hashCode());
            result = prime * result + ((coordA == null) ? 0 : coordA.hashCode());
            result = prime * result + ((coordB == null) ? 0 : coordB.hashCode());
            result = prime * result + ((coordC == null) ? 0 : coordC.hashCode());
            result = prime * result + ((coordAOffset == null) ? 0 : coordAOffset.hashCode());
            result = prime * result + ((coordBOffset == null) ? 0 : coordBOffset.hashCode());
            result = prime * result + ((coordCOffset == null) ? 0 : coordCOffset.hashCode());
            result = prime * result + ((corners == null) ? 0 : corners.hashCode());
            result = prime * result + ((edges == null) ? 0 : edges.hashCode());
            result = prime * result + ((faces == null) ? 0 : faces.hashCode());
            result = prime * result + ((volumes == null) ? 0 : volumes.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            Config other = (Config) obj;
            if (action != other.action) return false;
            if (coordMode != other.coordMode) return false;
            if (blockSelectMode != other.blockSelectMode) return false;
            if (removeMode != other.removeMode) return false;
            if (placeMode != other.placeMode) return false;
            if (shape != other.shape) return false;
            if (coordA == null) {
                if (other.coordA != null) return false;
            } else if (!coordA.equals(other.coordA)) return false;
            if (coordB == null) {
                if (other.coordB != null) return false;
            } else if (!coordB.equals(other.coordB)) return false;
            if (coordC == null) {
                if (other.coordC != null) return false;
            } else if (!coordC.equals(other.coordC)) return false;
            if (coordAOffset == null) {
                if (other.coordAOffset != null) return false;
            } else if (!coordAOffset.equals(other.coordAOffset)) return false;
            if (coordBOffset == null) {
                if (other.coordBOffset != null) return false;
            } else if (!coordBOffset.equals(other.coordBOffset)) return false;
            if (coordCOffset == null) {
                if (other.coordCOffset != null) return false;
            } else if (!coordCOffset.equals(other.coordCOffset)) return false;
            if (corners == null) {
                if (other.corners != null) return false;
            } else if (!corners.equals(other.corners)) return false;
            if (edges == null) {
                if (other.edges != null) return false;
            } else if (!edges.equals(other.edges)) return false;
            if (faces == null) {
                if (other.faces != null) return false;
            } else if (!faces.equals(other.faces)) return false;
            if (volumes == null) {
                if (other.volumes != null) return false;
            } else if (!volumes.equals(other.volumes)) return false;
            return true;
        }

    }

    static enum Shape {
        LINE,
        CUBE,
        SPHERE
    }

    static enum PendingAction {
        GEOM_MOVING_COORDS,
        GEOM_SELECTING_BLOCK,
        MARK_COPY_A,
        MARK_COPY_B,
        MARK_CUT_A,
        MARK_CUT_B,
        MARK_PASTE,
    }

    static enum CoordMode {
        SET_INTERLEAVED,
        SET_A,
        SET_B,
        SET_PASTE,
    }

    static enum BlockSelectMode {
        CORNERS,
        EDGES,
        FACES,
        VOLUMES,
        ALL,
    }

    static enum BlockRemoveMode {
        NONE,
        REPLACEABLE,
        ALL
    }

    static enum PlaceMode {
        GEOMETRY,
        MOVING,
        COPYING,
        DEBUGGING
    }

    static class PendingBlock extends Location {

        public UniqueIdentifier blockId;
        public int metadata;
        public TileAnalysisResult tileData;
        public int renderOrder, buildOrder;

        public transient ItemBlock item;
        public transient Block block;

        public PendingBlock(int worldId, int x, int y, int z, ItemStack block) {
            super(worldId, x, y, z);
            setBlock(block);
        }

        public PendingBlock(int worldId, int x, int y, int z, ItemStack block, int renderOrder, int buildOrder) {
            this(worldId, x, y, z, block);
            this.renderOrder = renderOrder;
            this.buildOrder = buildOrder;
        }

        public PendingBlock(int worldId, int x, int y, int z, Block block, int meta) {
            super(worldId, x, y, z);
            setBlock(block, meta);
        }

        public void reset() {
            this.block = null;
            this.item = null;
            this.blockId = null;
            this.metadata = 0;
        }

        public void setBlock(Block block, int metadata) {
            reset();

            this.blockId = GameRegistry.findUniqueIdentifierFor(block == null ? Blocks.air : block);
            this.metadata = metadata;
        }

        public void setBlock(ItemStack stack) {
            reset();

            Optional<Block> block = Optional.ofNullable(stack)
                .map(ItemStack::getItem)
                .map(Block::getBlockFromItem);

            if (block.isPresent()) {
                this.block = block.get();
                this.item = (ItemBlock) Item.getItemFromBlock(block.get());
                this.blockId = GameRegistry.findUniqueIdentifierFor(block.get());
                this.metadata = this.item.getMetadata(Items.feather.getDamage(stack));
            }
        }

        public Block getBlock() {
            if (block == null) {
                block = blockId == null ? Blocks.air : GameRegistry.findBlock(blockId.modId, blockId.name);
            }

            return block;
        }

        public ItemBlock getItem() {
            if (item == null) {
                Block block = getBlock();

                if (block != null) {
                    item = (ItemBlock) Item.getItemFromBlock(block);
                }
            }

            return item;
        }

        public ItemStack toStack() {
            Item item = getItem();

            return item == null ? null : new ItemStack(item, 1, metadata);
        }

        public boolean isFree() {
            Block block = getBlock();

            if (block == Blocks.air) {
                return true;
            }

            if (block == AEApi.instance()
                .definitions()
                .blocks()
                .multiPart()
                .maybeBlock()
                .get()) {
                return true;
            }

            return false;
        }

        public static PendingBlock fromBlock(World world, int x, int y, int z) {
            Block block = world.getBlock(x, y, z);

            Item item = Item.getItemFromBlock(block);

            Block realBlock = !block.isFlowerPot() ? Block.getBlockFromItem(item) : block;
            int meta = realBlock.getDamageValue(world, x, y, z);

            return new PendingBlock(world.provider.dimensionId, x, y, z, realBlock, meta);
        }

        @Override
        public String toString() {
            return "PendingBlock [blockId=" + blockId
                + ", metadata="
                + metadata
                + ", tileData="
                + tileData
                + ", renderOrder="
                + renderOrder
                + ", buildOrder="
                + buildOrder
                + ", x="
                + x
                + ", y="
                + y
                + ", z="
                + z
                + ", worldId="
                + worldId
                + ", world="
                + getWorld()
                + "]";
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + ((blockId == null) ? 0 : blockId.hashCode());
            result = prime * result + metadata;
            result = prime * result + ((tileData == null) ? 0 : tileData.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!super.equals(obj)) return false;
            if (getClass() != obj.getClass()) return false;
            PendingBlock other = (PendingBlock) obj;
            if (blockId == null) {
                if (other.blockId != null) return false;
            } else if (!blockId.equals(other.blockId)) return false;
            if (metadata != other.metadata) return false;
            if (tileData == null) {
                if (other.tileData != null) return false;
            } else if (!tileData.equals(other.tileData)) return false;
            return true;
        }

    }

    static class Location {

        public int worldId;
        public int x, y, z;

        public Location() {

        }

        public Location(int worldId, int x, int y, int z) {
            this.worldId = worldId;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public Location(@Nonnull World world, int x, int y, int z) {
            this(world.provider.dimensionId, x, y, z);
        }

        public Location(@Nonnull World world, Vector3i v) {
            this(world, v.x, v.y, v.z);
        }

        @Override
        public String toString() {
            return String.format("X=%,d Y=%,d Z=%,d", x, y, z);
        }

        public Vector3i toVec() {
            return new Vector3i(x, y, z);
        }

        public boolean isInWorld(@Nonnull World world) {
            return world.provider.dimensionId == worldId;
        }

        public @Nullable World getWorld() {
            if (MinecraftServer.getServer() != null) {
                for (WorldServer world : MinecraftServer.getServer().worldServers) {
                    if (world.provider.dimensionId == worldId) {
                        return world;
                    }
                }
            }

            if (Minecraft.getMinecraft() != null) {
                WorldClient world = Minecraft.getMinecraft().theWorld;

                if (world.provider.dimensionId == worldId) {
                    return world;
                }
            }

            return null;
        }

        public Location offset(ForgeDirection dir) {
            this.x += dir.offsetX;
            this.y += dir.offsetY;
            this.z += dir.offsetZ;
            return this;
        }

        public Location offset(int dx, int dy, int dz) {
            this.x += dx;
            this.y += dy;
            this.z += dz;
            return this;
        }

        public Location clone() {
            return new Location(worldId, x, y, z);
        }

        public static boolean areCompatible(Location a, Location b) {
            if (a == null || b == null) return false;

            if (a.worldId != b.worldId) return false;

            return true;
        }

        public static boolean areCompatible(Location a, Location b, Location c) {
            if (a == null || b == null || c == null) return false;

            if (a.worldId != b.worldId) return false;
            if (a.worldId != c.worldId) return false;

            return true;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + worldId;
            result = prime * result + x;
            result = prime * result + y;
            result = prime * result + z;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            Location other = (Location) obj;
            if (worldId != other.worldId) return false;
            if (x != other.x) return false;
            if (y != other.y) return false;
            if (z != other.z) return false;
            return true;
        }
    }

    public static class Region {

        public Location a, b;

        public Region() {

        }

        public Region(Location a, Location b) {
            this.a = a;
            this.b = b;
        }
    }

    @SuppressWarnings("unchecked")
    public static JsonElement toJsonObject(NBTBase nbt) {
        if (nbt == null) {
            return null;
        }

        if (nbt instanceof NBTTagCompound) {
            // NBTTagCompound
            final NBTTagCompound nbtTagCompound = (NBTTagCompound) nbt;
            final Map<String, NBTBase> tagMap = (Map<String, NBTBase>) nbtTagCompound.tagMap;

            JsonObject root = new JsonObject();

            for (Map.Entry<String, NBTBase> nbtEntry : tagMap.entrySet()) {
                root.add(nbtEntry.getKey(), toJsonObject(nbtEntry.getValue()));
            }

            return root;
        } else if (nbt instanceof NBTTagByte) {
            // Number (byte)
            return new JsonPrimitive(((NBTTagByte) nbt).func_150290_f());
        } else if (nbt instanceof NBTTagShort) {
            // Number (short)
            return new JsonPrimitive(((NBTTagShort) nbt).func_150289_e());
        } else if (nbt instanceof NBTTagInt) {
            // Number (int)
            return new JsonPrimitive(((NBTTagInt) nbt).func_150287_d());
        } else if (nbt instanceof NBTTagLong) {
            // Number (long)
            return new JsonPrimitive(((NBTTagLong) nbt).func_150291_c());
        } else if (nbt instanceof NBTTagFloat) {
            // Number (float)
            return new JsonPrimitive(((NBTTagFloat) nbt).func_150288_h());
        } else if (nbt instanceof NBTTagDouble) {
            // Number (double)
            return new JsonPrimitive(((NBTTagDouble) nbt).func_150286_g());
        } else if (nbt instanceof NBTBase.NBTPrimitive) {
            // Number
            return new JsonPrimitive(((NBTBase.NBTPrimitive) nbt).func_150286_g());
        } else if (nbt instanceof NBTTagString) {
            // String
            return new JsonPrimitive(((NBTTagString) nbt).func_150285_a_());
        } else if (nbt instanceof NBTTagList) {
            // Tag List
            final NBTTagList list = (NBTTagList) nbt;

            JsonArray arr = new JsonArray();
            list.tagList.forEach(c -> arr.add(toJsonObject((NBTBase) c)));
            return arr;
        } else if (nbt instanceof NBTTagIntArray) {
            // Int Array
            final NBTTagIntArray list = (NBTTagIntArray) nbt;

            JsonArray arr = new JsonArray();

            for (int i : list.func_150302_c()) {
                arr.add(new JsonPrimitive(i));
            }

            return arr;
        } else if (nbt instanceof NBTTagByteArray) {
            // Byte Array
            final NBTTagByteArray list = (NBTTagByteArray) nbt;

            JsonArray arr = new JsonArray();

            for (byte i : list.func_150292_c()) {
                arr.add(new JsonPrimitive(i));
            }

            return arr;
        } else {
            throw new IllegalArgumentException("Unsupported NBT Tag: " + NBTBase.NBTTypes[nbt.getId()] + " - " + nbt);
        }
    }

    public static NBTBase toNbt(JsonElement jsonElement) {
        if (jsonElement == null) {
            return null;
        }

        if (jsonElement instanceof JsonPrimitive) {
            final JsonPrimitive jsonPrimitive = (JsonPrimitive) jsonElement;

            if (jsonPrimitive.isNumber()) {
                if (jsonPrimitive.getAsBigDecimal()
                    .remainder(BigDecimal.ONE)
                    .equals(BigDecimal.ZERO)) {
                    long lval = jsonPrimitive.getAsLong();

                    if (lval >= Byte.MIN_VALUE && lval <= Byte.MAX_VALUE) {
                        return new NBTTagByte((byte) lval);
                    }

                    if (lval >= Short.MIN_VALUE && lval <= Short.MAX_VALUE) {
                        return new NBTTagShort((short) lval);
                    }

                    if (lval >= Integer.MIN_VALUE && lval <= Integer.MAX_VALUE) {
                        return new NBTTagInt((int) lval);
                    }

                    return new NBTTagLong(lval);
                } else {
                    double dval = jsonPrimitive.getAsDouble();
                    float fval = (float) dval;

                    if (Math.abs(dval - fval) < 0.0001) {
                        return new NBTTagFloat(fval);
                    }

                    return new NBTTagDouble(dval);
                }
            } else {
                return new NBTTagString(jsonPrimitive.getAsString());
            }
        } else if (jsonElement instanceof JsonArray) {
            // NBTTagIntArray or NBTTagList
            final JsonArray jsonArray = (JsonArray) jsonElement;
            final List<NBTBase> nbtList = new ArrayList<>();

            for (JsonElement element : jsonArray) {
                nbtList.add(toNbt(element));
            }

            // spotless:off
            if (nbtList.stream().allMatch(n -> n instanceof NBTTagInt)) {
                return new NBTTagIntArray(nbtList.stream().mapToInt(i -> ((NBTTagInt) i).func_150287_d()).toArray());
            } else if (nbtList.stream().allMatch(n -> n instanceof NBTTagByte)) {
                final byte[] abyte = new byte[nbtList.size()];

                for (int i = 0; i < nbtList.size(); i++) {
                    abyte[i] = ((NBTTagByte) nbtList.get(i)).func_150290_f();
                }

                return new NBTTagByteArray(abyte);
            } else {
                NBTTagList nbtTagList = new NBTTagList();
                nbtList.forEach(nbtTagList::appendTag);

                return nbtTagList;
            }
            // spotless:on
        } else if (jsonElement instanceof JsonObject) {
            // NBTTagCompound
            final JsonObject jsonObject = (JsonObject) jsonElement;

            NBTTagCompound nbtTagCompound = new NBTTagCompound();

            for (Map.Entry<String, JsonElement> jsonEntry : jsonObject.entrySet()) {
                nbtTagCompound.setTag(jsonEntry.getKey(), toNbt(jsonEntry.getValue()));
            }

            return nbtTagCompound;
        }

        throw new IllegalArgumentException("Unhandled element " + jsonElement);
    }
}