package eutros.coverseverywhere.compat.gregtech;

import eutros.coverseverywhere.CoversEverywhere;
import eutros.coverseverywhere.api.*;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.common.items.behaviors.CoverPlaceBehavior;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static eutros.coverseverywhere.api.CoversEverywhereAPI.getApi;

public class GTCECompat {

    private static void init() {
        MinecraftForge.EVENT_BUS.register(GTCECompat.class);
    }

    public static void check() {
        if(Loader.isModLoaded("gregtech")) init();
    }

    @SubscribeEvent
    public static void registerCovers(RegistryEvent.Register<ICoverType> evt) {
        IForgeRegistry<ICoverType> r = evt.getRegistry();

        r.register(GregTechCoverType.INSTANCE);
    }

    @Nullable
    private static <T> T getBehaviour(Class<T> behaviourClass, ItemStack stack) {
        Item item = stack.getItem();
        if(!(item instanceof MetaItem)) return null;
        MetaItem<?>.MetaValueItem metaItem = ((MetaItem<?>) item).getItem(stack);
        if(metaItem == null) return null;
        for(IItemBehaviour behaviour : metaItem.getBehaviours()) {
            if(behaviourClass.isInstance(behaviour)) {
                return behaviourClass.cast(behaviour);
            }
        }
        return null;
    }

    @SubscribeEvent
    public static void onStack(AttachCapabilitiesEvent<ItemStack> evt) {
        if(getBehaviour(CoverPlaceBehavior.class, evt.getObject()) == null) return;

        ICoverRevealer revealer = new ICoverRevealer() {
        };
        evt.addCapability(new ResourceLocation(CoversEverywhere.MOD_ID, "gregtech_cover_item"),
                new ICapabilityProvider() {
                    @Override
                    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
                        return capability == getApi().getRevealerCapability();
                    }

                    @Nullable
                    @Override
                    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
                        return capability == getApi().getRevealerCapability() ? getApi().getRevealerCapability().cast(revealer) :
                               null;
                    }
                });
    }

    @SubscribeEvent
    public static void onInteract(PlayerInteractEvent.RightClickBlock evt) {
        ItemStack stack = evt.getItemStack();
        CoverPlaceBehavior behaviour = getBehaviour(CoverPlaceBehavior.class, stack);
        if(behaviour == null) return;

        World world = evt.getWorld();
        BlockPos pos = evt.getPos();
        EntityPlayer player = evt.getEntityPlayer();
        EnumHand hand = evt.getHand();
        EnumFacing facing = evt.getFace();

        if(facing == null) return;

        TileEntity tile = world.getTileEntity(pos);
        if(tile == null) return;

        ICoverHolder cap = tile.getCapability(CoversEverywhereAPI.getApi().getHolderCapability(), null);
        if(cap == null) return;

        Vec3d eyes = player.getPositionEyes(0);
        RayTraceResult rtr = world.rayTraceBlocks(eyes, eyes.add(player.getLookVec().scale(10))); // nobody will have more than 10 reach right?
        if(rtr == null) return;

        EnumFacing side = GridSection.fromXYZ(facing, (float) rtr.hitVec.x, (float) rtr.hitVec.y, (float) rtr.hitVec.z).offset(facing);

        GregTechCover cover = new GregTechCover(behaviour.coverDefinition.createCoverBehavior(new TileWrapper(tile), side), tile, side);
        cap.put(side, cover);
        if(!player.isCreative()) player.getHeldItem(hand).shrink(1);

        evt.setCanceled(true);
        evt.setCancellationResult(EnumActionResult.SUCCESS);
    }

}
