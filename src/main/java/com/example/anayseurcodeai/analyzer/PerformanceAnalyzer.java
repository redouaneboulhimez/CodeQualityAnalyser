package com.example.anayseurcodeai.analyzer;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Analyzer for performance issues in code.
 */
public class PerformanceAnalyzer implements CodeAnalyzer {

    // List of inefficient methods
    private static final List<String> INEFFICIENT_METHODS = Arrays.asList(
        "indexOf", "substring", "toArray", "size"
    );

    @Override
    public List<CodeIssue> analyzeFile(CodeParser.ParsedFile parsedFile) {
        List<CodeIssue> issues = new ArrayList<>();
        CompilationUnit cu = parsedFile.getCompilationUnit();
        String fileName = parsedFile.getFile().getName();

        // Check for inefficient string concatenation
        checkStringConcatenation(cu, fileName, issues);

        // Check for inefficient loops
        checkIneffientLoops(cu, fileName, issues);

        // Check for inefficient collections usage
        checkIneffientCollections(cu, fileName, issues);

        // Check for boxing/unboxing issues
        checkBoxingUnboxing(cu, fileName, issues);

        return issues;
    }

    @Override
    public AnalyzerType getType() {
        return AnalyzerType.PERFORMANCE;
    }

    /**
     * Check for inefficient string concatenation.
     */
    private void checkStringConcatenation(CompilationUnit cu, String fileName, List<CodeIssue> issues) {
        cu.findAll(MethodDeclaration.class).forEach(method -> {
            if (method.getBody().isPresent()) {
                String body = method.getBody().get().toString();

                // Check for string concatenation in loops
                if ((body.contains("for") || body.contains("while")) && 
                    body.contains("+=") && body.contains("String")) {
                    issues.add(new CodeIssue(
                        fileName,
                        method.getBegin().get().line,
                        "Inefficient string concatenation in loop. Consider using StringBuilder: " + method.getNameAsString(),
                        AnalyzerType.PERFORMANCE,
                        CodeIssue.Severity.MEDIUM
                    ));
                }
            }
        });
    }

    /**
     * Check for inefficient loops.
     */
    private void checkIneffientLoops(CompilationUnit cu, String fileName, List<CodeIssue> issues) {
        // Check for collection size() call in loop condition
        cu.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(ForStmt n, Void arg) {
                super.visit(n, arg);

                if (n.getCompare().isPresent() && 
                    n.getCompare().get().toString().contains(".size()")) {
                    issues.add(new CodeIssue(
                        fileName,
                        n.getBegin().get().line,
                        "Inefficient loop: Collection size() called in loop condition. Store size in a variable before the loop.",
                        AnalyzerType.PERFORMANCE,
                        CodeIssue.Severity.LOW
                    ));
                }
            }
        }, null);

        // Check for inefficient list iteration
        cu.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(ForEachStmt n, Void arg) {
                super.visit(n, arg);

                // Check if we're removing elements while iterating
                if (n.getBody().toString().contains(".remove(")) {
                    issues.add(new CodeIssue(
                        fileName,
                        n.getBegin().get().line,
                        "Inefficient collection modification: Removing elements during foreach loop can cause ConcurrentModificationException. Use Iterator.remove() instead.",
                        AnalyzerType.PERFORMANCE,
                        CodeIssue.Severity.MEDIUM
                    ));
                }
            }
        }, null);
    }

    /**
     * Check for inefficient collections usage.
     */
    private void checkIneffientCollections(CompilationUnit cu, String fileName, List<CodeIssue> issues) {
        // Check for inefficient collection initialization
        cu.findAll(ClassOrInterfaceDeclaration.class).forEach(clazz -> {
            clazz.getFields().forEach(field -> {
                if (field.getVariable(0).getType().asString().contains("ArrayList") && 
                    !field.getVariable(0).getInitializer().isPresent()) {
                    issues.add(new CodeIssue(
                        fileName,
                        field.getBegin().get().line,
                        "Consider initializing ArrayList with an initial capacity if the size is known.",
                        AnalyzerType.PERFORMANCE,
                        CodeIssue.Severity.LOW
                    ));
                }
            });
        });
    }

    /**
     * Check for boxing/unboxing issues.
     */
    private void checkBoxingUnboxing(CompilationUnit cu, String fileName, List<CodeIssue> issues) {
        // This is a simplified example. A real implementation would be more sophisticated.
        cu.findAll(MethodDeclaration.class).forEach(method -> {
            if (method.getBody().isPresent()) {
                String body = method.getBody().get().toString();

                // Check for potential boxing/unboxing in loops
                if ((body.contains("Integer") || body.contains("Double") || body.contains("Long") || 
                     body.contains("Float") || body.contains("Boolean")) && 
                    (body.contains("for") || body.contains("while"))) {
                    issues.add(new CodeIssue(
                        fileName,
                        method.getBegin().get().line,
                        "Method may have boxing/unboxing overhead in loops: " + method.getNameAsString(),
                        AnalyzerType.PERFORMANCE,
                        CodeIssue.Severity.LOW
                    ));
                }
            }
        });
    }
}
