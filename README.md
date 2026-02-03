<a href="https://git.io/typing-svg"><img src="https://readme-typing-svg.herokuapp.com?font=Fira+Code&size=22&duration=4000&pause=400&width=435&lines=/PixelWanker/" alt="Typing SVG" /></a>

## Описание

PixelWanker накладывает измерительную сетку поверх любого приложения. Можно выбрать размер ячейки от
12 до 60 px/dp и цвет линий (белый, черный, зелёный, красный, а также дополнительный контрастный
цвет: жёлтый или синий). Сетка состоит из вертикальных и горизонтальных полос, их количество зависит
от выбранного размера ячейки. Также доступен список установленных приложений — откройте карточку и
запустите приложение вместе с сеткой.

## Технологический стек

- **Язык и сборка:** Kotlin 2.2, Gradle (Kotlin DSL).
- **UI:** Jetpack Compose + Material/Material3, собственная тема и компоненты.
- **Навигация:** Navigation Compose.
- **Асинхронность и состояние:** Kotlin Coroutines, StateFlow, ViewModel.
- **Работа с разрешениями:** Accompanist Permissions, Peko.
- **Изображения:** Coil.
- **Эффекты UI:** Haze (blur).
- **Системный слой:** AndroidX, overlay через `WindowManager` + `TYPE_APPLICATION_OVERLAY`.
- **Хранение настроек:** SharedPreferences (GridSettingsStore).

## Архитектура

- **Single-Activity:** входная точка `MainActivity`, весь интерфейс в Compose.
- **MVVM:** экраны используют `ViewModel`, состояние передаётся через `StateFlow`, события обрабатываются в UI-слое.
- **Repository слой:** `InstalledAppsRepository` изолирует работу с `PackageManager` и отдаёт модели домена.
- **Навигация по экранам:** `NavGraph` на базе Navigation Compose.
- **Сервис оверлея:** `PixelWankerOverlayService` создаёт и управляет сеткой через `WindowManager`.
- **Хранилище настроек сетки:** `GridSettingsStore` сохраняет параметры сетки и восстанавливает их при запуске.

<a href="https://git.io/typing-svg"><img src="https://readme-typing-svg.herokuapp.com?font=Fira+Code&size=22&duration=4000&pause=400&width=435&lines=/Павлов+Алексей/" alt="Typing SVG" /></a>
