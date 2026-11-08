# Лабораторные работы — Мобильное программирование

Один Gradle-проект на все лабораторные. Android Studio открывает **корень этой
папки**, каждая лабораторная — отдельный модуль со своей конфигурацией запуска и отладки.
Раньше каждая Android-лаба была отдельным проектом, а консольные ЛР1–ЛР3 вообще не
собирались в IDE — теперь всё в одном месте.

## Первый запуск на этом компьютере

```powershell
powershell -ExecutionPolicy Bypass -File .\setup-android.ps1
```

Скрипт проверяет (и при необходимости скачивает) JDK 17 в `%LOCALAPPDATA%\MobileLabs\jdk-17`
и записывает два локальных файла:

* `.gradle/config.properties` — отсюда Android Studio берёт JDK для Gradle;
* `local.properties` — путь к Android SDK.

Оба файла локальные и в git не попадают.

Дальше — Android Studio: **File → Open** → выбрать корень этой папки (не `ЛР4`, не `ЛР6`),
дождаться окончания `Gradle sync`.

## Запуск и отладка в Android Studio

Справа от кнопки Run в выпадающем списке уже есть готовые конфигурации:

| Конфигурация          | Что делает                                  |
| --------------------- | ------------------------------------------- |
| `ЛР1 — основной`      | запускает `ЛР1/Main.kt`                     |
| `ЛР1 — доп. задание`  | запускает `ЛР1/AdditionalTask.kt`           |
| `ЛР2 — основной`      | запускает `ЛР2/Main.kt`                     |
| `ЛР2 — доп. задание`  | запускает `ЛР2/AdditionalTask.kt`           |
| `ЛР3 — основной`      | запускает `ЛР3/Main.kt`                     |
| `ЛР3 — доп. задание`  | запускает `ЛР3/AdditionalTask.kt`           |
| `ЛР4 BusinessCard`    | собирает, ставит и запускает приложение ЛР4 |
| `ЛР5 MoodDialog`      | собирает, ставит и запускает приложение ЛР5 |
| `ЛР6 ImageCounter`    | собирает, ставит и запускает приложение ЛР6 |
| `ЛР7 FontSize`        | собирает, ставит и запускает приложение ЛР7 |

* **Run** (Shift+F10) — запуск, **Debug** (Shift+F9) — запуск с отладчиком.
* Точка останова ставится кликом слева от номера строки в `MainActivity.kt`, затем Debug.
* Для консольных ЛР1–ЛР3 вывод идёт в окно **Run**; отладка — обычные точки останова.
* Перед запуском Android-лабы нужен эмулятор или телефон со включённой отладкой
  (см. «Эмулятор» ниже).

Конфигурации лежат в `.idea/runConfigurations` и хранятся в git, поэтому правки в
`Build → Edit Configurations…` стоит сохранять (они попадут в тот же каталог).

## Модули и где лежат исходники

Исходники лабораторных **не переносились**: файлы остались в папках `ЛР1…ЛР7`.

| Лабораторная | Модуль Gradle | Исходники                        |
| ------------ | ------------- | -------------------------------- |
| ЛР1          | `:lr1:main`, `:lr1:additional`       | `ЛР1/Main.kt`, `ЛР1/AdditionalTask.kt` |
| ЛР2          | `:lr2:main`, `:lr2:additional`       | `ЛР2/…`                          |
| ЛР3          | `:lr3:main`, `:lr3:additional`       | `ЛР3/…`                          |
| ЛР4          | `:lr4:app`    | `ЛР4/app/src/main/…`             |
| ЛР5          | `:lr5:app`    | `ЛР5/app/src/main/…`             |
| ЛР6          | `:lr6:app`    | `ЛР6/app/src/main/…`             |
| ЛР7          | `:lr7:app`    | `ЛР7/app/src/main/…`             |

Каталог `labs/` содержит только короткие `build.gradle.kts`, которые подключают
исходники из папок лабораторных. В ЛР1–ЛР3 в каждом файле свой `fun main()`, поэтому
один файл — один модуль: иначе компилятор видит конфликт перегрузок.

## Командная строка

Если нужно собрать и запустить без IDE:

```powershell
powershell -ExecutionPolicy Bypass -File .\run-android.ps1 -Lab 7
```

* `-Lab 1..7` — номер лабораторной; для 1–3 программа запускается в консоли,
  для 4–7 APK собирается, ставится на устройство и открывается;
* `-Additional` — для ЛР1–ЛР3 запустить дополнительное задание;
* `-BuildOnly` — только собрать, без установки;
* `-Clean` — предварительно выполнить `clean`.

Эмулятор:

```powershell
powershell -ExecutionPolicy Bypass -File .\start-emulator.ps1        # запуск AVD LR6_Pixel_6
powershell -ExecutionPolicy Bypass -File .\start-emulator.ps1 -List  # список AVD
```

Тот же запуск напрямую через Gradle:

```powershell
.\gradlew.bat :lr6:app:assembleDebug
.\gradlew.bat :lr1:main:run
```

## Почему Gradle работает на JDK 17

Проект собран на Gradle 8.10.2 и Android Gradle Plugin 8.8.2 — это проверенная связка,
на ней проходят сборки всех трёх приложений. Gradle 8.10.2 поддерживает Java до 23,
а в Android Studio 2026.1 встроена JDK 25. При синхронизации на ней появляется ошибка
«Incompatible Gradle JVM version», поэтому Gradle запускается на отдельной JDK 17.

Это сделано без ручных настроек IDE:

* в `.idea/gradle.xml` у проекта выставлено `Gradle JDK = #GRADLE_LOCAL_JAVA_HOME`;
* в `.gradle/config.properties` записано `java.home=…\MobileLabs\jdk-17`
  (файл создаёт `setup-android.ps1`, в git он не попадает);
