package org.stepan1411.pvp_bot.mixin;

import net.minecraft.server.PlayerManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Увеличивает максимальное количество игроков на сервере до 99999
 * Это позволяет спавнить много ботов без ограничений
 */
@Mixin(PlayerManager.class)
public class ServerConfigHandlerMixin {
    
    @Inject(method = "getMaxPlayerCount", at = @At("RETURN"), cancellable = true)
    private void increaseMaxPlayers(CallbackInfoReturnable<Integer> cir) {
        // Увеличиваем лимит до 99999 игроков
        cir.setReturnValue(99999);
    }
}
