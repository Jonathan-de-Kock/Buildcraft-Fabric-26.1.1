package buildcraft.api.transport.pipe;

import javax.annotation.Nullable;

import net.minecraft.resources.Identifier;

/** Registry for pipe definitions. Implementations are provided by the transport module. */
public interface IPipeRegistry {
    void registerPipe(PipeDefinition definition);

    @Nullable
    PipeDefinition getDefinition(Identifier identifier);
}
