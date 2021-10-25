package carpet.utils;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.fluid.IFluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;

import java.util.ArrayList;
import java.util.List;

public class TntInfo {
    public static List<ITextComponent> simulateTntExplosion(EntityTNTPrimed tnt, BlockPos pos) {
        int amountOfRaysHitting = 0;
        double explosionHeight = tnt.posY +  (double) (tnt.height / 16F);
        ArrayList<Double> probabilityOfRayBreakingBlock = new ArrayList<>();
        float raySizeMultiplier = 1;
        for (int i = 0; i < 16; ++i) {
            for (int j = 0; j < 16; ++j) {
                for (int k = 0; k < 16; ++k) {
                    if (i == 0 || i == 15 || j == 0 || j == 15 || k == 0 || k == 15) {

                        // calculates how much the ray test position will need to be offset by
                        double xoffset = (2.0F * i - 15.0F) / 15.0F;
                        double yoffset = (2.0F * j - 15.0F) / 15.0F;
                        double zoffset = (2.0F * k - 15.0F) / 15.0F;
                        double g = Math.sqrt(xoffset * xoffset + yoffset * yoffset + zoffset * zoffset);
                        xoffset /= g;
                        yoffset /= g;
                        zoffset /= g;

                        float rayStrength = (0.7F + raySizeMultiplier * 0.6F) * 4.0F;

                        // first ray test position is at tnt position
                        double x = tnt.posX;
                        double y = explosionHeight;
                        double z = tnt.posZ;

                        while (rayStrength > 0.0F) {
                            BlockPos blockPos = new BlockPos(x, y, z);

                            // degrade ray strength in case of non air block
                            IBlockState block = tnt.world.getBlockState(blockPos);
                            IFluidState fluid = tnt.world.getFluidState(blockPos);
                            if (!block.isAir() || !fluid.isEmpty()) {
                                rayStrength -= (Math.max(block.getBlock().getExplosionResistance(), fluid.getExplosionResistance()) + 0.3F) * 0.3F; // raystrength -= (blastresistance + 0.3) * 0.3
                            }

                            // if ray goes through block then it is added to the list of blocks to explode
                            if (rayStrength > 0.0F) {
                                if (blockPos.equals(pos)) {
                                    amountOfRaysHitting++;

                                    // unwrap ray strength to get min nextFloat value that would break block
                                    double rayStrengthCopy = rayStrength;
                                    rayStrengthCopy = 5.2D - rayStrengthCopy;
                                    rayStrengthCopy /= 4.0D;
                                    rayStrengthCopy -= 0.7D;
                                    rayStrengthCopy /= 0.6D;
                                    // here raystrength needed to blow up block is below minimal of ray (2.8) and so block will be blown up 100% of the time
                                    if (rayStrengthCopy <= 0) {
                                        probabilityOfRayBreakingBlock.add(1.0D);
                                    } else {
                                        probabilityOfRayBreakingBlock.add(rayStrengthCopy);
                                    }
                                    // prevents code from counting a single ray as multiple succesful ones
                                    rayStrength = 0.0F;
                                }
                            }

                            // reduces ray strength (without this ray going through air only would keep looping)
                            rayStrength -= 0.225F;

                            // moves ray test position to 0.3 blocs further than current along the ray
                            x += xoffset * 0.3D;
                            y += yoffset * 0.3D;
                            z += zoffset * 0.3D;
                        }
                    }
                }
            }
        }
        double probabilityOfBlockBeingBlownUp = 1D;
        if (!probabilityOfRayBreakingBlock.contains(1D)) {
            for (double probabilityOfRay : probabilityOfRayBreakingBlock) {
                probabilityOfRay = 1D - probabilityOfRay;
                probabilityOfBlockBeingBlownUp *= probabilityOfRay;
            }
            probabilityOfBlockBeingBlownUp = 1D - probabilityOfBlockBeingBlownUp;
        }
        System.out.println("1");
        List<ITextComponent> messages = new ArrayList<>();
        messages.add(Messenger.c("w  - Position: " +
                        "x=" + pos.getX() +
                        " y=" + pos.getY() +
                        " z=" + pos.getZ() +
                        " has been struck by",
                "wb  " + amountOfRaysHitting,
                "w  rays"));
        System.out.println("2");
        messages.add(Messenger.c("w  - Probability of block being broken is",
                "wb  " + probabilityOfBlockBeingBlownUp,
                "w  or",
                "wb  " + probabilityOfBlockBeingBlownUp * 100 + "%"));
        System.out.println("3");
        return messages;
    }
}
