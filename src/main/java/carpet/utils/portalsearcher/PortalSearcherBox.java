package carpet.utils.portalsearcher;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PortalSearcherBox extends PortalSearcherAbstract {
    public PortalSearcherBox(World worldIn) {
        super(worldIn);
    }

    @Override
    public PortalSearchResult search(BlockPos searchCenter) {
        long startTimeNanoSec = System.nanoTime();

        BlockPos nearestPortalPos = BlockPos.ORIGIN;
        double d0 = -1.0;

        for (int r = 0; r <= 128 && (r*r <= d0 || d0 < 0); r++) {
            int x = -r, z = -r;
            int bottomWidth = (r << 1) + 1;
            int bothSideHeight = (r << 2) - 2;

            for (int count = 0; count < (r << 3) || r == 0; count ++) {

                //Find portal
                BlockPos pos = searchCenter.add(x, this.worldIn.getActualHeight() - 1 - searchCenter.getY(), z);
                do {
                    if (worldIn.getBlockState(pos).getBlock() == BLOCK_NETHER_PORTAL &&
                            worldIn.getBlockState(pos.down()).getBlock() != BLOCK_NETHER_PORTAL) {

                        double d1 = pos.distanceSq(searchCenter);
                        if (d0 < 0.0D || d1 < d0 || (d1 == d0 && vallinaComparator.compare(pos, nearestPortalPos) < 0)) {
                            d0 = d1;
                            nearestPortalPos = pos;
                        }
                    }
                    pos = pos.down();
                } while (pos.getY() >= 0);

                if (r == 0) break;
                if (count < bottomWidth-1) {
                    z++;
                } else if (count < bottomWidth + bothSideHeight) {
                    x +=  1 - (count & 0b1);
                    z = -z;
                } else {
                    z++;
                }
            }
        }

        long finishTimeNanoSec = System.nanoTime();
        return new PortalSearchResult(nearestPortalPos, d0, finishTimeNanoSec - startTimeNanoSec);
    }
}
