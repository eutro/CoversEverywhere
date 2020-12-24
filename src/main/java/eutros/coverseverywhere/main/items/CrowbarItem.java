package eutros.coverseverywhere.main.items;

import eutros.coverseverywhere.api.GridSection;
import eutros.coverseverywhere.api.ICover;
import eutros.coverseverywhere.api.ICoverHolder;
import eutros.coverseverywhere.api.ICoverRevealer;
import eutros.coverseverywhere.common.Constants;
import eutros.coverseverywhere.common.Initialize;
import eutros.coverseverywhere.common.networking.Packets;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static eutros.coverseverywhere.api.CoversEverywhereAPI.getApi;

public class CrowbarItem extends Item implements ICoverRevealer {

    public static final ResourceLocation NAME = new ResourceLocation(Constants.MOD_ID, "crowbar");

    public CrowbarItem() {
        setRegistryName(NAME);
        setUnlocalizedName(NAME.getResourceDomain() + "." + NAME.getResourcePath());
        setMaxStackSize(1);
        setCreativeTab(ModItems.CREATIVE_TAB);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(TextFormatting.GRAY + I18n.format("item.covers_everywhere.crowbar.tooltip"));
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        TileEntity tile = worldIn.getTileEntity(pos);
        if (tile == null) return EnumActionResult.PASS;

        ICoverHolder holder = tile.getCapability(getApi().getHolderCapability(), null);
        if (holder == null) return EnumActionResult.PASS;

        EnumFacing side = GridSection.fromXYZ(facing, hitX, hitY, hitZ).offset(facing);
        Collection<ICover> covers = holder.get(side);
        if (covers.isEmpty()) return EnumActionResult.PASS;
        if (worldIn.isRemote) {
            NonNullList<ItemStack> choices = covers.stream()
                    .map(ICover::getRepresentation)
                    .collect(Collectors.toCollection(NonNullList::create));
            RadialGuiScreen.prompt(choices, i -> Packets.NETWORK.sendToServer(new CrowbarMessage(i, pos, side)));
        }
        return EnumActionResult.SUCCESS;
    }

    public static class CrowbarMessage extends RadialPacket {
        public CrowbarMessage() {
        }

        protected CrowbarMessage(int index, BlockPos pos, EnumFacing side) {
            super(index, pos, side);
        }

        @Override
        public void handle(MessageContext ctx) {
            WorldServer world = ctx.getServerHandler().player.getServerWorld();
            coversFor(ctx).ifPresent(covers ->
                    Objects.requireNonNull(ctx.getServerHandler().player.getServer())
                            .addScheduledTask(() -> {
                                        covers.remove(index);
                                        getApi().synchronize(world, pos, side);
                                    }
                            ));
        }
    }

    private static final int CROWBAR_DISCRIMINATOR = 100;

    @Initialize
    public static void init() {
        RadialPacket.register(CROWBAR_DISCRIMINATOR, CrowbarMessage.class);
    }

}
