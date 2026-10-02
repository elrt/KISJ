package keep.it.simple.java;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * command line interface
 * handles reading the source file triggering compilation and saving the JVM bc
 */
public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage:");
            System.err.println("    java -jar KISJ.jar <source.kiss>");
            System.exit(1);
        }

        String sourceFilePath = args[0];

        try {
            if (!sourceFilePath.endsWith(".kiss")) {
                throw new IllegalArgumentException("Input file must have a '.kiss' extension.");
            }

            File sourceFile = new File(sourceFilePath);
            String className = sourceFile.getName().replace(".kiss", "");
            className = className.substring(0, 1).toUpperCase() + className.substring(1);
            String outputFilePath = className + ".class";

            System.out.println("Compiling " + sourceFilePath + " into " + outputFilePath + "...");

            List<String> sourceLines = Files.readAllLines(Paths.get(sourceFilePath));

            CompilerConfig config = new CompilerConfig();
            KissCompiler compiler = new KissCompiler(config);
            byte[] bytecode = compiler.compile(sourceLines, className);

            try (FileOutputStream fos = new FileOutputStream(outputFilePath)) {
                fos.write(bytecode);
            }

            System.out.println("Successfully compiled to " + outputFilePath);

        } catch (CompilationException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("fatal error!!! : " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
