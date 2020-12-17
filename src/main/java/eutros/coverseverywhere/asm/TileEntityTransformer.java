package eutros.coverseverywhere.asm;

import eutros.coverseverywhere.api.ICover;
import eutros.coverseverywhere.api.ICoverHolder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static eutros.coverseverywhere.api.CoversEverywhereAPI.getApi;

public class TileEntityTransformer extends ClassVisitor implements Opcodes {

    // called from transformed methods
    @Nullable
    @SuppressWarnings("unused")
    public static <T> T getCapabilityWrapped(@Nullable T toWrap, @Nonnull Object self,
                                             @Nonnull Capability<T> capability, @Nullable EnumFacing facing,
                                             @Nonnull Class<?> calling) {
        // there's no way of knowing whether a class is a tile entity when transforming so check that here.
        if(shouldWrap(self, capability, TransformedMethod.GET, calling)) {
            return doWrap(toWrap, (TileEntity) self, capability, facing);
        }
        return toWrap;
    }

    @SuppressWarnings("unused")
    public static boolean hasCapabilityWrapped(boolean toWrap, @Nonnull Object self,
                                               @Nonnull Capability<?> capability, @Nullable EnumFacing facing,
                                               @Nonnull Class<?> calling) {
        if(shouldWrap(self, capability, TransformedMethod.HAS, calling)) {
            return doWrapHas(toWrap, (TileEntity) self, capability, facing);
        }
        return toWrap;
    }

    private static boolean shouldWrap(Object self, Capability<?> capability, TransformedMethod method, Class<?> calling) {
        return self instanceof TileEntity &&
                capability != getApi().getHolderCapability() &&
                method.getImplementingFor(self.getClass()) == calling;
    }

    @Nullable
    private static <T> T doWrap(@Nullable T toWrap, @Nonnull TileEntity self,
                                @Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if(facing == null) return toWrap;
        ICoverHolder holder = self.getCapability(getApi().getHolderCapability(), facing);
        if(holder == null) return toWrap;
        for(ICover cover : holder.get(facing)) {
            toWrap = cover.wrapCapability(toWrap, capability);
        }
        return toWrap;
    }

    private static boolean doWrapHas(boolean toWrap, TileEntity self, Capability<?> capability, EnumFacing facing) {
        if(facing == null) return toWrap;
        ICoverHolder holder = self.getCapability(getApi().getHolderCapability(), facing);
        if(holder == null) return toWrap;
        for(ICover cover : holder.get(facing)) {
            toWrap = cover.wrapHasCapability(toWrap, capability);
        }
        return toWrap;
    }

    private final String internalName;

    public TileEntityTransformer(ClassVisitor cv, String internalName) {
        super(ASM5, cv);
        this.internalName = internalName;
    }

    private enum TransformedMethod {
        GET(Type.getType(Object.class)),
        HAS(Type.BOOLEAN_TYPE),
        ;

        public final Method TO_TRANSFORM;
        public final Method WRAPPER;
        public final int OPCODE;

        TransformedMethod(Type returnType) {
            Type OBJECT = Type.getType(Object.class);
            Type CAPABILITY = Type.getObjectType("net/minecraftforge/common/capabilities/Capability");
            Type ENUM_FACING = Type.getObjectType("net/minecraft/util/EnumFacing");
            Type CLASS = Type.getType(Class.class);

            TO_TRANSFORM = new Method(name().toLowerCase() + "Capability", returnType,
                    new Type[] {CAPABILITY, ENUM_FACING});
            WRAPPER = new Method(TO_TRANSFORM.getName() + "Wrapped", returnType,
                    new Type[] {returnType, OBJECT, CAPABILITY, ENUM_FACING, CLASS});
            OPCODE = returnType.getOpcode(IRETURN);
        }

        private boolean matches(String name, String desc) {
            return TO_TRANSFORM.getName().equals(name) && TO_TRANSFORM.getDescriptor().equals(desc);
        }

        private Set<String> transformedInternals = new HashSet<>();

        public static MethodVisitor maybeTransform(MethodVisitor mv, int access, String name, String desc, String thisInternalName) {
            if((access & ACC_STATIC) != 0) return mv;
            for(TransformedMethod transformable : values()) {
                if(transformable.matches(name, desc)) {
                    transformable.transformedInternals.add(thisInternalName);
                    return new CapMethodTransformer(mv, transformable, thisInternalName);
                }
            }
            return mv;
        }

        private Map<Class<?>, Class<?>> implCache = new ConcurrentHashMap<>();

        // can't use reflection because that may cause LinkageError-s
        public Class<?> getImplementingFor(Class<?> clazz) {
            if(!transformedInternals.isEmpty()) bakeCache();
            Class<?> impl = implCache.get(clazz);
            if(impl != null) return impl;
            Class<?> next = clazz.getSuperclass();
            while(next != null) {
                impl = implCache.get(next);
                if(impl != null) {
                    implCache.put(clazz, impl);
                    return impl;
                }
                next = next.getSuperclass();
            }
            implCache.put(clazz, null);
            return null;
        }

        private void bakeCache() {
            Iterator<String> it = transformedInternals.iterator();
            while(it.hasNext()) {
                String internalName = it.next();
                it.remove();
                try {
                    String className = internalName.replace('/', '.');
                    Class<?> clazz = Class.forName(className, false, getClass().getClassLoader());
                    if(TileEntity.class.isAssignableFrom(clazz)) implCache.put(clazz, clazz);
                } catch(ClassNotFoundException ignored) {
                }
            }
        }
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        return TransformedMethod.maybeTransform(mv, access, name, desc, internalName);
    }

    private static class CapMethodTransformer extends MethodVisitor {

        public static final String TET_INTERNAL = Type.getInternalName(TileEntityTransformer.class);
        private final Type thisType;
        private final Method wrapper;
        private final int target;

        public CapMethodTransformer(MethodVisitor mv, TransformedMethod transformable, String thisInternalName) {
            super(ASM5, mv);
            target = transformable.OPCODE;
            wrapper = transformable.WRAPPER;
            thisType = Type.getObjectType(thisInternalName);
        }

        @Override
        public void visitInsn(int opcode) {
            if(opcode == target) {
                visitVarInsn(ALOAD, 0);
                visitVarInsn(ALOAD, 1);
                visitVarInsn(ALOAD, 2);
                visitLdcInsn(thisType);
                visitMethodInsn(INVOKESTATIC, TET_INTERNAL, wrapper.getName(), wrapper.getDescriptor(), false);
            }
            super.visitInsn(opcode);
        }

    }

}
