package gewaeshaus.logic;

import java.util.Date;

public class Meldung {

    String Meldung;
    Date Zeitstempel;
    Class Melder;

    public Meldung(string text, Class from) {
        Meldung = text;
        Zeitstempel = new Date();
        Melder = from;
    }

}
