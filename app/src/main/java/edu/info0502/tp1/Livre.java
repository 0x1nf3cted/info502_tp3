package edu.info0502.tp1;


public class Livre extends Media {

    private String auteur;
    private String dateParution;  
    private int nbPages;       
    private String isbn;       


    public Livre() {
        super(); 
        this.auteur = "Auteur par défaut";
        this.dateParution = "Date de parution par défaut";
        this.nbPages = 0;
        this.isbn = "ISBN par défaut";
    }


    public Livre(String titre, StringBuffer cote, int note, String auteur, String dateParution, int nbPages, String isbn) {
        super(titre, cote, note); 
        this.auteur = auteur;
        this.dateParution = dateParution;
        this.nbPages = nbPages;
        this.isbn = isbn;
    }


    public Livre(String titre, StringBuffer cote, int note, String auteur, String isbn) {
        super(titre, cote, note);
        this.auteur = auteur;
        this.isbn = isbn;
        this.dateParution = "Date de parution par défaut";
        this.nbPages = 0;
    }


    public Livre(Livre autreLivre) {
        super(autreLivre);  
        this.auteur = autreLivre.auteur;
        this.dateParution = autreLivre.dateParution;
        this.nbPages = autreLivre.nbPages;
        this.isbn = autreLivre.isbn;
    }

    public String getAuteur() {
        return auteur;
    }

    public void setAuteur(String auteur) {
        this.auteur = auteur;
    }

    public String getDateParution() {
        return dateParution;
    }

    public void setDateParution(String dateParution) {
        this.dateParution = dateParution;
    }

    public int getNbPages() {
        return nbPages;
    }

    public void setNbPages(int nbPages) {
        this.nbPages = nbPages;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    @Override
    public String toString() {
        return "Livre{" +
                "titre='" + getTitre() + '\'' +
                ", cote=" + getCote() +
                ", note=" + getNote() +
                ", auteur='" + auteur + '\'' +
                ", dateParution='" + dateParution + '\'' +
                ", nbPages=" + nbPages +
                ", isbn='" + isbn + '\'' +
                '}';
    }
}
