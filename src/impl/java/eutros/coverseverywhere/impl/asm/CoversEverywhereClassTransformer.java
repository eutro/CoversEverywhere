package eutros.coverseverywhere.impl.asm;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import net.minecraft.launchwrapper.IClassTransformer;

import javax.annotation.Nullable;

public class CoversEverywhereClassTransformer implements IClassTransformer {

    public static final Logger LOGGER = LogManager.getLogger();

    @Override
    @Nullable
    public byte[] transform(String name, String transformedName, @Nullable byte[] basicClass) {
        try {
            if (basicClass != null) {
                ClassReader cr = new ClassReader(basicClass);
                ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
                cr.accept(new TileEntityTransformer(cw, transformedName.replace('.', '/')), 0);
                return cw.toByteArray();
            }
        } catch (Throwable t) {
            // Forge doesn't give a stack trace if it catches, so good luck debugging in prod!
            LOGGER.error("Error transforming {}", transformedName, t);
        }
        return basicClass;
    }

}
