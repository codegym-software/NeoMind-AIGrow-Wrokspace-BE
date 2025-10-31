/**
 * Workspace Permission Automated Tester
 *
 * Usage (PowerShell/CMD):
 *   set BASE_URL=http://localhost:8080
 *   set ACCESS_TOKEN=eyJ...  (JWT of non-owner test user)
 *   set WORKSPACE_ID=xxxx
 *   set COMPANY_ID=yyyy
 *   set TEST_ROLE_ID=123             (role assigned to the test user; its permissions will be overwritten repeatedly)
 *   set TARGET_MEMBER_USER_ID=some-user-id   (a non-owner member in the workspace)
 *   node scripts/permission_tester/test_workspace_permissions.js
 *
 * The script will:
 * 1) Fetch workspace permissions to map code -> id
 * 2) For each permission code, overwrite TEST_ROLE_ID permissions to only that code
 * 3) Call representative API(s) and record whether 2xx or 403
 * 4) Print a compact report
 */

const fetch = (...args) => import('node-fetch').then(({default: f}) => f(...args));

const BASE_URL = process.env.BASE_URL || 'http://localhost:8080';
const TOKEN = process.env.ACCESS_TOKEN;
const WORKSPACE_ID = process.env.WORKSPACE_ID;
const COMPANY_ID = process.env.COMPANY_ID;
const TEST_ROLE_ID = Number(process.env.TEST_ROLE_ID);
const TARGET_MEMBER_USER_ID = process.env.TARGET_MEMBER_USER_ID; // should be a non-owner userId in ws

if (!TOKEN || !WORKSPACE_ID || !TEST_ROLE_ID) {
  console.error('Missing env. Required: ACCESS_TOKEN, WORKSPACE_ID, TEST_ROLE_ID. Optional: COMPANY_ID, TARGET_MEMBER_USER_ID');
  process.exit(1);
}

function authHeaders(extra = {}) {
  return {
    Authorization: `Bearer ${TOKEN}`,
    'Content-Type': 'application/json',
    ...extra,
  };
}

async function getJson(url, opts = {}) {
  const res = await fetch(url, opts);
  const text = await res.text();
  try { return { res, json: text ? JSON.parse(text) : null }; } catch { return { res, json: text }; }
}

async function upsertRolePermissions(permissionCodes) {
  const { idByCode } = await loadWorkspacePermissionCatalog();
  const ids = permissionCodes.map(c => idByCode[c]).filter(Boolean);
  const url = new URL(`/api/workspaces/${WORKSPACE_ID}/roles/${TEST_ROLE_ID}/permissions`, BASE_URL).toString();
  const { res, json } = await getJson(url, { method: 'PUT', headers: authHeaders(), body: JSON.stringify({ permissionIds: ids })});
  if (!res.ok) throw new Error(`Failed to upsert permissions ${permissionCodes.join(', ')}: ${res.status} ${JSON.stringify(json)}`);
}

let cachedCatalog = null;
async function loadWorkspacePermissionCatalog() {
  if (cachedCatalog) return cachedCatalog;
  const url = new URL(`/api/permissions?scope=workspace`, BASE_URL).toString();
  const { res, json } = await getJson(url, { headers: authHeaders() });
  if (!res.ok) throw new Error(`Failed to load workspace permissions: ${res.status} ${JSON.stringify(json)}`);
  const idByCode = Object.fromEntries((json || []).map(p => [p.code, p.id]));
  cachedCatalog = { list: json, idByCode };
  return cachedCatalog;
}

async function callAndResult(name, url, opts) {
  const { res } = await getJson(url, opts);
  return { name, status: res.status, ok: res.ok };
}

