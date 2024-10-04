package umontpellier.erl.calculs.part1;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class ParserMain {

    public static void main(String[] args) {
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
        String projectPath;
        int X = 5;

        try {
            if (args.length < 1) {
                projectPath = demanderCheminProjet(inputReader);
            } else {
                projectPath = verifierCheminProjet(inputReader, args[0]);
            }

            Parser parser = new Parser(projectPath, X);
            parser.printCount();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String demanderCheminProjet(BufferedReader inputReader) throws IOException {
        System.out.println("Veuillez fournir le chemin vers le dossier src/ d'un projet Java : (laisser vide pour utiliser le répertoire courant)");
        String cheminProjet = inputReader.readLine();
        if (cheminProjet.isEmpty()) {
            cheminProjet = System.getProperty("user.dir") + "/src/";
            System.out.println("Chemin non fourni. Utilisation du répertoire courant : " + cheminProjet);
        }
        if (!cheminProjet.endsWith("/")) {
            cheminProjet += "/";
        }
        File dossierProjet = new File(cheminProjet);

        while (!dossierProjet.exists() || !cheminProjet.endsWith("src/")) {
            System.err.println("Erreur : " + cheminProjet + " n'existe pas ou n'est pas un dossier src/ de projet Java. Veuillez réessayer : ");
            cheminProjet = inputReader.readLine();
            if (cheminProjet.isEmpty()) {
                cheminProjet = System.getProperty("user.dir") + "/src/";
            }
            dossierProjet = new File(cheminProjet);
        }

        return cheminProjet;
    }

    private static String verifierCheminProjet(BufferedReader inputReader, String cheminUtilisateur) throws IOException {
        if (cheminUtilisateur.isEmpty()) {
            cheminUtilisateur = System.getProperty("user.dir") + "/src/";
        }
        File dossierProjet = new File(cheminUtilisateur);

        while (!dossierProjet.exists() || !cheminUtilisateur.endsWith("src/")) {
            System.err.println("Erreur : " + cheminUtilisateur + " n'existe pas ou n'est pas un dossier src/ de projet Java. Veuillez réessayer : ");
            cheminUtilisateur = inputReader.readLine();
            if (cheminUtilisateur.isEmpty()) {
                cheminUtilisateur = System.getProperty("user.dir") + "/src/";
            }
            dossierProjet = new File(cheminUtilisateur);
        }

        return cheminUtilisateur;
    }
}