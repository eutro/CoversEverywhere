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

import java.util.*;

import static eutros.coverseverywhere.api.CoversEverywhereAPI.getApi;

public class CoverHolder implements ICoverHolder, INBTSerializable<NBTTagCompound> {

    private TileEntity tile;
    private final Multimap<EnumFacing, ICover> covers = Multimaps.newSetMultimap(new EnumMap<>(EnumFacing.class), CoverSet::new);

    public CoverHolder(TileEntity tile) {
        this.tile = tile;
    }

    private static final String TYPE_TAG = "type";
    private static final String DATA_TAG = "data";

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();

        for(EnumFacing side : EnumFacing.values()) {
            Collection<ICover> sideCovers = get(side);
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

    private CoverManager manager = null;

    void setManager(CoverManager manager) {
        this.manager = manager;
    }

    public TileEntity getTile() {
        return tile;
    }

    boolean checkInvalid() {
        if (covers.isEmpty()) {
            manager = null;
            return true;
        }
        return tile.isInvalid();
    }

    void onAddition() {
        if(manager == null) {
            manager = CoverManager.get(tile.getWorld());
            manager.register(this);
        } else {
            manager.markDirty();
        }
    }

    void onRemoval() {
        manager.markDirty();
    }

    private class CoverSet extends LinkedHashSet<ICover> {

        @Override
        public boolean add(ICover cover) {
            if(super.add(cover)) {
                onAddition();
                return true;
            }
            return false;
        }

        @Override
        public boolean remove(Object o) {
            if(super.remove(o)) {
                ((ICover) o).onRemoved();
                onRemoval();
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
                onRemoval();
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
                        onRemoval();
                    }
                }

                @Override
                public ICover next() {
                    return current = delegate.next();
                }
            };
        }

    }

}