* JDK лежит вне папки проекта — в пути нет кириллицы, и этот путь понимают
  и командная строка, и IDE.

Не нажимайте «Upgrade Gradle wrapper»: Java 25 поддерживается только с Gradle 9.1,
а он несовместим с AGP 8.8.2; офлайн-обновление всё равно не скачается.

Путь к папке проекта содержит кириллицу, поэтому в `gradle.properties` стоит
`android.overridePathCheck=true`. Без этой строки AGP отказывается собирать проект.

## Эмулятор

```powershell
adb devices
```

Нормальный результат — строка со статусом `device`:

```text
List of devices attached
emulator-5554    device
```

`start-emulator.ps1` сам ждёт не только появления устройства, но и завершения загрузки
Android (`sys.boot_completed`), поэтому установка APK не падает на «полузагруженном»
эмуляторе.

## Если что-то не работает

### Android Studio пишет «Incompatible Gradle JVM version»

Повторно выполните `.\setup-android.ps1`: он перезапишет `.gradle/config.properties`.
Проверить, что взят нужный JDK, можно в `Build → Execution, Deployment → Build Tools →
Gradle → Gradle JDK` — должен быть выбран `GRADLE_LOCAL_JAVA_HOME`, а не `jbr-25`.
В `idea.log` строка `Instructing gradle to use java from …\MobileLabs\jdk-17`
подтверждает, что всё сработало.

### Синхронизация падает с «Your project path contains non-ASCII characters»

Проверьте `android.overridePathCheck=true` в `gradle.properties` в корне проекта.

### При установке: `INSTALL_FAILED_VERIFICATION_FAILURE: Integrity verification timed out`

Так ведёт себя проверка приложений Google Play в образе эмулятора. Отключите её один раз:

```powershell
adb shell settings put global verifier_verify_adb_installs 0
adb shell settings put global package_verifier_enable 0
```

### Установка зависает на `Performing Push Install`

```powershell
adb kill-server
adb start-server
powershell -ExecutionPolicy Bypass -File .\start-emulator.ps1
```

### Эмулятор тормозит, появляются «System UI isn't responding» / «Digital Wellbeing isn't responding»

Это не ошибка приложения: эмулятору не хватает CPU, когда одновременно идёт
индексация в Android Studio и сборка. Помогает закрыть лишние тяжёлые окна,
дождаться окончания индексации и отключить фоновое приложение:

```powershell
adb shell pm disable-user --user 0 com.google.android.apps.wellbeing
```

### Эмулятор долго висит на заставке

`adb devices` показывает `device`, но `adb shell getprop sys.boot_completed` пуст.
Дождитесь окончания загрузки (первый старт бывает несколько минут) или перезапустите AVD:

```powershell
adb emu kill
powershell -ExecutionPolicy Bypass -File .\start-emulator.ps1
```

Если зависание повторяется, верните образ к заводскому состоянию: удалите в
`%USERPROFILE%\.android\avd\LR6_Pixel_6.avd` файлы `*.qcow2` и каталог `snapshots`,
затем запустите эмулятор с отключёнными снимками:

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\emulator\emulator.exe" -avd LR6_Pixel_6 -no-snapshot-load -no-snapshot-save
```

### Работа без интернета

Дистрибутив Gradle 8.10.2 и зависимости уже лежат в `~/.gradle` после первой успешной
сборки, JDK 17 — в `%LOCALAPPDATA%\MobileLabs`. Пока этот кеш на месте, сборка идёт без
сети. Если кеша нет, `setup-android.ps1` и Gradle wrapper попробуют скачать JDK и Gradle
и без сети упадут.

## Что изменилось по сравнению с прежней схемой

* три отдельных Android-проекта заменены одним корневым проектом `MobileLabs`;
* ЛР1–ЛР3 (консольный Kotlin) подключены как модули и теперь запускаются и отлаживаются
  в Android Studio; их исходники не менялись;
* JDK 17 перенесена из `_android-tools` в `%LOCALAPPDATA%\MobileLabs\jdk-17` — в пути нет
  кириллицы, и он же прописан для IDE;
* из папок лабораторных удалены дубли `settings.gradle.kts`, `build.gradle.kts`,
  `gradlew.bat`, `gradle/`, `gradle.properties`, `run.ps1`, `.gradle`, `.kotlin`, `.idea` —
  единственный источник правды теперь в корне;
* из `AndroidManifest.xml` убран устаревший атрибут `package` (AGP 8 его игнорирует,
  пространство имён задаётся в `build.gradle.kts`);
* `.idea` корня пересобран (раньше там был битый Java-проект с повреждёнными именами
  и без Android-конфигураций) и добавлен в git;
* скрипты сохранены в UTF-8 с BOM — Windows PowerShell иначе читает кириллицу как ANSI;
* `start-emulator.ps1` ждёт полной загрузки Android, `run-android.ps1` работает из корня
  для всех семи лабораторных.

## Структура проекта

```text
.
├─ settings.gradle.kts         единый список модулей
├─ build.gradle.kts            версии AGP и Kotlin-плагинов
├─ gradle.properties           параметры сборки (JDK, кириллица в пути)
├─ gradlew.bat                 Gradle wrapper 8.10.2
├─ setup-android.ps1           локальная JDK 17 и local.properties
├─ run-android.ps1             сборка/запуск любой лабораторной
├─ start-emulator.ps1          запуск AVD с ожиданием загрузки
├─ _tools/AndroidEnv.ps1       общие функции скриптов
├─ labs/lrN/…/build.gradle.kts модули консольных ЛР1–ЛР3
├─ ЛР1 … ЛР7                   исходники, отчёты, PDF, скриншоты
└─ .idea                       настройки проекта и конфигурации запуска
```
