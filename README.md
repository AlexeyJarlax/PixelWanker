<a href="https://git.io/typing-svg"><img src="https://readme-typing-svg.herokuapp.com?font=Fira+Code&size=22&duration=4000&pause=400&width=435&lines=/PixelWanker/" alt="Typing SVG" /></a>

## Описание

PixelWanker накладывает измерительную сетку поверх любого приложения. Можно выбрать размер ячейки от
4 до 400 px/dp и базовый цвет линий (белый, чёрный, зелёный, красный). Дополнительно включается
второй цвет, который рисуется «двойной» линией рядом с основной — можно выбрать отсутствие второго
цвета или любой из вариантов: белый, чёрный, зелёный, красный, жёлтый, синий. Сетка состоит из
вертикальных и горизонтальных полос, их количество зависит от выбранного размера ячейки. В оверлее
есть быстрые кнопки скрытия сетки, смещения по шагу и выхода. Также доступен список установленных
приложений — откройте карточку и запустите приложение вместе с сеткой.

## Технологический стек

- **Язык и сборка:** Kotlin 2.2, Java 17, Gradle (Kotlin DSL).
- **UI:** Jetpack Compose + Material/Material3, собственная тема и компоненты.
- **Локализация:** 7 языков (русский, английский, испанский, хинди, французский, португальский, японский).
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
- **Сервис оверлея:** `PixelWankerOverlayService` создаёт и управляет сеткой через `WindowManager`, а также
  кнопками управления (сдвиг, скрытие, выход).
- **Хранилище настроек сетки:** `GridSettingsStore` сохраняет параметры сетки и восстанавливает их при запуске.

<a href="https://git.io/typing-svg"><img src="https://readme-typing-svg.herokuapp.com?font=Fira+Code&size=22&duration=4000&pause=400&width=435&lines=/Павлов+Алексей/" alt="Typing SVG" /></a>
