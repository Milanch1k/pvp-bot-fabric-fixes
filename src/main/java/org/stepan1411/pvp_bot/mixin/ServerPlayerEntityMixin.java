package org.stepan1411.pvp_bot.mixin;

// Этот mixin больше не используется - логика перенесена в BotDamageHandler
// Оставлен пустым чтобы не ломать конфигурацию

import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
    // Пустой mixin - логика damage перенесена в Fabric Events
}
