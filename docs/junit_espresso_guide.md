# QA Guide: JUnit + Espresso in PixelWanker

> Документ на практический стандарт команды dev по JUnit и Espresso для PixelWanker.

## 1. Текущие приоритетные зоны:

1. **ViewModel-логика** (состояния, эффекты, ветвления, pending-flow);
2. **Фильтрация/поиск** (корректность отбора данных);
3. **Форматтеры** (преобразование чисел, байтов, дат и т.д.);
4. **Мапперы** (UI ↔ domain/persistence);
5. **Prefs/хранилище настроек** (SharedPreferences, default/fallback behavior);
6. **UI smoke-checks через Espresso/Compose UI Test** (запуск экрана, навигация между вкладками, открытие/закрытие диалогов).

---

## 2. Где лежат тесты

- JUnit unit-тесты: `app/src/test/java/...`
- Espresso instrumented-тесты: `app/src/androidTest/java/...`

---

## 3. Инструменты и зависимости

В проекте используются:

- **JUnit 4** (unit-тесты);
- **kotlinx-coroutines-test** — контроль корутин и `Dispatchers.Main`;
- **AndroidX Test + Espresso + Compose UI Test** (instrumented UI-тесты);
  - `androidx.test.ext:junit`
  - `androidx.test:runner`
  - `androidx.test:rules`
  - `androidx.test.espresso:espresso-core`
  - `androidx.compose.ui:ui-test-junit4`
  - `androidx.compose.ui:ui-test-manifest` (debug)

Для Android-ресурсов в unit-тестах включено:

```kotlin
testOptions {
    unitTests {
        isIncludeAndroidResources = true
    }
}
```

---

## 4. Как работает Espresso в этом проекте

- Espresso-тесты запускаются **на устройстве или эмуляторе** (не на локальной JVM).
- Инструментальный раннер задаётся в `defaultConfig`:
  - `testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"`
- Базовый smoke/critical UI-тест: `MainActivityEspressoTest`.
  - Сценарий использует `createAndroidComposeRule<MainActivity>()`.
  - Проверяются ключевые пользовательские ветки: стартовый экран Grid, переходы по нижнему бару (Apps/About), и открытие/закрытие privacy policy диалога.
- Назначение smoke-теста:
  - быстро валидировать, что приложение стартует и UI-дерево поднимается корректно;
  - дать точку расширения для следующих UI-кейсов (клики, текст, навигация, проверки экранов).

---

## 5. Как запускать

### Unit (JUnit)

```bash
./gradlew :app:testDebugUnitTest

# запуск с итоговой QA-сводкой в конце
./gradlew :app:qaTestSummary
```

### UI instrumented (Espresso)

```bash
# все instrumented-тесты модуля app
./gradlew :app:connectedDebugAndroidTest

# только Espresso-класс
./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.pavlovalexey.pavlovAlexeySandbox.ui.MainActivityEspressoTest
```

> Важно: для `connectedDebugAndroidTest` должен быть поднят эмулятор или подключено Android-устройство (`adb devices`).

## Полезные варианты

```bash
# только один класс unit-тестов
./gradlew :app:testDebugUnitTest --tests "*InstalledAppsViewModelTest"

# один конкретный unit-тест
./gradlew :app:testDebugUnitTest --tests "*InstalledAppsViewModelTest.filters by app name and package ignoring case"

# подробный вывод
./gradlew :app:testDebugUnitTest --info
```

---

## 6. Где смотреть результаты

После запуска Gradle формирует отчёты:

### JUnit

- HTML: `app/build/reports/tests/testDebugUnitTest/index.html`
- XML (для CI): `app/build/test-results/testDebugUnitTest/`

### Espresso

- HTML: `app/build/reports/androidTests/connected/index.html`
- XML (для CI): `app/build/outputs/androidTest-results/connected/`

### Интерпретация статусов

