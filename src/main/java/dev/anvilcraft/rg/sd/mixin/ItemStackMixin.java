package dev.anvilcraft.rg.sd.mixin;

import dev.anvilcraft.rg.sd.SiliconeDollsServerRules;
import dev.anvilcraft.rg.sd.entity.FakePlayer;
import dev.anvilcraft.rg.sd.tool.FakePlayerAutoReplaceTool;
import dev.anvilcraft.rg.sd.tool.FakePlayerAutoReplenishment;
import net.minecraft.world.InteractionHand;
//? if <1.21.8
import net.minecraft.world.InteractionResultHolder;
//? if >=1.21.8
/*import net.minecraft.world.InteractionResult;*/
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
abstract class ItemStackMixin {
    //? if <1.21.8 {
    @Inject(method = "use", at = @At("HEAD"))
    private void use(Level level, Player player, InteractionHand usedHand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        if (SiliconeDollsServerRules.fakePlayerAutoReplenishment && player instanceof FakePlayer fakePlayer) {
            FakePlayerAutoReplenishment.autoReplenishment(fakePlayer);
        }
    }
    //?} else {
    /*@Inject(method = "use", at = @At("HEAD"))
    private void use(Level level, Player player, InteractionHand usedHand, CallbackInfoReturnable<InteractionResult> cir) {
        if (SiliconeDollsServerRules.fakePlayerAutoReplenishment && player instanceof FakePlayer fakePlayer) {
            FakePlayerAutoReplenishment.autoReplenishment(fakePlayer);
        }
    }
     *///?}

    @Inject(method = "hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V", at = @At("HEAD"))
    private void hurtAndBreak(int i, LivingEntity entity, EquipmentSlot equipmentSlot, CallbackInfo ci) {
        if (SiliconeDollsServerRules.fakePlayerAutoReplaceTool && entity instanceof FakePlayer fakePlayer) {
            FakePlayerAutoReplaceTool.autoReplaceTool(fakePlayer);
        }
    }
}
