package com.example;

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
    private static boolean isAutoScanning = false;
    private int tickCounter = 0;

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.autocore.toggle_auto_off",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "category.autocore"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;

            while (toggleKey.wasPressed()) {
                isAutoScanning = !isAutoScanning;
                if (isAutoScanning) {
                    client.player.sendMessage(Text.literal("§a[AutoCore] បើក Auto Scanner! កំពុងរង់ចាំ Heavy Core..."), true);
                } else {
                    client.player.sendMessage(Text.literal("§c[AutoCore] បានបិទ Auto Scanner!"), true);
                }
            }

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

                    if (displayItem != null && displayItem.isOf(Items.HEAVY_CORE)) {
                        BlockHitResult hitResult = new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos, false);
                        
                        if (client.interactionManager != null) {
                            client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, hitResult);
                            client.player.sendMessage(Text.literal("§a[AutoCore] ចាក់បាន Heavy Core រួចរាល់! ប្រព័ន្ធត្រូវ​បានបិទស្វ័យប្រវត្តិ។"), false);
                            isAutoScanning = false; 
                            break;
                        }
                    }
                }
            }
        }
    }
}
