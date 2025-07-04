package com.example.anayseurcodeai.analyzer;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Analyzer for potential bugs in code.
 */
public class BugAnalyzer implements CodeAnalyzer {
    
    @Override
    public List<CodeIssue> analyzeFile(CodeParser.ParsedFile parsedFile) {
        List<CodeIssue> issues = new ArrayList<>();
        CompilationUnit cu = parsedFile.getCompilationUnit();
        String fileName = parsedFile.getFile().getName();
        
        // Check for null pointer dereferences
        checkNullPointerDereferences(cu, fileName, issues);
        
        // Check for infinite loops
        checkInfiniteLoops(cu, fileName, issues);
        
        // Check for empty catch blocks
        checkEmptyCatchBlocks(cu, fileName, issues);
        
        // Check for equality comparison issues
        checkEqualityComparisons(cu, fileName, issues);
        
        return issues;
    }
    
    @Override
    public AnalyzerType getType() {
        return AnalyzerType.BUG;
    }
    
    /**
     * Check for potential null pointer dereferences.
     */
    private void checkNullPointerDereferences(CompilationUnit cu, String fileName, List<CodeIssue> issues) {
        cu.findAll(MethodDeclaration.class).forEach(method -> {
            // This is a simplified example. A real implementation would be more sophisticated.
            if (method.getBody().isPresent() && method.getBody().get().toString().contains(".") 
                    && !method.toString().contains("null")) {
                issues.add(new CodeIssue(
                    fileName,
                    method.getBegin().get().line,
                    "Method might contain null pointer dereference: " + method.getNameAsString(),
                    AnalyzerType.BUG,
                    CodeIssue.Severity.HIGH
                ));
            }
        });
    }
    
    /**
     * Check for potential infinite loops.
     */
    private void checkInfiniteLoops(CompilationUnit cu, String fileName, List<CodeIssue> issues) {
        // This is a simplified example. A real implementation would be more sophisticated.
        cu.findAll(MethodDeclaration.class).forEach(method -> {
            if (method.getBody().isPresent() && 
                    (method.getBody().get().toString().contains("while(true)") || 
                     method.getBody().get().toString().contains("while (true)"))) {
                issues.add(new CodeIssue(
                    fileName,
                    method.getBegin().get().line,
                    "Method contains a potential infinite loop: " + method.getNameAsString(),
                    AnalyzerType.BUG,
                    CodeIssue.Severity.MEDIUM
                ));
            }
        });
    }
    
    /**
     * Check for empty catch blocks.
     */
    private void checkEmptyCatchBlocks(CompilationUnit cu, String fileName, List<CodeIssue> issues) {
        // This is a simplified example. A real implementation would be more sophisticated.
        cu.findAll(MethodDeclaration.class).forEach(method -> {
            if (method.getBody().isPresent() && 
                    (method.getBody().get().toString().contains("catch") && 
                     method.getBody().get().toString().contains("{}"))) {
                issues.add(new CodeIssue(
                    fileName,
                    method.getBegin().get().line,
                    "Method contains empty catch block: " + method.getNameAsString(),
                    AnalyzerType.BUG,
                    CodeIssue.Severity.MEDIUM
                ));
            }
        });
    }
    
    /**
     * Check for equality comparison issues (e.g., == vs equals).
     */
    private void checkEqualityComparisons(CompilationUnit cu, String fileName, List<CodeIssue> issues) {
        cu.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(BinaryExpr n, Void arg) {
                super.visit(n, arg);
                
                // Check for == or != on objects that should use equals()
                if ((n.getOperator() == BinaryExpr.Operator.EQUALS || 
                     n.getOperator() == BinaryExpr.Operator.NOT_EQUALS) &&
                    !n.getLeft().toString().equals("null") && 
                    !n.getRight().toString().equals("null") &&
                    !n.getLeft().toString().matches("\\d+") && 
                    !n.getRight().toString().matches("\\d+")) {
                    
                    issues.add(new CodeIssue(
                        fileName,
                        n.getBegin().get().line,
                        "Possible incorrect equality comparison. Consider using .equals() instead of " + n.getOperator(),
                        AnalyzerType.BUG,
                        CodeIssue.Severity.HIGH
                    ));
                }
            }
        }, null);
    }
}