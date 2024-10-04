package umontpellier.erl.calculs.part2.GUI;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;
import umontpellier.erl.calculs.part2.CallGraph;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class GraphGUI extends JFrame {

    public GraphGUI() {
        setTitle("Graphe d'appels");
        setSize(600, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        JButton selectButton = new JButton("Sélectionner le dossier Java");
        selectButton.setFont(new Font("Arial", Font.BOLD, 24));
        selectButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        selectButton.setMaximumSize(new Dimension(400, selectButton.getPreferredSize().height)); // Réduire la largeur

        selectButton.addActionListener(e -> selectProjectDirectory());

        JLabel svgLabel = new JLabel();
        svgLabel.setHorizontalAlignment(SwingConstants.CENTER);
        svgLabel.setVerticalAlignment(SwingConstants.CENTER);

        add(Box.createVerticalGlue());
        add(selectButton);
        add(Box.createVerticalGlue());
        add(svgLabel, BorderLayout.CENTER);
    }

    private void selectProjectDirectory() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        String projectPath = System.getProperty("user.dir");
        File currentDirectory = new File(projectPath);
        fileChooser.setCurrentDirectory(currentDirectory);

        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedDirectory = fileChooser.getSelectedFile();
            System.out.println("Dossier sélectionné : " + selectedDirectory.getAbsolutePath());

            try {
                CallGraph callGraph = CallGraph.createCallGraph(selectedDirectory.getAbsolutePath());
                String dotFilePath = selectedDirectory.getAbsolutePath() + "/callgraph.dot";
                callGraph.exportToDotFile(dotFilePath);

                System.out.println("Le graphe d'appels a été exporté au format DOT.");

                convertDotToSvg(dotFilePath);

                displayGraph(callGraph);

            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Erreur lors de l'analyse du projet ou de l'export du graphe.", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void convertDotToSvg(String dotFilePath) {
        try {
            MutableGraph g = new Parser().read(new File(dotFilePath));

            Graphviz.fromGraph(g).render(Format.SVG).toFile(new File("output.svg"));
            System.out.println("Fichier DOT converti en SVG.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void displayGraph(CallGraph callGraph) {
        StringBuilder graphText = new StringBuilder();

        for (String source : callGraph.getInvocations().keySet()) {
            graphText.append(source).append(":\n");

            for (String target : callGraph.getInvocations().get(source).keySet()) {
                int count = callGraph.getInvocations().get(source).get(target);
                graphText.append("  --> ").append(target);
                if (count > 1) {
                    graphText.append(" (").append(count).append(" appels)");
                }
                graphText.append("\n");
            }
            graphText.append("\n");
        }

        JTextArea textArea = new JTextArea(graphText.toString());
        JScrollPane scrollPane = new JScrollPane(textArea);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        scrollPane.setPreferredSize(new Dimension(1000, 800));

        JLabel label = new JLabel("Graphe d'appels");
        label.setFont(new Font("Arial", Font.BOLD, 24));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        scrollPane.setColumnHeaderView(label);

        JButton openButton = new JButton("Visualiser le graphe");

        openButton.addActionListener(e -> {
            try {
                File file = new File("output.svg");
                Desktop.getDesktop().open(file);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(new JScrollPane(scrollPane), BorderLayout.CENTER);
        panel.add(openButton, BorderLayout.SOUTH);

        JOptionPane.showMessageDialog(null, panel, "Call Graph", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GraphGUI app = new GraphGUI();
            app.setVisible(true);
        });
    }
}