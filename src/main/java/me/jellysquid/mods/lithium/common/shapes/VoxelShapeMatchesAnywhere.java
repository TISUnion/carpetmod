package me.jellysquid.mods.lithium.common.shapes;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapePart;

import java.util.Optional;

import static net.minecraft.util.EnumFacing.Axis.*;

public class VoxelShapeMatchesAnywhere
{
    // mapping:
    // yarn -> mcp
    // net.minecraft.util.function.BooleanBiFunction -> net.minecraft.util.math.shapes.IBooleanFunction
    // net.minecraft.util.shape.VoxelShapes#matchesAnywhere -> net.minecraft.util.math.shapes.VoxelShapes.compare
    // net.minecraft.util.shape.VoxelSet -> net.minecraft.util.math.shapes.VoxelShapePart
    // net.minecraft.util.shape.VoxelShape#getPointPositions -> net.minecraft.util.math.shapes.VoxelShape.getValues
    // net.minecraft.util.shape.VoxelSet#inBoundsAndContains(int, int, int) -> net.minecraft.util.math.shapes.VoxelShapePart.contains
    public static Optional<Boolean> cuboidMatchesAnywhere(VoxelShape shapeA, VoxelShape shapeB, IBooleanFunction predicate) {
        //calling this method only if both shapes are not empty and have bounding box overlap

        if (shapeA instanceof VoxelShapeSimpleCube && shapeB instanceof VoxelShapeSimpleCube) {
            if (((VoxelShapeSimpleCube) shapeA).isTiny || ((VoxelShapeSimpleCube) shapeB).isTiny) {
                //vanilla fallback: Handling this special case would mean using the whole
                //pointPosition merging code in SimplePairList. A tiny shape can have very odd effects caused by
                //having three pointPositions within 2e-7 or even 1e-7. Vanilla merges the point positions
                //by always taking the most negative one and skipping those with less than 1e-7 distance to it.
                //The optimization partially relies on only having to check the previous point position, which is
                //not possible when 3 or more are within 2e-7 of another, as the previous point position could have
                //been skipped by the merging code.
                return Optional.empty();
            }
            //both shapes are simple cubes, matching two cubes anywhere is really simple. Also handle epsilon margins.
            if (predicate.apply(true, true)) {
                if (intersects((VoxelShapeSimpleCube) shapeA, (VoxelShapeSimpleCube) shapeB)) {
                    return Optional.of(true);
                }
                return Optional.of(predicate.apply(true, false) || predicate.apply(false, true));
            } else if (predicate.apply(true, false) &&
                    exceedsShape((VoxelShapeSimpleCube) shapeA, (VoxelShapeSimpleCube) shapeB)) {
                return Optional.of(true);
            } else if (predicate.apply(false, true) &&
                    exceedsShape((VoxelShapeSimpleCube) shapeB, (VoxelShapeSimpleCube) shapeA)) {
                return Optional.of(true);
            }
            return Optional.of(false);
        }
        else if (shapeA instanceof VoxelShapeSimpleCube || shapeB instanceof VoxelShapeSimpleCube) {
            //only one of the two shapes is a simple cube, but there are still some shortcuts that can be taken
            VoxelShapeSimpleCube simpleCube = (VoxelShapeSimpleCube) (shapeA instanceof VoxelShapeSimpleCube ? shapeA : shapeB);
            VoxelShape otherShape = simpleCube == shapeA ? shapeB : shapeA;

            if (simpleCube.isTiny || isTiny(otherShape)) {
                //vanilla fallback, same reason as above
                return Optional.empty();
            }

            boolean acceptSimpleCubeAlone = predicate.apply(shapeA == simpleCube, shapeB == simpleCube);
            //test the area outside otherShape
            if (acceptSimpleCubeAlone && exceedsCube(simpleCube,
                    otherShape.getStart(X), otherShape.getStart(Y), otherShape.getStart(Z),
                    otherShape.getEnd(X), otherShape.getEnd(Y), otherShape.getEnd(Z))) {
                return Optional.of(true);
            }
            boolean acceptAnd = predicate.apply(true, true);
            boolean acceptOtherShapeAlone = predicate.apply(shapeA == otherShape, shapeB == otherShape);

            //test the area inside otherShape
            VoxelShapePart voxelSet = otherShape.part;
            DoubleList pointPositionsX = otherShape.invokeGetValues(X);
            DoubleList pointPositionsY = otherShape.invokeGetValues(Y);
            DoubleList pointPositionsZ = otherShape.invokeGetValues(Z);

            int xMax = voxelSet.getEnd(X); // xMax <= pointPositionsX.size()
            int yMax = voxelSet.getEnd(Y);
            int zMax = voxelSet.getEnd(Z);

            //keep the cube positions in local vars to avoid looking them up all the time
            double simpleCubeMaxX = simpleCube.getEnd(X);
            double simpleCubeMinX = simpleCube.getStart(X);
            double simpleCubeMaxY = simpleCube.getEnd(Y);
            double simpleCubeMinY = simpleCube.getStart(Y);
            double simpleCubeMaxZ = simpleCube.getEnd(Z);
            double simpleCubeMinZ = simpleCube.getStart(Z);

            //iterate over all entries of the VoxelSet
            for (int x = voxelSet.getStart(X); x < xMax; x++) {
                //all of the positions of +1e-7 and -1e-7 and >, >=, <, <= are carefully chosen:
                //for example for the following line:                       >= here fails the test
                //                                        moving the - 1e-7 here to the other side of > as + 1e-7 fails the test
                // seems like in 1.13 it should be >= instead of >, same as below and <=, <
                boolean simpleCubeIntersectsXSlice = (simpleCubeMaxX - 1e-7 >= pointPositionsX.getDouble(x) && simpleCubeMinX <= pointPositionsX.getDouble(x + 1) - 1e-7);
                if (!acceptOtherShapeAlone && !simpleCubeIntersectsXSlice) {
                    //if we cannot return when the simple cube is not intersecting the area, skip forward
                    continue;
                }
                boolean xSliceExceedsCube = acceptOtherShapeAlone && !((simpleCubeMaxX >= pointPositionsX.getDouble(x + 1) - 1e-7 && simpleCubeMinX - 1e-7 <= pointPositionsX.getDouble(x)));
                for (int y = voxelSet.getStart(Y); y < yMax; y++) {
                    boolean simpleCubeIntersectsYSlice = (simpleCubeMaxY - 1e-7 >= pointPositionsY.getDouble(y) && simpleCubeMinY <= pointPositionsY.getDouble(y + 1) - 1e-7);
                    if (!acceptOtherShapeAlone && !simpleCubeIntersectsYSlice) {
                        //if we cannot return when the simple cube is not intersecting the area, skip forward
                        continue;
                    }
                    boolean ySliceExceedsCube = acceptOtherShapeAlone && !((simpleCubeMaxY >= pointPositionsY.getDouble(y + 1) - 1e-7 && simpleCubeMinY - 1e-7 <= pointPositionsY.getDouble(y)));
                    for (int z = voxelSet.getStart(Z); z < zMax; z++) {
                        boolean simpleCubeIntersectsZSlice = (simpleCubeMaxZ - 1e-7 >= pointPositionsZ.getDouble(z) && simpleCubeMinZ <= pointPositionsZ.getDouble(z + 1) - 1e-7);
                        if (!acceptOtherShapeAlone && !simpleCubeIntersectsZSlice) {
                            //if we cannot return when the simple cube is not intersecting the area, skip forward
                            continue;
                        }
                        boolean zSliceExceedsCube = acceptOtherShapeAlone && !((simpleCubeMaxZ >= pointPositionsZ.getDouble(z + 1) - 1e-7 && simpleCubeMinZ - 1e-7 <= pointPositionsZ.getDouble(z)));

                        boolean o = voxelSet.contains(x, y, z);
                        boolean s = simpleCubeIntersectsXSlice && simpleCubeIntersectsYSlice && simpleCubeIntersectsZSlice;
                        if (acceptAnd && o && s || acceptSimpleCubeAlone && !o && s || acceptOtherShapeAlone && o && (xSliceExceedsCube || ySliceExceedsCube || zSliceExceedsCube)) {
                            return Optional.of(true);
                        }
                    }
                }
            }
            return Optional.of(false);
        }
        return Optional.empty();
    }

