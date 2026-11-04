# Демонстрация Android-лабораторных без Android Studio

Работа выполняется в Zed: редактирование — в редакторе, сборка и запуск — из встроенного терминала PowerShell. Android Studio открывать не нужно.

> Ниже `<корень проекта>` — папка `ЛР_Мобильное_программирование`.

## Первый запуск на этом компьютере

Этот раздел выполняется **один раз**. Откройте терминал PowerShell в корне проекта и выполните:

```powershell
powershell -ExecutionPolicy Bypass -File .\setup-android.ps1
```

Скрипт подготовит локальную JDK 17 в папке `_android-tools`. Системная Java не меняется.

Проверить, что ADB доступен:

```powershell
adb devices
```

Если команда выводит заголовок `List of devices attached`, ADB работает. Устройство появится после запуска эмулятора.

## Если всё-таки открываешь Android Studio

Проект рассчитан на Gradle 8.10.2 и AGP 8.8.2. Android Studio 2026.1.4 запускает Gradle на своей встроенной Java 25, а Gradle 8.10.2 поддерживает Java только до 23. Поэтому синхронизация падает с ошибкой «Incompatible Gradle JVM version». Лечится без интернета:

1. `File → Settings → Build, Execution, Deployment → Build Tools → Gradle`.
2. В поле **Gradle JDK** нажмите `Add JDK…` и укажите папку `<корень проекта>\_android-tools\jdk-17` (ту, где лежит `bin`).
3. Выполните `Sync Project with Gradle Files`.

Важно: открывайте в Android Studio папку конкретной работы (`ЛР4`, `ЛР5`, `ЛР6`), а не корень `ЛР_Мобильное_программирование` — в корне нет `settings.gradle.kts`, и Android Studio покажет пустой проект без конфигураций запуска.

Не нажимайте «Upgrade Gradle wrapper»: Java 25 поддерживается только с Gradle 9.1, а он несовместим с AGP 8.8.2; офлайн апгрейд всё равно не скачается.

### Офлайн

Дистрибутив Gradle 8.10.2 и зависимости уже лежат в `~/.gradle` после первой успешной сборки. На машине без интернета сборка работает, пока этот кэш и `_android-tools` на месте. Если кэша нет, `setup-android.ps1` и Gradle wrapper попытаются скачать JDK и Gradle и без сети упадут.

Путь к папке содержит кириллицу, поэтому в `gradle.properties` стоит `android.overridePathCheck=true` (experimental). Не переименовывайте промежуточные папки; для переноса на другую машину лучше выбрать путь без кириллицы, например `C:\dev\mobile-labs`.

## Как запустить эмулятор перед показом лабораторной

### 1. Откройте терминал в корне проекта

В Zed откройте встроенный терминал. Его рабочая папка должна быть `ЛР_Мобильное_программирование`.

### 2. Запустите эмулятор

```powershell
powershell -ExecutionPolicy Bypass -File .\start-emulator.ps1
```

Скрипт запускает AVD `LR6_Pixel_6` и ожидает, пока эмулятор станет доступен для ADB.

Если нужно вывести список доступных виртуальных устройств:

```powershell
powershell -ExecutionPolicy Bypass -File .\start-emulator.ps1 -List
```

### 3. Дождитесь полной загрузки Android

На экране эмулятора должен появиться рабочий стол Android. Затем проверьте подключение:

```powershell
adb devices
```

Нормальный результат:

```text
List of devices attached
emulator-5554    device
```

Важно: статус должен быть именно `device`, а не `offline` или `unauthorized`.

## Как собрать, установить и запустить лабораторную

То же самое можно сделать из корня проекта одной командой, без перехода в папку:

```powershell
powershell -ExecutionPolicy Bypass -File .\run-android.ps1 -Lab 4   # или -Lab 5, -Lab 6
```

Дальше — то же в виде `run.ps1` внутри папки лабораторной.

### ЛР4

```powershell
cd .\ЛР4
powershell -ExecutionPolicy Bypass -File .\run.ps1
```

### ЛР5

```powershell
cd .\ЛР5
powershell -ExecutionPolicy Bypass -File .\run.ps1
```

### ЛР6

```powershell
cd .\ЛР6
powershell -ExecutionPolicy Bypass -File .\run.ps1
```

`run.ps1` автоматически выполняет три действия:

1. собирает debug-версию приложения;
2. устанавливает APK на запущенный эмулятор;
3. запускает главную Activity приложения.

Успешный запуск заканчивается строкой вида:

```text
Started ru.tulsu.mobileprogramming.<имя приложения>/.MainActivity
```

