package eutros.coverseverywhere.common.covers.impl;

import eutros.coverseverywhere.api.ICover;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.*;

public class CoverManager {

    private static final Map<World, CoverManager> INSTANCES = new WeakHashMap<>();

    public static CoverManager get(@Nullable World world) {
        if(world != null) redistributeNull();
        return getFor(world);
    }

    private static CoverManager getFor(@Nullable World world) {
        return INSTANCES.computeIfAbsent(world, $ -> new CoverManager());
    }

    private static void redistributeNull() {
        if(!INSTANCES.containsKey(null)) return;
        CoverManager nullManager = getFor(null);
        Iterator<CoverHolder> it = nullManager.holders.iterator();
        while(it.hasNext()) {
            CoverHolder holder = it.next();
            World world = holder.getTile().getWorld();
            //noinspection ConstantConditions we wouldn't be here in the first place
            if(world != null) {
                it.remove();
                CoverManager newManager = getFor(world);
                newManager.register(holder);
                holder.setManager(newManager);
            }
        }
        if(nullManager.holders.isEmpty()) INSTANCES.remove(null);
    }

    private Set<CoverHolder> holders = Collections.newSetFromMap(new WeakHashMap<>());

    public Iterable<ICover> getCovers() {
        poll();
        return () -> new LinkedList<>(holders)
                .stream()
                .flatMap(h -> Arrays.stream(EnumFacing.values())
                        .map(h::get)
                        .flatMap(Collection::stream))
                .iterator();
    }

    public void register(CoverHolder holder) {
        holders.add(holder);
        markDirty();
    }

    public void unregister(CoverHolder holder) {
        holders.remove(holder);
        markDirty();
    }

    private List<Runnable> dirtyListeners = new LinkedList<>();

    public void markDirty() {
        for(Runnable dirtyListener : dirtyListeners) dirtyListener.run();
    }

    public void onDirty(Runnable listener) {
        dirtyListeners.add(listener);
    }

    public void poll() {
        boolean dirty = false;
        if(holders.isEmpty()) return;

        // temporarily disable dirtying
        List<Runnable> dirtyListeners = this.dirtyListeners;
        this.dirtyListeners = Collections.emptyList();

        Iterator<CoverHolder> it = holders.iterator();
        while(it.hasNext()) {
            CoverHolder holder = it.next();
            if(holder.checkInvalid()) {
                it.remove(); // remove before, or it will throw CME
                for(EnumFacing side : EnumFacing.values()) {
                    holder.get(side).clear();
                }
                dirty = true;
            }
        }

        this.dirtyListeners = dirtyListeners;
        if(dirty) markDirty();
    }

}