    private static boolean isTiny(VoxelShape shapeA) {
        //avoid properties of SimplePairList, really close point positions are subject to special merging behavior
        return shapeA.getStart(X) > shapeA.getEnd(X) - 3e-7 ||
                shapeA.getStart(Y) > shapeA.getEnd(Y) - 3e-7 ||
                shapeA.getStart(Z) > shapeA.getEnd(Z) - 3e-7;
    }

    private static boolean exceedsCube(VoxelShapeSimpleCube a, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return a.getStart(X) < minX - 1e-7 || a.getEnd(X) > maxX + 1e-7 ||
                a.getStart(Y) < minY - 1e-7 || a.getEnd(Y) > maxY + 1e-7 ||
                a.getStart(Z) < minZ - 1e-7 || a.getEnd(Z) > maxZ + 1e-7;
    }

    private static boolean exceedsShape(VoxelShapeSimpleCube a, VoxelShapeSimpleCube b) {
        return a.getStart(X) < b.getStart(X) - 1e-7 || a.getEnd(X) > b.getEnd(X) + 1e-7 ||
                a.getStart(Y) < b.getStart(Y) - 1e-7 || a.getEnd(Y) > b.getEnd(Y) + 1e-7 ||
                a.getStart(Z) < b.getStart(Z) - 1e-7 || a.getEnd(Z) > b.getEnd(Z) + 1e-7;
    }

    private static boolean intersects(VoxelShapeSimpleCube a, VoxelShapeSimpleCube b) {
        return a.getStart(X) < b.getEnd(X) - 1e-7 && a.getEnd(X) > b.getStart(X) + 1e-7 &&
                a.getStart(Y) < b.getEnd(Y) - 1e-7 && a.getEnd(Y) > b.getStart(Y) + 1e-7 &&
                a.getStart(Z) < b.getEnd(Z) - 1e-7 && a.getEnd(Z) > b.getStart(Z) + 1e-7;
    }
}