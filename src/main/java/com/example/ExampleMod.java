package com.autocore;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.VaultBlockEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class HeavyCoreAutoLoopMod implements ClientModInitializer {
    private static KeyBinding toggleKey;
    private static boolean isAutoScanning = false; // រក្សា trạngថានៃការ Auto-Loop
    private int tickCounter = 0;

    @Override
    public void onInitializeClient() {
        // បង្កើត Keybinding [G]
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.autocore.toggle_loop",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "category.autocore"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;

            // ១. ចាប់ Event ពេលចុច [G] ដើម្បី បើក ឬ បិទ (ON / OFF)
            while (toggleKey.wasPressed()) {
                isAutoScanning = !isAutoScanning;
                if (isAutoScanning) {
                    client.player.sendMessage(Text.literal("§a[AutoCore] បានបើក Auto Scanner! កំពុងរង់ចាំ Heavy Core..."), true);
                } else {
                    client.player.sendMessage(Text.literal("§c[AutoCore] បានបិទ Auto Scanner!"), true);
                }
            }

            // ២. ប្រសិនបើកំពុងបើក (ON) វានឹងស្កេនរៀងរាល់ 1/2 វិនាទី (10 ticks) ម្ដងដោយស្វ័យប្រវត្តិ
            if (isAutoScanning) {
                tickCounter++;
                if (tickCounter >= 10) { 
                    tickCounter = 0;
                    scanAndUnlockVault(client);
                }
            }
        });
    }

    private void scanAndUnlockVault(net.minecraft.client.MinecraftClient client) {
        BlockPos playerPos = client.player.getBlockPos();
        int radius = 5;

        for (BlockPos pos : BlockPos.iterate(playerPos.add(-radius, -radius, -radius), playerPos.add(radius, radius, radius))) {
            if (client.world.getBlockState(pos).isOf(Blocks.VAULT)) {
                BlockEntity blockEntity = client.world.getBlockEntity(pos);

                if (blockEntity instanceof VaultBlockEntity vault) {
                    var displayItem = vault.getClientData().getDisplayItem();

                    // បើឃើញ Heavy Core វានឹងចាក់ស្វ័យប្រវត្តិភ្លាម
                    if (displayItem != null && displayItem.isOf(Items.HEAVY_CORE)) {
                        BlockHitResult hitResult = new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos, false);
                        if (client.interactionManager != null) {
                            client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, hitResult);
                            client.player.sendMessage(Text.literal("§a[AutoCore] បានចាក់យក Heavy Core ដោយស្វ័យប្រវត្តិ!"), true);
                        }
                    }
                }
            }
        }
    }
}
