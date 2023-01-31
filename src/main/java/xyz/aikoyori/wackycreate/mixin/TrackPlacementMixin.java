package xyz.aikoyori.wackycreate.mixin;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.logistics.trains.BezierConnection;
import com.simibubi.create.content.logistics.trains.ITrackBlock;
import com.simibubi.create.content.logistics.trains.track.TrackBlock;
import com.simibubi.create.content.logistics.trains.track.TrackPlacement;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.utility.*;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemHandlerHelper;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;



@Mixin(value = TrackPlacement.class,remap = false)
public abstract class TrackPlacementMixin {/*
    @Redirect(method = "tryConnect",at = @At(value = "FIELD", target = "Lcom/simibubi/create/content/logistics/trains/track/TrackPlacement$PlacementInfo;valid:Z", opcode = Opcodes.PUTFIELD))
    private static void wackycreate$placementninfo(TrackPlacement.PlacementInfo instance, boolean value){
        ((TrackPlacementInfoAccessor)(instance)).setValid(true);
    }
    @Redirect(method = "tryConnect",at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/BlockPos;getSquaredDistance(Lnet/minecraft/util/math/Vec3i;)D"))
    private static double wackycreate$placementninfo(BlockPos instance, Vec3i vec3i){
        return 0;
    }*/

    @Shadow public static TrackPlacement.PlacementInfo cached;

    @Shadow private static boolean hoveringMaxed;

    @Shadow
    private static TrackPlacement.PlacementInfo placeTracks(World level, TrackPlacement.PlacementInfo info, BlockState state1, BlockState state2, BlockPos targetPos1, BlockPos targetPos2, boolean simulate) {
        return null;
    }

