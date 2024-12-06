package edu.info0502.tp1;


import java.util.Vector;

public class Mediatheque {
    private String nomProprietaire;     
    private Vector<Media> collection;    

    public Mediatheque() {
        this.nomProprietaire = "Propriétaire par défaut";
        this.collection = new Vector<>(); 
    }

    public Mediatheque(Mediatheque autreMediatheque) {
        this.nomProprietaire = autreMediatheque.nomProprietaire;
        this.collection = new Vector<>(autreMediatheque.collection);   
    }

    public void add(Media media) {
        this.collection.add(media);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Mediatheque du propriétaire : ").append(this.nomProprietaire).append("\n");
        sb.append("Contenu de la médiathèque :\n");

 
        for (Media media : collection) {
            sb.append(media.toString()).append("\n");
        }

        return sb.toString();
    }

    public String getNomProprietaire() {
        return nomProprietaire;
    }

    public void setNomProprietaire(String nomProprietaire) {
        this.nomProprietaire = nomProprietaire;
    }

    public Vector<Media> getCollection() {
        return collection;
    }

    public void setCollection(Vector<Media> collection) {
        this.collection = collection;
    }
}
