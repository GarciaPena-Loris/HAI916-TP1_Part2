package umontpellier.erl.calculs.part1;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;
import umontpellier.erl.calculs.Visitor.FieldDeclarationVisitor;
import umontpellier.erl.calculs.Visitor.MethodDeclarationVisitor;
import umontpellier.erl.calculs.Visitor.PackageDeclarationVisitor;
import umontpellier.erl.calculs.Visitor.TypeDeclarationVisitor;

public class Parser {
    protected String projectPath;
    protected String jrePath;
    protected ASTParser parser;
    protected int X;

    public Parser(String projectPath, int X) {
        setProjectPath(projectPath);
        setJREPath(System.getProperty("java.home"));
        this.X = X;
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


    public static ArrayList<File> listJavaFilesForFolder(final File folder) {
        ArrayList<File> javaFiles = new ArrayList<>();
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File fileEntry : files) {
                    if (fileEntry.isDirectory()) {
                        javaFiles.addAll(listJavaFilesForFolder(fileEntry));
                    } else if (fileEntry.getName().endsWith(".java")) {
                        javaFiles.add(fileEntry);
                    }
                }
            } else {
                throw new IllegalArgumentException("Le dossier est vide");
            }
        } else {
            throw new IllegalArgumentException("Aucune fichier src trouvé");
        }
        return javaFiles;
    }

    public void printCount() throws IOException {
        final File folder = new File(projectPath);
        ArrayList<File> javaFiles = listJavaFilesForFolder(folder);

        int classCount = countClasses(javaFiles);
        int methodCount = countMethods(javaFiles);
        int packageCount = countPackages(javaFiles);
        int lineCount = countLines(javaFiles);

        double averageMethodsPerClass = countAverageMethodsPerClass(javaFiles);
        double averageLinesPerMethod = countAverageLinesPerMethod(javaFiles);
        double averageFieldsPerClass = countAverageFieldsPerClass(javaFiles);

        Map<String, List<MethodDeclaration>> classMethods = getClassMethods(javaFiles);
        Map<String, Integer> classMethodCount = getClassMethodCount(javaFiles);
        Map<String, Integer> classFieldCount = getClassFieldCount(javaFiles);

        List<Map.Entry<String, Integer>> top10PercentClassesMethods = getTop10PercentClassesMethods(classMethodCount);
        List<Map.Entry<String, Integer>> top10PercentClassesFields = getTop10PercentClassesFields(classFieldCount);
        List<String> top10PercentClassesBothMethodsAndFields = getTop10PercentClassesBothMethodsAndFields(classMethodCount, classFieldCount);
        List<String> classesWithMoreThanXMethods = getClassesWithMoreThanXMethods(classMethodCount, X);
        Map<String, List<MethodDeclaration>> top10PercentMethodsPerClass = getTop10PercentMethodsCodeLinePerClass(classMethods);
        int maxParameters = getMaxParameters(classMethods);

        System.out.println("\n========= Résumé du projet ==========");
        System.out.println(" Chemin du projet : " + projectPath);
        System.out.println(" Nombre de fichiers Java analysées : " + javaFiles.size());
        System.out.println("----------------------------------------------------");
        System.out.println(" - " + classCount + " classes.");
        System.out.println(" - " + lineCount + " lignes de code non vide.");
        System.out.println(" - " + methodCount + " méthodes.");
        System.out.println(" - " + packageCount + " packages.");
        System.out.println(" - " + String.format("%.2f", averageMethodsPerClass) + " méthodes par classe en moyenne.");
        System.out.println(" - " + String.format("%.2f", averageLinesPerMethod) + " lignes de code par méthode en moyenne.");
        System.out.println(" - " + String.format("%.2f", averageFieldsPerClass) + " attributs par classe en moyenne.");
        System.out.println("=================================");

        System.out.println(" - Top 10% des classes avec le plus grand nombre de méthodes :");
        for (Map.Entry<String, Integer> entry : top10PercentClassesMethods) {
            System.out.println("   * " + entry.getKey() + " : " + entry.getValue() + " méthodes");
        }
        System.out.println(" - Top 10% des classes avec le plus grand nombre d'attributs :");
        for (Map.Entry<String, Integer> entry : top10PercentClassesFields) {
            System.out.println("   * " + entry.getKey() + " : " + entry.getValue() + " attributs");
        }
        System.out.println(" - Classes qui font partie en même temps des deux catégories précédentes :");
        for (String className : top10PercentClassesBothMethodsAndFields) {
            System.out.println("   * " + className);
        }
        System.out.println(" - Classes avec plus de " + X + " méthodes :");
        for (String className : classesWithMoreThanXMethods) {
            System.out.println("   * " + className + " : " + classMethodCount.get(className) + " méthodes");
        }
        System.out.println(" - Nombre maximal de paramètres par rapport à toutes les méthodes de l'application : " + maxParameters);
        System.out.println("----------------------------------------------------\n");

        System.out.println(" - Top 10% des méthodes avec le plus grand nombre de lignes de code (par classe) :");
        for (Map.Entry<String, List<MethodDeclaration>> entry : top10PercentMethodsPerClass.entrySet()) {
            System.out.println("   * " + entry.getKey() + " :");
            for (MethodDeclaration method : entry.getValue()) {
                System.out.println("     - méthode '" + method.getName().getFullyQualifiedName() + "' : " + LineCounter.countLines(method) + " lignes");
            }
        }
        System.out.println("=================================");
    }

    public int countClasses(ArrayList<File> javaFiles) throws IOException {
        int classCount = 0;
        for (File fileEntry : javaFiles) {
            CompilationUnit parse = parse(fileEntry);
            TypeDeclarationVisitor typeVisitor = new TypeDeclarationVisitor();
            parse.accept(typeVisitor);
            classCount += typeVisitor.getTypes().size();
        }
        return classCount;
    }

    public int countMethods(ArrayList<File> javaFiles) throws IOException {
        int methodCount = 0;
        for (File fileEntry : javaFiles) {
            CompilationUnit parse = parse(fileEntry);
            MethodDeclarationVisitor methodVisitor = new MethodDeclarationVisitor();
            parse.accept(methodVisitor);
            methodCount += methodVisitor.getMethods().size();
        }
        return methodCount;
    }

    public int countPackages(ArrayList<File> javaFiles) throws IOException {
        int packageCount = 0;
        for (File fileEntry : javaFiles) {
            CompilationUnit parse = parse(fileEntry);
            PackageDeclarationVisitor packageVisitor = new PackageDeclarationVisitor();
            parse.accept(packageVisitor);
            packageCount += packageVisitor.getPackages().size();
        }
        return packageCount;
    }

    public int countLines(ArrayList<File> javaFiles) throws IOException {
        int lineCount = 0;
        for (File fileEntry : javaFiles) {
            lineCount += LineCounter.countLines(fileEntry);
        }
        return lineCount;
    }

    public int countFields(ArrayList<File> javaFiles) throws IOException {
        int fieldCount = 0;
        for (File fileEntry : javaFiles) {
            CompilationUnit parse = parse(fileEntry);
            FieldDeclarationVisitor fieldVisitor = new FieldDeclarationVisitor();
            parse.accept(fieldVisitor);
            fieldCount += fieldVisitor.getFields().size();
        }
        return fieldCount;
    }

    public double countAverageMethodsPerClass(ArrayList<File> javaFiles) throws IOException {
        int classCount = countClasses(javaFiles);
        int methodCount = countMethods(javaFiles);
        return classCount > 0 ? (double) methodCount / classCount : 0;
    }

    public double countAverageLinesPerMethod(ArrayList<File> javaFiles) throws IOException {
        int methodCount = countMethods(javaFiles);
        int lineCount = countLines(javaFiles);
        return methodCount > 0 ? (double) lineCount / methodCount : 0;
    }

    public double countAverageFieldsPerClass(ArrayList<File> javaFiles) throws IOException {
        int classCount = countClasses(javaFiles);
        int fieldCount = countFields(javaFiles);
        return classCount > 0 ? (double) fieldCount / classCount : 0;
    }

    // get all methods of all classes
    public Map<String, List<MethodDeclaration>> getClassMethods(ArrayList<File> javaFiles) throws IOException {
        Map<String, List<MethodDeclaration>> classMethods = new HashMap<>();
        for (File fileEntry : javaFiles) {
            CompilationUnit parse = parse(fileEntry);
            TypeDeclarationVisitor typeVisitor = new TypeDeclarationVisitor();
            parse.accept(typeVisitor);
            for (TypeDeclaration type : typeVisitor.getTypes()) {
                MethodDeclarationVisitor methodVisitor = new MethodDeclarationVisitor();
                type.accept(methodVisitor);
                classMethods.put(type.getName().getFullyQualifiedName(), methodVisitor.getMethods());
            }
        }
        return classMethods;
    }

    // count methods of all classes
    public Map<String, Integer> getClassMethodCount(ArrayList<File> javaFiles) throws IOException {
        Map<String, List<MethodDeclaration>> classMethods = getClassMethods(javaFiles);
        return classMethods.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().size()));
    }

    // get all fields of all classes
    public Map<String, List<FieldDeclaration>> getClassFields(ArrayList<File> javaFiles) throws IOException {
        Map<String, List<FieldDeclaration>> classFields = new HashMap<>();
        for (File fileEntry : javaFiles) {
            CompilationUnit parse = parse(fileEntry);
            TypeDeclarationVisitor typeVisitor = new TypeDeclarationVisitor();
            parse.accept(typeVisitor);
            for (TypeDeclaration type : typeVisitor.getTypes()) {
                FieldDeclarationVisitor fieldVisitor = new FieldDeclarationVisitor();
                type.accept(fieldVisitor);
                classFields.put(type.getName().getFullyQualifiedName(), fieldVisitor.getFields());
            }
        }
        return classFields;
    }

    // count fields of all classes
    public Map<String, Integer> getClassFieldCount(ArrayList<File> javaFiles) throws IOException {
        Map<String, List<FieldDeclaration>> classFields = getClassFields(javaFiles);
        return classFields.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().size()));
    }

    // get top 10% classes with most methods
    public List<Map.Entry<String, Integer>> getTop10PercentClassesMethods(Map<String, Integer> classMethodCount) {
        int top10PercentCount = (int) Math.ceil(classMethodCount.size() * 0.1);
        return classMethodCount.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(top10PercentCount)
                .collect(Collectors.toList());
    }

    // get top 10% classes with most fields
    public List<Map.Entry<String, Integer>> getTop10PercentClassesFields(Map<String, Integer> classFieldCount) {
        int top10PercentCount = (int) Math.ceil(classFieldCount.size() * 0.1);
        return classFieldCount.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(top10PercentCount)
                .collect(Collectors.toList());
    }

    // get top 10% classes with most methods and fields
    public List<String> getTop10PercentClassesBothMethodsAndFields(Map<String, Integer> classMethodCount, Map<String, Integer> classFieldCount) {
        List<Map.Entry<String, Integer>> top10PercentClassesMethods = getTop10PercentClassesMethods(classMethodCount);
        List<Map.Entry<String, Integer>> top10PercentClassesFields = getTop10PercentClassesFields(classFieldCount);

        List<String> topClassesMethods = top10PercentClassesMethods.stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        List<String> topClassesFields = top10PercentClassesFields.stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // Intersection of the two lists
        topClassesMethods.retainAll(topClassesFields);

        return topClassesMethods;
    }

    // get classes with more than X methods
    public List<String> getClassesWithMoreThanXMethods(Map<String, Integer> classMethodCount, int X) {
        return classMethodCount.entrySet().stream()
                .filter(entry -> entry.getValue() > X)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    // get top 10% methods with most lines of code per class
    public Map<String, List<MethodDeclaration>> getTop10PercentMethodsCodeLinePerClass(Map<String, List<MethodDeclaration>> classMethods) {
        Map<String, List<MethodDeclaration>> top10PercentMethodsPerClass = new HashMap<>();
        for (Map.Entry<String, List<MethodDeclaration>> entry : classMethods.entrySet()) {
            List<MethodDeclaration> methods = entry.getValue();
            int top10PercentCount = (int) Math.ceil(methods.size() * 0.1);
            List<MethodDeclaration> topMethods = methods.stream()
                    .sorted((m1, m2) -> Integer.compare(LineCounter.countLines(m2), LineCounter.countLines(m1)))
                    .limit(top10PercentCount)
                    .collect(Collectors.toList());
            top10PercentMethodsPerClass.put(entry.getKey(), topMethods);
        }
        return top10PercentMethodsPerClass;
    }

    // get max parameters of all methods
    public int getMaxParameters(Map<String, List<MethodDeclaration>> classMethods) {
        return classMethods.values().stream()
                .flatMap(List::stream)
                .mapToInt(m -> m.parameters().size())
                .max()
                .orElse(0);
    }
}