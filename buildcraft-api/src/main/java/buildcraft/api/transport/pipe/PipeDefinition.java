package buildcraft.api.transport.pipe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;

public final class PipeDefinition {
    public final Identifier identifier;
    public final IPipeCreator logicConstructor;
    public final IPipeLoader logicLoader;
    public final PipeFlowType flowType;
    public final String[] textures;
    public final PipeFaceTex itemModelTop, itemModelCenter, itemModelBottom;
    public final boolean canBeColoured;
    private EnumPipeColourType colourType;

    public PipeDefinition(PipeDefinitionBuilder builder) {
        this.identifier = builder.identifier;
        this.textures = new String[builder.textureSuffixes.length];
        for (int i = 0; i < textures.length; i++) {
            textures[i] = builder.texturePrefix + builder.textureSuffixes[i];
        }
        this.logicConstructor = builder.logicConstructor;
        this.logicLoader = builder.logicLoader;
        this.flowType = builder.flowType;
        this.itemModelBottom = builder.itemModelBottom;
        this.itemModelCenter = builder.itemModelCenter;
        this.itemModelTop = builder.itemModelTop;
        this.canBeColoured = builder.canBeColoured;
        this.colourType = builder.colourType;
    }

    @Nonnull
    public EnumPipeColourType getColourType() {
        if (colourType != null) {
            return colourType;
        }
        if (flowType.fallbackColourType != null) {
            return flowType.fallbackColourType;
        }
        return EnumPipeColourType.TRANSLUCENT;
    }

    public void setColourType(@Nullable EnumPipeColourType colourType) {
        this.colourType = colourType;
    }

    @FunctionalInterface
    public interface IPipeCreator {
        PipeBehaviour createBehaviour(IPipe t);
    }

    @FunctionalInterface
    public interface IPipeLoader {
        PipeBehaviour loadBehaviour(IPipe t, CompoundTag u);
    }

    public static class PipeDefinitionBuilder {
        public Identifier identifier;
        public String texturePrefix;
        public String[] textureSuffixes = { "" };
        public IPipeCreator logicConstructor;
        public IPipeLoader logicLoader;
        public PipeFlowType flowType;
        public PipeFaceTex itemModelTop = PipeFaceTex.get(0);
        public PipeFaceTex itemModelCenter = PipeFaceTex.get(0);
        public PipeFaceTex itemModelBottom = PipeFaceTex.get(0);
        public boolean canBeColoured;
        public EnumPipeColourType colourType;

        public PipeDefinitionBuilder() {}

        public PipeDefinitionBuilder(Identifier identifier, IPipeCreator logicConstructor,
            IPipeLoader logicLoader, PipeFlowType flowType) {
            this.identifier = identifier;
            this.logicConstructor = logicConstructor;
            this.logicLoader = logicLoader;
            this.flowType = flowType;
        }

        public PipeDefinitionBuilder idTexPrefix(String both) {
            return id(both).texPrefix(both);
        }

        public PipeDefinitionBuilder idTex(String both) {
            return id(both).tex(both);
        }

        /** Sets the identifier using the given modId and post string. In Fabric, the active mod context
         * is not automatically available, so the modId must be provided explicitly. */
        public PipeDefinitionBuilder id(String modId, String post) {
            identifier = Identifier.fromNamespaceAndPath(modId, post);
            return this;
        }

        /** Sets the identifier. The caller must provide a fully qualified resource location string ("modid:path"). */
        public PipeDefinitionBuilder id(String post) {
            if (post.contains(":")) {
                identifier = Identifier.parse(post);
            } else {
                throw new IllegalArgumentException(
                    "In Fabric, you must provide a fully qualified identifier (modid:path). Got: " + post);
            }
            return this;
        }

        public PipeDefinitionBuilder tex(String prefix, String... suffixes) {
            return texPrefix(prefix).texSuffixes(suffixes);
        }

        /** Sets the texture prefix. In Fabric the caller should provide the full prefix
         * (e.g., "buildcrafttransport:pipes/wood"). */
        public PipeDefinitionBuilder texPrefix(String prefix) {
            texturePrefix = prefix;
            return this;
        }

        /** Sets the texture prefix to be: <code>[modId]:pipes/[prefix]</code>.
         *
         * @return this */
        public PipeDefinitionBuilder texPrefix(String modId, String prefix) {
            return texPrefixDirect(modId + ":pipes/" + prefix);
        }

        /** Sets the {@link #texturePrefix} to the input string, without any additions or changes.
         *
         * @return this */
        public PipeDefinitionBuilder texPrefixDirect(String prefix) {
            texturePrefix = prefix;
            return this;
        }

        /** Sets {@link #textureSuffixes} to the given array, or to <code>{""}</code> if the argument list is empty or
         * null.
         *
         * @return this. */
        public PipeDefinitionBuilder texSuffixes(String... suffixes) {
            if (suffixes == null || suffixes.length == 0) {
                textureSuffixes = new String[] { "" };
            } else {
                textureSuffixes = suffixes;
            }
            return this;
        }

        public PipeDefinitionBuilder itemTex(int all) {
            itemModelBottom = PipeFaceTex.get(all);
            itemModelCenter = itemModelBottom;
            itemModelTop = itemModelBottom;
            return this;
        }

        public PipeDefinitionBuilder itemTex(int top, int center, int bottom) {
            itemModelBottom = PipeFaceTex.get(bottom);
            itemModelCenter = PipeFaceTex.get(center);
            itemModelTop = PipeFaceTex.get(top);
            return this;
        }

        public PipeDefinitionBuilder logic(IPipeCreator creator, IPipeLoader loader) {
            logicConstructor = creator;
            logicLoader = loader;
            return this;
        }

        public PipeDefinitionBuilder disableColouring() {
            canBeColoured = false;
            return this;
        }

        public PipeDefinitionBuilder enableColouring(EnumPipeColourType type) {
            canBeColoured = true;
            colourType = type;
            return this;
        }

        public PipeDefinitionBuilder enableColouring() {
            return enableColouring(null);
        }

        public PipeDefinitionBuilder enableTranslucentColouring() {
            return enableColouring(EnumPipeColourType.TRANSLUCENT);
        }

        public PipeDefinitionBuilder enableBorderColouring() {
            return enableColouring(EnumPipeColourType.BORDER_OUTER);
        }

        public PipeDefinitionBuilder enableInnerBorderColouring() {
            return enableColouring(EnumPipeColourType.BORDER_INNER);
        }

        public PipeDefinitionBuilder enableCustomColouring() {
            return enableColouring(EnumPipeColourType.CUSTOM);
        }

        public PipeDefinitionBuilder flowItem() {
            return flow(PipeApi.flowItems);
        }

        public PipeDefinitionBuilder flowFluid() {
            return flow(PipeApi.flowFluids);
        }

        public PipeDefinitionBuilder flowPower() {
            return flow(PipeApi.flowPower);
        }

        public PipeDefinitionBuilder flow(PipeFlowType flow) {
            flowType = flow;
            return this;
        }

        public PipeDefinition define() {
            PipeDefinition def = new PipeDefinition(this);
            if (PipeApi.pipeRegistry != null) {
                PipeApi.pipeRegistry.registerPipe(def);
            }
            return def;
        }
    }
}