После этого приложение открыто в окне эмулятора — его можно демонстрировать преподавателю.

## Обычный порядок работы

При каждой правке кода:

1. Отредактируйте Kotlin/XML-файлы в Zed.
2. Убедитесь, что эмулятор запущен и `adb devices` показывает статус `device`.
3. Войдите в папку нужной лабораторной:

   ```powershell
   cd .\ЛР4   # или .\ЛР5, .\ЛР6
   ```

4. Запустите:

   ```powershell
   powershell -ExecutionPolicy Bypass -File .\run.ps1
   ```

Переустанавливать приложение вручную не нужно: повторный запуск `run.ps1` обновит его на эмуляторе.

## Полезные команды

### Только собрать APK, не устанавливая на эмулятор

Из папки конкретной ЛР:

```powershell
powershell -ExecutionPolicy Bypass -File .\run.ps1 -BuildOnly
```

Готовый файл будет здесь:

```text
app\build\outputs\apk\debug\app-debug.apk
```

### Полная очистка и пересборка

Если Gradle ведёт себя странно или после серьёзных изменений:

```powershell
powershell -ExecutionPolicy Bypass -File .\run.ps1 -Clean
```

### Запустить уже установленное приложение без пересборки

Используйте, если APK уже установлен и надо лишь быстро открыть его перед показом.

**ЛР4:**

```powershell
adb shell am start -n ru.tulsu.mobileprogramming.businesscard/.MainActivity
```

**ЛР5:**

```powershell
adb shell am start -n ru.tulsu.mobileprogramming.mooddialog/.MainActivity
```

**ЛР6:**

```powershell
adb shell am start -n ru.tulsu.mobileprogramming.imagecounter/.MainActivity
```

## Если приложение не устанавливается или не запускается

### 1. Проверьте эмулятор

```powershell
adb devices
```

Если нет строки со статусом `device`, закройте окно эмулятора и снова выполните из корня проекта:

```powershell
powershell -ExecutionPolicy Bypass -File .\start-emulator.ps1
```

### 2. Перезапустите ADB

```powershell
adb kill-server
adb start-server
adb devices
```

После появления `emulator-5554    device` снова запустите `run.ps1` из папки нужной ЛР.

### 3. Если установка зависает на `Performing Push Install`

Закройте эмулятор, затем последовательно выполните в корне проекта:

```powershell
adb kill-server
adb start-server
powershell -ExecutionPolicy Bypass -File .\start-emulator.ps1
```

Дождитесь рабочего стола Android и строки `emulator-5554    device` в `adb devices`. После этого повторите запуск лабораторной.

### 4. Если установка падает с `INSTALL_FAILED_VERIFICATION_FAILURE: Integrity verification timed out`

Так ведёт себя проверка приложений Google Play в образе эмулятора. Отключите её один раз:

```powershell
adb shell settings put global verifier_verify_adb_installs 0
adb shell settings put global package_verifier_enable 0
```

и повторите запуск лабораторной. Настройка сохраняется в образе эмулятора, поэтому после перезапуска AVD повторять её не нужно.

### 5. Если после перезапуска эмулятор долго висит на заставке

Признак: `adb devices` показывает `device`, но `adb shell getprop sys.boot_completed` пуст, установка падает с `NullPointerException ... StorageManagerService`, а `adb shell pm list packages` отвечает `Can't find service: package`. Дождитесь окончания загрузки (первый старт после сброса данных занимает несколько минут), при необходимости перезапустите AVD:

```powershell
adb emu kill
powershell -ExecutionPolicy Bypass -File .\start-emulator.ps1
```

Если после перезапуска зависание повторяется, верните образ к заводскому состоянию: удалите в папке `%USERPROFILE%\.android\avd\LR6_Pixel_6.avd` файлы `*.qcow2` и каталог `snapshots`, затем запустите эмулятор напрямую с отключёнными снимками состояния:

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\emulator\emulator.exe" -avd LR6_Pixel_6 -no-snapshot-load -no-snapshot-save
```

При этом удаляются установленные приложения — APK придётся поставить заново через `run.ps1`. Данные проекта на диске не затрагиваются.

## Кратко: что делать на защите

```powershell
# 1. В корне проекта запустить эмулятор
powershell -ExecutionPolicy Bypass -File .\start-emulator.ps1

# 2. Убедиться, что Android виден
adb devices

# 3. Перейти в нужную лабораторную и запустить её
cd .\ЛР6
powershell -ExecutionPolicy Bypass -File .\run.ps1
```

Для другой лабораторной замените `ЛР6` на `ЛР4` или `ЛР5`.
