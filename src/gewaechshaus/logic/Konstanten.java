package gewaechshaus.logic;

/**
 * Sammelklasse für Konstanten zur einfacheren Manipulation der Simulation
 */
public class Konstanten {

    public static double AkkuEntladungNormal = -0.001;
    public static double AkkuEntladungBeschäftigt = -0.2;
    public static double AkkuAufladung = 0.5;
    public static double roboterSchrittweite = 0.5f;
    public static double kritischeLadeschwelle = 50f;
    public static int beetBreite = 5;
    public static int maximalerFuellstand = 2;
    public static double GewichtGurke = 0.6;
    public static double GewichtTomate = 0.2;
    public static double WachstumGurke = 0.2; // % pro Uhr-Tick
    public static double WachstumTomate = 0.4;// % pro Uhr-Tick
    public static boolean loggingAn = true;

    // Skalierung und Auflösung des Canvas
    public static int canvasDimensionX = 700;
    public static int canvasDimensionY = 700;
    // Kleiner == Größer
    public static int skalierungsfaktor = 20;
}
