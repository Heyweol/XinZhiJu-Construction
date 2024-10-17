Set oShell = CreateObject("Wscript.Shell")
Dim strArgs
strArgs = "cmd /c " & WScript.Arguments(0)
oShell.Run strArgs, 0, false