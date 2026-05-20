#Requires -Version 5.1
<#
.SYNOPSIS
  Convert touched source files to UTF-8 without BOM (fixes UTF-16 LE / UTF-8 BOM).

.DESCRIPTION
  Cursor agent `Write` on Windows has been observed to persist some files as UTF-16 LE
  (bytes like 70 00 61 00 for "pa"). This script rewrites only files that need fixing.

.PARAMETER Path
  One or more file paths to normalize.

.EXAMPLE
  .\scripts\ensure-utf8-no-bom.ps1 -Path 'back-end\sdp-admin\sdp-modules\sdp-module-system\src\main\java\org\nmgyj\system\entity\SysMenu.java'
#>
param(
    [Parameter(Mandatory = $true)]
    [string[]] $Path
)

$utf8NoBom = New-Object System.Text.UTF8Encoding $false

function Convert-OneFile {
    param([string] $FilePath)
    if (-not (Test-Path -LiteralPath $FilePath -PathType Leaf)) {
        Write-Warning "Skip (not a file): $FilePath"
        return
    }
    $b = [System.IO.File]::ReadAllBytes($FilePath)
    if ($b.Length -eq 0) { return }

    # UTF-8 with BOM -> strip BOM
    if ($b.Length -ge 3 -and $b[0] -eq 0xEF -and $b[1] -eq 0xBB -and $b[2] -eq 0xBF) {
        $text = [System.Text.Encoding]::UTF8.GetString($b, 3, $b.Length - 3)
        [System.IO.File]::WriteAllText($FilePath, $text, $utf8NoBom)
        Write-Host "UTF-8 BOM removed: $FilePath"
        return
    }

    # UTF-16 LE with BOM
    if ($b.Length -ge 2 -and $b[0] -eq 0xFF -and $b[1] -eq 0xFE) {
        $text = [System.Text.Encoding]::Unicode.GetString($b, 2, $b.Length - 2)
        [System.IO.File]::WriteAllText($FilePath, $text, $utf8NoBom)
        Write-Host "UTF-16 LE (BOM) -> UTF-8: $FilePath"
        return
    }

    # UTF-16 LE no BOM: markdown often starts with "#" (0x23 0x00); a newline right after breaks the ASCII rune heuristic below
    if ($b.Length -ge 4 -and $b[0] -eq 0x23 -and $b[1] -eq 0 -and $b[3] -eq 0) {
        $text = [System.Text.Encoding]::Unicode.GetString($b)
        [System.IO.File]::WriteAllText($FilePath, $text, $utf8NoBom)
        Write-Host "UTF-16 LE (markdown # heading) -> UTF-8: $FilePath"
        return
    }

    # UTF-16 LE no BOM: ASCII run with zero high bytes (e.g. Java "package" -> 70 00 61 00 ...)
    $looksUtf16Ascii = $b.Length -ge 8 -and $b[1] -eq 0 -and $b[3] -eq 0 -and $b[5] -eq 0 `
        -and $b[0] -ge 32 -and $b[0] -lt 127 -and $b[2] -ge 32 -and $b[2] -lt 127 -and $b[4] -ge 32 -and $b[4] -lt 127
    if ($looksUtf16Ascii) {
        $text = [System.Text.Encoding]::Unicode.GetString($b)
        [System.IO.File]::WriteAllText($FilePath, $text, $utf8NoBom)
        Write-Host "UTF-16 LE (no BOM) -> UTF-8: $FilePath"
        return
    }

    # UTF-16 BE with BOM FE FF
    if ($b.Length -ge 2 -and $b[0] -eq 0xFE -and $b[1] -eq 0xFF) {
        $text = [System.Text.Encoding]::BigEndianUnicode.GetString($b, 2, $b.Length - 2)
        [System.IO.File]::WriteAllText($FilePath, $text, $utf8NoBom)
        Write-Host "UTF-16 BE (BOM) -> UTF-8: $FilePath"
        return
    }
}

foreach ($p in $Path) {
    Convert-OneFile -FilePath $p
}
