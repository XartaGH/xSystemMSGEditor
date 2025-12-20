package me.xarta.xsystemmsgeditor.mixin;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommandSourceStack.class)
public class CommandSourceStackMixin {

    @Inject(method = "sendFailure", at = @At("HEAD"), cancellable = true)
    private void xsystemmsgeditor$cancelVanillaFailure(Component message, CallbackInfo ci) {
        ci.cancel(); // cancel vanilla error message
    }
}