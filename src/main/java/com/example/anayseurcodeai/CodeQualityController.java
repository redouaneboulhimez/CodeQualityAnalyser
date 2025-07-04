package com.example.anayseurcodeai;

import com.example.anayseurcodeai.analyzer.CodeAnalysisService;
import com.example.anayseurcodeai.analyzer.CodeIssue;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

public class CodeQualityController {
    @FXML
    private Button selectFileButton;

    @FXML
    private Button selectDirectoryButton;

    @FXML
    private Button analyzeButton;

    @FXML
    private Label statusLabel;

    @FXML
    private TextArea resultsTextArea;

    @FXML
    private TreeView<String> issuesTreeView;

    @FXML
    private TabPane tabPane;

    private File selectedFile;
    private File selectedDirectory;
    private final CodeAnalysisService analysisService;

    public CodeQualityController() {
        this.analysisService = new CodeAnalysisService();
    }

    @FXML
    private void initialize() {
        statusLabel.setText("Ready to analyze code. Please select a file or directory.");
        analyzeButton.setDisable(true);

        // Configure the TreeView
        issuesTreeView.setCellFactory(TextFieldTreeCell.forTreeView());
        issuesTreeView.setRoot(new TreeItem<>("No issues found"));
    }

    @FXML
    protected void onSelectFileButtonClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Java File");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Java Files", "*.java")
        );

        selectedFile = fileChooser.showOpenDialog(selectFileButton.getScene().getWindow());
        if (selectedFile != null) {
            selectedDirectory = null;
            statusLabel.setText("Selected file: " + selectedFile.getAbsolutePath());
            analyzeButton.setDisable(false);
        }
    }

    @FXML
    protected void onSelectDirectoryButtonClick() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Project Directory");

        selectedDirectory = directoryChooser.showDialog(selectDirectoryButton.getScene().getWindow());
        if (selectedDirectory != null) {
            selectedFile = null;
            statusLabel.setText("Selected directory: " + selectedDirectory.getAbsolutePath());
            analyzeButton.setDisable(false);
        }
    }

    @FXML
    protected void onAnalyzeButtonClick() {
        statusLabel.setText("Analyzing code...");
        resultsTextArea.clear();

        // Disable UI during analysis
        selectFileButton.setDisable(true);
        selectDirectoryButton.setDisable(true);
        analyzeButton.setDisable(true);

        // Run analysis in a background thread to keep UI responsive
        new Thread(() -> {
            try {
                CodeAnalysisService.AnalysisResult result;

                if (selectedFile != null) {
                    result = analyzeFile(selectedFile);
                } else if (selectedDirectory != null) {
                    result = analyzeDirectory(selectedDirectory);
                } else {
                    return;
                }

                // Update UI on the JavaFX application thread
                Platform.runLater(() -> {
                    updateResultsDisplay(result);
                    selectFileButton.setDisable(false);
                    selectDirectoryButton.setDisable(false);
                    analyzeButton.setDisable(false);
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Error during analysis: " + e.getMessage());
                    selectFileButton.setDisable(false);
                    selectDirectoryButton.setDisable(false);
                    analyzeButton.setDisable(false);
                });
            }
        }).start();
    }

    private CodeAnalysisService.AnalysisResult analyzeFile(File file) throws FileNotFoundException {
        return analysisService.analyzeFile(file);
    }

    private CodeAnalysisService.AnalysisResult analyzeDirectory(File directory) {
        return analysisService.analyzeDirectory(directory);
    }

    private void updateResultsDisplay(CodeAnalysisService.AnalysisResult result) {
        // Update the results text area with the summary
        resultsTextArea.setText(result.getSummary());

        // Update the recommendations tab
        if (tabPane.getTabs().size() > 1) {
            TextArea recommendationsTextArea = (TextArea) tabPane.getTabs().get(1).getContent();
            recommendationsTextArea.setText(result.getRecommendations());
        }

        // Update the issues tree view
        updateIssuesTreeView(result);

        // Update status
        List<CodeIssue> issues = result.getIssues();
        if (issues.isEmpty()) {
            statusLabel.setText("Analysis completed. No issues found.");
        } else {
            statusLabel.setText("Analysis completed. Found " + issues.size() + " issues.");
        }
    }

    private void updateIssuesTreeView(CodeAnalysisService.AnalysisResult result) {
        TreeItem<String> root = new TreeItem<>("Issues");
        root.setExpanded(true);

        // Group by file
        Map<String, List<CodeIssue>> issuesByFile = result.getIssuesByFile();

        if (issuesByFile.isEmpty()) {
            root.getChildren().add(new TreeItem<>("No issues found"));
        } else {
            for (Map.Entry<String, List<CodeIssue>> entry : issuesByFile.entrySet()) {
                String fileName = entry.getKey();
                List<CodeIssue> fileIssues = entry.getValue();

                TreeItem<String> fileNode = new TreeItem<>(fileName + " (" + fileIssues.size() + " issues)");

                // Group by severity within each file
                Map<CodeIssue.Severity, List<CodeIssue>> issuesBySeverity = 
                    fileIssues.stream().collect(java.util.stream.Collectors.groupingBy(CodeIssue::getSeverity));

                for (CodeIssue.Severity severity : CodeIssue.Severity.values()) {
                    List<CodeIssue> severityIssues = issuesBySeverity.get(severity);

                    if (severityIssues != null && !severityIssues.isEmpty()) {
                        TreeItem<String> severityNode = new TreeItem<>(
                            severity.getName() + " (" + severityIssues.size() + " issues)");

                        for (CodeIssue issue : severityIssues) {
                            TreeItem<String> issueNode = new TreeItem<>(
                                "Line " + issue.getLineNumber() + ": " + issue.getMessage());
                            severityNode.getChildren().add(issueNode);
                        }

                        fileNode.getChildren().add(severityNode);
                    }
                }

                root.getChildren().add(fileNode);
            }
        }

        issuesTreeView.setRoot(root);
    }
}
