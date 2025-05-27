-- =========================
-- 1. Clean start
-- =========================
delete from scheduled_at;
delete from took_courses;
delete from enrolled;
delete from courseoffering_offeredin;
delete from prerequisites;
delete from mandatory;
delete from elective;
delete from student;
delete from major_managedby;
delete from department;
delete from course;
delete from slot;
delete from quarter;

-- =========================
-- 2. Departments + Majors
-- =========================
insert into department values ( 'CS' );
insert into department values ( 'ECE' );

insert into major_managedby values ( 'CS',
                                     'CS',
                                     5 );
insert into major_managedby values ( 'ECE',
                                     'ECE',
                                     5 );

-- =========================
-- 3. Students (must use 4-digit PINs!)
-- =========================
insert into student values ( '1234567',
                             'Alfred Hitchcock',
                             '6667 El Colegio #40',
                             '1234',
                             'CS',
                             'CS' );
insert into student values ( '1468222',
                             'Billy Clinton',
                             '5777 Hollister',
                             '4321',
                             'ECE',
                             'ECE' );

-- =========================
-- 4. Courses
-- =========================
insert into course values ( 'CS130',
                            'Data Structures' );
insert into course values ( 'CS026',
                            'Intro to CS' );

-- =========================
-- 5. Quarters
-- =========================
insert into quarter values ( 1,
                             2025,
                             'Winter',
                             date '2025-01-10',
                             date '2025-03-20' );
insert into quarter values ( 2,
                             2025,
                             'Spring',
                             date '2025-03-21',
                             date '2025-06-20' );

-- =========================
-- 6. Offerings
-- =========================
insert into courseoffering_offeredin values ( 56789,
                                              'CS130',
                                              1,
                                              0,
                                              15,
                                              'Star',
                                              null,
                                              'Chem',
                                              '1111',
                                              'TR2-4' );

insert into courseoffering_offeredin values ( 76543,
                                              'CS026',
                                              1,
                                              0,
                                              15,
                                              'Mars',
                                              null,
                                              'Bio',
                                              '2222',
                                              'MWF2-3' );

-- =========================
-- 7. Grades from previous quarter
-- =========================


-- added by skanda for enrolled table..., from chat
-- Add current enrollments for testing
INSERT INTO enrolled VALUES ('1234567', 56789);  -- Enroll Alfred in CS130
INSERT INTO enrolled VALUES ('1234567', 76543);  -- Enroll Alfred in CS026
INSERT INTO enrolled VALUES ('1468222', 56789);  -- Enroll Billy in CS130

-- Update enrollment counts
UPDATE courseoffering_offeredin SET act_enrolled = 2 WHERE enrollment_id = 56789;  -- CS130 has 2 students
UPDATE courseoffering_offeredin SET act_enrolled = 1 WHERE enrollment_id = 76543;  -- CS026 has 1 student



select *
  from student;
select *
  from courseoffering_offeredin;


 commit;