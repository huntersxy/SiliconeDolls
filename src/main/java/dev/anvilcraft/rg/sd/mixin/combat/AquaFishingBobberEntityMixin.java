package dev.anvilcraft.rg.sd.mixin.combat;

//? if <26
import com.teammetallurgy.aquaculture.entity.AquaFishingBobberEntity;
//? if >=26
/*import com.teammetallurgy.aquaculture.entity.AquaFishingHookEntity;*/
import dev.anvilcraft.rg.sd.SiliconeDollsServerRules;
import dev.anvilcraft.rg.sd.entity.FakePlayer;
import dev.anvilcraft.rg.sd.tool.FakePlayerAutoFish;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FishingHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if <26
@Mixin(AquaFishingBobberEntity.class)
//? if >=26
/*@Mixin(AquaFishingHookEntity.class)*/
abstract class AquaFishingBobberEntityMixin {
    @Inject(method = "catchingFish", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/syncher/SynchedEntityData;set(Lnet/minecraft/network/syncher/EntityDataAccessor;Ljava/lang/Object;)V", ordinal = 1))
    private void catchingFish(BlockPos pos, CallbackInfo ci) {
        Entity entity = ((FishingHook) (Object) this).getOwner();
        if (SiliconeDollsServerRules.fakePlayerAutoFish && entity instanceof FakePlayer player) {
            FakePlayerAutoFish.autoFish(player);
        }
    }
}
