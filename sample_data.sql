-- Sample Data for NeoMind Database
-- This file contains sample data for all tables with proper relationships

-- =============================================
-- 1. USERS TABLE
-- =============================================

-- =============================================
-- 2. COMPANIES TABLE
-- =============================================
INSERT INTO companies (name, logo_url, created_at, updated_at) VALUES
( 'TechCorp Solutions', 'https://example.com/logos/techcorp.png', '2024-01-01 08:00:00', '2024-01-01 08:00:00'),
( 'InnovateLab Inc', 'https://example.com/logos/innovatelab.png', '2024-01-02 09:30:00', '2024-01-02 09:30:00'),
( 'DigitalWorks Co', 'https://example.com/logos/digitalworks.png', '2024-01-03 10:15:00', '2024-01-03 10:15:00');

-- =============================================
INSERT INTO workspaces (id, company_id, name, description, created_at, updated_at, created_by) VALUES
('ws-550e8400-e29b-41d4-a716-446655440001', 'comp-550e8400-e29b-41d4-a716-446655440001', 'Development Team', 'Main workspace for development projects', '2024-01-02 09:00:00', '2024-01-02 09:00:00', '550e8400-e29b-41d4-a716-446655440001'),
('ws-550e8400-e29b-41d4-a716-446655440002', 'comp-550e8400-e29b-41d4-a716-446655440001', 'Marketing Team', 'Workspace for marketing initiatives', '2024-01-03 10:00:00', '2024-01-03 10:00:00', '550e8400-e29b-41d4-a716-446655440002'),
('ws-550e8400-e29b-41d4-a716-446655440003', 'comp-550e8400-e29b-41d4-a716-446655440002', 'Product Development', 'Core product development workspace', '2024-01-04 11:00:00', '2024-01-04 11:00:00', '550e8400-e29b-41d4-a716-446655440005'),
('ws-550e8400-e29b-41d4-a716-446655440004', 'comp-550e8400-e29b-41d4-a716-446655440002', 'Research & Innovation', 'R&D workspace for experimental projects', '2024-01-05 12:00:00', '2024-01-05 12:00:00', '550e8400-e29b-41d4-a716-446655440005'),
('ws-550e8400-e29b-41d4-a716-446655440005', 'comp-550e8400-e29b-41d4-a716-446655440003', 'Client Projects', 'Workspace for client-specific projects', '2024-01-06 13:00:00', '2024-01-06 13:00:00', '550e8400-e29b-41d4-a716-446655440008');

INSERT INTO projects (id, workspace_id, name, description, status, color, created_at, updated_at) VALUES
('proj-550e8400-e29b-41d4-a716-446655440001', 'ws-550e8400-e29b-41d4-a716-446655440001', 'E-commerce Platform', 'Building a modern e-commerce platform with React and Node.js', 'active', '#FF6B6B', '2024-01-03 09:00:00', '2024-01-03 09:00:00'),
('proj-550e8400-e29b-41d4-a716-446655440002', 'ws-550e8400-e29b-41d4-a716-446655440001', 'Mobile App Development', 'iOS and Android mobile application', 'active', '#4ECDC4', '2024-01-04 10:00:00', '2024-01-04 10:00:00'),
('proj-550e8400-e29b-41d4-a716-446655440003', 'ws-550e8400-e29b-41d4-a716-446655440002', 'Brand Campaign 2024', 'Annual brand awareness campaign', 'active', '#45B7D1', '2024-01-05 11:00:00', '2024-01-05 11:00:00'),
('proj-550e8400-e29b-41d4-a716-446655440004', 'ws-550e8400-e29b-41d4-a716-446655440003', 'AI Integration Project', 'Integrating AI capabilities into existing products', 'active', '#96CEB4', '2024-01-06 12:00:00', '2024-01-06 12:00:00'),
('proj-550e8400-e29b-41d4-a716-446655440005', 'ws-550e8400-e29b-41d4-a716-446655440003', 'Database Migration', 'Migrating legacy database to cloud', 'active', '#FFEAA7', '2024-01-07 13:00:00', '2024-01-07 13:00:00'),
('proj-550e8400-e29b-41d4-a716-446655440006', 'ws-550e8400-e29b-41d4-a716-446655440004', 'Blockchain Research', 'Research and development of blockchain solutions', 'active', '#DDA0DD', '2024-01-08 14:00:00', '2024-01-08 14:00:00'),
('proj-550e8400-e29b-41d4-a716-446655440007', 'ws-550e8400-e29b-41d4-a716-446655440005', 'Client Portal Redesign', 'Redesigning client portal interface', 'active', '#98D8C8', '2024-01-09 15:00:00', '2024-01-09 15:00:00');

-- =============================================

