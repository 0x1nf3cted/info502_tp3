package edu.info0502.tp1;


public class Film extends Media {

    private String realisateur; 
    private int annee;           

 
    public Film() {
        super();   
        this.realisateur = "Réalisateur par défaut";
        this.annee = 2000;   
    }

 
    public Film(String titre, StringBuffer cote, int note, String realisateur, int annee) {
        super(titre, cote, note);  
        this.realisateur = realisateur;
        this.annee = annee;
    }

 
    public Film(Film autreFilm) {
        super(autreFilm);  
        this.realisateur = autreFilm.realisateur;
        this.annee = autreFilm.annee;
    }
 

    public String getRealisateur() {
        return realisateur;
    }

    public void setRealisateur(String realisateur) {
        this.realisateur = realisateur;
    }

    public int getAnnee() {
        return annee;
    }

    public void setAnnee(int annee) {
        this.annee = annee;
    }
 
    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("Clonage non supporté pour la classe Film.");
    }
 
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Film)) {
            return false;
        }
        Film autreFilm = (Film) obj;
        return super.equals(autreFilm) &&   
               this.realisateur.equals(autreFilm.realisateur) &&
               this.annee == autreFilm.annee;
    }

    @Override
    public String toString() {
        return "Film{" +
               "titre='" + getTitre() + '\'' +
               ", cote=" + getCote() +
               ", note=" + getNote() +
               ", realisateur='" + realisateur + '\'' +
               ", annee=" + annee +
               '}';
    }
}
