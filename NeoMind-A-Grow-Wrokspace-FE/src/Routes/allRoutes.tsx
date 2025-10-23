import ProjectList from '../pages/ProjectList';
import WorkspaceList from '../pages/Workspace/WorkspaceList';

export type AuthRoute = {
    path: string;
    component: React.ReactNode;
};

export const authProtectedRoutes: AuthRoute[] = [
    {path: '/companies/:companyId/roles/:roleId/permission', component: <EditRolePermissionPage/>},
    {path: '/companies/:companyId/projects', component: <ProjectList/>},
    {path: '/companies/:companyId/workspaces', component: <WorkspaceList/>},
];

const publicRoutes = [];