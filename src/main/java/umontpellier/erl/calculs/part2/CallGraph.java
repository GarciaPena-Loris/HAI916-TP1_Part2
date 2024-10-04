package umontpellier.erl.calculs.part2;

import org.eclipse.jdt.core.dom.*;
import umontpellier.erl.calculs.Visitor.MethodDeclarationVisitor;
import umontpellier.erl.calculs.Visitor.TypeDeclarationVisitor;
import umontpellier.erl.calculs.Visitor.MethodInvocationsVisitor;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.LongStream;

public class CallGraph {
    private final Set<String> methods = new HashSet<>();
    private final Map<String, Map<String, Integer>> invocations = new HashMap<>();
    private final Parser parser;

    // Constructor to initialize the CallGraph with the project path
    public CallGraph(String projectPath) {
        this.parser = new Parser(projectPath);
    }

    // Returns the set of methods in the call graph
    public Set<String> getMethods() {
        return methods;
    }

    // Returns the total number of invocations in the call graph
    public long getNbInvocations() {
        return invocations.keySet()
                .stream()
                .map(invocations::get)
                .map(Map::values)
                .flatMap(Collection::stream)
                .flatMapToLong(value -> LongStream.of((long) value))
                .sum();
    }

    // Returns the map of invocations in the call graph
    public Map<String, Map<String, Integer>> getInvocations() {
        return invocations;
    }

    // Adds a method to the call graph
    public void addMethod(String method) {
        methods.add(method);
    }

    // Adds multiple methods to the call graph
    public void addMethods(Set<String> methods) {
        this.methods.addAll(methods);
    }

    // Adds an invocation between two methods
    public void addInvocation(String source, String destination) {
        if (invocations.containsKey(source)) {
            if (invocations.get(source).containsKey(destination)) {
                invocations.get(source).compute(destination, (k, numberOfArrows) ->
                        Objects.requireNonNullElse(numberOfArrows, 0) + 1);
            } else {
                methods.add(destination);
                invocations.get(source).put(destination, 1);
            }
        } else {
            methods.add(source);
            methods.add(destination);
            invocations.put(source, new HashMap<>());
            invocations.get(source).put(destination, 1);
        }
    }

    // Adds an invocation between two methods with a specified number of occurrences
    public void addInvocation(String source, String destination, int occurrences) {
        methods.add(source);
        methods.add(destination);

        if (!invocations.containsKey(source))
            invocations.put(source, new HashMap<>());

        invocations.get(source).put(destination, occurrences);
    }

    // Adds multiple invocations to the call graph
    public void addInvocations(Map<String, Map<String, Integer>> map) {
        for (String source : map.keySet())
            for (String destination : map.get(source).keySet())
                this.addInvocation(source, destination, map.get(source).get(destination));
    }

    // Returns a string representation of the call graph
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\nDans votre projet Java il y a :");
        builder.append("\n ").append(methods.size()).append(" méthodes.");
        builder.append("\n ").append(getNbInvocations()).append(" invocations.");
        builder.append("\n\n");

        for (String source : invocations.keySet()) {
            builder.append(source).append(":\n");

            for (String destination : invocations.get(source).keySet()) {
                int count = invocations.get(source).get(destination);
                builder.append("\t--> ").append(destination);
                if (count > 1) {
                    builder.append(" (").append(count).append(" appels)");
                }
                builder.append("\n");
            }
            builder.append("\n");
        }