- **PASSED** — тест успешно проверил ожидаемое поведение;
- **FAILED** — логика не соответствует ожиданию (либо ошибка в тесте);
- **SKIPPED/IGNORED** — тест пропущен, нужно проверить причину.

### Как читать падение

1. имя упавшего теста (должно описывать сценарий);
2. assertion message / expected vs actual;
3. stacktrace (первая строка в коде, а не в инфраструктуре);
4. не является ли тест flaky (зависимость от времени, порядка, внешней среды).

---

## 7. Стандарт написания тестов

## 7.1 Именование

Юзаем поведенческие имена:

- `fun \`returns all apps when query is blank with spaces\`()`
- `fun \`first launch dialog is shown once\`()`
- `fun launchMainActivity_displaysRootView()`

Формула: **что проверяется + при каком условии + ожидаемый результат**.

## 7.2 Структура AAA

Каждый тест должен быть в стиле **Arrange / Act / Assert**:

1. Arrange — подготовка состояния и входных данных;
2. Act — одно действие;
3. Assert — точные проверки результата.

## 7.3 Один тест — одна причина падения

Не объединяем независимые проверки в один тест. Меньше когнитивной нагрузки и проще дебаг.

## 7.4 Детерминированность

- не используем реальные network/диск/время;
- исключить зависимость от локали/часового пояса;
- для coroutines test-dispatcher и MainDispatcherRule;
- для Espresso избегаем жёстких `Thread.sleep`, используем механизмы синхронизации/IdlingResource при необходимости.

## 7.5 Минимально необходимый scope

Unit-тест проверяет бизнес-логику; UI-поведение — в instrumented/Espresso тестах.

---

## 8. Политика качества для PR

Минимум для PR с логикой:

1. добавлены/обновлены unit-тесты для изменённой логики;
2. если затронут UI, добавлены/обновлены Espresso-кейсы;
3. тесты воспроизводимо запускаются локально;
4. в PR описано:
    - что покрыто;
    - как запускали;
    - результат;
    - ограничения среды (если были).

---

## 9. Типовые причины нестабильности и как лечить

- **Проблемы с Dispatchers.Main** → использовать `MainDispatcherRule`;
- **Случайные тайминги** → избегать `delay` в тестах, использовать `advanceUntilIdle()`;
- **Android API в unit-тестах** → вынос логики в чистые функции/модели;
- **Порядок выполнения тестов** → каждый тест изолирован, без shared mutable state;
- **Падения Espresso из-за async/UI** → добавлять IdlingResource/ожидание состояния, а не sleep.

---

## 10. Быстрый QA-чеклист перед merge

- [ ] Тесты названы поведенчески и читаемо;
- [ ] Есть проверки на edge cases;
- [ ] Нет скрытых внешних зависимостей;
- [ ] Проверены success + error ветки (где применимо);
- [ ] UI-сценарии проверены Espresso (если затронут интерфейс);
- [ ] Результат прогона приложен в PR;
- [ ] При падении теста понятно, где и почему проблема.

---

## 11. Пример рабочего цикла

1. Изменили бизнес-логику/UI;
2. Добавили/обновили тесты в `app/src/test/java` и/или `app/src/androidTest/java`;
3. Запустили `./gradlew :app:testDebugUnitTest`;
4. Запустили `./gradlew :app:connectedDebugAndroidTest`;
5. Изучили HTML-отчёты при падениях;
6. Повторили до зелёного прогона;
7. Добавили в PR раздел `Testing` с командами и статусом.

## 12. Быстрая финальная QA-сводка

Реализация фичи вынесена из `app/build.gradle.kts` в отдельный build-script:
`gradle/qa-summary.gradle.kts` и подключается через `apply(from = ...)`.

Для запуска теста с короткой итоговой строкой используем:

```bash
./gradlew :app:qaTestSummary
```

После завершения задача печатает итог такого вида:

```
> Task :app:qaTestSummary
[QA][SUMMARY]: 100% of the tests were successfully completed
[QA][SUMMARY]: total=8, passed=8, failed=0, errors=0, skipped=0
```
