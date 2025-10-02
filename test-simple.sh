#!/bin/bash

# Simple bash script to test Project Management API
BASE_URL="http://localhost:8080"
WORKSPACE_ID="test-workspace-001"

echo "🚀 Testing Project Management API"
echo "=================================="

# Check if server is running
echo "📡 Checking if server is running..."
if curl -s "$BASE_URL/api/health" > /dev/null 2>&1; then
    echo "✅ Server is running"
else
    echo "❌ Server is not running or health endpoint not available"
    echo "Please start your Spring Boot application first"
    exit 1
fi

# Test 1: Create Project
echo ""
echo "📝 Test 1: Creating a project..."
PROJECT_DATA='{
  "workspaceId": "'$WORKSPACE_ID'",
  "name": "Test Project '$(date +%H%M%S)'",
  "description": "Test project created via curl",
  "status": "PLANNING",
  "priority": "HIGH",
  "startDate": "2024-01-01",
  "endDate": "2024-12-31",
  "color": "#FF5733"
}'

RESPONSE=$(curl -s -X POST "$BASE_URL/api/projects" \
  -H "Content-Type: application/json" \
  -d "$PROJECT_DATA")

if echo "$RESPONSE" | grep -q '"id"'; then
    PROJECT_ID=$(echo "$RESPONSE" | grep -o '"id":"[^"]*' | cut -d'"' -f4)
    echo "✅ Project created successfully with ID: $PROJECT_ID"
else
    echo "❌ Failed to create project"
    echo "Response: $RESPONSE"
    exit 1
fi

# Test 2: Get Project
echo ""
echo "🔍 Test 2: Getting project by ID..."
GET_RESPONSE=$(curl -s "$BASE_URL/api/projects/$PROJECT_ID")

if echo "$GET_RESPONSE" | grep -q '"name"'; then
    PROJECT_NAME=$(echo "$GET_RESPONSE" | grep -o '"name":"[^"]*' | cut -d'"' -f4)
    echo "✅ Project retrieved: $PROJECT_NAME"
else
    echo "❌ Failed to get project"
    echo "Response: $GET_RESPONSE"
fi

# Test 3: Update Project
echo ""
echo "✏️ Test 3: Updating project..."
UPDATE_DATA='{
  "name": "Updated Test Project '$(date +%H%M%S)'",
  "status": "ACTIVE",
  "priority": "MEDIUM"
}'

UPDATE_RESPONSE=$(curl -s -X PUT "$BASE_URL/api/projects/$PROJECT_ID" \
  -H "Content-Type: application/json" \
  -d "$UPDATE_DATA")

if echo "$UPDATE_RESPONSE" | grep -q '"status":"ACTIVE"'; then
    echo "✅ Project updated successfully"
else
    echo "❌ Failed to update project"
    echo "Response: $UPDATE_RESPONSE"
fi

# Test 4: Get Projects by Workspace
echo ""
echo "📋 Test 4: Getting projects by workspace..."
WORKSPACE_RESPONSE=$(curl -s "$BASE_URL/api/projects/workspace/$WORKSPACE_ID")

if echo "$WORKSPACE_RESPONSE" | grep -q '"content"'; then
    TOTAL_ELEMENTS=$(echo "$WORKSPACE_RESPONSE" | grep -o '"totalElements":[0-9]*' | cut -d':' -f2)
    echo "✅ Workspace projects retrieved. Total: $TOTAL_ELEMENTS"
else
    echo "❌ Failed to get workspace projects"
    echo "Response: $WORKSPACE_RESPONSE"
fi

# Test 5: Search Projects
echo ""
echo "🔎 Test 5: Searching projects..."
SEARCH_RESPONSE=$(curl -s "$BASE_URL/api/projects/workspace/$WORKSPACE_ID?q=Test")

if echo "$SEARCH_RESPONSE" | grep -q '"content"'; then
    echo "✅ Project search completed"
else
    echo "❌ Failed to search projects"
    echo "Response: $SEARCH_RESPONSE"
fi

# Test 6: Archive Project
echo ""
echo "📦 Test 6: Archiving project..."
ARCHIVE_RESPONSE=$(curl -s -X PATCH "$BASE_URL/api/projects/$PROJECT_ID/archive")

if echo "$ARCHIVE_RESPONSE" | grep -q '"isArchived":true'; then
    echo "✅ Project archived successfully"
else
    echo "❌ Failed to archive project"
    echo "Response: $ARCHIVE_RESPONSE"
fi

# Test 7: Restore Project
echo ""
echo "🔄 Test 7: Restoring project..."
RESTORE_RESPONSE=$(curl -s -X PATCH "$BASE_URL/api/projects/$PROJECT_ID/restore")

if echo "$RESTORE_RESPONSE" | grep -q '"isArchived":false'; then
    echo "✅ Project restored successfully"
else
    echo "❌ Failed to restore project"
    echo "Response: $RESTORE_RESPONSE"
fi

# Cleanup
echo ""
echo "🧹 Cleanup: Deleting test project..."
DELETE_RESPONSE=$(curl -s -X DELETE "$BASE_URL/api/projects/$PROJECT_ID")

if [ $? -eq 0 ]; then
    echo "✅ Test project deleted successfully"
else
    echo "❌ Failed to delete test project"
fi

echo ""
echo "🎉 Testing completed!"
echo "=================================="