        return builder.toString();
    }

    // Creates a call graph from a project path and a compilation unit
    public static CallGraph createCallGraph(String projectPath, CompilationUnit cUnit) {
        CallGraph graph = new CallGraph(projectPath);
        TypeDeclarationVisitor classCollector = new TypeDeclarationVisitor();
        cUnit.accept(classCollector);

        for (TypeDeclaration cls : classCollector.getTypes()) {
            MethodDeclarationVisitor methodCollector = new MethodDeclarationVisitor();
            cls.accept(methodCollector);

            for (MethodDeclaration method : methodCollector.getMethods())
                graph.addMethodAndInvocations(cls, method);
        }

        return graph;
    }

    // Creates a call graph from a project path
    public static CallGraph createCallGraph(String projectPath) throws IOException {
        CallGraph graph = new CallGraph(projectPath);

        for (CompilationUnit cUnit : graph.parser.parseProject()) {
            CallGraph partial = CallGraph.createCallGraph(projectPath, cUnit);
            graph.addMethods(partial.getMethods());
            graph.addInvocations(partial.getInvocations());
        }

        return graph;
    }

    // Adds a method and its invocations to the call graph
    private void addMethodAndInvocations(TypeDeclaration cls, MethodDeclaration method) {
        if (method.getBody() != null) {
            String methodName = getMethodFullyQualifiedName(cls, method);
            this.addMethod(methodName);

            MethodInvocationsVisitor invocationCollector = new MethodInvocationsVisitor();
            this.addInvocations(cls, method, methodName, invocationCollector);
            this.addSuperInvocations(methodName, invocationCollector);
        }
    }

    // Adds invocations of a method to the call graph
    private void addInvocations(TypeDeclaration cls, MethodDeclaration method,
                                String methodName, MethodInvocationsVisitor invocationCollector) {
        method.accept(invocationCollector);

        for (MethodInvocation invocation : invocationCollector.getMethodInvocations()) {
            String invocationName = getMethodInvocationName(cls, invocation);
            this.addMethod(invocationName);
            this.addInvocation(methodName, invocationName);
        }
    }

    // Returns the fully qualified name of a method invocation
    private String getMethodInvocationName(TypeDeclaration cls, MethodInvocation invocation) {
        Expression expr = invocation.getExpression();
        String invocationName = "";

        if (expr != null) {
            ITypeBinding type = expr.resolveTypeBinding();

            if (type != null)
                invocationName = type.getQualifiedName() + "::" + invocation.getName().toString();
            else
                invocationName = expr + "::" + invocation.getName().toString();
        } else
            invocationName = getClassFullyQualifiedName(cls)
                    + "::" + invocation.getName().toString();

        return invocationName;
    }

    // Adds super method invocations to the call graph
    private void addSuperInvocations(String methodName, MethodInvocationsVisitor invocationCollector) {
        for (SuperMethodInvocation superInvocation : invocationCollector.getSuperMethodInvocations()) {
            String superInvocationName = superInvocation.getName().getFullyQualifiedName();
            this.addMethod(superInvocationName);
            this.addInvocation(methodName, superInvocationName);
        }
    }

    // Returns the fully qualified name of a class
    public static String getClassFullyQualifiedName(TypeDeclaration typeDeclaration) {
        String name = typeDeclaration.getName().getIdentifier();

        if (typeDeclaration.getRoot().getClass() == CompilationUnit.class) {
            CompilationUnit root = (CompilationUnit) typeDeclaration.getRoot();

            if (root.getPackage() != null)
                name = root.getPackage().getName().getFullyQualifiedName() + "." + name;
        }

        return name;
    }

    // Returns the fully qualified name of a method
    public static String getMethodFullyQualifiedName(TypeDeclaration cls, MethodDeclaration method) {
        return getClassFullyQualifiedName(cls) + "::" + method.getName();
    }

    // Generates the DOT representation of the call graph
    public String toDot() {
        StringBuilder builder = new StringBuilder();
        builder.append("digraph CallGraph {\n");

        for (String source : invocations.keySet()) {
            for (String destination : invocations.get(source).keySet()) {
                int count = invocations.get(source).get(destination);

                String formattedDestination = destination.replace("\"", "'");

                if (count > 1) {
                    builder.append(String.format("    \"%s\" -> \"%s\" [label=\"%d\"];\n", source, formattedDestination, count));
                } else {
                    builder.append(String.format("    \"%s\" -> \"%s\";\n", source, formattedDestination));
                }
            }
        }

        builder.append("}\n");
        return builder.toString();
    }

    // Exports the DOT representation to a file
    public void exportToDotFile(String filePath) throws IOException {
        try (PrintWriter out = new PrintWriter(filePath)) {
            out.println(toDot());
        }
    }
}