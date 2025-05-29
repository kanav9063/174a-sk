-- ====================================================
-- Master Database Setup Script
-- Run this to set up the entire database
-- ====================================================

-- 1. Create tables
@schema.sql

-- 2. Create functions and triggers
@functions.sql

-- 3. Load initial data
@data.sql

-- Verify setup
select 'Database setup complete!' as status
  from dual;
