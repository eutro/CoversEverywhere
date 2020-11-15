package eutros.coverseverywhere.compat.gregtech;

import eutros.coverseverywhere.CoversEverywhere;
import eutros.coverseverywhere.api.*;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.common.items.behaviors.CoverPlaceBehavior;
import gregtech.common.items.behaviors.CrowbarBehaviour;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.util.TriConsumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Optional;

import static eutros.coverseverywhere.api.CoversEverywhereAPI.getApi;

public class GregTechItemCompatHandler {

    public static void init() {
        MinecraftForge.EVENT_BUS.register(GridHandler.class);
        MinecraftForge.EVENT_BUS.register(InteractHandler.class);
    }

    static <T> Optional<T> getBehaviour(Class<T> behaviourClass, ItemStack stack) {
        Item item = stack.getItem();
        if(!(item instanceof MetaItem)) return Optional.empty();
        MetaItem<?>.MetaValueItem metaItem = ((MetaItem<?>) item).getItem(stack);
        if(metaItem == null) return Optional.empty();
        for(IItemBehaviour behaviour : metaItem.getBehaviours()) {
            if(behaviourClass.isInstance(behaviour)) {
                return Optional.of(behaviourClass.cast(behaviour));
            }
        }
        return Optional.empty();
    }

    private static class GridHandler {

        public static final ResourceLocation CAP_NAME = new ResourceLocation(CoversEverywhere.MOD_ID, "gregtech_cover_item");

        @SubscribeEvent
        public static void onStack(AttachCapabilitiesEvent<ItemStack> evt) {
            evt.addCapability(CAP_NAME, new GregTechCapabilityProvider(evt.getObject()));
        }

        private static class GregTechRevealer implements ICoverRevealer {

            static GregTechRevealer INSTANCE = new GregTechRevealer();

        }

        private static class GregTechCapabilityProvider implements ICapabilityProvider {

            private final ItemStack stack;
            private boolean checked = false;
            private boolean valid = false;

            GregTechCapabilityProvider(ItemStack stack) {
                this.stack = stack;
            }

            private void doChecks() {
                if(checked) return;
                checked = true;
                valid = getBehaviour(CoverPlaceBehavior.class, stack).isPresent() ||
                        getBehaviour(CrowbarBehaviour.class, stack).isPresent() ||
                        stack.hasCapability(GregtechCapabilities.CAPABILITY_SCREWDRIVER, null);
            }

            @Override
            public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
                doChecks();
                return valid && capability == getApi().getRevealerCapability();
            }

            @Nullable
            @Override
            public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
                doChecks();
                return !valid ? null :
                       capability == getApi().getRevealerCapability() ?
                       getApi().getRevealerCapability().cast(GregTechRevealer.INSTANCE) :
                       null;
            }

        }

    }

    private static class InteractHandler {

        @SubscribeEvent
        public static void onInteract(PlayerInteractEvent.RightClickBlock evt) {
            ItemStack stack = evt.getItemStack();

            if(getBehaviour(CoverPlaceBehavior.class, stack).map(b -> {
                placeCover(b, evt);
                return b;
            }).isPresent()) return;

            if(Optional.ofNullable(stack.getCapability(GregtechCapabilities.CAPABILITY_SCREWDRIVER, null)).map(s -> {
                configureCover(evt);
                return s;
            }).isPresent()) return;

            getBehaviour(CrowbarBehaviour.class, stack).map(b -> {
                removeCover(evt);
                return b;
            });

        }

        private static void withSide(PlayerInteractEvent.RightClickBlock evt, TriConsumer<TileEntity, ICoverHolder, EnumFacing> consumer) {
            BlockPos pos = evt.getPos();
            EnumFacing facing = evt.getFace();
            Vec3d hitVec = evt.getHitVec();

            if(facing == null) return;

            TileEntity tile = evt.getWorld().getTileEntity(pos);
            if(tile == null) return;

            // let GregTech do its own thing
            if(tile instanceof MetaTileEntityHolder) return;

            ICoverHolder holder = tile.getCapability(CoversEverywhereAPI.getApi().getHolderCapability(), null);
            if(holder == null) return;

            consumer.accept(tile, holder,
                    GridSection.fromXYZ(facing,
                            (float) (hitVec.x - pos.getX()),
                            (float) (hitVec.y - pos.getY()),
                            (float) (hitVec.z - pos.getZ()))
                            .offset(facing));
        }

        private static void placeCover(CoverPlaceBehavior behaviour, PlayerInteractEvent.RightClickBlock evt) {

            withSide(evt, (tile, holder, side) -> {
                EntityPlayer player = evt.getEntityPlayer();

                GregTechCover cover = new GregTechCover(behaviour.coverDefinition.createCoverBehavior(new TileWrapper(tile), side), tile, side);
                holder.put(side, cover);
                tile.markDirty();
                if(!player.isCreative()) player.getHeldItem(evt.getHand()).shrink(1);

                evt.setCanceled(true);
                evt.setCancellationResult(EnumActionResult.SUCCESS);
            });
        }

        private static void removeCover(PlayerInteractEvent.RightClickBlock evt) {
            withSide(evt, (tile, holder, side) -> {
                Collection<ICover> covers = holder.get(side);
                for(ICover cover : covers) {
                    holder.drop(side, cover);
                }
                covers.clear();
                tile.markDirty();

                evt.setCanceled(true);
                evt.setCancellationResult(EnumActionResult.SUCCESS);
            });
        }

        private static void configureCover(PlayerInteractEvent.RightClickBlock evt) {
            withSide(evt, (tile, holder, side) -> {
                Vec3d hitVec = evt.getHitVec();
                BlockPos pos = evt.getPos();
                for(ICover cover : holder.get(side)) {
                    if(cover.configure(evt.getEntityPlayer(),
                            evt.getHand(),
                            (float) hitVec.x - pos.getX(),
                            (float) hitVec.y - pos.getY(),
                            (float) hitVec.z - pos.getZ())) {
                        evt.setCanceled(true);
                        evt.setCancellationResult(EnumActionResult.SUCCESS);
                    }
                }
            });
        }

    }

}
