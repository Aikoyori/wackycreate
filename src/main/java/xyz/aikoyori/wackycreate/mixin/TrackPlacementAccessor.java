package xyz.aikoyori.wackycreate.mixin;

import com.simibubi.create.content.logistics.trains.track.TrackPlacement;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = TrackPlacement.class,remap = false)
public interface TrackPlacementAccessor {

    @Accessor("hoveringMaxed")
    public static boolean getHoveringMaxed() {
        throw new AssertionError();
    }
    @Accessor("hoveringMaxed")
    public static void setHoveringMaxed(boolean value) {
        throw new AssertionError();
    }
    @Accessor("hoveringPos")
    public static BlockPos getHoveringPos() {
        throw new AssertionError();
    }
    @Accessor("hoveringPos")
    public static void setHoveringPos(BlockPos value) {
        throw new AssertionError();
    }

    @Accessor("hoveringAngle")
    public static int gethoveringAngle() {
        throw new AssertionError();
    }
    @Accessor("hoveringAngle")
    public static void sethoveringAngle(int value) {
        throw new AssertionError();
    }
    @Accessor("lastItem")
    public static ItemStack getLastItem() {
        throw new AssertionError();
    }
    @Accessor("lastItem")
    public static void setLastItem(ItemStack value) {
        throw new AssertionError();
    }
}
