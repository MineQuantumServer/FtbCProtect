package me.quantum.ftbcprotect;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
@EventBusSubscriber(modid = Ftbcprotect.MODID)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.IntValue REGION_CHECK_RADIUS = BUILDER
            .comment("检查领地附近区域的半径（以区块为单位）","例如: 1 表示检查当前区块 + 周围1圈区块 (3x3)").
            defineInRange("region_check_radius", 2, 0, Integer.MAX_VALUE);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static int region_check_radius;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        region_check_radius = REGION_CHECK_RADIUS.get();
    }
}
