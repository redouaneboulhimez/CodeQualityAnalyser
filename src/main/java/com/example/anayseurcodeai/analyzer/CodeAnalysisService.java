package com.example.anayseurcodeai.analyzer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for analyzing code quality and generating reports.
 */
public class CodeAnalysisService {
    private final CodeParser codeParser;
    private final List<CodeAnalyzer> analyzers;
    
    /**
     * Create a new code analysis service with all available analyzers.
     */
    public CodeAnalysisService() {
        this.codeParser = new CodeParser();
        this.analyzers = new ArrayList<>();
        
        // Add all analyzers
        analyzers.add(new BugAnalyzer());
        analyzers.add(new VulnerabilityAnalyzer());
        analyzers.add(new PerformanceAnalyzer());
        analyzers.add(new StyleAnalyzer());
    }
    
    /**
     * Analyze a single Java file.
     * 
     * @param file The Java file to analyze
     * @return Analysis results containing all issues found
     * @throws FileNotFoundException If the file cannot be found
     */
    public AnalysisResult analyzeFile(File file) throws FileNotFoundException {
        if (!file.getName().endsWith(".java")) {
            return new AnalysisResult(new ArrayList<>());
        }
        
        CodeParser.ParsedFile parsedFile = new CodeParser.ParsedFile(file, codeParser.parseFile(file));
        if (parsedFile.getCompilationUnit() == null) {
            return new AnalysisResult(new ArrayList<>());
        }
        
        List<CodeIssue> allIssues = new ArrayList<>();
        for (CodeAnalyzer analyzer : analyzers) {
            List<CodeIssue> issues = analyzer.analyzeFile(parsedFile);
            allIssues.addAll(issues);
        }
        
        return new AnalysisResult(allIssues);
    }
    
    /**
     * Analyze all Java files in a directory (and its subdirectories).
     * 
     * @param directory The directory to analyze
     * @return Analysis results containing all issues found
     */
    public AnalysisResult analyzeDirectory(File directory) {
        List<CodeParser.ParsedFile> parsedFiles = codeParser.parseDirectory(directory);
        List<CodeIssue> allIssues = new ArrayList<>();
        
        for (CodeParser.ParsedFile parsedFile : parsedFiles) {
            for (CodeAnalyzer analyzer : analyzers) {
                List<CodeIssue> issues = analyzer.analyzeFile(parsedFile);
                allIssues.addAll(issues);
            }
        }
        
        return new AnalysisResult(allIssues);
    }
    
    /**
     * Class representing the results of a code analysis.
     */
    public static class AnalysisResult {
        private final List<CodeIssue> issues;
        
        public AnalysisResult(List<CodeIssue> issues) {
            this.issues = issues;
        }
        
        /**
         * Get all issues found during analysis.
         * 
         * @return List of all issues
         */
        public List<CodeIssue> getIssues() {
            return issues;
        }
        
        /**
         * Get issues grouped by file.
         * 
         * @return Map of file names to lists of issues
         */
        public Map<String, List<CodeIssue>> getIssuesByFile() {
            return issues.stream()
                .collect(Collectors.groupingBy(CodeIssue::getFileName));
        }
        
        /**
         * Get issues grouped by analyzer type.
         * 
         * @return Map of analyzer types to lists of issues
         */
        public Map<CodeAnalyzer.AnalyzerType, List<CodeIssue>> getIssuesByType() {
            return issues.stream()
                .collect(Collectors.groupingBy(CodeIssue::getType));
        }
        
        /**
         * Get issues grouped by severity.
         * 
         * @return Map of severities to lists of issues
         */
        public Map<CodeIssue.Severity, List<CodeIssue>> getIssuesBySeverity() {
            return issues.stream()
                .collect(Collectors.groupingBy(CodeIssue::getSeverity));
        }
        
