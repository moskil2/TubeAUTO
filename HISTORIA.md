# TubeAUTO (dawniej YouTubeAUTO) - historia

## 2026-11-15

- Sprawdzenie, czy projekt jest wykonalny: tak (Car App Library, kategoria nawigacji, obraz przeglądarki na ekranie auta).
- Omówiona płynność obrazu: zwykle 30 kl./s, czasem 60 kl./s, zależnie od radia.
- Omówione wyświetlanie YouTube i blokowanie reklam.
- Decyzja: silnik GeckoView (Firefox) z uBlock Origin, bo na telefonie używany jest Firefox + uBlock.
- Utworzony folder projektu `C:\TEMP\YouTubeAUTO` i plik `USTALENIA.md` ze wstępnymi ustaleniami.
- Utworzony `PLAN.md` z etapami 0-6.
- Etap 0 zakończony: S24 Ultra, Android 14, Android Auto 18.0.664264, połączenie bezprzewodowe, "Nieznane źródła" włączone.
- Etap 1: utworzony projekt w `app\` (Kotlin, Car App Library 1.7.0, kategoria nawigacji, pakiet `app.youtubeauto`, wersja 0.0.1). Ekran w aucie: niebieskie tło z napisem "YouTubeAUTO działa" i rozmiarem ekranu, przycisk "Wyjdź". Pierwsze budowanie udane (APK 2,7 MB).
- Ustalone: APK trafia do folderu głównego jako `YouTubeAUTO-<wersja>.apk`, każda nowa wersja +1. Dodany krok kopiowania w `build.gradle.kts`.
- Decyzja: testy na symulatorze DHU zamiast w aucie (brak dostępu do auta). Zainstalowany DHU 2.0 (11 MB) w `C:\Android\Sdk\extras\google\auto`.
- Zbudowany `YouTubeAUTO-0.0.1.apk` w folderze głównym (2,7 MB). Czeka na instalację na telefonie przez USB.
- 0.0.1 zainstalowana przez USB, symulator połączony (w polskim Android Auto opcja nazywa się "Włącz serwer radioodtwarzacza"; po zamknięciu symulatora serwer się wyłącza). Aplikacji nie było na liście w Android Auto.
- Przyczyna: brak wpisu `com.google.android.gms.car.application` w manifeście (Car App Library nie dodaje go sama). Wersja 0.0.2: dodany wpis i plik `res\xml\automotive_app_desc.xml`.
- 0.0.2 widoczna w Android Auto i działa w symulatorze: niebieski ekran z napisem, ekran symulatora 800x400 (domyślny). Etap 1 zrobiony na symulatorze, test w aucie na koniec.

## 2026-11-16

- Etap 2, wersja 0.0.3: GeckoView 160.0.20261104181552 rysuje stronę (m.youtube.com) bezpośrednio na ekranie auta (GeckoDisplay na Surface z Android Auto, bez wirtualnego ekranu). Bez dotyku.
- GeckoView 160 wymusił zmianę narzędzi: AGP 8.13.0 -> 9.3.1, Gradle 8.14.3 -> 9.7.1, Kotlin 2.4.20 (wbudowany w AGP 9 Kotlin 2.2.0 za stary), kompilacja pod Android 37.2 (doinstalowana platforma, 68 MB). targetSdk bez zmian: 36.
- Tylko arm64-v8a. APK ma 200 MB (silnik `libxul.so` 155 MB bez kompresji), a nie szacowane 50-80 MB. Do zmniejszenia później.
- Wolne miejsce na C: spadło z 22 GB do 18 GB (GeckoView, Gradle 9, pamięć podręczna).
- 0.0.3 zainstalowana przez USB. Test w symulatorze: strona YouTube widoczna (okno zgód), etap 2 działa. Problem: wszystko kilka razy za duże, bo GeckoView wziął gęstość pikseli ekranu telefonu.
- 0.0.4: silnik startuje dopiero po otrzymaniu danych ekranu auta i dostaje gęstość (dpi) oraz rozmiar ekranu auta. Test w symulatorze: skala poprawna, okno zgód YouTube mieści się na ekranie.
- Etap 3, wersja 0.0.5: dotyk z ekranu auta do strony (`TouchInput.kt`): stuknięcie, przewijanie, rzut z rozpędem, powiększanie dwoma palcami (udawane dwa palce). Przyciski na pasku: Wstecz, Start, Odśwież, Wyjdź. Test w symulatorze: stuknięcia działają (zgody YouTube, uruchomienie filmu), film się odtwarza. Dźwięk filmu był słychać dopiero po włączeniu innej muzyki, potem oba źródła grały naraz. Przyczyna: aplikacja nie prosiła o pierwszeństwo dźwięku (audio focus).
- 0.0.6: pierwszeństwo dźwięku (`AudioFocus.kt`, przez MediaSession z GeckoView): film zatrzymuje inną muzykę, inna muzyka lub rozmowa zatrzymuje film, komunikaty nawigacji przyciszają film. Test w symulatorze: dźwięk od razu, pauza filmu i muzyki działa, przyciski Wstecz/Start/Odśwież działają, przewijanie działa, ale bardzo wolno.
- 0.0.7: przewijanie jako udawane przeciąganie palcem (silnik sam nadaje rozpęd po puszczeniu), zapis wartości przewijania w dzienniku telefonu (znacznik `YouTubeAUTO`) do pomiaru. Test: przewijanie dobre, ale na końcu przewijania otwierał się link/film pod środkiem ekranu.
- Przyczyna (z dziennika): Android Auto wysyła po przeciąganiu dodatkowy ruch o zerowej długości (~0,2 s po ostatnim), który kładł nowy udawany palec i podnosił go bez ruchu, czyli stuknięcie w środek ekranu.
- 0.0.8: ruchy o zerowej długości pomijane; palec podniesiony bez przesunięcia wysyła "gest anulowany" (ACTION_CANCEL) zamiast podniesienia. Test: lepiej, ale dalej czasem otwiera link na końcu przewijania.
- Przyczyna (z dziennika): pod koniec ruchu ręka zatrzymuje się na 0,13-0,55 s, kod po 0,15 s podnosił palec, a późniejszy ruch o 1-2 px kładł nowy palec. Tak mały ruch strona uznaje za stuknięcie.
- 0.0.9: nowy palec dopiero po ruchu ponad 10 px; czas uznania palca za podniesiony 0,15 s -> 0,35 s (rozpęd rusza ok. 0,2 s później). Dodatkowo (poza planem, zgłoszone): po dojściu udawanego palca do krawędzi ekranu palec jest tylko podnoszony, nowy pojawia się dopiero po ruchu ponad 10 px.
- Test 0.0.9 w symulatorze: OK, przewijanie bez przypadkowego otwierania linków.
- Etap 4, wersja 0.1.0: uBlock Origin 1.76.4 (podpisany plik z addons.mozilla.org, 6,3 MB) wbudowany w APK w `app\src\main\assets\ublock_origin.xpi`. Pierwszy start: instalacja z APK bez internetu. Każdy start: sprawdzenie nowszej wersji na addons.mozilla.org i aktualizacja bez okienka. Test: reklam brak.
- Sprawdzone miejsce na dysku: od 2026-11-15 projekt zajął ok. 5,5 GB (stare APK 1,3 GB, pliki budowania 1,3 GB, pamięć Gradle 2,6 GB, Android 37.2 + DHU 0,14 GB). APK ma ok. 200 MB, bo silnik `libxul.so` (155 MB) jest w nim bez kompresji.
- Pełny ekran filmu: film zostawał w rozmiarze 629x354 przyklejony do góry, zamiast wypełnić ekran 800x400.
- 0.1.1: zdalne debugowanie silnika (port przez USB, skrypt pomiarowy w folderze tymczasowym sesji) i zapis rozmiaru ekranu auta do dziennika. Pomiar: strona widzi 800x400, ale ustawienie ekranu "portrait".
- 0.1.2: próba ustawienia poziomego przez GeckoRuntime, nie zadziałała (silnik nadpisuje je ustawieniem z telefonu).
- 0.1.3: nasłuch zmian ustawienia ekranu w silniku (wewnętrzna część GeckoView, `GeckoScreenOrientation`) i przywracanie poziomego. Strona widzi "landscape", ale film dalej 629x354, więc to nie była przyczyna. Zmiana na razie zostaje.
- Przyczyna (pomiar): okno strony ma 800x400 przed i po pełnym ekranie, więc YouTube nie dostaje sygnału zmiany rozmiaru i nie przelicza filmu. Ręczne wysłanie sygnału: film 711x400.
- 0.1.4: wbudowany dodatek `app\src\main\assets\fsfix\` (installBuiltIn): przy wejściu i wyjściu z pełnego ekranu wysyła sygnał zmiany rozmiaru. Test: film 711x400, wyjście przyciskiem YouTube działa.
- Pasek postępu: stuknięcia nie przewijały, a szybkie stukanie przewijało film do początku.
- 0.1.5: zapis wszystkich gestów z Android Auto do dziennika. Wynik: podwójne stuknięcie Android Auto podaje jako jeden gest powiększania ze skalą 2,0 w miejscu stuknięcia, a aplikacja robiła z niego dwa udawane palce (100 px w lewo i w prawo), lewy palec przesuwał pasek.
- 0.1.6: pojedynczy gest powiększania 2,0 bez kolejnych kroków przez 0,1 s = dwa stuknięcia w tym samym miejscu.
- 0.1.7: stuknięcie jak prawdziwym palcem (w dół, ruch 1 px po 30 ms, w górę po 80 ms). Nie pomogło na pasek, zostaje.
- Ustalone na telefonie w Firefoksie: w YouTube stuknięcie w pasek tylko pokazuje miniaturkę, przewija się przeciąganiem kropki. Przeciąganie w Android Auto nie zadziała (Android Auto nie podaje miejsca początku ruchu).
- 0.1.8: dodatek `fsfix` 1.1: krótkie stuknięcie w pasek postępu ustawia film w miejscu stuknięcia. Test: działa (gdy przyciski YouTube są schowane, pierwsze stuknięcie je pokazuje, drugie przewija).
- Etap 4 zrobiony na symulatorze. Zgłoszony następny problem: brak wpisywania tekstu (etap 5).
- Etap 5, wersja 0.1.9: stuknięte pole tekstowe na stronie (prośba silnika o klawiaturę) otwiera `SearchScreen.kt`, ekran wyszukiwania Android Auto z klawiaturą na ekranie auta. Na YouTube zatwierdzony tekst otwiera wyniki wyszukiwania (`m.youtube.com/results?search_query=...`), na innych stronach tekst jest wpisywany w pole i naciskany Enter (niesprawdzone). W dzienniku tylko liczba znaków. Android Auto blokuje klawiaturę w czasie jazdy. Test YouTube: działa.
- Zgłoszone: przyciski Wstecz/Start/Odśwież/Wyjdź (pasek przycisków Android Auto, prawy górny róg) przez kilka sekund zasłaniają lupę YouTube.
- 0.2.0: próba z samymi ikonami (Material Design, `res\drawable\ic_*.xml`). Wynik: ikony wyświetlane błędnie (słuchawka, białe kwadraty), przyciski prawie nie mniejsze. Sprawdzone w bibliotece: pionowy pasek przyjmuje tylko ikony, a górny pasek musi mieć co najmniej jeden przycisk (stałby nad lupą); czasu wyświetlania przycisków nie da się ustawić.
- 0.2.1: decyzja użytkownika: powrót do napisów, bez zmian w położeniu strony. Pliki ikon zostają na dysku, nieużywane.

### Stan na koniec dnia (2026-11-16)

- Ostatnia wersja: 0.2.1 (`C:\TEMP\YouTubeAUTO\YouTubeAUTO-0.2.1.apk`), zainstalowana na telefonie.
- Działa w symulatorze: strona YouTube na ekranie auta w poprawnej skali, stuknięcia, przewijanie z rozpędem, przyciski Wstecz/Start/Odśwież/Wyjdź, dźwięk z pierwszeństwem, uBlock Origin (brak reklam, aktualizacja z internetu), pełny ekran filmu, przewijanie filmu stuknięciem w pasek postępu, wyszukiwanie w YouTube klawiaturą na ekranie auta.
- Do sprawdzenia w aucie: powiększanie dwoma palcami, połączenie bezprzewodowe, płynność.
- Następne kroki według `PLAN.md`: etap 5 do sprawdzenia na innych stronach niż YouTube, etap 6 (blokada na postoju).
- Do rozważenia: zmniejszenie APK (200 MB, `libxul.so` bez kompresji). Zdalne debugowanie silnika jest włączone (do pomiarów, tylko przez USB).
- Uruchamianie symulatora: kabel USB, w Android Auto "Włącz serwer radioodtwarzacza", `adb forward tcp:5277 tcp:5277`, potem symulator z `C:\Android\Sdk\extras\google\auto`.

## 2026-11-17

- Katalog projektu przemianowany z `C:\TEMP\YouTubeAUTO` na `C:\TEMP\TubeAUTO` (przez użytkownika). Kod bez zmian: kopiowanie APK używa ścieżki względnej, więc APK trafia teraz do `C:\TEMP\TubeAUTO`.
- Następny krok: zmiana nazwy programu.
- Pierwsza próba w aucie (wersja 0.2.1): aplikacja się uruchomiła, YouTube działał. Po wyjściu do menu auta i powrocie Android Auto pokazał komunikat, że YouTubeAUTO przestało działać, a aplikacja zniknęła z menu auta i z listy "Dostosuj aplikacje w menu".
- Odczyt przez debugowanie bezprzewodowe (bez kabla, komputer na hotspocie telefonu): w dzienniku awarii telefonu brak awarii YouTubeAUTO (są tylko dwie awarie usługi symulatora Android Auto). Zwykły dziennik z czasu próby już nadpisany, przyczyna nieznana.
- Symulator przez debugowanie bezprzewodowe: wyjście do menu i powrót bez błędu. Po tym uruchomieniu aplikacja wróciła na listę w ustawieniach Android Auto.
- 0.2.2: trwały dziennik na telefonie (`Dziennik.kt`): wszystko, co zapisuje proces aplikacji, w `files/dziennik/log*` (maks. 5 plików po 1 MB), nieobsłużone błędy z pełnym opisem w `files/dziennik/bledy.txt`, zdarzenia: start i koniec usługi, sesji i ekranu, zniknięcie ekranu auta. Odczyt: `adb shell run-as app.youtubeauto ls files/dziennik`. Jeden start zapisuje ok. 200 KB.
- 0.2.2: nowa ikona (biały kwadrat, czerwony prostokąt jak YouTube, w środku białe auto od przodu), pliki w `res\mipmap-anydpi\ic_launcher.xml` i `res\drawable\ic_launcher_*.xml`, generowane skryptem `ikona\paths.py` (podgląd `ikona\podglad.png`).
- 0.2.2 zbudowana (`YouTubeAUTO-0.2.2.apk`) i zainstalowana przez Wi-Fi (27 s). Sprawdzone: dziennik zapisuje się na telefonie.
- Następnie: próba w aucie z 0.2.2 (wyjście do menu i powrót), potem odczyt dziennika; zmiana nazwy programu na TubeAUTO.
- 0.2.3: zmiana nazwy na TubeAUTO: nazwa w telefonie i w Android Auto, plik APK `TubeAUTO-<wersja>.apk`, nazwa projektu, znacznik w dzienniku `TubeAUTO`, nazwa dodatku fsfix. Bez zmian: wewnętrzna nazwa pakietu `app.youtubeauto` (decyzja na później). Stare pliki `YouTubeAUTO-*.apk` zostają.
- 0.2.3: aplikacja na telefonie (`MainActivity.kt`, `ui\Theme.kt`, `ui\PhoneApp.kt`, Jetpack Compose): ikona na liście aplikacji, po otwarciu ciemny ekran z napisem "TubeAUTO" na środku ("Tube" biały, "AUTO" czerwony) i przyciskiem menu w lewym górnym rogu. Menu boczne jak w OnlyMusic (układ, wysuwanie, przycisk ZAMKNIJ), kolory w czerwieniach. Pozycje: Ustawienia ("Na razie brak ustawień"), O aplikacji. Język: decyzja użytkownika, bez tej pozycji, teksty tylko po polsku.
- O aplikacji: układ jak w OnlyMusic; "Oceń aplikację w Google Play", "Sprawdź aktualizacje", kontakt i strona widoczne bez linków (decyzja użytkownika). Teksty dopasowane do TubeAUTO: prywatność (aplikacja korzysta z internetu do stron i aktualizacji uBlock), warunki (oglądanie tylko na postoju), kontakt przez e-mail zamiast formularza, prawa autorskie bez zdania o Google Play. Licencje: GeckoView (MPL 2.0), uBlock Origin (GPLv3), Car App Library i AndroidX (Apache 2.0), czcionka Inter (OFL), teksty Apache i OFL skopiowane z OnlyMusic.
- 0.2.3 zbudowana (`TubeAUTO-0.2.3.apk`, 212 MB) i zainstalowana przez Wi-Fi. W czasie budowania wolne miejsce na C: chwilowo spadło do 8,6 GB, potem wróciło do 15 GB.
- Sprawdzone ciasteczka TubeAUTO (`files/mozilla/*.default/cookies.sqlite`): zapisywane na stałe, przetrwają restart i nową wersję. YouTube: 9 ciasteczek do 2027 r., m.in. anonimowy numer gościa `VISITOR_INFO1_LIVE`; brak ciasteczek logowania Google, czyli TubeAUTO to niezalogowany gość, bez historii z konta. Zalogowanie na konto Google: decyzja użytkownika, otwarte.
- 0.2.4: większy przycisk menu na telefonie (kółko 40 -> 52 dp, kreski 18 -> 26 dp). Zbudowana i zainstalowana przez Wi-Fi.
- 0.2.5: w menu telefonu pozycja "uBlock" (`UblockStatus.kt`, ekran w `ui\PhoneApp.kt`): zainstalowana wersja, data i wynik ostatniego sprawdzenia (zapisywane przez aplikację w aucie), przycisk "Sprawdź teraz" (addons.mozilla.org), przycisk "Odśwież listy filtrów" (przy następnym uruchomieniu w aucie otwiera stronę uBlock "Listy filtrów", bo polecenie odświeżenia uBlock przyjmuje tylko z własnych stron). Zbudowana i zainstalowana przez Wi-Fi (nowe parowanie, port połączenia z `adb mdns services`).
- Test 0.2.5 w symulatorze: czarny ekran auta (tylko przyciski). Dziennik: strona się ładowała (procesy kart i mediów działały), problem tylko z obrazem. Prośby o odświeżenie list nie było w zapisie. Po `am force-stop` obraz wrócił. Próba odtworzenia (najpierw ekran na telefonie, potem auto): obraz działa, czyli przyczyna czarnego ekranu nieznana.
- Nowe narzędzie `narzedzia\symulator.py`: uruchamia symulator i przekazuje mu polecenia z `narzedzia\polecenia.txt` (np. `tap x y`, `screenshot plik.png`, a także `speed` do etapu 6), odpowiedzi w `narzedzia\symulator.log`. Dzięki temu testy w symulatorze mogą iść bez użytkownika.
- Test 0.2.5 sterowany samodzielnie (adb na telefonie, `narzedzia\symulator.py` w symulatorze): "Sprawdź teraz" pokazuje "Najnowsza: 1.76.4. Masz aktualną wersję.", "Odśwież listy filtrów" zapisuje prośbę. Po ponownym uruchomieniu w aucie otwiera się strona uBlock "Listy filtrów" (działa w GeckoView na ekranie auta), prośba kasowana. Przycisk uBlock "Zaktualizuj teraz" był szary, bo żadna lista nie wymagała odświeżenia. Przycisk "Wstecz" z paska Android Auto przez kilka sekund zasłania "Zaktualizuj teraz".
- Odtworzony błąd obrazu: po `am force-stop` przy połączonym aucie Android Auto sam uruchamia aplikację ponownie, ale ekran auta pokazuje zamrożony ostatni obraz poprzedniego procesu (stuknięcia nie działają, przyciski Wstecz/Start/... są). Wyjście do menu auta i ponowne wejście przywraca obraz. Pewnie to ten sam błąd co wcześniejszy czarny ekran (po instalacji nowej wersji przy połączonym aucie). Przyczyna jeszcze nieustalona.
- Na telefonie: ikony paska stanu (godzina, bateria) są ciemne na ciemnym tle, słabo widoczne.
- 0.2.6: uBlock aktualizowany z telefonu, bez auta: aplikacja zapamiętuje ekran auta z ostatniego połączenia (za pierwszym razem 800x400, 160 dpi), więc silnik może wystartować z telefonu w skali auta. "Sprawdź teraz" od razu sprawdza i instaluje nową wersję. "Odśwież listy filtrów" otwiera stronę uBlock "Listy filtrów" na ekranie telefonu (GeckoView, mała, można powiększyć palcami). Usunięte działanie przez auto (prośba i otwieranie strony w aucie). Pasek stanu i nawigacji telefonu: jasne ikony na ciemnym tle. Zbudowana (`TubeAUTO-0.2.6.apk`), czeka na instalację (brak połączenia z telefonem).

### Stan na koniec dnia (2026-11-17)

- Ostatnia wersja: 0.2.6 (`C:\TEMP\TubeAUTO\TubeAUTO-0.2.6.apk`) zbudowana, NIE zainstalowana (brak połączenia z telefonem). Na telefonie jest 0.2.5.
- Działa: nazwa TubeAUTO, nowa ikona, ekran na telefonie z menu (Ustawienia, uBlock, O aplikacji, ZAMKNIJ), trwały dziennik na telefonie, w aucie wszystko jak w 0.2.1.
- Do sprawdzenia po instalacji 0.2.6: "Sprawdź teraz" i "Odśwież listy filtrów" z telefonu (bez auta), jasne ikony pasków telefonu, obraz w aucie po starcie silnika z telefonu (skala ekranu auta zapamiętana).
- Otwarte:
  - Czarny lub zamrożony ekran w aucie, gdy proces aplikacji zostanie zamknięty przy połączonym aucie (instalacja, force-stop) i Android Auto sam uruchomi ją ponownie. Obejście: wyjść do menu auta i wejść ponownie. Przyczyna nieznana. Możliwe, że to też przyczyna komunikatu "przestał działać" z próby w aucie (niepotwierdzone).
  - Logowanie na konto Google w TubeAUTO (historia i polecenia YouTube): decyzja użytkownika.
  - Nazwa pakietu `app.youtubeauto`: zmiana = osobna aplikacja na telefonie, decyzja użytkownika.
  - Etap 6 (blokada w czasie jazdy): symulator ma polecenie `speed`.
- Połączenie bez kabla: komputer na hotspocie telefonu, w telefonie "Debugowanie bezprzewodowe" > "Paruj urządzenie za pomocą kodu parowania", `adb pair IP:port_parowania kod`, port połączenia z `adb mdns services`. Kod wygasa szybko, używać od razu.
- Symulator sterowany przez `narzedzia\symulator.py` (polecenia w `narzedzia\polecenia.txt`): `adb forward tcp:5277 tcp:5277`, w Android Auto "Włącz serwer radioodtwarzacza".

## 2026-11-18

- Próba w aucie 18.11 o 02:05 (wersja 0.2.5, kabel): według użytkownika ekran auta był biały, widać było tylko przyciski, a w menu auta nie było TubeAUTO na liście aplikacji.
- Dziennik aplikacji (`files/dziennik/log`): TubeAUTO uruchomiło się samo, ekran auta 1344x589, dpi 213 (pierwszy pomiar prawdziwego auta). Sprawdzenie uBlock: "HTTP Request failed", o 02:06:14 błąd ładowania `https://m.youtube.com/` (GeckoView:OnLoadError). Silnik nie miał połączenia z internetem, więc biały ekran to niezaładowana strona. Nie wiadomo, czy telefon miał wtedy internet.
- Zapis dziennika urywa się w pół linii o 02:06:15, a proces działał do 02:27 (zamknięty przez system jako pusty, `dumpsys activity exit-info`). Przyczyna przerwy w zapisie nieznana. Brak awarii (`logcat -b crash` pusty).
- Rano (11:09, bez auta) Android Auto odrzucał TubeAUTO: `CAR.VALIDATOR: Package DENIED; failed all other checks`, a obok `isPackageInstalledByPlayCheck service not connected`. Tak samo odrzucana jest inna aplikacja spoza Google Play (`sksa.aa.customapps`).
- Ustawienia programisty Android Auto: "Nieznane źródła" są WŁĄCZONE, tryb programisty włączony, serwer radioodtwarzacza włączony. To nie ta przyczyna.
- Symulator (kabel, 11:20): przy połączeniu brak odrzucenia TubeAUTO. Aplikacja jest na liście w menu auta (na końcu, obok AAAD), uruchamia się, YouTube ładuje się poprawnie.
- Wniosek: poza połączeniem z autem odrzucenie w dzienniku jest widoczne, przy połączeniu z symulatorem go nie ma. Dlaczego w prawdziwym aucie o 02:06 aplikacji nie było na liście: nieustalone, bo dziennik systemu z tej godziny jest już nadpisany (zaczynał się o 08:09).
- 0.2.6 zainstalowana na telefonie przez kabel (8 s, aktualizacja z zachowaniem danych), sprawdzone: versionName 0.2.6, versionCode 26.
- Do sprawdzenia przy następnej próbie w aucie: czy telefon ma internet (biały ekran), oraz od razu po próbie odczytać dziennik systemu (`adb logcat -d | grep CAR.VALIDATOR`), zanim zostanie nadpisany.
