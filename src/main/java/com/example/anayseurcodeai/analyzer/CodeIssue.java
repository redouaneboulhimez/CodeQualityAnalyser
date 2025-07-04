package com.example.anayseurcodeai.analyzer;

/**
 * Represents an issue found during code analysis.
 */
public class CodeIssue {
    private final String fileName;
    private final int lineNumber;
    private final String message;
    private final CodeAnalyzer.AnalyzerType type;
    private final Severity severity;
    
    /**
     * Enum representing the severity of an issue.
     */
    public enum Severity {
        CRITICAL("Critical", "Must be fixed immediately"),
        HIGH("High", "Should be fixed soon"),
        MEDIUM("Medium", "Should be fixed"),
        LOW("Low", "Consider fixing"),
        INFO("Info", "Informational only");
        
        private final String name;
        private final String description;
        
        Severity(String name, String description) {
            this.name = name;
            this.description = description;
        }
        
        public String getName() {
            return name;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * Create a new code issue.
     * 
     * @param fileName The name of the file where the issue was found
     * @param lineNumber The line number where the issue was found
     * @param message A description of the issue
     * @param type The type of analyzer that found the issue
     * @param severity The severity of the issue
     */
    public CodeIssue(String fileName, int lineNumber, String message, 
                    CodeAnalyzer.AnalyzerType type, Severity severity) {
        this.fileName = fileName;
        this.lineNumber = lineNumber;
        this.message = message;
        this.type = type;
        this.severity = severity;
    }
    
    /**
     * Get the name of the file where the issue was found.
     * 
     * @return The file name
     */
    public String getFileName() {
        return fileName;
    }
    
    /**
     * Get the line number where the issue was found.
     * 
     * @return The line number
     */
    public int getLineNumber() {
        return lineNumber;
    }
    
    /**
     * Get a description of the issue.
     * 
     * @return The issue message
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * Get the type of analyzer that found the issue.
     * 
     * @return The analyzer type
     */
    public CodeAnalyzer.AnalyzerType getType() {
        return type;
    }
    
    /**
     * Get the severity of the issue.
     * 
     * @return The issue severity
     */
    public Severity getSeverity() {
        return severity;
    }
    
    @Override
    public String toString() {
        return String.format("[%s][%s] %s (line %d): %s", 
                            type.getName(), 
                            severity.getName(), 
                            fileName, 
                            lineNumber, 
                            message);
    }
}