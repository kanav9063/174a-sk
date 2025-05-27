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
-- 3. Students
-- =========================
insert into student values ( '12345',
                             'Alfred Hitchcock',
                             '6667 El Colegio #40',
                             '12345',
                             'CS',
                             'CS' );
insert into student values ( '14682',
                             'Billy Clinton',
                             '5777 Hollister',
                             '14682',
                             'ECE',
                             'ECE' );
insert into student values ( '37642',
                             'Cindy Laugher',
                             '7000 Hollister',
                             '37642',
                             'CS',
                             'CS' );
insert into student values ( '85821',
                             'David Copperfill',
                             '1357 State St',
                             '85821',
                             'CS',
                             'CS' );
insert into student values ( '38567',
                             'Elizabeth Sailor',
                             '4321 State St',
                             '38567',
                             'ECE',
                             'ECE' );
insert into student values ( '81934',
                             'Fatal Castro',
                             '3756 La Cumbre Plaza',
                             '81934',
                             'CS',
                             'CS' );
insert into student values ( '98246',
                             'George Brush',
                             '5346 Foothill Av',
                             '98246',
                             'CS',
                             'CS' );
insert into student values ( '35328',
                             'Hurryson Ford',
                             '678 State St',
                             '35328',
                             'ECE',
                             'ECE' );
insert into student values ( '84713',
                             'Ivan Lendme',
                             '1235 Johnson Dr',
                             '84713',
                             'ECE',
                             'ECE' );
insert into student values ( '36912',
                             'Joe Pepsi',
                             '3210 State St',
                             '36912',
                             'CS',
                             'CS' );
insert into student values ( '46590',
                             'Kelvin Coster',
                             'Santa Cruz #3579',
                             '46590',
                             'CS',
                             'CS' );
insert into student values ( '91734',
                             'Li Kung',
                             '2 People''s Rd Beijing',
                             '91734',
                             'ECE',
                             'ECE' );
insert into student values ( '73521',
                             'Magic Jordon',
                             '3852 Court Rd',
                             '73521',
                             'CS',
                             'CS' );
insert into student values ( '53540',
                             'Nam-hoi Chung',
                             '1997 People''s St HK',
                             '53540',
                             'CS',
                             'CS' );
insert into student values ( '82452',
                             'Olive Stoner',
                             '6689 El Colegio #151',
                             '82452',
                             'ECE',
                             'ECE' );
insert into student values ( '18221',
                             'Pit Wilson',
                             '911 State St',
                             '18221',
                             'ECE',
                             'ECE' );

-- =========================
-- 4. Courses
-- =========================
insert into course values ( 'CS174',
                            'Advanced Databases' );
insert into course values ( 'CS170',
                            'Algorithms' );
insert into course values ( 'CS160',
                            'Operating Systems' );
insert into course values ( 'CS154',
                            'Computer Architecture' );
insert into course values ( 'CS130',
                            'Data Structures' );
insert into course values ( 'CS026',
                            'Intro to CS' );
insert into course values ( 'CS010',
                            'Intro to Programming' );
insert into course values ( 'EC154',
                            'Digital Circuits' );
insert into course values ( 'EC152',
                            'Microprocessors' );
insert into course values ( 'EC140',
                            'Signals & Systems' );
insert into course values ( 'EC015',
                            'Intro to ECE' );
insert into course values ( 'EC010',
                            'ECE Fundamentals' );

-- =========================
-- 5. Quarters
-- =========================
insert into quarter values ( 1,
                             2025,
                             'Spring',
                             date '2025-03-24',
                             date '2025-06-10' );
insert into quarter values ( 2,
                             2025,
                             'Winter',
                             date '2025-01-10',
                             date '2025-03-20' );
insert into quarter values ( 3,
                             2024,
                             'Fall',
                             date '2024-09-20',
                             date '2024-12-10' );

-- =========================
-- 6. Course Offerings (Spring 2025)
-- =========================
insert into courseoffering_offeredin values ( 10001,
                                              'CS174',
                                              1,
                                              0,
                                              8,
                                              'Venus',
                                              null,
                                              'Psycho',
                                              '1132',
                                              'TR10-12' );
insert into courseoffering_offeredin values ( 10002,
                                              'CS170',
                                              1,
                                              0,
                                              8,
                                              'Jupiter',
                                              null,
                                              'English',
                                              '1124',
                                              'MWF10-11' );
insert into courseoffering_offeredin values ( 10003,
                                              'CS160',
                                              1,
                                              0,
                                              8,
                                              'Mercury',
                                              null,
                                              'Engr',
                                              '1132',
                                              'MWF2-3' );
insert into courseoffering_offeredin values ( 10004,
                                              'CS026',
                                              1,
                                              0,
                                              8,
                                              'Mars',
                                              null,
                                              'Bio',
                                              '2222',
                                              'MWF2-3' );
