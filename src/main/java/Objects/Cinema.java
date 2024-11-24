package Objects;

public class Cinema {
    private int numCinema;
    private String nomCinema;
    private String adresseCinema;

    public Cinema(int numCinema, String nomCinema, String adresseCinema) {
        this.numCinema = numCinema;
        this.nomCinema = nomCinema;
        this.adresseCinema = adresseCinema;
    }

    public int getNumCinema() {
        return numCinema;
    }

    public void setNumCinema(int numCinema) {
        this.numCinema = numCinema;
    }

    public String getNomCinema() {
        return nomCinema;
    }

    public void setNomCinema(String nomCinema) {
        this.nomCinema = nomCinema;
    }

    public String getAdresseCinema() {
        return adresseCinema;
    }

    public void setAdresseCinema(String adresseCinema) {
        this.adresseCinema = adresseCinema;
    }
}