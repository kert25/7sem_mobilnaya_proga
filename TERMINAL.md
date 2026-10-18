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
