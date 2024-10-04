package umontpellier.erl.calculs.part2;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Parser {
    protected String projectPath;
    protected String jrePath;
    protected ASTParser parser;

    public Parser(String projectPath) {
        setProjectPath(projectPath);
        setJREPath(System.getProperty("java.home"));
        configure();
    }

    public String getProjectPath() {
        return projectPath;
    }

    public void setProjectPath(String projectPath) {
        this.projectPath = projectPath;
    }

    public String getJREPath() {
        return jrePath;
    }

    public void setJREPath(String jrePath) {
        this.jrePath = jrePath;
    }

    public void configure() {
        parser = ASTParser.newParser(AST.JLS4);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setResolveBindings(true);
        parser.setBindingsRecovery(true);
        parser.setCompilerOptions(JavaCore.getOptions());
        parser.setUnitName("");
        parser.setEnvironment(new String[]{getJREPath()}, new String[]{getProjectPath()}, new String[]{"UTF-8"}, true);
    }

    public CompilationUnit parse(File sourceFile) throws IOException {
        parser.setSource(FileUtils.readFileToString(sourceFile, (Charset) null).toCharArray());
        return (CompilationUnit) parser.createAST(null);
    }

    public List<CompilationUnit> parseProject() throws IOException {
        List<CompilationUnit> cUnits = new ArrayList<>();
        for (File sourceFile : listJavaProjectFiles()) {
            cUnits.add(parse(sourceFile));
        }
        return cUnits;
    }

    public List<File> listJavaFiles(String filePath) {
        File folder = new File(filePath);
        List<File> javaFiles = new ArrayList<>();
        String fileName = "";

        for (File file : Objects.requireNonNull(folder.listFiles())) {
            fileName = file.getName();

            if (file.isDirectory())
                javaFiles.addAll(listJavaFiles(file.getAbsolutePath()));
            else if (fileName.endsWith(".java"))
                javaFiles.add(file);
        }

        return javaFiles;
    }

    public List<File> listJavaProjectFiles() {
        return listJavaFiles(getProjectPath());
    }
}