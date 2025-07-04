package com.example.anayseurcodeai.analyzer;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Analyzer for coding style issues.
 */
public class StyleAnalyzer implements CodeAnalyzer {
    
    // Patterns for naming conventions
    private static final Pattern CLASS_PATTERN = Pattern.compile("^[A-Z][a-zA-Z0-9]*$");
    private static final Pattern METHOD_PATTERN = Pattern.compile("^[a-z][a-zA-Z0-9]*$");
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("^[a-z][a-zA-Z0-9]*$");
    private static final Pattern CONSTANT_PATTERN = Pattern.compile("^[A-Z][A-Z0-9_]*$");
    
    @Override
    public List<CodeIssue> analyzeFile(CodeParser.ParsedFile parsedFile) {
        List<CodeIssue> issues = new ArrayList<>();
        CompilationUnit cu = parsedFile.getCompilationUnit();
        String fileName = parsedFile.getFile().getName();
        
        // Check naming conventions
        checkNamingConventions(cu, fileName, issues);
        
        // Check for missing Javadoc
        checkMissingJavadoc(cu, fileName, issues);
        
        // Check for long methods
        checkLongMethods(cu, fileName, issues);
        
        // Check for magic numbers
        checkMagicNumbers(cu, fileName, issues);
        
        return issues;
    }
    
    @Override
    public AnalyzerType getType() {
        return AnalyzerType.STYLE;
    }
    
    /**
     * Check for naming convention violations.
     */
    private void checkNamingConventions(CompilationUnit cu, String fileName, List<CodeIssue> issues) {
        // Check class names
        cu.findAll(ClassOrInterfaceDeclaration.class).forEach(clazz -> {
            String name = clazz.getNameAsString();
            if (!CLASS_PATTERN.matcher(name).matches()) {
                issues.add(new CodeIssue(
                    fileName,
                    clazz.getBegin().get().line,
                    "Class name '" + name + "' does not follow the PascalCase naming convention",
                    AnalyzerType.STYLE,
                    CodeIssue.Severity.LOW
                ));
            }
        });
        
        // Check method names
        cu.findAll(MethodDeclaration.class).forEach(method -> {
            String name = method.getNameAsString();
            if (!METHOD_PATTERN.matcher(name).matches()) {
                issues.add(new CodeIssue(
                    fileName,
                    method.getBegin().get().line,
                    "Method name '" + name + "' does not follow the camelCase naming convention",
                    AnalyzerType.STYLE,
                    CodeIssue.Severity.LOW
                ));
            }
        });
        
        // Check field names
        cu.findAll(FieldDeclaration.class).forEach(field -> {
            boolean isConstant = field.isFinal() && field.isStatic();
            field.getVariables().forEach(var -> {
                String name = var.getNameAsString();
                if (isConstant) {
                    if (!CONSTANT_PATTERN.matcher(name).matches()) {
                        issues.add(new CodeIssue(
                            fileName,
                            field.getBegin().get().line,
                            "Constant '" + name + "' does not follow the UPPER_CASE naming convention",
                            AnalyzerType.STYLE,
                            CodeIssue.Severity.LOW
                        ));
                    }
                } else {
                    if (!VARIABLE_PATTERN.matcher(name).matches()) {
                        issues.add(new CodeIssue(
                            fileName,
                            field.getBegin().get().line,
                            "Field '" + name + "' does not follow the camelCase naming convention",
                            AnalyzerType.STYLE,
                            CodeIssue.Severity.LOW
                        ));
                    }
                }
            });
        });
    }
    
    /**
     * Check for missing Javadoc comments.
     */
    private void checkMissingJavadoc(CompilationUnit cu, String fileName, List<CodeIssue> issues) {
        // Check for missing class Javadoc
        cu.findAll(ClassOrInterfaceDeclaration.class).forEach(clazz -> {
            if (!clazz.getComment().isPresent() || !(clazz.getComment().get() instanceof JavadocComment)) {
                issues.add(new CodeIssue(
                    fileName,
                    clazz.getBegin().get().line,
                    "Class '" + clazz.getNameAsString() + "' is missing Javadoc comment",
                    AnalyzerType.STYLE,
                    CodeIssue.Severity.LOW
                ));
            }
        });
        
        // Check for missing method Javadoc (only for public methods)
        cu.findAll(MethodDeclaration.class).forEach(method -> {
            if (method.isPublic() && 
                (!method.getComment().isPresent() || !(method.getComment().get() instanceof JavadocComment))) {
                issues.add(new CodeIssue(
                    fileName,
                    method.getBegin().get().line,
                    "Public method '" + method.getNameAsString() + "' is missing Javadoc comment",
                    AnalyzerType.STYLE,
                    CodeIssue.Severity.LOW
                ));
            }
        });
    }
    
    /**
     * Check for methods that are too long.
     */
    private void checkLongMethods(CompilationUnit cu, String fileName, List<CodeIssue> issues) {
        cu.findAll(MethodDeclaration.class).forEach(method -> {
            if (method.getBody().isPresent()) {
                String[] lines = method.getBody().get().toString().split("\n");
                if (lines.length > 30) {  // Arbitrary threshold for demonstration
                    issues.add(new CodeIssue(
                        fileName,
                        method.getBegin().get().line,
                        "Method '" + method.getNameAsString() + "' is too long (" + lines.length + " lines). Consider refactoring.",
                        AnalyzerType.STYLE,
                        CodeIssue.Severity.MEDIUM
                    ));
                }
            }
        });
    }
    
    /**
     * Check for magic numbers in code.
     */
    private void checkMagicNumbers(CompilationUnit cu, String fileName, List<CodeIssue> issues) {
        // This is a simplified implementation. A real one would be more sophisticated.
        cu.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(NameExpr n, Void arg) {
                super.visit(n, arg);
                
                String expr = n.toString();
                if (expr.matches("\\d+") && !expr.equals("0") && !expr.equals("1")) {
                    issues.add(new CodeIssue(
                        fileName,
                        n.getBegin().get().line,
                        "Magic number '" + expr + "' found. Consider using a named constant.",
                        AnalyzerType.STYLE,
                        CodeIssue.Severity.LOW
                    ));
                }
            }
        }, null);
    }
}