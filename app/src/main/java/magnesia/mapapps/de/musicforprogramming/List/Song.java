package magnesia.mapapps.de.musicforprogramming.List;

/**
 * Created by Hagen on 07.02.2018.
 */

public class Song {

    private String title, length, url, number, song;

    // default constructor
    public Song() {
    }

    public Song(String title, String length, String url, String number) {
        this.title = title;
        this.length = length;
        this.url = url;
        this.number = number;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLength() {
        return length;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
