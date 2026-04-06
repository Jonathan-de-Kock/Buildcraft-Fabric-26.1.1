package buildcraft.api.transport.pipe;

import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface IPipe {
    IPipeHolder getHolder();

    PipeDefinition getDefinition();

    PipeBehaviour getBehaviour();

    PipeFlow getFlow();

    DyeColor getColour();

    void setColour(DyeColor colour);

    void markForUpdate();

    BlockEntity getConnectedTile(Direction side);

    IPipe getConnectedPipe(Direction side);

    boolean isConnected(Direction side);

    ConnectedType getConnectedType(Direction side);

    enum ConnectedType {
        TILE,
        PIPE
    }
}
