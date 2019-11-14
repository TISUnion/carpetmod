package carpet.utils.portalsearcher;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PortalSearcherSpiral extends PortalSearcherAbstract {

    public PortalSearcherSpiral(World worldIn) {
        super(worldIn);
    }

    private enum SearchDirection {
        UP,
        LEFT,
        DOWN,
        RIGHT
    }


    @Override
    public PortalSearchResult search(BlockPos searchCenter) {
        long startTimeNanoSec = System.nanoTime();

        BlockPos nearestPortalPos = BlockPos.ORIGIN;
        double d0 = -1.0;


        int r = 0;
        int x = 0, z = 0;
        SearchDirection direction = SearchDirection.LEFT;
        while (r <= 128 && (r*r <= d0 || d0 < 0)) {
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

            //Change spiral direction
            switch (direction) {
                case LEFT:
                    if (z <= -r) {
                        direction = SearchDirection.DOWN;
                    } else {
                        z--;
                        break;
                    }
                case DOWN:
                    if (x <= -r) {
                        direction = SearchDirection.RIGHT;
                    } else {
                        x--;
                        break;
                    }
                case RIGHT:
                    if (z >= r) {
                        direction = SearchDirection.UP;
                    } else {
                        z++;
                        break;
                    }
                case UP:
                    if (x >= r) {
                        if (r <= 128) {
                            r++;
                            direction = SearchDirection.LEFT;
                            x++;
                            break;
                        }
                    } else {
                        x++;
                        break;
                    }
            }
        }
        long finishTimeNanoSec = System.nanoTime();
        return new PortalSearchResult(nearestPortalPos, d0, finishTimeNanoSec - startTimeNanoSec);
    }
}
