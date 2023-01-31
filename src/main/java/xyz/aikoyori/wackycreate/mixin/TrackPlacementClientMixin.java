package xyz.aikoyori.wackycreate.mixin;

import com.simibubi.create.content.logistics.trains.track.TrackPlacement;
import net.minecraft.util.math.Vec3d;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Objects;

@Mixin(value = TrackPlacement.class,remap = false)
public class TrackPlacementClientMixin {
    @Redirect(method = "clientTick",at = @At(value = "FIELD", target = "Lcom/simibubi/create/content/logistics/trains/track/TrackPlacement$PlacementInfo;valid:Z", opcode = Opcodes.GETFIELD))
    private static boolean wackycreate$placementninfo(TrackPlacement.PlacementInfo instance){
        return true;
    }

}
