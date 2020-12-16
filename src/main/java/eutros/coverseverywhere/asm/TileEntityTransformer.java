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
import java.util.Map;
import java.util.WeakHashMap;

import static eutros.coverseverywhere.api.CoversEverywhereAPI.getApi;

public class TileEntityTransformer extends ClassVisitor implements Opcodes {

    // called from transformed methods
    @Nullable
    @SuppressWarnings("unused")
    public static <T> T wrapCapability(@Nullable T toWrap, @Nonnull Object self,
                                       @Nonnull Capability<T> capability, @Nullable EnumFacing facing,
                                       @Nonnull Class<?> calling) {
        // there's no way of knowing whether a class is a tile entity when transforming so check that here.
        if(!(self instanceof TileEntity)) return toWrap; // I'm so sorry
        if(capability == getApi().getHolderCapability()) return toWrap;
        if(getImplClass(self.getClass(), getImplCache, "getCapability") != calling) return toWrap;
        return doWrap(toWrap, (TileEntity) self, capability, facing);
    }

    @SuppressWarnings("unused")
    public static boolean wrapHasCapability(boolean toWrap, @Nonnull Object self,
                                            @Nonnull Capability<?> capability, @Nullable EnumFacing facing,
                                            @Nonnull Class<?> calling) {
        if(!(self instanceof TileEntity)) return toWrap;
        if(capability == getApi().getHolderCapability()) return toWrap;
        if(getImplClass(self.getClass(), hasImplCache, "hasCapability") != calling) return toWrap;
        return doWrapHas(toWrap, (TileEntity) self, capability, facing);
    }

    private static Map<Class<?>, Class<?>> getImplCache = new WeakHashMap<>();

    private static Map<Class<?>, Class<?>> hasImplCache = new WeakHashMap<>();

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

    private static Class<?> getImplClass(Class<?> clazz, Map<Class<?>, Class<?>> implCache, String name) {
        Class<?> impl = implCache.get(clazz);
        if(impl != null) return impl;
        try {
            impl = clazz.getMethod(name, Capability.class, EnumFacing.class)
                    .getDeclaringClass();
        } catch(NoSuchMethodException e) {
            impl = Object.class;
        }
        implCache.put(clazz, impl);
        return impl;
    }

    private final String name;

    public TileEntityTransformer(ClassVisitor cv, String name) {
        super(ASM5, cv);
        this.name = name;
    }

    private static final Method WRAP_CAPABILITY_METHOD;
    private static final Method WRAP_HAS_CAPABILITY_METHOD;
    private static final Method GET_CAPABILITY_METHOD;
    private static final Method HAS_CAPABILITY_METHOD;

    static {
        Type OBJECT = Type.getType(Object.class);
        Type CAPABILITY = Type.getObjectType("net/minecraftforge/common/capabilities/Capability");
        Type ENUM_FACING = Type.getObjectType("net/minecraft/util/EnumFacing");
        Type CLASS = Type.getType(Class.class);

        WRAP_CAPABILITY_METHOD = new Method("wrapCapability",
                OBJECT,
                new Type[] {
                        OBJECT,
                        OBJECT,
                        CAPABILITY,
                        ENUM_FACING,
                        CLASS
                });

        WRAP_HAS_CAPABILITY_METHOD = new Method("wrapHasCapability",
                Type.BOOLEAN_TYPE,
                new Type[] {
                        Type.BOOLEAN_TYPE,
                        OBJECT,
                        CAPABILITY,
                        ENUM_FACING,
                        CLASS
                });

        GET_CAPABILITY_METHOD = new Method("getCapability",
                OBJECT,
                new Type[] {
                        CAPABILITY,
                        ENUM_FACING
                });

        HAS_CAPABILITY_METHOD = new Method("hasCapability",
                Type.BOOLEAN_TYPE,
                new Type[] {
                        CAPABILITY,
                        ENUM_FACING
                });
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if ((access & ACC_STATIC) == 0) {
            if(GET_CAPABILITY_METHOD.getName().equals(name) &&
                    GET_CAPABILITY_METHOD.getDescriptor().equals(desc)) {
                return new GetCapabilityTransformer(mv);
            } else if(HAS_CAPABILITY_METHOD.getName().equals(name) &&
                    HAS_CAPABILITY_METHOD.getDescriptor().equals(desc)) {
                return new HasCapabilityTransformer(mv);
            }
        }
        return mv;
    }

    private class GetCapabilityTransformer extends MethodVisitor {

        public GetCapabilityTransformer(MethodVisitor mv) {
            super(TileEntityTransformer.this.api, mv);
        }

        @Override
        public void visitInsn(int opcode) {
            if(opcode == ARETURN) {
                visitVarInsn(ALOAD, 0);
                visitVarInsn(ALOAD, 1);
                visitVarInsn(ALOAD, 2);
                visitLdcInsn(Type.getObjectType(name));
                visitMethodInsn(INVOKESTATIC,
                        Type.getInternalName(TileEntityTransformer.class),
                        WRAP_CAPABILITY_METHOD.getName(),
                        WRAP_CAPABILITY_METHOD.getDescriptor(),
                        false);
            }
            super.visitInsn(opcode);
        }

    }

    private class HasCapabilityTransformer extends MethodVisitor {

        public HasCapabilityTransformer(MethodVisitor mv) {
            super(TileEntityTransformer.this.api, mv);
        }

        @Override
        public void visitInsn(int opcode) {
            if(opcode == IRETURN) {
                visitVarInsn(ALOAD, 0);
                visitVarInsn(ALOAD, 1);
                visitVarInsn(ALOAD, 2);
                visitLdcInsn(Type.getObjectType(name));
                visitMethodInsn(INVOKESTATIC,
                        Type.getInternalName(TileEntityTransformer.class),
                        WRAP_HAS_CAPABILITY_METHOD.getName(),
                        WRAP_HAS_CAPABILITY_METHOD.getDescriptor(),
                        false);
            }
            super.visitInsn(opcode);
        }

    }

}
