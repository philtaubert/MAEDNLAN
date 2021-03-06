package vsys.projekt.maednlan;

import java.util.HashMap;

public class Spieler {

	int spielernummer = 0;
	String name = "Spieler";
	boolean beendet = false;
	HashMap<Integer, Spielfigur> spielfiguren = new HashMap<Integer, Spielfigur>();

	public Spieler(int spielernummer, String spielername) { //erzeuge Spieler mit Name
		this.spielernummer = spielernummer;
		this.name = spielername;
	}

	public void erzeugeSpielfiguren() { //erzeuge 4 Spielfiguren
		for (int k = 1; k <= 4; k++) {
			Spielfigur spielfigur = new Spielfigur(this.spielernummer, k);
			spielfiguren.put(k, spielfigur);
		}
	}

	public int rutschen(int augenzahl) { //prüfe, welche Spielfiguren gerutscht werden können, rutsche wenn möglich

		HashMap<Integer, Integer> figurenStatus = pruefeFiguren(augenzahl);

		if (augenzahl == 6) {
			for (int rfigurnummer : figurenStatus.keySet()) {
				if (figurenStatus.get(rfigurnummer) == -1)
					return ausruecken();
			}
		}

		boolean figuren = false;
		for (int rfigurnummer : figurenStatus.keySet()) {
			if (figurenStatus.get(rfigurnummer) == 1 || figurenStatus.get(rfigurnummer) == 10) {
				figuren = true;
				break;
			}
		}

		int neuesFeld = -99;
		if (figuren == true) {
			Netzwerk.zeigeText(augenzahl + " gewürfelt! Welche Spielfigur rutschen? Anklicken zum Auswählen...");
			do {
				int figurnummer = Netzwerk.welcheSpielfigur(spielernummer);
				int figurStatus = pruefeFiguren(augenzahl).get(figurnummer);
				switch (figurStatus) {
				case -1:
					Netzwerk.zeigeText("Spielfigur steht im Startbereich! Andere Figur auswählen...");
					break;
				case 9:
					Netzwerk.zeigeText("Auf dem Feld steht bereits ein eigener Spielstein. Andere Figur auswählen...");
					break;
				case 99:
					Netzwerk.zeigeText("Im Ziel kann nicht überholt werden! Andere Figur auswählen...");
					break;
				case 999:
					Netzwerk.zeigeText("Augenzahl ist zu groß! Andere Figur auswählen...");
					break;
				default:
					neuesFeld = setzeFigur(augenzahl, figurnummer);
					break;
				}
			} while (neuesFeld == -99);
		}

		return neuesFeld;

	}

	private int ausruecken() { //prüfe, ob Spielfiguren ausgerückt werden können, rücke aus wenn möglich

		int neuesFeld = -99;
		int figurnummer = 0;

		Netzwerk.zeigeText("6 Gewürfelt! Welche Spielfigur ausrücken? Anklicken zum Auswählen...");
		do {

			figurnummer = Netzwerk.welcheSpielfigur(spielernummer);
			Spielfigur spielfigur = spielfiguren.get(figurnummer);

			if (pruefeFigur(spielernummer * 10, figurnummer) != -1) {
				Netzwerk.zeigeText("Spielfigur steht nicht im Startbereich! Andere Figur auswählen...");
			} else {
				neuesFeld = spielfigur.ausRuecken();
			}

		} while (neuesFeld == -99);

		return neuesFeld;
	}

	public int nachausruecken() { //Nach ausrücken nochmal würfeln und Spielfigur vom Startfeld rutschen

		Netzwerk.zeigeText("Nochmal würfeln und vom Start runter...");
		int augenzahl = Wuerfel.einmalWuerfeln(spielernummer);

		HashMap<Integer, Integer> figurenStatus = pruefeFiguren(augenzahl);

		boolean start = false;
		for (int rfigurnummer : figurenStatus.keySet()) {
			if (figurenStatus.get(rfigurnummer) == -1) {
				start = true;
				break;
			}
		}

		int figurnummer = 0;
		for (int rfigurnummer : figurenStatus.keySet()) {
			if (figurenStatus.get(rfigurnummer) == 10) {
				figurnummer = rfigurnummer;
				break;
			}
		}

		int neuesFeld = 0;

		if (pruefeFigur(augenzahl, figurnummer) != 1 && start == false) {
			neuesFeld = rutschen(augenzahl);
		} else {
			neuesFeld = setzeFigur(augenzahl, figurnummer);
		}

		return neuesFeld;

	}

	private int setzeFigur(int augenzahl, int figurnummer) { //setze neue Feldnummer
		Spielfigur spielfigur = spielfiguren.get(figurnummer);
		int neuesFeld = spielfigur.berechneNeuesFeld(augenzahl);
		spielfigur.setzeNeuesFeld(neuesFeld);
		return neuesFeld;
	}

	private HashMap<Integer, Integer> pruefeFiguren(int augenzahl) { //prüfe alle Figuren
		HashMap<Integer, Integer> figurenStatus = new HashMap<Integer, Integer>();
		for (int i = 1; i <= 4; i++) {
			figurenStatus.put(i, pruefeFigur(augenzahl, i));
		}
		return figurenStatus;
	}

	private int pruefeFigur(int augenzahl, int figurnummer) { //prüfe, Figur, ob sie gerutscht werden kann und evtl. warum nicht

		Spielfigur spielfigur = spielfiguren.get(figurnummer);
		int neuesFeld = spielfigur.berechneNeuesFeld(augenzahl);

		if (spielfigur.feldnummer < 0) {
			return -1;
		}

		if (neuesFeld > this.spielernummer * 10 + 103) {
			return 999;
		}

		if (spielfigur.feldnummer == spielernummer * 10) {
			return 10;
		}

		for (int spielFigurNummer : spielfiguren.keySet()) {
			if (spielFigurNummer != figurnummer) {
				Spielfigur rspielfigur = spielfiguren.get(spielFigurNummer);
				if (rspielfigur.feldnummer == neuesFeld) {
					return 9;
				}
				if (rspielfigur.feldnummer > 100 && rspielfigur.feldnummer < neuesFeld
						&& spielfigur.feldnummer < rspielfigur.feldnummer) {
					return 99;
				}
			}
		}

		return 1;

	}

	public boolean dreimalWuerfeln() { //prüfe, ob Spieler dreimal würfeln darf (wenn einzige Möglichkeit weiterzuspielen = Spielstein ausrücken)
		int zielFeld = spielernummer * 10 + 103;
		int imZiel = 0;
		int imStart = 0;
		for (Integer spielernummer : spielfiguren.keySet()) {
			int figurFeldnummer = spielfiguren.get(spielernummer).feldnummer;
			if (figurFeldnummer == zielFeld) {
				zielFeld--;
				imZiel++;
			} else if (figurFeldnummer < 0) {
				imStart++;
			}
		}
		if (imZiel + imStart == 4) {
			return true;
		} else {
			return false;
		}
	}

	public boolean pruefeBeendet() { //prüfe, ob Spieler im Ziel ist
		int figurenImZiel = 0;
		for (int spielFigurNummer : spielfiguren.keySet()) {
			Spielfigur spielfigur = spielfiguren.get(spielFigurNummer);
			if (spielfigur.feldnummer > 100) {
				figurenImZiel++;
			}
		}
		if (figurenImZiel == 4) {
			beendet = true;
			return true;
		} else {
			return false;
		}
	}

	public HashMap<Integer, Spielfigur> holeSpielfiguren() {
		return spielfiguren;
	}

}
