package com.example.anayseurcodeai.analyzer;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * Responsible for parsing Java source files using JavaParser.
 */
public class CodeParser {
    private final JavaParser javaParser;
    
    public CodeParser() {
        // Configure JavaParser with a symbol solver for better analysis
        CombinedTypeSolver typeSolver = new CombinedTypeSolver();
        typeSolver.add(new ReflectionTypeSolver());
        
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);
        javaParser = new JavaParser();
        javaParser.getParserConfiguration().setSymbolResolver(symbolSolver);
    }
    
    /**
     * Parse a single Java file.
     * 
     * @param file The Java file to parse
     * @return The parsed CompilationUnit or null if parsing failed
     */
    public CompilationUnit parseFile(File file) throws FileNotFoundException {
        ParseResult<CompilationUnit> result = javaParser.parse(file);
        
        if (result.isSuccessful() && result.getResult().isPresent()) {
            return result.getResult().get();
        }
        
        return null;
    }
    
    /**
     * Parse all Java files in a directory (and its subdirectories).
     * 
     * @param directory The directory to scan for Java files
     * @return A list of successfully parsed CompilationUnits
     */
    public List<ParsedFile> parseDirectory(File directory) {
        List<ParsedFile> parsedFiles = new ArrayList<>();
        
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        parsedFiles.addAll(parseDirectory(file));
                    } else if (file.getName().endsWith(".java")) {
                        try {
                            CompilationUnit cu = parseFile(file);
                            if (cu != null) {
                                parsedFiles.add(new ParsedFile(file, cu));
                            }
                        } catch (FileNotFoundException e) {
                            // Log error and continue with next file
                            System.err.println("Error parsing file: " + file.getAbsolutePath());
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        
        return parsedFiles;
    }
    
    /**
     * Class to hold a parsed file and its CompilationUnit.
     */
    public static class ParsedFile {
        private final File file;
        private final CompilationUnit compilationUnit;
        
        public ParsedFile(File file, CompilationUnit compilationUnit) {
            this.file = file;
            this.compilationUnit = compilationUnit;
        }
        
        public File getFile() {
            return file;
        }
        
        public CompilationUnit getCompilationUnit() {
            return compilationUnit;
        }
    }
}