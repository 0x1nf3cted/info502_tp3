package edu.info0502.tp1;



public class Media {
    private String titre;               
    private StringBuffer cote;          
    private int note;                   

    private static String nomMediatheque = "Mediatheque par défaut"; 

    public Media() {
        this.titre = "Titre par défaut";
        this.cote = new StringBuffer("Cote par défaut");
        this.note = 0;
    }

    public Media(String titre, StringBuffer cote, int note) {
        this.titre = titre;
        this.cote = new StringBuffer(cote);
        this.note = note;
    }

    public Media(Media autreMedia) {
        this.titre = autreMedia.titre;
        this.cote = new StringBuffer(autreMedia.cote); 
        this.note = autreMedia.note;
    }



    public String getTitre() {
        return this.titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public StringBuffer getCote() {
        return this.cote;
    }

    public void setCote(StringBuffer cote) {
        this.cote = new StringBuffer(cote);
    }

    public int getNote() {
        return this.note;
    }

    public void setNote(int note) {
        this.note = note;
    }

    public static String getNomMediatheque() {
        return nomMediatheque;
    }

    public static void setNomMediatheque(String nom) {
        nomMediatheque = nom;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("Clonage non supporté pour la classe Media.");
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Media media = (Media) obj;
        return note == media.note &&
               titre.equals(media.titre) &&
               cote.toString().equals(media.cote.toString());
    }

    @Override
    public String toString() {
        return "Media{" +
                "titre='" + titre + '\'' +
                ", cote=" + cote +
                ", note=" + note +
                '}';
    }
}
