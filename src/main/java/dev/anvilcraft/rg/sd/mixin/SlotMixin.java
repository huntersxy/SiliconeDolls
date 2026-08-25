package dev.anvilcraft.rg.sd.mixin;

//? if <1.21.8
import com.mojang.datafixers.util.Pair;
import dev.anvilcraft.rg.sd.util.ISlotIconInjector;
import net.minecraft.resources.ResourceLocation;
//? if <1.21.8
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
//? if >=1.21.8
/*import org.jetbrains.annotations.NotNull;*/
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
abstract class SlotMixin implements ISlotIconInjector {
    //? if <1.21.8 {
    @Unique
    private Pair<ResourceLocation, ResourceLocation> siliconeDolls$pair;

    @Inject(method = "getNoItemIcon", at = @At("HEAD"), cancellable = true)
    private void getNoItemIcon(
        CallbackInfoReturnable<Pair<ResourceLocation, ResourceLocation>> cir
    ) {
        if (this.siliconeDolls$pair != null) cir.setReturnValue(this.siliconeDolls$pair);
    }

    @Override
    public void siliconeDolls$setIcon(ResourceLocation resource) {
        if (resource != null) {
            this.siliconeDolls$pair = Pair.of(InventoryMenu.BLOCK_ATLAS, resource);
        }
    }
    //?} else {
    /*@Unique
    private ResourceLocation siliconeDolls$location;

    @Inject(method = "getNoItemIcon", at = @At("HEAD"), cancellable = true)
    private void getNoItemIcon(@NotNull CallbackInfoReturnable<ResourceLocation> cir) {
        cir.setReturnValue(this.siliconeDolls$location);
    }

    @Override
    public void siliconeDolls$setIcon(ResourceLocation resource) {
        if (resource != null) {
            this.siliconeDolls$location = resource;
        }
    }
     *///?}
}
