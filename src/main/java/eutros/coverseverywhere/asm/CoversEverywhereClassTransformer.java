package eutros.coverseverywhere.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import net.minecraft.launchwrapper.IClassTransformer;

public class CoversEverywhereClassTransformer implements IClassTransformer {

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (name.startsWith("eutros.coverseverywhere.asm")) return basicClass;
        ClassReader cr = new ClassReader(basicClass);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
        cr.accept(new TileEntityTransformer(cw, transformedName.replace('.', '/')), 0);
        return cw.toByteArray();
    }

}
