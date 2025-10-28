# Run this script to make sprint dates nullable
# Usage: .\run-migration-nullable-dates.ps1

Write-Host "🔧 Running migration: Make sprint dates nullable..." -ForegroundColor Cyan

# Database connection details (modify as needed)
$dbHost = "localhost"
$dbPort = "3306"
$dbName = "ai_grow"
$dbUser = "root"
$dbPassword = "25112005"

# SQL to execute
$sql = @"
ALTER TABLE sprints 
    MODIFY COLUMN start_date DATE NULL,
    MODIFY COLUMN end_date DATE NULL;
"@

# Check if MySQL client is available
$mysqlPath = "mysql"
if (!(Get-Command mysql -ErrorAction SilentlyContinue)) {
    Write-Host "❌ MySQL client not found. Please install MySQL or add it to PATH." -ForegroundColor Red
    Write-Host "📝 Manual migration SQL:" -ForegroundColor Yellow
    Write-Host $sql -ForegroundColor Gray
    exit 1
}

# Execute migration
Write-Host "📊 Connecting to database: $dbName@$dbHost..." -ForegroundColor Yellow

try {
    $sql | mysql -h $dbHost -P $dbPort -u $dbUser -p$dbPassword $dbName
    Write-Host "✅ Migration completed successfully!" -ForegroundColor Green
    Write-Host "📝 Sprint start_date and end_date are now nullable." -ForegroundColor Green
    Write-Host "🔄 Please restart the backend server for changes to take effect." -ForegroundColor Cyan
} catch {
    Write-Host "❌ Migration failed: $_" -ForegroundColor Red
    Write-Host "📝 Please run the SQL manually:" -ForegroundColor Yellow
    Write-Host $sql -ForegroundColor Gray
}
