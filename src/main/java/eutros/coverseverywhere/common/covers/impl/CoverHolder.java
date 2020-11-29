package eutros.coverseverywhere.common.covers.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import eutros.coverseverywhere.api.ICover;
import eutros.coverseverywhere.api.ICoverHolder;
import eutros.coverseverywhere.api.ICoverType;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.*;

import static eutros.coverseverywhere.api.CoversEverywhereAPI.getApi;

public class CoverHolder implements ICoverHolder, INBTSerializable<NBTTagCompound> {

    private final Multimap<EnumFacing, ICover> covers = Multimaps.newSetMultimap(
            new LinkedHashMap<>(),
            () -> new LinkedHashSet<ICover>() {
                @Override
                public boolean add(ICover cover) {
                    if(super.add(cover)) {
                        CoverManager.get(tile.getWorld()).markDirty();
                        return true;
                    }
                    return false;
                }

                @Override
                public boolean remove(Object o) {
                    if(super.remove(o)) {
                        ((ICover) o).onRemoved();
                        CoverManager.get(tile.getWorld()).markDirty();
                        return true;
                    }
                    return false;
                }

                @Override
                public void clear() {
                    if(!isEmpty()) {
                        LinkedList<ICover> covers = new LinkedList<>(this);
                        super.clear();
                        for(ICover cover : covers) cover.onRemoved();
                        CoverManager.get(tile.getWorld()).markDirty();
                    }
                }

                @Override
                public Iterator<ICover> iterator() {
                    Iterator<ICover> delegate = super.iterator();
                    return new Iterator<ICover>() {
                        private ICover current;

                        @Override
                        public boolean hasNext() {
                            return delegate.hasNext();
                        }

                        @Override
                        public void remove() {
                            delegate.remove();
                            if(current != null) {
                                current.onRemoved();
                                CoverManager.get(tile.getWorld()).markDirty();
                            }
                        }

                        @Override
                        public ICover next() {
                            return current = delegate.next();
                        }
                    };
                }
            });

    @Nullable // null means this has been invalidated
    private TileEntity tile;

    @Nullable
    TileEntity getTile() {
        return tile;
    }

    void invalidate() {
        tile = null;
    }

    public CoverHolder(@Nullable TileEntity tile) {
        this.tile = tile;
        if(tile != null) CoverManager.get(null).register(this);
    }

    private static final String TYPE_TAG = "type";
    private static final String DATA_TAG = "data";

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        if(tile == null) return nbt;

        for(EnumFacing side : EnumFacing.values()) {
            Collection<ICover> sideCovers = covers.get(side);
            if(sideCovers.isEmpty()) continue;

            NBTTagList sideNbt = new NBTTagList();
            for(ICover cover : sideCovers) {
                ICoverType type = cover.getType();
                NBTTagCompound coverNbt = new NBTTagCompound();
                coverNbt.setString(TYPE_TAG, Preconditions.checkNotNull(type.getRegistryName(),
                        "Attempted to serialize unregistered cover type: %s", type)
                        .toString());
                coverNbt.setTag(DATA_TAG, type.serialize(cover));
                sideNbt.appendTag(coverNbt);
            }
            nbt.setTag(side.getName(), sideNbt);

        }

        return nbt;
    }

    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        if(tile == null) return;
        covers.clear();
        for(String key : nbt.getKeySet()) {
            EnumFacing side = EnumFacing.byName(key);
            if(side == null) continue;
            NBTTagList tagList = nbt.getTagList(key, Constants.NBT.TAG_COMPOUND);
            for(NBTBase tag : tagList) {
                NBTTagCompound coverNbt = (NBTTagCompound) tag;
                ResourceLocation typeLoc = new ResourceLocation(coverNbt.getString(TYPE_TAG));
                ICoverType type = getApi().getRegistry().getValue(typeLoc);
                if(type == null) {
                    LOGGER.warn("Unknown cover type: {}.", typeLoc);
                    continue;
                }
                ICover cover = type.makeCover(tile, side, coverNbt.getCompoundTag(DATA_TAG));
                if(cover != null) covers.put(side, cover);
            }
        }
    }

    @Override
    public Collection<ICover> get(EnumFacing side) {
        return covers.get(side);
    }

}
