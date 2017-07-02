package gewaechshaus.logic;

import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Erstellt einen Auftrag zum Ernten einer einzelnen Pflanze.
 */
public class Einzelernte extends Unterauftrag {

    private static final Logger log = Logger.getLogger(Einzelernte.class.getName());
    private Position zielPosition;
    private Einzelpflanze ep;
    private int zustand = 0;

    /**
     * Konstruktor eines Unterauftrags, der eine einzelne Pflanze erntet
     *
     * @param ep                Die Pflanze, die geerntet werden sollen
     * @param roboterleitsystem Das Roboterleitsystem des Gewächshauses
     */
    public Einzelernte(Einzelpflanze ep, Roboterleitsystem roboterleitsystem) {
        try {
            Handler handler = new FileHandler(Konstanten.loggingFilePath);
            log.addHandler(handler);
        } catch (Exception e) {

        }
        this.status = UnterauftragsStatus.erstellt;
        this.roboterleitsystem = roboterleitsystem;
        this.ep = ep;
        this.zustand = 0;
        List<Position> freieNachbarnVonPflanze = roboterleitsystem.getFreieNachbarFelderVon(ep.getPosition());
        zielPosition = (Position) freieNachbarnVonPflanze.toArray()[0];
        Logging.log(this.getClass().getName(), Level.INFO, "Einzelernte Unterauftrag erstellt");
    }

    private Position berechneZielPosition() {
        List<Position> freieNachbarnVonPflanze = roboterleitsystem.getFreieNachbarFelderVon(ep.getPosition());
        return (Position) freieNachbarnVonPflanze.toArray()[0];
    }

    /**
     * Führt den aktuellen Unterauftrag bzgl. des aktuellen Zustandes aus (Wird mehrfach aufgerufen, bis der
     * Unterauftragsstatus beendet oder abgebrochen ist
     *
     * @param roboter Roboter mit dem der Unterauftrag ausgeführt werden soll
     */
    @Override
    public void ausfuehren(Roboter roboter) {
        // Zustand für Clock

        this.status = UnterauftragsStatus.ausfuehrend;
        switch (zustand) {
            // Initialisiere und fahre eventuell schon erste Position an
            case 0:
                roboter.addObserver(this);
                roboter.setRoboterStatus(RoboterStatus.eBeschaeftigt);
                // Prüfe ob Zielposition == null, falls ja berechne neu und prüfe erneut ob 0, falls ja dann warte
                if (zielPosition == null) {
                    zielPosition = berechneZielPosition();
                    if (zielPosition == null) {
                        roboter.warte();
                        break;
                    }
                }
                fahreZuNachbarposition(roboter);
                zustand++;
                Logging.log(this.getClass().getName(), Level.INFO, "Initialisiere und beginne Fahrt zu Position: " + roboter.getPosition().toString());
                break;
            // Fahre zu Position
            case 1:
                if (!roboter.getPosition().equals(zielPosition)) {
                    fahreZuNachbarposition(roboter);
                } else {
                    zustand++;
                }
                Logging.log(this.getClass().getName(), Level.INFO, "Roboter fährt zu Position: " + roboter.getPosition().toString());
                break;
            // scanne
            case 2:
                roboter.scanne(ep.getPosition());
                zustand++;
                Logging.log(this.getClass().getName(), Level.INFO, "Scanne zu erntende Pflanze");
                break;
            // Schneide
            case 3:
                roboter.schneide(ep.getPosition());
                zustand++;
                Logging.log(this.getClass().getName(), Level.INFO, "Schneide Pflanze ab");
                break;
            // lade ein und setze wieder auf bereit
            case 4:
                roboter.ladePflanzeAuf(ep);
                roboter.deleteObserver(this);
                // Prüfe ob sich Status im Laufe des Auftrags geändert hat
                if (roboter.getStatus() == RoboterStatus.eBeschaeftigt) {
                    roboter.setRoboterStatus(RoboterStatus.eBereit);
                }
                Logging.log(this.getClass().getName(), Level.INFO, "Lade Pflanze ein und beende Unterauftrag");
                this.status = UnterauftragsStatus.beendet;
                // Unterauftrag abgeschlossen Auftrag benachrichtigen
                setChanged();
                notifyObservers();
                break;
        }
    }


    /**
     * Fährt ein Nachbarkästchen des Roboters an.
     *
     * @param roboter Roboter aus der Roboterverwaltung der zur Position fahren soll
     */
    private void fahreZuNachbarposition(Roboter roboter) {
        ArrayList<Position> wegListe;
        try {
            wegListe = roboterleitsystem.getPfadVonNach(roboter.getPosition(), zielPosition);


            // Roboter-Position aus Liste entfernen
            if (wegListe.size() > 1) {
                wegListe.remove(wegListe.size() - 1);
            }
            Position nPos = roboter.getPosition();
            nPos = wegListe.get(wegListe.size() - 1);

            Position rPos = roboter.getPosition();
            // Fahre in Richtung der Position
            if (nPos.getReihenID() > rPos.getReihenID()) {
                roboter.fahreNachUnten();
            } else if (nPos.getReihenID() < rPos.getReihenID()) {
                roboter.fahreNachOben();
            } else if (nPos.getSpaltenID() < rPos.getSpaltenID()) {
                roboter.fahreNachLinks();
            } else if (nPos.getSpaltenID() > rPos.getSpaltenID()) {
                roboter.fahreNachRechts();
            }

            if (roboter.getPosition().equals(nPos)) {
                wegListe.remove(0);
            }

        } catch (KeinWegGefundenException e) {
            Logging.log(this.getClass().getName(), Level.WARNING, "Kein Weg gefunden.");
            roboter.warte();
        }

    }

    /**
     * Update-Methode des Observers. Wird bei jedem Schritt der Uhr gecalled
     *
     * @param o
     * @param arg
     */
    @Override
    public void update(Observable o, Object arg) {
        if (o instanceof Clock) {
            ausfuehren(this.roboter);
        }
    }
}
