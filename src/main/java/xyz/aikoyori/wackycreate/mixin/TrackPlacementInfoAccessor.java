package xyz.aikoyori.wackycreate.mixin;

import com.simibubi.create.content.logistics.trains.BezierConnection;
import com.simibubi.create.content.logistics.trains.track.TrackPlacement;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = TrackPlacement.PlacementInfo.class,remap = false)
public interface TrackPlacementInfoAccessor {
    @Accessor("valid")
    void setValid(boolean validity);
    @Accessor("valid")
    boolean getValid();

    @Accessor("curve")
    BezierConnection getCurve();
    @Accessor("curve")
    void setCurve(BezierConnection bezier);
    @Accessor("end1Extent")
    int getEnd1Extend();
    @Accessor("end1Extent")
    void setEnd1Extend(int value);
    @Accessor("end2Extent")
    int getEnd2Extend();
    @Accessor("end2Extent")
    void setEnd2Extend(int value);
    @Accessor("message")
    String getMessage();
    @Accessor("message")
    void setMessage(String value);


    @Accessor("end1")
    Vec3d getEnd1();
    @Accessor("end1")
    void setEnd1(Vec3d value);
    @Accessor("end2")
    Vec3d getEnd2();
    @Accessor("end2")
    void setEnd2(Vec3d value);
    @Accessor("normal1")
    Vec3d getNormal1();
    @Accessor("normal1")
    void setNormal1(Vec3d value);
    @Accessor("normal2")
    Vec3d getNormal2();
    @Accessor("normal2")
    void setNormal2(Vec3d value);
    @Accessor("axis1")
    Vec3d getAxis1();
    @Accessor("axis1")
    void setAxis1(Vec3d value);
    @Accessor("axis2")
    Vec3d getAxis2();
    @Accessor("axis2")
    void setAxis2(Vec3d value);
    @Accessor("pos1")
    BlockPos getPos1();
    @Accessor("pos1")
    void setPos1(BlockPos value);
    @Accessor("pos2")
    BlockPos getPos2();
    @Accessor("pos2")
    void setPos2(BlockPos value);

    /*

    * */
}