insert into courseoffering_offeredin values ( 10005,
                                              'EC154',
                                              1,
                                              0,
                                              7,
                                              'Saturn',
                                              null,
                                              'Maths',
                                              '3333',
                                              'T3-5' );
insert into courseoffering_offeredin values ( 10006,
                                              'EC140',
                                              1,
                                              0,
                                              10,
                                              'Gold',
                                              null,
                                              'Chem',
                                              '1234',
                                              'TR1-3' );
insert into courseoffering_offeredin values ( 10007,
                                              'EC015',
                                              1,
                                              0,
                                              8,
                                              'Silver',
                                              null,
                                              'Engr',
                                              '2116',
                                              'MW11-1' );

-- =========================
-- 7. Course Offerings (Winter 2025)
-- =========================
insert into courseoffering_offeredin values ( 20001,
                                              'CS170',
                                              2,
                                              0,
                                              18,
                                              'Copper',
                                              null,
                                              'English',
                                              '1124',
                                              'MWF10-11' );
insert into courseoffering_offeredin values ( 20002,
                                              'CS160',
                                              2,
                                              0,
                                              15,
                                              'Iron',
                                              null,
                                              'Engr',
                                              '1132',
                                              'MWF2-3' );
insert into courseoffering_offeredin values ( 20003,
                                              'CS154',
                                              2,
                                              0,
                                              10,
                                              'Tin',
                                              null,
                                              'Engr',
                                              '2116',
                                              'MF8-9' );
insert into courseoffering_offeredin values ( 20004,
                                              'CS130',
                                              2,
                                              0,
                                              15,
                                              'Star',
                                              null,
                                              'Chem',
                                              '1111',
                                              'TR2-4' );
insert into courseoffering_offeredin values ( 20005,
                                              'CS026',
                                              2,
                                              0,
                                              15,
                                              'Tin',
                                              null,
                                              'Bio',
                                              '2222',
                                              'MWF2-3' );
insert into courseoffering_offeredin values ( 20006,
                                              'EC154',
                                              2,
                                              0,
                                              18,
                                              'Saturn',
                                              null,
                                              'Maths',
                                              '3333',
                                              'T3-5' );
insert into courseoffering_offeredin values ( 20007,
                                              'EC152',
                                              2,
                                              0,
                                              10,
                                              'Gold',
                                              null,
                                              'Engr',
                                              '3163',
                                              'MW11-1' );

-- =========================
-- 8. Course Offerings (Fall 2024)
-- =========================
insert into courseoffering_offeredin values ( 30001,
                                              'CS170',
                                              3,
                                              0,
                                              15,
                                              'Copper',
                                              null,
                                              'English',
                                              '1124',
                                              'MWF10-11' );
insert into courseoffering_offeredin values ( 30002,
                                              'CS160',
                                              3,
                                              0,
                                              10,
                                              'Mercury',
                                              null,
                                              'Engr',
                                              '1132',
                                              'MWF2-3' );
insert into courseoffering_offeredin values ( 30003,
                                              'CS154',
                                              3,
                                              0,
                                              10,
                                              'Mars',
                                              null,
                                              'Engr',
                                              '2116',
                                              'MWF8-9' );
insert into courseoffering_offeredin values ( 30004,
                                              'CS130',
                                              3,
                                              0,
                                              15,
                                              'Jupiter',
                                              null,
                                              'Chem',
                                              '1111',
                                              'TR2-4' );
insert into courseoffering_offeredin values ( 30005,
                                              'CS026',
                                              3,
                                              0,
                                              15,
                                              'Tin',
                                              null,
                                              'Bio',
                                              '2222',
                                              'MWF2-3' );
insert into courseoffering_offeredin values ( 30006,
                                              'CS010',
                                              3,
                                              0,
                                              10,
                                              'Gold',
                                              null,
                                              'Chem',
                                              '3333',
                                              'MWR3-4' );
insert into courseoffering_offeredin values ( 30007,
                                              'EC154',
                                              3,
                                              0,
                                              10,
                                              'Silver',
                                              null,
                                              'Maths',
                                              '3333',
                                              'T3-5' );
insert into courseoffering_offeredin values ( 30008,
                                              'EC152',
                                              3,
                                              0,
                                              10,
                                              'Sun',
                                              null,
                                              'Engr',
                                              '3163',
                                              'MW11-1' );
insert into courseoffering_offeredin values ( 30009,
                                              'EC015',
                                              3,
                                              0,
                                              15,
                                              'Moon',
                                              null,
                                              'Engr',
                                              '1124',
                                              'TR2-4' );
insert into courseoffering_offeredin values ( 30010,
                                              'EC010',
                                              3,
                                              0,
                                              15,
                                              'Earth',
                                              null,
                                              'Physics',
                                              '4004',
                                              'MWF8-9' );

-- =========================
-- 9. COMMIT
-- =========================
commit;

select *
  from student;
select *
  from courseoffering_offeredin;
select *
  from enrolled;
