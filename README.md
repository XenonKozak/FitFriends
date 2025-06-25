# FitFriends

Aplikacja mobilna do śledzenia aktywności fizycznej, która łączy GPS, pogodę i funkcje społecznościowe, aby pomóc Ci osiągać cele!

## Funkcje
- Śledzenie aktywności sportowych z wykorzystaniem GPS (czas, dystans, kalorie)
- Podgląd trasy na mapie Google
- Sprawdzanie aktualnej pogody na podstawie lokalizacji
- Spersonalizowane rekomendacje aktywności w zależności od pogody
- Rejestracja i logowanie użytkownika (Firebase Authentication)
- Profil użytkownika z możliwością zapisywania notatki motywacyjnej
- Bezpieczne wylogowanie i ochrona dostępu do aplikacji

## Wymagania
- Android Studio (zalecana najnowsza wersja)
- Android 7.0 (API 24) lub nowszy
- Konto Firebase (do autoryzacji)
- Klucz API do OpenWeatherMap (pogoda)
- Klucz API do Google Maps (mapa)

## Instalacja
1. Sklonuj repozytorium:
   ```bash
   git clone <adres_repozytorium>
   ```
2. Otwórz projekt w Android Studio.
3. Skonfiguruj plik `google-services.json` (Firebase) w katalogu `app/`.
4. Dodaj klucze API do pliku `app/src/main/res/values/strings.xml`:
   ```xml
   <string name="openweathermap_api_key">TWÓJ_KLUCZ</string>
   <string name="google_maps_key">TWÓJ_KLUCZ</string>
   ```
5. Uruchom aplikację na emulatorze lub urządzeniu fizycznym.

## Konfiguracja
- Upewnij się, że masz włączone usługi lokalizacji na urządzeniu.
- W razie potrzeby zaktualizuj uprawnienia w pliku `AndroidManifest.xml`.

## Technologie
- Kotlin
- Android Jetpack (Navigation, ViewModel, LiveData)
- Firebase Authentication
- Google Maps SDK
- OpenWeatherMap API
- Material Design

## Autorzy
- Wiktor Wardziak

---
Aplikacja stworzona w celach edukacyjnych. 
