# TubeAUTO (dawniej YouTubeAUTO) - plan budowy

Każdy etap kończy się testem w aucie. Każdy etap zaczyna się dopiero po zatwierdzeniu.

## Etap 0 - sprawdzenie auta - ZROBIONE (2026-11-15)

- Wersja Android Auto: 18.0.664264 (Samsung S24 Ultra, Android 14).
- Połączenie: głównie bezprzewodowe.
- Ustawienia dewelopera Android Auto włączone, "Nieznane źródła" zaznaczone.

## Etap 1 - pusta aplikacja w aucie - ZROBIONE na symulatorze (2026-11-15, wersja 0.0.2)

- Najprostszy projekt Android (Kotlin) z Car App Library w kategorii nawigacji.
- Na ekranie auta wyświetla się kolorowy prostokąt z napisem.
- Test: aplikacja pojawia się w menu Android Auto i otwiera się w aucie.

## Etap 2 - strona internetowa w aucie - ZROBIONE na symulatorze (2026-11-16, wersja 0.0.3)

- Dodanie GeckoView, które w tle wyświetla stronę na ekranie auta.
- Test: widać stronę i można sprawdzić płynność (30 czy 60 kl./s).

## Etap 3 - dotyk - ZROBIONE na symulatorze (2026-11-16, wersja 0.0.6), powiększanie do sprawdzenia w aucie

- Kliknięcia, przewijanie i powiększanie z ekranu auta trafiają do strony.
- Przyciski na ekranie: wstecz, strona główna, odśwież.
- Test: można swobodnie poruszać się po YouTube.

## Etap 4 - uBlock Origin i YouTube - ZROBIONE na symulatorze (2026-11-16, wersja 0.1.8)

- Wbudowanie uBlock Origin do aplikacji.
- Pełny ekran filmu oraz dźwięk przez głośniki auta. (Dźwięk z pierwszeństwem: wersja 0.0.6. Pełny ekran: wersja 0.1.4. Przewijanie filmu stuknięciem w pasek: wersja 0.1.8.)
- Test: film odtwarza się bez reklam.

## Etap 5 - wpisywanie tekstu - ZROBIONE na symulatorze dla YouTube (2026-11-16, wersja 0.1.9)

- Wpisywanie adresu i wyszukiwanie przez klawiaturę na telefonie albo głosem.

## Etap 6 - blokada na postoju (opcjonalnie)

- Jeśli auto podaje prędkość, w czasie jazdy obraz się zasłania.

## Testowanie

- Etapy 1-5 testujemy na symulatorze ekranu auta na komputerze (DHU, Desktop Head Unit, w `C:\Android\Sdk\extras\google\auto`). Telefon podłączony kablem USB, w Android Auto włączony "Serwer jednostki głównej".
- W aucie robimy tylko testy końcowe: połączenie bezprzewodowe, płynność w prawdziwym radiu, prędkość auta (etap 6).

## Uwagi

- Gotową aplikację instaluje się w telefonie przez kabel USB albo jako plik APK.
- Kod zostaje tylko lokalnie w `C:\TEMP\TubeAUTO`.
