package carpet.helpers;

import carpet.utils.TextUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class UpdateSuppressionException extends RuntimeException {
    private final World world;
    private final BlockPos pos;

    public UpdateSuppressionException(World world, BlockPos pos) {
        this.world = world;
        this.pos = pos;
    }

    @Override
    public String getMessage()
    {
        return String.format("Update Suppression at %s in %s", TextUtil.coord(this.pos), this.world.getDimension().getType());
    }

    @Override
    public String toString()
    {
        return this.getMessage();
    }
}