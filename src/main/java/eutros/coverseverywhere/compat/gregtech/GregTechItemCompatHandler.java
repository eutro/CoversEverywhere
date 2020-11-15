package eutros.coverseverywhere.compat.gregtech;

import eutros.coverseverywhere.CoversEverywhere;
import eutros.coverseverywhere.api.*;
import eutros.coverseverywhere.common.util.SingletonCapProvider;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.common.items.behaviors.CoverPlaceBehavior;
import gregtech.common.items.behaviors.CrowbarBehaviour;
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
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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

            @Override
            public boolean shouldShowGrid() {
                return false;
            }

            @Override
            public boolean shouldShowCover(ICover cover) {
                return cover.getType() == GregTechCoverType.INSTANCE;
            }

        }

        private static class GregTechCapabilityProvider extends SingletonCapProvider<ICoverRevealer> {

            private final ItemStack stack;

            GregTechCapabilityProvider(ItemStack stack) {
                super(getApi().getRevealerCapability(), GregTechRevealer.INSTANCE);
                this.stack = stack;
            }

            private boolean checked = false;
            private boolean valid = false;

            private boolean isValid() {
                if(!checked) {
                    checked = true;
                    valid = getBehaviour(CoverPlaceBehavior.class, stack).isPresent() ||
                            getBehaviour(CrowbarBehaviour.class, stack).isPresent() ||
                            stack.hasCapability(GregtechCapabilities.CAPABILITY_SCREWDRIVER, null);
                }
                return valid;
            }

            @Override
            public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
                return isValid() && super.hasCapability(capability, facing);
            }

            @Nullable
            @Override
            public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
                return isValid() ? super.getCapability(capability, facing) : null;
            }

        }

    }

    private static class InteractHandler {

        @SubscribeEvent
        public static void onInteract(PlayerInteractEvent.RightClickBlock evt) {
            ItemStack stack = evt.getItemStack();

            if(stack.hasCapability(GregtechCapabilities.CAPABILITY_SCREWDRIVER, null)) {
                configureCover(evt);
            }
        }

        private static void configureCover(PlayerInteractEvent.RightClickBlock evt) {
            BlockPos pos1 = evt.getPos();
            EnumFacing facing = evt.getFace();
            Vec3d hitVec1 = evt.getHitVec();

            if(facing == null) return;

            TileEntity tile1 = evt.getWorld().getTileEntity(pos1);
            if(tile1 == null) return;

            // let GregTech do its own thing
            if(tile1 instanceof MetaTileEntityHolder) return;

            ICoverHolder holder1 = tile1.getCapability(CoversEverywhereAPI.getApi().getHolderCapability(), null);
            if(holder1 == null) return;

            EnumFacing side = GridSection.fromXYZ(facing,
                    (float) (hitVec1.x - pos1.getX()),
                    (float) (hitVec1.y - pos1.getY()),
                    (float) (hitVec1.z - pos1.getZ()))
                    .offset(facing);
            Vec3d hitVec = evt.getHitVec();
            BlockPos pos = evt.getPos();
            for(ICover cover : holder1.get(side)) {
                if(cover.configure(evt.getEntityPlayer(),
                        evt.getHand(),
                        (float) hitVec.x - pos.getX(),
                        (float) hitVec.y - pos.getY(),
                        (float) hitVec.z - pos.getZ())) {
                    evt.setCanceled(true);
                    evt.setCancellationResult(EnumActionResult.SUCCESS);
                }
            }
        }

    }

}
