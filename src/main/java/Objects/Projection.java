package Objects;



public class Projection {
    private int idSalle;
    private  String NomCinema;
    private String Film;

    public void setIdSalle(int idSalle) {
        this.idSalle = idSalle;
    }

    public void setNomCinema(String nomCinema) {
        NomCinema = nomCinema;
    }

    public void setFilm(String film) {
        Film = film;
    }

    public void setHoraire(String horaire) {
        Horaire = horaire;
    }

    public void setDate(String date) {
        Date = date;
    }

    public int getIdSalle() {
        return idSalle;
    }

    public String getNomCinema() {
        return NomCinema;
    }

    public String getFilm() {
        return Film;
    }

    public String getHoraire() {
        return Horaire;
    }

    public String getDate() {
        return Date;
    }

    private String Horaire;
    private String Date;

    public Projection(int idSalle, String nomCinema, String film, String horaire, String date) {
        this.idSalle = idSalle;
        NomCinema = nomCinema;
        Film = film;
        Horaire = horaire;
        Date = date;
    }


}