    @Shadow
    protected static void paveTracks(World level, TrackPlacement.PlacementInfo info, BlockItem blockItem, boolean simulate) {
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public static TrackPlacement.PlacementInfo tryConnect(World level, PlayerEntity player, BlockPos pos2, BlockState state2,
                                                          ItemStack stack, boolean girder, boolean maximiseTurn) {
        try{
            Vec3d lookVec = player.getRotationVector();
            int lookAngle = (int) (22.5 + AngleHelper.deg(MathHelper.atan2(lookVec.z, lookVec.x)) % 360) / 8;

            if (level.isClient && cached != null && pos2.equals(TrackPlacementAccessor.getHoveringPos()) && stack.equals(TrackPlacementAccessor.getLastItem())
                    && TrackPlacementAccessor.getHoveringMaxed() == maximiseTurn && lookAngle == TrackPlacementAccessor.gethoveringAngle())
                return cached;

            TrackPlacement.PlacementInfo info = new TrackPlacement.PlacementInfo();
            TrackPlacementAccessor.setHoveringMaxed(hoveringMaxed);
            TrackPlacementAccessor.sethoveringAngle(lookAngle);
            TrackPlacementAccessor.setHoveringPos(pos2);
            TrackPlacement.cached = info;

            ITrackBlock track = (ITrackBlock) state2.getBlock();
            Pair<Vec3d, Direction.AxisDirection> nearestTrackAxis = track.getNearestTrackAxis(level, pos2, state2, lookVec);
            Vec3d axis2 = nearestTrackAxis.getFirst()
                    .multiply(nearestTrackAxis.getSecond() == Direction.AxisDirection.POSITIVE ? -1 : 1);
            Vec3d normal2 = track.getUpNormal(level, pos2, state2)
                    .normalize();
            Vec3d normedAxis2 = axis2.normalize();
            Vec3d end2 = track.getCurveStart(level, pos2, state2, axis2);

            NbtCompound itemTag = stack.getNbt();
            NbtCompound selectionTag = itemTag.getCompound("ConnectingFrom");
            BlockPos pos1 = NbtHelper.toBlockPos(selectionTag.getCompound("Pos"));
            Vec3d axis1 = VecHelper.readNBT(selectionTag.getList("Axis", NbtElement.DOUBLE_TYPE));
            Vec3d normedAxis1 = axis1.normalize();
            Vec3d end1 = VecHelper.readNBT(selectionTag.getList("End", NbtElement.DOUBLE_TYPE));
            Vec3d normal1 = VecHelper.readNBT(selectionTag.getList("Normal", NbtElement.DOUBLE_TYPE));
            boolean front1 = selectionTag.getBoolean("Front");
            BlockState state1 = level.getBlockState(pos1);

            if (level.isClient) {
                ((TrackPlacementInfoAccessor)info).setEnd1(end1);
                ((TrackPlacementInfoAccessor)info).setEnd2(end2);
                ((TrackPlacementInfoAccessor)info).setNormal1(normal1);
                ((TrackPlacementInfoAccessor)info).setNormal2(normal2);
                ((TrackPlacementInfoAccessor)info).setAxis1(axis1);
                ((TrackPlacementInfoAccessor)info).setAxis2(axis2);
            }

            if (!state1.contains(TrackBlock.HAS_TE))
                return info.withMessage("original_missing");

            if (axis1.dotProduct(end2.subtract(end1)) < 0) {
                axis1 = axis1.multiply(-1);
                normedAxis1 = normedAxis1.multiply(-1);
                front1 = !front1;
                end1 = track.getCurveStart(level, pos1, state1, axis1);
                if (level.isClient) {
                    ((TrackPlacementInfoAccessor)info).setEnd1(end1);
                    ((TrackPlacementInfoAccessor)info).setAxis1(axis1);
                }
            }

            double[] intersect = VecHelper.intersect(end1, end2, normedAxis1, normedAxis2, Direction.Axis.Y);
            boolean parallel = intersect == null;
            boolean skipCurve = false;

            if ((parallel && normedAxis1.dotProduct(normedAxis2) > 0) || (!parallel && (intersect[0] < 0 || intersect[1] < 0))) {
                axis2 = axis2.multiply(-1);
                normedAxis2 = normedAxis2.multiply(-1);
                end2 = track.getCurveStart(level, pos2, state2, axis2);
                if (level.isClient) {
                    ((TrackPlacementInfoAccessor)info).setEnd2(end2);
                    ((TrackPlacementInfoAccessor)info).setAxis2(axis2);
                }
            }

            Vec3d cross2 = normedAxis2.crossProduct(new Vec3d(0, 1, 0));

            double a1 = MathHelper.atan2(normedAxis2.z, normedAxis2.x);
            double a2 = MathHelper.atan2(normedAxis1.z, normedAxis1.x);
            double angle = a1 - a2;
            double ascend = end2.subtract(end1).y;
            double absAscend = Math.abs(ascend);
            boolean slope = !normal1.equals(normal2);

            if (level.isClient) {
                Vec3d offset1 = axis1.multiply(((TrackPlacementInfoAccessor)info).getEnd1Extend());
                Vec3d offset2 = axis2.multiply(((TrackPlacementInfoAccessor)info).getEnd2Extend());
                BlockPos targetPos1 = pos1.add(offset1.x, offset1.y, offset1.z);
                BlockPos targetPos2 = pos2.add(offset2.x, offset2.y, offset2.z);
                ((TrackPlacementInfoAccessor)info).setCurve(new BezierConnection(Couple.create(targetPos1, targetPos2),
                        Couple.create(end1.add(offset1), end2.add(offset2)), Couple.create(normedAxis1, normedAxis2),
                        Couple.create(normal1, normal2), true, girder));
            }

            // S curve or Straight

            double dist = 0;

            if (parallel) {
                double[] sTest = VecHelper.intersect(end1, end2, normedAxis1, cross2, Direction.Axis.Y);
                if (sTest != null) {
                    double t = Math.abs(sTest[0]);
                    double u = Math.abs(sTest[1]);

                    skipCurve = MathHelper.approximatelyEquals(u, 0);

                    if (skipCurve) {
                        dist = VecHelper.getCenterOf(pos1)
                                .distanceTo(VecHelper.getCenterOf(pos2));
                        ((TrackPlacementInfoAccessor)info).setEnd1Extend((int) Math.round((dist + 1) / axis1.length()));

                    } else {
                        double targetT = u <= 1 ? 3 : u * 2;
                        // This is for standardising s curve sizes
                        if (t > targetT) {
                            int correction = (int) ((t - targetT) / axis1.length());
                            ((TrackPlacementInfoAccessor)info).setEnd1Extend(maximiseTurn ? 0 : correction / 2 + (correction % 2));
                            ((TrackPlacementInfoAccessor)info).setEnd2Extend(maximiseTurn ? 0 : correction / 2);
                        }
                    }
                }
            }

            // Slope

            if (slope) {
                if (!skipCurve)
                    return info.withMessage("slope_turn").tooJumbly();
                if (MathHelper.approximatelyEquals(normal1.dotProduct(normal2), 0))
                    return info.withMessage("opposing_slopes").tooJumbly();
                if ((axis1.y < 0 || axis2.y > 0) && ascend > 0)
                    return info.withMessage("leave_slope_ascending").tooJumbly();
                if ((axis1.y > 0 || axis2.y < 0) && ascend < 0)
                    return info.withMessage("leave_slope_descending").tooJumbly();
                skipCurve = false;
                ((TrackPlacementInfoAccessor)info).setEnd1Extend(0);
                ((TrackPlacementInfoAccessor)info).setEnd2Extend(0);

                Direction.Axis plane = MathHelper.approximatelyEquals(axis1.x, 0) ? Direction.Axis.X : Direction.Axis.Z;
                intersect = VecHelper.intersect(end1, end2, normedAxis1, normedAxis2, plane);
                double dist1 = Math.abs(intersect[0] / axis1.length());
                double dist2 = Math.abs(intersect[1] / axis2.length());

                if (dist1 > dist2)
                    ((TrackPlacementInfoAccessor)info).setEnd1Extend((int) Math.round(dist1 - dist2));
                if (dist2 > dist1)
                    ((TrackPlacementInfoAccessor)info).setEnd2Extend((int) Math.round(dist2 - dist1));

                double turnSize = Math.min(dist1, dist2);

                if (intersect[0] < 0 || intersect[1] < 0)
                    return info.withMessage("too_sharp")
                            .tooJumbly();
                if (turnSize < 2)
                    return info.withMessage("too_sharp");
                // This is for standardising curve sizes
                if (turnSize > 2 && !maximiseTurn) {
                    ((TrackPlacementInfoAccessor)info).setEnd1Extend((int) (((TrackPlacementInfoAccessor)info).getEnd1Extend()+turnSize -2));
                    ((TrackPlacementInfoAccessor)info).setEnd2Extend((int) (((TrackPlacementInfoAccessor)info).getEnd2Extend()+turnSize -2));
                    turnSize = 2;
                }
            }

            // Straight ascend

            if (skipCurve && !MathHelper.approximatelyEquals(ascend, 0)) {
                int hDistance = ((TrackPlacementInfoAccessor)info).getEnd1Extend();
                if (axis1.y == 0 || !MathHelper.approximatelyEquals(absAscend + 1, dist / axis1.length())) {


                    ((TrackPlacementInfoAccessor)info).setEnd1Extend(0);
                    double minHDistance = Math.max(absAscend < 4 ? absAscend * 4 : absAscend * 3, 6) / axis1.length();
                    if (hDistance > minHDistance) {
                        int correction = (int) (hDistance - minHDistance);
                        ((TrackPlacementInfoAccessor)info).setEnd1Extend(maximiseTurn ? 0 : correction / 2 + (correction % 2));
                        ((TrackPlacementInfoAccessor)info).setEnd2Extend(maximiseTurn ? 0 : correction / 2);
                    }

                    skipCurve = false;
                }
            }

            // Turn

            if (!parallel) {
                float absAngle = Math.abs(AngleHelper.deg(angle));

                intersect = VecHelper.intersect(end1, end2, normedAxis1, normedAxis2, Direction.Axis.Y);
                double dist1 = Math.abs(intersect[0]);
                double dist2 = Math.abs(intersect[1]);
                float ex1 = 0;
                float ex2 = 0;

                if (dist1 > dist2)
                    ex1 = (float) ((dist1 - dist2) / axis1.length());
                if (dist2 > dist1)
                    ex2 = (float) ((dist2 - dist1) / axis2.length());

                double turnSize = Math.min(dist1, dist2) - .1d;
                boolean ninety = (absAngle + .25f) % 90 < 1;


                double minTurnSize = ninety ? 7 : 3.25;
                double turnSizeToFitAscend =
                        minTurnSize + (ninety ? Math.max(0, absAscend - 3) * 2f : Math.max(0, absAscend - 1.5f) * 1.5f);

                // This is for standardising curve sizes
                if (!maximiseTurn) {
                    ex1 += (turnSize - turnSizeToFitAscend) / axis1.length();
                    ex2 += (turnSize - turnSizeToFitAscend) / axis2.length();
                }
                ((TrackPlacementInfoAccessor)info).setEnd1Extend(MathHelper.floor(ex1));
                ((TrackPlacementInfoAccessor)info).setEnd2Extend(MathHelper.floor(ex2));
                turnSize = turnSizeToFitAscend;
            }

            Vec3d offset1 = axis1.multiply(((TrackPlacementInfoAccessor)info).getEnd1Extend());
            Vec3d offset2 = axis2.multiply(((TrackPlacementInfoAccessor)info).getEnd2Extend());
            BlockPos targetPos1 = pos1.add(offset1.x, offset1.y, offset1.z);
            BlockPos targetPos2 = pos2.add(offset2.x, offset2.y, offset2.z);

            ((TrackPlacementInfoAccessor)info).setCurve(skipCurve ? null
                    : new BezierConnection(Couple.create(targetPos1, targetPos2),
                    Couple.create(end1.add(offset1), end2.add(offset2)), Couple.create(normedAxis1, normedAxis2),
                    Couple.create(normal1, normal2), true, girder));

            ((TrackPlacementInfoAccessor)info).setValid(true);

            ((TrackPlacementInfoAccessor)info).setPos1(pos1);
            ((TrackPlacementInfoAccessor)info).setPos2(pos2);
            ((TrackPlacementInfoAccessor)info).setAxis1(axis1);
            ((TrackPlacementInfoAccessor)info).setAxis2(axis2);

            placeTracks(level, info, state1, state2, targetPos1, targetPos2, true);

            ItemStack offhandItem = player.getOffHandStack()
                    .copy();
            boolean shouldPave = offhandItem.getItem() instanceof BlockItem;
            if (shouldPave) {
                BlockItem paveItem = (BlockItem) offhandItem.getItem();
                paveTracks(level, info, paveItem, true);
                info.hasRequiredPavement = true;
            }

            info.hasRequiredTracks = true;

            if (!player.isCreative()) {
                for (boolean simulate : Iterate.trueAndFalse) {
                    if (level.isClient && !simulate)
                        break;

                    int tracks = info.requiredTracks;
                    int pavement = info.requiredPavement;
                    int foundTracks = 0;
                    int foundPavement = 0;

                    PlayerInventory inv = player.getInventory();
                    int size = inv.main.size();
                    for (int j = 0; j <= size + 1; j++) {
                        int i = j;
                        boolean offhand = j == size + 1;
                        if (j == size)
                            i = inv.selectedSlot;
                        else if (offhand)
                            i = 0;
                        else if (j == inv.selectedSlot)
                            continue;

                        ItemStack stackInSlot = (offhand ? inv.offHand : inv.main).get(i);
                        boolean isTrack = AllBlocks.TRACK.isIn(stackInSlot);
                        if (!isTrack && (!shouldPave || offhandItem.getItem() != stackInSlot.getItem()))
                            continue;
                        if (isTrack ? foundTracks >= tracks : foundPavement >= pavement)
                            continue;

                        int count = stackInSlot.getCount();

                        if (!simulate) {
                            int remainingItems =
                                    count - Math.min(isTrack ? tracks - foundTracks : pavement - foundPavement, count);
                            if (i == inv.selectedSlot)
                                stackInSlot.setNbt(null);
                            ItemStack newItem = ItemHandlerHelper.copyStackWithSize(stackInSlot, remainingItems);
                            if (offhand)
                                player.setStackInHand(Hand.OFF_HAND, newItem);
                            else
                                inv.setStack(i, newItem);
                        }

                        if (isTrack)
                            foundTracks += count;
                        else
                            foundPavement += count;
                    }

                    if (simulate && foundTracks < tracks) {
                        ((TrackPlacementInfoAccessor)info).setValid(false);
                        info.tooJumbly();
                        info.hasRequiredTracks = false;
                        return info.withMessage("not_enough_tracks");
                    }

                    if (simulate && foundPavement < pavement) {
                        ((TrackPlacementInfoAccessor)info).setValid(false);
                        info.tooJumbly();
                        info.hasRequiredPavement = false;
                        return info.withMessage("not_enough_pavement");
                    }
                }
            }

            if (level.isClient())
                return info;
            if (shouldPave) {
                BlockItem paveItem = (BlockItem) offhandItem.getItem();
                paveTracks(level, info, paveItem, false);
            }

            if (((TrackPlacementInfoAccessor)info).getCurve() != null && ((TrackPlacementInfoAccessor)info).getCurve().getLength() > 29)
                AllAdvancements.LONG_BEND.awardTo(player);

            return placeTracks(level, info, state1, state2, targetPos1, targetPos2, false);
        }
        catch (Exception ex)
        {
            TrackPlacement.PlacementInfo info = new TrackPlacement.PlacementInfo();
            ((TrackPlacementInfoAccessor)(info)).setValid(false);
            return info.withMessage("too_sharp");
        }

    }


}
