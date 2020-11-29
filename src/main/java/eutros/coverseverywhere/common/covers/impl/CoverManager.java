package eutros.coverseverywhere.common.covers.impl;

import com.google.common.collect.ImmutableSet;
import eutros.coverseverywhere.api.ICoverHolder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.*;

public class CoverManager {

    private static final Map<World, CoverManager> instances = new WeakHashMap<>();

    private final Set<CoverHolder> holders = Collections.newSetFromMap(Collections.synchronizedMap(new WeakHashMap<>()));
    private final ReferenceQueue<CoverHolder> holderQueue = new ReferenceQueue<>();

    private final List<Runnable> dirtyListeners = new LinkedList<>();

    private void destroy(CoverHolder holder) {
        if(holder.getTile() != null) {
            for(EnumFacing side : EnumFacing.values()) {
                holder.get(side).clear();
            }
            holder.invalidate();
        }
    }

    private void checkDirty() {
        boolean isDirty = false;

        Iterator<CoverHolder> it = holders.iterator();
        while(it.hasNext()) {
            CoverHolder holder = it.next();
            TileEntity tile = holder.getTile();
            if(tile == null || tile.isInvalid()) {
                destroy(holder);
                it.remove();
                isDirty = true;
            }
        }

        while(holderQueue.poll() != null) isDirty = true;

        if(isDirty) markDirty();
    }

    public static CoverManager get(@Nullable World world) {
        if(world != null) redistributeNull();
        return getFor(world);
    }

    public static void pollDirty(@Nullable World world) {
        get(world).checkDirty();
    }

    private static CoverManager getFor(@Nullable World world) {
        return instances.computeIfAbsent(world, $ -> new CoverManager());
    }

    private static void redistributeNull() {
        Iterator<CoverHolder> it = getFor(null).holders.iterator();
        while(it.hasNext()) {
            CoverHolder holder = it.next();
            TileEntity tile = holder.getTile();
            if(tile == null || tile.isInvalid()) {
                it.remove();
                continue;
            }
            //noinspection ConstantConditions we wouldn't be here in the first place
            if(tile.getWorld() != null) {
                it.remove();
                getFor(tile.getWorld()).register(holder);
            }
        }
    }

    public void register(CoverHolder holder) {
        holders.add(holder);
        new WeakReference<>(holder, holderQueue).enqueue();
        markDirty();
    }

    public Set<ICoverHolder> getHolders() {
        checkDirty();
        return ImmutableSet.copyOf(holders);
    }

    public void markDirty() {
        for(Runnable listener : dirtyListeners) listener.run();
    }

    public void onDirty(Runnable listener) {
        dirtyListeners.add(listener);
    }

}
