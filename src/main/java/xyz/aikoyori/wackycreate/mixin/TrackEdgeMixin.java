package xyz.aikoyori.wackycreate.mixin;

import com.simibubi.create.content.logistics.trains.TrackEdge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TrackEdge.class,remap = false)
public class TrackEdgeMixin {
    @Inject(method = "canTravelTo",at = @At("HEAD"),cancellable = true)
    void wackycreate$travellable(TrackEdge other, CallbackInfoReturnable<Boolean> cir){
        cir.setReturnValue(true);
    }
}
