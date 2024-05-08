Set WshShell = CreateObject("WScript.Shell")
WshShell.Run "cmd /c java -jar AddressManagement-1.0-SNAPSHOT.jar & exit", 0, False
