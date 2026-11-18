# TubeAUTO (dawniej YouTubeAUTO) - wstępne ustalenia

Nazwa robocza: **YouTubeAUTO**. Data ustaleń: 2026-11-15.

## Cel

Aplikacja na Androida do użytku prywatnego (bez publikacji w Sklepie Play). W Android Auto podaje się za aplikację nawigacyjną, ale zamiast mapy pokazuje na ekranie auta przeglądarkę internetową, w tym YouTube.

## Jak to działa technicznie

- Android Auto wpuszcza tylko aplikacje z określonych kategorii. Tylko kategoria "nawigacja" może rysować własny obraz na ekranie auta.
- Biblioteka: **Car App Library** (androidx.car.app). Aplikacja nawigacyjna dostaje od Android Auto powierzchnię do rysowania (`Surface`).
- Przeglądarka działa w tle na wirtualnym ekranie telefonu, a jej obraz jest przesyłany na tę powierzchnię.
- Dotyk z ekranu auta (kliknięcie, przewijanie, szczypanie) trafia do aplikacji i jest przekazywany do przeglądarki.
- Ten mechanizm już działa w praktyce: wykorzystują go istniejące nieoficjalne aplikacje (lustra ekranu, odtwarzacze wideo dla Android Auto).

## Silnik przeglądarki: GeckoView + uBlock Origin

- Na telefonie używany jest Firefox z uBlock Origin, bez reklam.
- Zwykła przeglądarka wbudowana w Androida (WebView) nie obsługuje dodatków, więc odpada.
- **Wybór: GeckoView** (silnik Firefoxa od Mozilli). Obsługuje dodatki, więc można w nim zainstalować **uBlock Origin**. Blokowanie reklam, także na YouTube, działa tak samo jak w Firefoxie.
- uBlock aktualizuje się sam.
- Aplikacja ma własną kopię uBlocka. Ustawienia można przenieść z Firefoxa przez eksport/import kopii zapasowej uBlocka.
- Koszt: aplikacja będzie większa o około 50-80 MB.

## YouTube

- Strona m.youtube.com w przeglądarce w aplikacji, z obsługą pełnego ekranu filmu (trzeba ją osobno zaprogramować).
- Dźwięk idzie przez głośniki auta tak jak muzyka z telefonu. Trzeba obsłużyć przełączanie dźwięku (np. komunikaty nawigacji).
- Logowanie na konto Google w przeglądarkach wbudowanych w aplikacje bywa blokowane przez Google. Do sprawdzenia w GeckoView. W najgorszym razie oglądanie bez konta.
- YouTube wykrywa blokowanie reklam. Czasem coś przestaje działać, dopóki uBlock się nie zaktualizuje.

## Płynność obrazu

- Telefon koduje obraz Android Auto jako strumień wideo (H.264) i wysyła go do radia.
- Liczbę klatek ustalają telefon i radio: zwykle **30 kl./s**, w niektórych nowszych radiach **60 kl./s**.
- Filmy 60 kl./s będą wyświetlane w 30 kl./s, jeśli radio nie obsługuje 60.
- Przesyłanie obrazu przez wirtualny ekran może dodać małe opóźnienie. Połączenie bezprzewodowe przy słabym Wi-Fi może powodować przycięcia.
- Nie wiadomo, czy da się wymusić 60 kl./s w ustawieniach dewelopera Android Auto. Do sprawdzenia.

## Ograniczenia

- **Instalacja spoza Sklepu Play:** w ustawieniach dewelopera Android Auto trzeba włączyć "Nieznane źródła". Bez tego Android Auto nie pokaże aplikacji.
- **Klawiatura:** szablon nawigacyjny nie pozwala wywołać klawiatury na ekranie auta. Możliwości: wpisywanie na telefonie, głos albo własna klawiatura narysowana na ekranie.
- **Aktualizacje Android Auto:** Google może w przyszłości zaostrzyć zasady dla aplikacji spoza Sklepu.
- **Bezpieczeństwo:** blokada "tylko na postoju". Car App Library może podawać prędkość auta, ale nie każde auto ją udostępnia.

## Opcja zapasowa: lustro ekranu telefonu

- Do auta trafia cały obraz telefonu, więc działa każda aplikacja, w tym Firefox z uBlockiem.
- Wady: Android przy każdym uruchomieniu pyta o zgodę na nagrywanie ekranu, ekran telefonu musi być włączony, a obraz ma proporcje telefonu (czarne pasy po bokach).

## Plik aplikacji i numer wersji

- Gotowy APK trafia do folderu głównego: `C:\TEMP\YouTubeAUTO\YouTubeAUTO-<wersja>.apk`, np. `YouTubeAUTO-0.0.1.apk`.
- Pierwsza wersja: 0.0.1. Każda nowa wersja z nowym kodem: +1 (0.0.1 -> 0.0.2 ... 0.0.9 -> 0.1.0, każda cyfra najwyżej 9).
- Numer wersji jest w `app\build.gradle.kts` (versionName i versionCode). Jego podniesienie jest zawsze wpisane w plan zmian.

## Sprzęt testowy

- Telefon: Samsung Galaxy S24 Ultra, Android 14.
- Android Auto: 18.0.664264.
- Połączenie z autem: głównie bezprzewodowe (Wi-Fi + Bluetooth).
- Ustawienia dewelopera Android Auto: włączone, "Nieznane źródła" zaznaczone (2026-11-15).

## Do sprawdzenia przed pisaniem

- Czy auto udostępnia prędkość (blokada "tylko na postoju").
- Czy radio obsługuje 60 kl./s.
- Czy logowanie Google działa w GeckoView.
