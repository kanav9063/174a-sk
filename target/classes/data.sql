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
insert into student values ( '12345',
                             'Alfred Hitchcock',
                             '6667 El Colegio #40',
                             '1234',
                             'CS',
                             'CS' );
insert into student values ( '14682',
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
insert into took_courses values ( '12345',
                                  56789,
                                  'B' );  -- CS130
insert into took_courses values ( '12345',
                                  76543,
                                  'A' );  -- CS026


select *
  from student;
select *
  from courseoffering_offeredin;
select *
  from took_courses;

 