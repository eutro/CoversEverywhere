package eutros.coverseverywhere.asm;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import net.minecraft.launchwrapper.IClassTransformer;

public class CoversEverywhereClassTransformer implements IClassTransformer {

    public static final Logger LOGGER = LogManager.getLogger();

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (name.startsWith("eutros.coverseverywhere.asm")) return basicClass;
        ClassReader cr = new ClassReader(basicClass);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
        try {
            cr.accept(new TileEntityTransformer(cw, transformedName.replace('.', '/')), 0);
        } catch(Throwable t) {
            LOGGER.error("Error transforming {}", transformedName, t);
        }
        return cw.toByteArray();
    }

}
