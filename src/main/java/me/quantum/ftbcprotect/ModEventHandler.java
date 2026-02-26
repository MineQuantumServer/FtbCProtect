package me.quantum.ftbcprotect;

import dev.ftb.mods.ftbchunks.data.ClaimedChunkImpl;
import dev.ftb.mods.ftbchunks.data.ClaimedChunkManagerImpl;
import dev.ftb.mods.ftblibrary.math.ChunkDimPos;
import java.util.Arrays;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;


@EventBusSubscriber(modid = Ftbcprotect.MODID)
public class ModEventHandler {

    // 监听玩家右键点击物品事件
    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getLevel().isClientSide) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!isOp(player)) return;
        ItemStack itemStack = event.getItemStack();
        var pos = event.getPos();
        var itemName = Arrays.stream(itemStack.getTags().map(TagKey::location).map(Object::toString).toArray(String[]::new)).toList();
        if (itemName.isEmpty()) return;
        if (!hasPermissionAt(player, pos)) {
            if (itemName.contains(Ftbcprotect.Prohibited_Item)) {
                event.getEntity().displayClientMessage(Component.literal("检测到禁用物品"), true);
                event.setCanceled(true);
            }
        }

        int radius = Config.region_check_radius;

        // 检查是否在无权领地附近
        if (hasNearbyRestrictedArea(player, radius)) {
            if (itemName.contains(Ftbcprotect.Prohibited_ItemInRegion)) {
                player.sendSystemMessage(
                        Component.literal("§c你附近有领地，不能使用这个物品！")
                );
                event.setCanceled(true);
            }
        }
    }

    /**
     * 情况3: 在无权领地附近区域内 - 禁止放置 Prohibited_Block_InRegion 标签的方块
     */
    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!isOp(player)) return;
        BlockState placedBlock = event.getPlacedBlock();
        int radius = Config.region_check_radius;
        var itemName = Arrays.stream(placedBlock.getTags().map(TagKey::location).map(Object::toString).toArray(String[]::new)).toList();
        // 检查是否在无权领地附近
        if (hasNearbyRestrictedArea(player, radius)) {
            // 检查放置的方块是否被禁止
            if (itemName.contains(Ftbcprotect.Prohibited_BlockInRegion)) {
                player.sendSystemMessage(
                        Component.literal("§c你附近有领地，不能放置这个方块！")
                );
                event.setCanceled(true);
            }
        }
    }

    /**
     * 左键点击方块事件
     * 拦截：斧头剥皮、锄头耕地、剑/工具攻击方块等
     */
    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getLevel().isClientSide) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!isOp(player)) return;
        ItemStack itemStack = event.getItemStack();
        BlockPos clickPos = event.getPos(); // 点击的方块位置
        int radius = Config.region_check_radius;
        var itemName = Arrays.stream(itemStack.getTags().map(TagKey::location).map(Object::toString).toArray(String[]::new)).toList();
        // 情况1: 在无权领地附近区域内 - 禁止使用 Prohibited_Item_InRegion 标签的物品左键点击
        if (hasNearbyRestrictedArea(player, radius)) {
            if (itemName.contains(Ftbcprotect.Prohibited_ItemInRegion)) {
                player.sendSystemMessage(
                        Component.literal("§c你附近有领地，不能使用这个物品左键点击！")
                );
                event.setCanceled(true);
                return;
            }
        }

        // 情况2: 点击的位置在无权领地内 - 检查基础禁止物品
        if (!hasPermissionAt(player, clickPos)) {
            if (itemName.contains(Ftbcprotect.Prohibited_Item)) {
                player.sendSystemMessage(
                        Component.literal("§c你不能在这个领地使用这个物品！")
                );
                event.setCanceled(true);
                return;
            }
        }

        // 情况3: 玩家本身站在无权领地内 - 检查基础禁止物品
        if (!hasPermissionAt(player, player.blockPosition())) {
            if (itemName.contains(Ftbcprotect.Prohibited_Item)) {
                player.sendSystemMessage(
                        Component.literal("§c你不能在此领地使用这个物品！")
                );
                event.setCanceled(true);
            }
        }
    }

    /**
     * 左键点击实体事件
     * 拦截：用剑/斧攻击生物、用工具攻击玩家等
     */
    @SubscribeEvent
    public static void onLeftClickEntity(PlayerInteractEvent.EntityInteractSpecific event) {
        if (event.getLevel().isClientSide) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!isOp(player)) return;
        ItemStack itemStack = event.getItemStack();
        BlockPos playerPos = player.blockPosition();
        BlockPos entityPos = event.getTarget().blockPosition(); // 实体的位置
        int radius = Config.region_check_radius;
        var itemName = Arrays.stream(itemStack.getTags().map(TagKey::location).map(Object::toString).toArray(String[]::new)).toList();
        // 检查附近是否有无权领地
        if (hasNearbyRestrictedArea(player, radius)) {
            if (itemName.contains(Ftbcprotect.Prohibited_ItemInRegion)) {
                player.sendSystemMessage(
                        Component.literal("§c你附近有领地，不能攻击！")
                );
                event.setCanceled(true);
                return;
            }
        }

        // 检查玩家位置或实体位置是否在无权领地内
        if (!hasPermissionAt(player, playerPos) ||
                !hasPermissionAt(player, entityPos)) {
            if (itemName.contains(Ftbcprotect.Prohibited_Item)) {
                player.sendSystemMessage(
                        Component.literal("§c你不能在此领地攻击！")
                );
                event.setCanceled(true);
            }
        }
    }

    /**
     * 通用实体交互事件（包括空手左键点击实体）
     */
    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getLevel().isClientSide) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!isOp(player)) return;
        ItemStack itemStack = event.getItemStack();
        BlockPos playerPos = player.blockPosition();
        BlockPos entityPos = event.getTarget().blockPosition();
        int radius = Config.region_check_radius;
        var itemName = Arrays.stream(itemStack.getTags().map(TagKey::location).map(Object::toString).toArray(String[]::new)).toList();
        // 如果附近有无权领地，禁止区域限制物品
        if (hasNearbyRestrictedArea(player, radius)) {
            if (itemName.contains(Ftbcprotect.Prohibited_ItemInRegion)) {
                player.sendSystemMessage(
                        Component.literal("§c你附近有领地，不能与实体交互！")
                );
                event.setCanceled(true);
                return;
            }
        }

        // 如果在无权领地内，禁止基础物品
        if (!hasPermissionAt(player, playerPos) ||
                !hasPermissionAt(player, entityPos)) {
            if (itemName.contains(Ftbcprotect.Prohibited_Item)) {
                player.sendSystemMessage(
                        Component.literal("§c你不能在此领地与实体交互！")
                );
                event.setCanceled(true);
            }
        }
    }

    public static boolean isOp(Player ep) {
        return ServerLifecycleHooks.getCurrentServer().getPlayerList().isOp(ep.getGameProfile());
    }

    /**
     * 检查玩家在指定位置是否有权限
     * @return true = 有权限, false = 无权限
     */
    public static boolean hasPermissionAt(Player player, BlockPos pos) {
        if (!ModList.get().isLoaded("ftbchunks")) {
            return true; // 没有FTB Chunks则全部允许
        }
        ClaimedChunkImpl chunk = ClaimedChunkManagerImpl.getInstance().getChunk(new ChunkDimPos(player.level(), pos));
        if (chunk == null) {
            return true;
        }
        if (chunk.getTeamData().isAlly(player.getUUID())) {
            return true;
        }
        return chunk.getTeamData().isTeamMember(player.getUUID());
    }

    /**
     * 检查指定区块是否被任何人认领
     */
    public static boolean isChunkClaimed(ServerPlayer player, ChunkPos chunkPos) {
        ClaimedChunkImpl chunk = ClaimedChunkManagerImpl.getInstance().getChunk(new ChunkDimPos(player.level(), chunkPos.getWorldPosition()));
        return chunk != null;
    }

    /**
     * 检查玩家周围指定半径内是否有被认领的领地（基于方块位置判断）
     * 只有当玩家当前位置不在任何领地内时才进行检查
     */
    public static boolean hasNearbyRestrictedArea(ServerPlayer player, int radiusBlocks) {
        BlockPos playerPos = player.blockPosition();

        // 首先检查玩家当前位置是否在领地内
        if (isChunkClaimed(player, new ChunkPos(playerPos))) {
            // 如果玩家已经在领地内，允许放置方块
            return false;
        }

        for (int dx = -radiusBlocks; dx <= radiusBlocks; dx++) {
            for (int dz = -radiusBlocks; dz <= radiusBlocks; dz++) {
                BlockPos checkPos = playerPos.offset(dx, 0, dz);
                ChunkPos checkChunk = new ChunkPos(checkPos);

                // 计算距离是否在指定半径内
                double distance = Math.sqrt(dx * dx + dz * dz);
                if (distance <= radiusBlocks) {
                    // 如果该区块被认领
                    if (isChunkClaimed(player, checkChunk)) {
                        return true; // 附近有被认领的领地
                    }
                }
            }
        }
        return false;
    }
}