async function main() {
  const tests = [];

  // workspace:read
  tests.push({ perm: ['workspace:read'], calls: [() => callAndResult('GET ws', new URL(`/api/workspaces/${WORKSPACE_ID}`, BASE_URL).toString(), { headers: authHeaders() })] });

  // workspace:update
  tests.push({ perm: ['workspace:update'], calls: [() => callAndResult('PUT ws', new URL(`/api/workspaces/${WORKSPACE_ID}`, BASE_URL).toString(), { method: 'PUT', headers: authHeaders(), body: JSON.stringify({ name: 'perm-test-update' }) })] });

  // workspace:delete (dry-run by expecting 403/405 on prod) – we only hit and read status, do NOT actually delete unless permitted; skip if dangerous.
  tests.push({ perm: ['workspace:delete'], calls: [() => callAndResult('DELETE ws', new URL(`/api/workspaces/${WORKSPACE_ID}`, BASE_URL).toString(), { method: 'DELETE', headers: authHeaders() })] });

  // member:read
  tests.push({ perm: ['workspace:member:read'], calls: [() => callAndResult('GET members', new URL(`/api/workspaces/${WORKSPACE_ID}/members`, BASE_URL).toString(), { headers: authHeaders() })] });

  // member:create (requires a company member userId & roleId). We will no-op payload; backend will validate. Adjust as needed.
  tests.push({ perm: ['workspace:member:create'], calls: [() => callAndResult('POST member', new URL(`/api/workspaces/${WORKSPACE_ID}/members`, BASE_URL).toString(), { method: 'POST', headers: authHeaders(), body: JSON.stringify({ userId: 'dummy-user', roleId: 0 }) })] });

  // member:update
  if (TARGET_MEMBER_USER_ID) {
    tests.push({ perm: ['workspace:member:update'], calls: [() => callAndResult('PUT member role', new URL(`/api/workspaces/${WORKSPACE_ID}/members/${encodeURIComponent(TARGET_MEMBER_USER_ID)}/role`, BASE_URL).toString(), { method: 'PUT', headers: authHeaders(), body: JSON.stringify({ roleId: 0 }) })] });
  }

  // member:delete
  if (TARGET_MEMBER_USER_ID) {
    tests.push({ perm: ['workspace:member:delete'], calls: [() => callAndResult('DELETE member', new URL(`/api/workspaces/${WORKSPACE_ID}/members/${encodeURIComponent(TARGET_MEMBER_USER_ID)}`, BASE_URL).toString(), { method: 'DELETE', headers: authHeaders() })] });
  }

  // member:transfer_ownership
  if (TARGET_MEMBER_USER_ID) {
    tests.push({ perm: ['workspace:member:transfer_ownership'], calls: [() => callAndResult('POST transfer owner', new URL(`/api/workspaces/${WORKSPACE_ID}/members/transfer-ownership`, BASE_URL).toString(), { method: 'POST', headers: authHeaders(), body: JSON.stringify({ toUserId: TARGET_MEMBER_USER_ID, downgradeRoleId: 0 }) })] });
  }

  // role:read/create/update/delete/grant_permission – create a temp role name to operate safely
  const tempRoleCode = `perm_test_${Date.now()}`;
  tests.push({ perm: ['workspace:role:read'], calls: [() => callAndResult('GET roles', new URL(`/api/workspaces/${WORKSPACE_ID}/roles`, BASE_URL).toString(), { headers: authHeaders() })] });
  tests.push({ perm: ['workspace:role:create'], calls: [() => callAndResult('POST role', new URL(`/api/workspaces/${WORKSPACE_ID}/roles`, BASE_URL).toString(), { method: 'POST', headers: authHeaders(), body: JSON.stringify({ code: tempRoleCode, name: 'Perm Test' }) })] });
  tests.push({ perm: ['workspace:role:update'], calls: [() => callAndResult('PUT role (id=1?)', new URL(`/api/workspaces/${WORKSPACE_ID}/roles/1`, BASE_URL).toString(), { method: 'PUT', headers: authHeaders(), body: JSON.stringify({ name: 'Rename' }) })] });
  tests.push({ perm: ['workspace:role:delete'], calls: [() => callAndResult('DELETE role (id=1?)', new URL(`/api/workspaces/${WORKSPACE_ID}/roles/1`, BASE_URL).toString(), { method: 'DELETE', headers: authHeaders() })] });
  tests.push({ perm: ['workspace:role:grant_permission'], calls: [() => callAndResult('PUT role perms (id=1?)', new URL(`/api/workspaces/${WORKSPACE_ID}/roles/1/permissions`, BASE_URL).toString(), { method: 'PUT', headers: authHeaders(), body: JSON.stringify({ permissionIds: [] }) })] });

  // workspace:project:create
  tests.push({ perm: ['workspace:project:create'], calls: [() => callAndResult('POST project', new URL(`/api/workspaces/${WORKSPACE_ID}/projects`, BASE_URL).toString(), { method: 'POST', headers: authHeaders(), body: JSON.stringify({ name: `perm-test-${Date.now()}` }) })] });

  const report = [];
  for (const t of tests) {
    try {
      await upsertRolePermissions(t.perm);
      const results = [];
      for (const call of t.calls) {
        const r = await call();
        results.push(r);
      }
      report.push({ perm: t.perm.join(','), results });
    } catch (e) {
      report.push({ perm: t.perm.join(','), error: e.message });
    }
  }

  // Print summary
  console.log('\n=== Workspace Permission Test Report ===');
  for (const item of report) {
    if (item.error) {
      console.log(`- ${item.perm}: ERROR ${item.error}`);
    } else {
      const statuses = item.results.map(r => `${r.name}:${r.status}`).join(', ');
      console.log(`- ${item.perm}: ${statuses}`);
    }
  }
}

main().catch(err => {
  console.error(err);
  process.exit(1);
});


