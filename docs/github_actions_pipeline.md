# GitHub Actions Pipeline (Android CI)

Этот документ описывает, как в проекте настроен пайплайн GitHub Actions для Android.

## Файл workflow

- Основной workflow: `.github/workflows/android-ci.yml`.

## Триггеры

Pipeline запускается при:

- `push` в `main` и `master`;
- `pull_request`.

## Concurrency

Используется:

- `group: android-ci-${{ github.ref }}`
- `cancel-in-progress: true`

Это отменяет предыдущий незавершённый запуск для той же ветки/PR и оставляет только актуальный.

## Структура jobs

### 1) `checks` — Unit tests + lint

- Runner: `ubuntu-latest`.
- Шаги:
  1. Checkout (`actions/checkout@v4`);
  2. JDK 17 (`actions/setup-java@v4`, Temurin, cache Gradle);
  3. Android SDK (`android-actions/setup-android@v3`);
  4. `chmod +x ./gradlew`;
  5. `./gradlew --no-daemon clean testDebugUnitTest lintDebug`;
  6. Upload artifacts (`actions/upload-artifact@v4`):
     - `**/build/reports/**`
     - `**/build/test-results/testDebugUnitTest/*.xml`

### 2) `espresso-smoke` — Espresso smoke tests

- Runner: `ubuntu-latest`.
- Зависит от `checks` (`needs: checks`).
- Шаги:
  1. Checkout;
  2. Включение KVM для аппаратно-ускоренного эмулятора;
  3. JDK 17;
  4. Android SDK;
  5. `chmod +x ./gradlew`;
  6. Запуск `reactivecircus/android-emulator-runner@v2` с параметрами:
     - `api-level: 34`
     - `arch: x86_64`
     - `profile: pixel_6`
     - `emulator-boot-timeout: 900`
     - `disable-animations: true`
     - script:
       `./gradlew --no-daemon :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.pavlovalexey.pavlovAlexeySandbox.ui.MainActivityEspressoTest`
  7. Upload artifacts:
     - `app/build/reports/androidTests/connected/**`
     - `app/build/outputs/androidTest-results/connected/**`

## Почему эмулятор не связан с Android Studio разработчика

- CI-эмулятор создаётся и запускается **временно на GitHub runner** во время job.
- Локальные AVD из Android Studio в этот процесс не участвуют.

## Как читать артефакты после падения

Скачайте артефакты из job:

- `android-reports` — unit/lint отчёты;
- `android-espresso-reports` — отчёты instrumented/UI тестов.

Ключевые отчёты:

- Unit HTML: `app/build/reports/tests/testDebugUnitTest/index.html`;
- Espresso HTML: `app/build/reports/androidTests/connected/index.html`.
