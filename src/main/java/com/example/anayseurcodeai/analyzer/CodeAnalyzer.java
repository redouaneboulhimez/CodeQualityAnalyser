package com.example.anayseurcodeai.analyzer;

import java.util.List;

/**
 * Interface for all code analyzers.
 */
public interface CodeAnalyzer {
    
    /**
     * Analyze a single parsed file.
     * 
     * @param parsedFile The parsed file to analyze
     * @return A list of issues found in the file
     */
    List<CodeIssue> analyzeFile(CodeParser.ParsedFile parsedFile);
    
    /**
     * Get the analyzer type.
     * 
     * @return The type of this analyzer
     */
    AnalyzerType getType();
    
    /**
     * Enum representing different types of analyzers.
     */
    enum AnalyzerType {
        BUG("Bug", "Identifies potential bugs and logical errors"),
        VULNERABILITY("Vulnerability", "Identifies security vulnerabilities"),
        PERFORMANCE("Performance", "Identifies performance issues"),
        STYLE("Style", "Identifies coding style issues");
        
        private final String name;
        private final String description;
        
        AnalyzerType(String name, String description) {
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
}