        /**
         * Get a summary of the analysis results.
         * 
         * @return A string containing a summary of the analysis results
         */
        public String getSummary() {
            StringBuilder summary = new StringBuilder();
            summary.append("Code Analysis Summary\n");
            summary.append("====================\n\n");
            
            summary.append("Total issues found: ").append(issues.size()).append("\n\n");
            
            // Summary by type
            summary.append("Issues by type:\n");
            Map<CodeAnalyzer.AnalyzerType, Long> countByType = issues.stream()
                .collect(Collectors.groupingBy(CodeIssue::getType, Collectors.counting()));
            for (CodeAnalyzer.AnalyzerType type : CodeAnalyzer.AnalyzerType.values()) {
                long count = countByType.getOrDefault(type, 0L);
                summary.append("- ").append(type.getName()).append(": ").append(count).append("\n");
            }
            summary.append("\n");
            
            // Summary by severity
            summary.append("Issues by severity:\n");
            Map<CodeIssue.Severity, Long> countBySeverity = issues.stream()
                .collect(Collectors.groupingBy(CodeIssue::getSeverity, Collectors.counting()));
            for (CodeIssue.Severity severity : CodeIssue.Severity.values()) {
                long count = countBySeverity.getOrDefault(severity, 0L);
                summary.append("- ").append(severity.getName()).append(": ").append(count).append("\n");
            }
            summary.append("\n");
            
            // Files with most issues
            summary.append("Top files with issues:\n");
            Map<String, Long> countByFile = issues.stream()
                .collect(Collectors.groupingBy(CodeIssue::getFileName, Collectors.counting()));
            countByFile.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .forEach(entry -> summary.append("- ").append(entry.getKey())
                    .append(": ").append(entry.getValue()).append(" issues\n"));
            
            return summary.toString();
        }
        
        /**
         * Get recommendations for fixing the issues.
         * 
         * @return A string containing recommendations
         */
        public String getRecommendations() {
            StringBuilder recommendations = new StringBuilder();
            recommendations.append("Recommendations\n");
            recommendations.append("===============\n\n");
            
            // Critical and high severity issues
            List<CodeIssue> criticalIssues = issues.stream()
                .filter(i -> i.getSeverity() == CodeIssue.Severity.CRITICAL)
                .collect(Collectors.toList());
            
            List<CodeIssue> highIssues = issues.stream()
                .filter(i -> i.getSeverity() == CodeIssue.Severity.HIGH)
                .collect(Collectors.toList());
            
            if (!criticalIssues.isEmpty()) {
                recommendations.append("Critical Issues (Fix Immediately):\n");
                criticalIssues.forEach(issue -> 
                    recommendations.append("- ").append(issue.getFileName())
                        .append(" (line ").append(issue.getLineNumber()).append("): ")
                        .append(issue.getMessage()).append("\n")
                );
                recommendations.append("\n");
            }
            
            if (!highIssues.isEmpty()) {
                recommendations.append("High Priority Issues (Fix Soon):\n");
                highIssues.forEach(issue -> 
                    recommendations.append("- ").append(issue.getFileName())
                        .append(" (line ").append(issue.getLineNumber()).append("): ")
                        .append(issue.getMessage()).append("\n")
                );
                recommendations.append("\n");
            }
            
            // General recommendations by type
            recommendations.append("General Recommendations:\n");
            
            Map<CodeAnalyzer.AnalyzerType, List<CodeIssue>> issuesByType = getIssuesByType();
            
            if (issuesByType.containsKey(CodeAnalyzer.AnalyzerType.BUG)) {
                recommendations.append("- Bug Prevention: Review error handling and null checks in your code.\n");
            }
            
            if (issuesByType.containsKey(CodeAnalyzer.AnalyzerType.VULNERABILITY)) {
                recommendations.append("- Security: Use parameterized queries for database operations and validate all user inputs.\n");
            }
            
            if (issuesByType.containsKey(CodeAnalyzer.AnalyzerType.PERFORMANCE)) {
                recommendations.append("- Performance: Optimize loops and string operations, and be mindful of collection operations.\n");
            }
            
            if (issuesByType.containsKey(CodeAnalyzer.AnalyzerType.STYLE)) {
                recommendations.append("- Code Style: Follow consistent naming conventions and add proper documentation.\n");
            }
            
            return recommendations.toString();
        }
    }
}