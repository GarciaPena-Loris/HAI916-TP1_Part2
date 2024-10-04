package umontpellier.erl.calculs.part1.GUI;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import umontpellier.erl.calculs.part1.Parser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CountGUI extends JFrame {
    private final CardLayout cardLayout;
    private final JPanel cards;
    private String projectPath;

    public CountGUI() {
        setTitle("Calculs Application");
        setSize(1200, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);

        JButton selectDirectoryButton = new JButton("Selectionnez le dossier src/ du projet Java");
        selectDirectoryButton.setFont(new Font("Arial", Font.BOLD, 24));

        projectPath = System.getProperty("user.dir");

        selectDirectoryButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            File currentDirectory = new File(projectPath);
            fileChooser.setCurrentDirectory(currentDirectory);
            int option = fileChooser.showOpenDialog(CountGUI.this);
            if (option == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                projectPath = file.getAbsolutePath();
                try {
                    Parser parser = new Parser(projectPath, 10);
                    ArrayList<File> javaFiles = Parser.listJavaFilesForFolder(new File(projectPath));

                    // Display the results in the text area
                    JTextArea resultArea1 = (JTextArea) ((JScrollPane) ((JPanel) cards.getComponent(1)).getComponent(0)).getViewport().getView();
                    resultArea1.setText("Dans le projet Java " + projectPath + " il y a :\n\n\n");
                    resultArea1.append(" - " + parser.countClasses(javaFiles) + " classes\n");
                    resultArea1.append(" - " + parser.countLines(javaFiles) + " lignes de code\n");
                    resultArea1.append(" - " + parser.countMethods(javaFiles) + " méthodes\n");
                    resultArea1.append(" - " + parser.countPackages(javaFiles) + " packages\n");
                    resultArea1.append(" - " + parser.countAverageMethodsPerClass(javaFiles) + " méthodes par classe en moyenne\n");
                    resultArea1.append(" - " + parser.countAverageLinesPerMethod(javaFiles) + " lignes par méthode en moyenne\n");
                    resultArea1.append(" - " + parser.countAverageFieldsPerClass(javaFiles) + " attributs par classe en moyenne\n");

                    JTextArea resultArea2 = (JTextArea) ((JScrollPane) ((JPanel) cards.getComponent(2)).getComponent(0)).getViewport().getView();
                    resultArea2.setText("Dans le projet Java " + projectPath + " il y a :\n\n\n");
                    List<Map.Entry<String, Integer>> top10PercentClassesMethods = parser.getTop10PercentClassesMethods(parser.getClassMethodCount(javaFiles));
                    resultArea2.append(" - Top 10% des classes avec le plus grand nombre de méthodes :\n");
                    for (Map.Entry<String, Integer> entry : top10PercentClassesMethods) {
                        resultArea2.append("   * " + entry.getKey() + " : " + entry.getValue() + " méthodes\n");
                    }
                    resultArea2.append("\n");

                    List<Map.Entry<String, Integer>> top10PercentClassesFields = parser.getTop10PercentClassesFields(parser.getClassFieldCount(javaFiles));
                    resultArea2.append(" - Top 10% des classes avec le plus grand nombre d'attributs :\n");
                    for (Map.Entry<String, Integer> entry : top10PercentClassesFields) {
                        resultArea2.append("   * " + entry.getKey() + " : " + entry.getValue() + " attributs\n");
                    }
                    resultArea2.append("\n");

                    List<String> top10PercentClassesBothMethodsAndFields = parser.getTop10PercentClassesBothMethodsAndFields(parser.getClassMethodCount(javaFiles), parser.getClassFieldCount(javaFiles));
                    resultArea2.append(" - Classes qui font partie en même temps des deux catégories précédentes :\n");
                    for (String className : top10PercentClassesBothMethodsAndFields) {
                        resultArea2.append("   * " + className + "\n");
                    }
                    resultArea2.append("\n");

                    List<String> classesWithMoreThanXMethods = parser.getClassesWithMoreThanXMethods(parser.getClassMethodCount(javaFiles), 10);
                    resultArea2.append(" - Classes avec plus de " + 10 + " méthodes :\n");
                    for (String className : classesWithMoreThanXMethods) {
                        resultArea2.append("   * " + className + " : " + parser.getClassMethodCount(javaFiles).get(className) + " méthodes\n");
                    }
                    resultArea2.append("\n");

                    int maxParameters = parser.getMaxParameters(parser.getClassMethods(javaFiles));
                    resultArea2.append(" - Nombre maximal de paramètres par rapport à toutes les méthodes de l'application : " + maxParameters + "\n");


                    JTextArea resultArea3 = (JTextArea) ((JScrollPane) ((JPanel) cards.getComponent(3)).getComponent(0)).getViewport().getView();
                    resultArea3.setText("Dans le projet Java " + projectPath + " il y a :\n\n\n");
                    Map<String, List<MethodDeclaration>> top10PercentMethodsPerClass = parser.getTop10PercentMethodsCodeLinePerClass(parser.getClassMethods(javaFiles));
                    StringBuilder sb = new StringBuilder();
                    for (Map.Entry<String, List<MethodDeclaration>> entry : top10PercentMethodsPerClass.entrySet()) {
                        sb.append("\n  ").append(entry.getKey());
                        for (MethodDeclaration method : entry.getValue()) {
                            sb.append("\n   - ").append(method.getName().getFullyQualifiedName()).append(", Lines: ").append(method.getBody().statements().size());
                        }
                        sb.append("\n");
                    }
                    resultArea3.append("Les 10% des méthodes qui possèdent le plus grand nombre de lignes de code (par classe): " + sb.toString());

                    cardLayout.next(cards);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });

        // Create the navigation buttons
        JButton prevButton = new JButton("Précédent");
        prevButton.addActionListener(e -> cardLayout.previous(cards));

        JButton nextButton = new JButton("Suivant");
        nextButton.addActionListener(e -> cardLayout.next(cards));

        // Add the select directory button to its own card
        JPanel card1 = new JPanel(new GridBagLayout());
        card1.add(selectDirectoryButton);
        cards.add(card1, "page1");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        JPanel card2 = new JPanel(new GridBagLayout());
        JTextArea resultArea1 = new JTextArea();
        resultArea1.setEditable(false);
        resultArea1.setFont(new Font("Arial", Font.BOLD, 20));
        card2.add(new JScrollPane(resultArea1), gbc);
        cards.add(card2, "page2");

        JPanel card3 = new JPanel(new GridBagLayout());
        JTextArea resultArea2 = new JTextArea();
        resultArea2.setEditable(false);
        resultArea2.setFont(new Font("Arial", Font.BOLD, 20));
        card3.add(new JScrollPane(resultArea2), gbc);
        cards.add(card3, "page3");

        JPanel card4 = new JPanel(new GridBagLayout());
        JTextArea resultArea3 = new JTextArea();
        resultArea3.setEditable(false);
        resultArea3.setFont(new Font("Arial", Font.BOLD, 20));
        card4.add(new JScrollPane(resultArea3), gbc);
        cards.add(card4, "page4");

        setLayout(new BorderLayout());
        add(cards, BorderLayout.CENTER);
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.add(prevButton);
        buttonsPanel.add(nextButton);
        add(buttonsPanel, BorderLayout.SOUTH);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CountGUI().setVisible(true));
    }
}