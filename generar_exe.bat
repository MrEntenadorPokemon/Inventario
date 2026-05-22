@echo off
echo ===================================================
echo Generador de Ejecutable - Sistema de Inventario
echo ===================================================

set JAVA_HOME=C:\Users\PoksPC\.jdks\openjdk-26.0.1
set MVN_BIN="C:\Program Files\JetBrains\IntelliJ IDEA 2026.1.2\plugins\maven\lib\maven3\bin\mvn.cmd"
set JPACKAGE="C:\Users\PoksPC\.jdks\openjdk-26.0.1\bin\jpackage.exe"

echo [1/3] Compilando y generando el archivo JAR...
call %MVN_BIN% clean package -DskipTests

if %errorlevel% neq 0 (
    echo [ERROR] Hubo un error al compilar el proyecto.
    pause
    exit /b
)

echo [2/3] Generando el archivo .exe (App Image)...
for %%f in (target\Proyecto_Inventario-*-shaded.jar) do set MAIN_JAR=%%f

%JPACKAGE% ^
  --input target/ ^
  --name "InventarioUnison" ^
  --main-jar Proyecto_Inventario-1.0-SNAPSHOT.jar ^
  --main-class mx.unison.app.Main ^
  --type app-image ^
  --dest dist/ ^
  --vendor "Universidad de Sonora" ^
  --description "Sistema Basico de Inventario" ^
  --app-version "1.0.0"

copy InventarioBD.db dist\InventarioUnison\ >nul

echo [3/3] ¡Proceso completado!
echo La aplicacion portable se encuentra en: dist\InventarioUnison\
echo Ejecute InventarioUnison.exe para iniciar.
pause

