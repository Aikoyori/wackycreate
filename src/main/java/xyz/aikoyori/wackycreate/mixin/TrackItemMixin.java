package xyz.aikoyori.wackycreate.mixin;

import com.simibubi.create.content.logistics.trains.track.TrackBlockItem;
import com.simibubi.create.content.logistics.trains.track.TrackPlacement;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = TrackBlockItem.class)
public class TrackItemMixin {
    @Redirect(method = "useOnBlock",at = @At(value = "FIELD", target = "Lcom/simibubi/create/content/logistics/trains/track/TrackPlacement$PlacementInfo;valid:Z", opcode = Opcodes.GETFIELD))
    private boolean wackycreate$placementninfo(TrackPlacement.PlacementInfo instance){
        return true;
    }
}
