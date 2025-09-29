# PowerShell script to test Workspace API
# Make sure to update the variables below before running

$baseUrl = "http://localhost:8088"
$jwtToken = "YOUR_JWT_TOKEN_HERE"  # Replace with your actual JWT token
$companyId = "your-company-id-here"  # Replace with actual company ID

$headers = @{
    "Content-Type" = "application/json"
    "Authorization" = "Bearer $jwtToken"
}

Write-Host "Testing Workspace API..." -ForegroundColor Green

# 1. Create Workspace
Write-Host "`n1. Creating workspace..." -ForegroundColor Yellow
$createBody = @{
    companyId = $companyId
    name = "Test Workspace PowerShell"
    description = "Created via PowerShell script"
} | ConvertTo-Json

try {
    $createResponse = Invoke-RestMethod -Uri "$baseUrl/api/workspaces" -Method POST -Headers $headers -Body $createBody
    $workspaceId = $createResponse.id
    Write-Host "✅ Workspace created successfully!" -ForegroundColor Green
    Write-Host "Workspace ID: $workspaceId" -ForegroundColor Cyan
    Write-Host "Response: $($createResponse | ConvertTo-Json -Depth 3)" -ForegroundColor Gray
} catch {
    Write-Host "❌ Failed to create workspace: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# 2. Get Workspace by ID
Write-Host "`n2. Getting workspace by ID..." -ForegroundColor Yellow
try {
    $getResponse = Invoke-RestMethod -Uri "$baseUrl/api/workspaces/$workspaceId" -Method GET -Headers $headers
    Write-Host "✅ Workspace retrieved successfully!" -ForegroundColor Green
    Write-Host "Response: $($getResponse | ConvertTo-Json -Depth 3)" -ForegroundColor Gray
} catch {
    Write-Host "❌ Failed to get workspace: $($_.Exception.Message)" -ForegroundColor Red
}

# 3. Update Workspace
Write-Host "`n3. Updating workspace..." -ForegroundColor Yellow
$updateBody = @{
    name = "Updated Test Workspace PowerShell"
    description = "Updated via PowerShell script"
} | ConvertTo-Json

try {
    $updateResponse = Invoke-RestMethod -Uri "$baseUrl/api/workspaces/$workspaceId" -Method PUT -Headers $headers -Body $updateBody
    Write-Host "✅ Workspace updated successfully!" -ForegroundColor Green
    Write-Host "Response: $($updateResponse | ConvertTo-Json -Depth 3)" -ForegroundColor Gray
} catch {
    Write-Host "❌ Failed to update workspace: $($_.Exception.Message)" -ForegroundColor Red
}

# 4. Get Workspaces by Company
Write-Host "`n4. Getting workspaces by company..." -ForegroundColor Yellow
try {
    $listResponse = Invoke-RestMethod -Uri "$baseUrl/api/workspaces/company/$companyId?page=0&size=10" -Method GET -Headers $headers
    Write-Host "✅ Workspaces retrieved successfully!" -ForegroundColor Green
    Write-Host "Total elements: $($listResponse.totalElements)" -ForegroundColor Cyan
    Write-Host "Response: $($listResponse | ConvertTo-Json -Depth 3)" -ForegroundColor Gray
} catch {
    Write-Host "❌ Failed to get workspaces: $($_.Exception.Message)" -ForegroundColor Red
}

# 5. Search Workspaces
Write-Host "`n5. Searching workspaces..." -ForegroundColor Yellow
try {
    $searchResponse = Invoke-RestMethod -Uri "$baseUrl/api/workspaces/company/$companyId?q=Test&page=0&size=10" -Method GET -Headers $headers
    Write-Host "✅ Search completed successfully!" -ForegroundColor Green
    Write-Host "Found: $($searchResponse.totalElements) workspaces" -ForegroundColor Cyan
    Write-Host "Response: $($searchResponse | ConvertTo-Json -Depth 3)" -ForegroundColor Gray
} catch {
    Write-Host "❌ Failed to search workspaces: $($_.Exception.Message)" -ForegroundColor Red
}

# 6. Count Workspaces
Write-Host "`n6. Counting workspaces..." -ForegroundColor Yellow
try {
    $countResponse = Invoke-RestMethod -Uri "$baseUrl/api/workspaces/company/$companyId/count" -Method GET -Headers $headers
    Write-Host "✅ Count retrieved successfully!" -ForegroundColor Green
    Write-Host "Total workspaces: $countResponse" -ForegroundColor Cyan
} catch {
    Write-Host "❌ Failed to count workspaces: $($_.Exception.Message)" -ForegroundColor Red
}

# 7. Delete Workspace (Soft Delete)
Write-Host "`n7. Deleting workspace (soft delete)..." -ForegroundColor Yellow
try {
    Invoke-RestMethod -Uri "$baseUrl/api/workspaces/$workspaceId" -Method DELETE -Headers $headers
    Write-Host "✅ Workspace deleted successfully!" -ForegroundColor Green
} catch {
    Write-Host "❌ Failed to delete workspace: $($_.Exception.Message)" -ForegroundColor Red
}

# 8. Restore Workspace
Write-Host "`n8. Restoring workspace..." -ForegroundColor Yellow
try {
    $restoreResponse = Invoke-RestMethod -Uri "$baseUrl/api/workspaces/$workspaceId/restore" -Method PATCH -Headers $headers
    Write-Host "✅ Workspace restored successfully!" -ForegroundColor Green
    Write-Host "Response: $($restoreResponse | ConvertTo-Json -Depth 3)" -ForegroundColor Gray
} catch {
    Write-Host "❌ Failed to restore workspace: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n🎉 All tests completed!" -ForegroundColor Green