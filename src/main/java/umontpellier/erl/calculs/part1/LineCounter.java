package umontpellier.erl.calculs.part1;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class LineCounter {
    public static int countLines(File file) throws IOException {
        List<String> lines = Files.readAllLines(file.toPath());
        int lineCount = 0;
        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                lineCount++;
            }
        }
        return lineCount;
    }

    public static int countLines(MethodDeclaration method) {
        int startLine = ((CompilationUnit) method.getRoot()).getLineNumber(method.getStartPosition());
        int endLine = ((CompilationUnit) method.getRoot()).getLineNumber(method.getStartPosition() + method.getLength());
        return endLine - startLine + 1;
    }
}