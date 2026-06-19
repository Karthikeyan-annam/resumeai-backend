# ResumeIQ AI Backend Runner Script
# This script downloads a portable Maven instance and runs the Spring Boot server.

$ErrorActionPreference = "Stop"

# 1. Download Maven if not present
if (-not (Test-Path "apache-maven-3.9.6")) {
    Write-Host "--------------------------------------------------" -ForegroundColor Cyan
    Write-Host "Portable Maven not found. Downloading Apache Maven 3.9.6..." -ForegroundColor Cyan
    Write-Host "--------------------------------------------------" -ForegroundColor Cyan
    
    $url = "https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip"
    $output = "maven.zip"
    
    Invoke-WebRequest -Uri $url -OutFile $output
    
    Write-Host "Extracting Maven archive..." -ForegroundColor Cyan
    Expand-Archive -Path $output -DestinationPath "."
    Remove-Item $output
    
    Write-Host "Maven downloaded and ready." -ForegroundColor Green
}

# 2. Check and load environment variables from .env
if (Test-Path ".env") {
    Write-Host "--------------------------------------------------" -ForegroundColor Cyan
    Write-Host "Loading environment variables from local .env file..." -ForegroundColor Cyan
    Write-Host "--------------------------------------------------" -ForegroundColor Cyan
    Get-Content ".env" | ForEach-Object {
        $line = $_.Trim()
        if ($line -and -not $line.StartsWith("#")) {
            $pos = $line.IndexOf("=")
            if ($pos -gt 0) {
                $name = $line.Substring(0, $pos).Trim()
                $val = $line.Substring($pos + 1).Trim()
                # Remove optional surrounding quotes
                if (($val.StartsWith("'") -and $val.EndsWith("'")) -or ($val.StartsWith("`"") -and $val.EndsWith("`""))) {
                    $val = $val.Substring(1, $val.Length - 2)
                }
                [System.Environment]::SetEnvironmentVariable($name, $val, [System.EnvironmentVariableTarget]::Process)
            }
        }
    }
} else {
    Write-Host "--------------------------------------------------" -ForegroundColor Yellow
    Write-Host "Local .env file not found. Creating default .env..." -ForegroundColor Yellow
    Write-Host "--------------------------------------------------" -ForegroundColor Yellow
    
    $defaultEnv = @"
# ========================================================
# ResumeIQ AI Environment Variables (Local Development Only)
# ========================================================
SPRING_DATASOURCE_URL=jdbc:postgresql://db.mmpiztsgujwlkursybri.supabase.co:5432/postgres
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=H2e:y_A2Gqh8dEV
JWT_SECRET=4b1fba8501a90c79ce0369c210e37cda7f1974b6c893f684caebf56546b065a1
GEMINI_API_KEY=YOUR_GEMINI_API_KEY_HERE
"@
    $defaultEnv | Out-File -FilePath ".env" -Encoding utf8
    
    # Load the variables
    $defaultEnv -split "`r?`n" | ForEach-Object {
        $line = $_.Trim()
        if ($line -and -not $line.StartsWith("#")) {
            $pos = $line.IndexOf("=")
            if ($pos -gt 0) {
                $name = $line.Substring(0, $pos).Trim()
                $val = $line.Substring($pos + 1).Trim()
                [System.Environment]::SetEnvironmentVariable($name, $val, [System.EnvironmentVariableTarget]::Process)
            }
        }
    }
    Write-Host "Local .env file generated successfully and loaded." -ForegroundColor Green
}

# 3. Launch Spring Boot application

Write-Host "--------------------------------------------------" -ForegroundColor Green
Write-Host "Launching ResumeIQ AI Spring Boot Backend..." -ForegroundColor Green
Write-Host "--------------------------------------------------" -ForegroundColor Green

.\apache-maven-3.9.6\bin\mvn.cmd clean spring-boot:run
