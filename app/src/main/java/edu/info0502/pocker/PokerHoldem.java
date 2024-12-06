package edu.info0502.pocker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PokerHoldem {

    private Talon talon;
    private List<Joueur> joueurs;
    private CartesCommunautaires cartesCommunautaires;

    public PokerHoldem(int nombreDeJoueurs) {
        talon = new Talon(1);
        talon.melanger();

        // créer les joueurs et distribuer les cartes privées
        joueurs = new ArrayList<>();
        for (int i = 0; i < nombreDeJoueurs; i++) {
            Joueur joueur = new Joueur("Joueur " + (i + 1));
            joueur.recevoirCartesPrivees(talon.tirerCarte(), talon.tirerCarte());
            joueurs.add(joueur);
        }

        // initialiser les cartes "community cards"
        cartesCommunautaires = new CartesCommunautaires();
        for (int i = 0; i < 5; i++) {
            cartesCommunautaires.ajouterCarte(talon.tirerCarte());
        }
    }

    public void demarrerPartie() {
        System.out.println("~~~ Début de la partie de Texas Hold'em ~~~");
        cartesCommunautaires.afficher();

        // évaluer et afficher la meilleure main de chaque joueur
        for (Joueur joueur : joueurs) {
            joueur.evaluerMeilleureMain(cartesCommunautaires);
            joueur.afficherMain();
        }

        // déterminer le gagnant
        determinerGagnant();
    }

    private void determinerGagnant() {
        Joueur gagnant = joueurs.get(0);

        for (Joueur joueur : joueurs) {
            if (joueur.getMeilleureMain().comparerAvec(gagnant.getMeilleureMain()) > 0) {
                gagnant = joueur;
            }
        }

        System.out.println("Le gagnant est " + gagnant.getNom() + " avec la combinaison: " + gagnant.getMeilleureMain().evaluerMain());
    }

    public static void main(String[] args) {
        PokerHoldem jeu = new PokerHoldem(4);
        jeu.demarrerPartie();
    }
}

class Joueur {

    private String nom;
    private List<Carte> cartesPrivees;
    private Main meilleureMain;

    public Joueur(String nom) {
        this.nom = nom;
        this.cartesPrivees = new ArrayList<>();
    }

    public void recevoirCartesPrivees(Carte carte1, Carte carte2) {
        cartesPrivees.add(carte1);
        cartesPrivees.add(carte2);
    }

    public void evaluerMeilleureMain(CartesCommunautaires cartesCommunautaires) {
        List<Carte> toutesLesCartes = new ArrayList<>(cartesPrivees);
        toutesLesCartes.addAll(cartesCommunautaires.getCartes());

        // trouver la meilleure combinaison de 5 cartes parmi les 7 disponibles
        meilleureMain = trouverMeilleureCombinaison(toutesLesCartes);
    }

    private Main trouverMeilleureCombinaison(List<Carte> toutesLesCartes) {
        Main meilleureCombinaison = null;

        // générer les combinaisons de 5 cartes parmi les 7 disponibles
        List<List<Carte>> combinaisons = genererCombinaisons(toutesLesCartes, 5);
        for (List<Carte> combinaison : combinaisons) {
            Main main = new Main();
            for (Carte carte : combinaison) {
                main.ajouterCarte(carte);
            }

            if (meilleureCombinaison == null || main.comparerAvec(meilleureCombinaison) > 0) {
                meilleureCombinaison = main;
            }
        }

        return meilleureCombinaison; // en utilisant les fonctions de Main
    }

    private List<List<Carte>> genererCombinaisons(List<Carte> cartes, int taille) {
        List<List<Carte>> combinaisons = new ArrayList<>();
        genererCombinaisonsHelper(cartes, taille, 0, new ArrayList<>(), combinaisons);
        return combinaisons;
    }

    private void genererCombinaisonsHelper(List<Carte> cartes, int taille, int debut, List<Carte> current, List<List<Carte>> resultat) {
        if (current.size() == taille) {
            resultat.add(new ArrayList<>(current));
            return;
        }

        for (int i = debut; i < cartes.size(); i++) {
            current.add(cartes.get(i));
            genererCombinaisonsHelper(cartes, taille, i + 1, current, resultat);
            current.remove(current.size() - 1);
        }
    }

    public String getNom() {
        return nom;
    }

    public Main getMeilleureMain() {
        return meilleureMain;
    }

    public void afficherMain() {
        System.out.println(nom + " - Cartes privées: " + cartesPrivees);
        System.out.println("Meilleure main: " + meilleureMain);
        System.out.println("Combinaison: " + meilleureMain.evaluerMain());
        System.out.println();
    }
}

class CartesCommunautaires {

    private List<Carte> cartes;

    public CartesCommunautaires() {
        cartes = new ArrayList<>();
    }

    public void ajouterCarte(Carte carte) {
        if (cartes.size() < 5) {
            cartes.add(carte);
        } else {
            throw new IllegalStateException("Les cartes de la communauté sont déjà complètes");
        }
    }

    public List<Carte> getCartes() {
        return cartes;
    }

    public void afficher() {
        System.out.println("Cartes de la communauté: " + cartes);
    }
}