DELETE FROM takes_courses;
DELETE FROM courseoffering_offeredin;
DELETE FROM prerequisites;
DELETE FROM elective;
DELETE FROM mandatory;
DELETE FROM course;
DELETE FROM student;
DELETE FROM major_managedby;
DELETE FROM department;
-- ====================================================
-- 1. Departments
-- ====================================================
INSERT INTO department(departmentname) VALUES ('CS');
INSERT INTO department(departmentname) VALUES ('ECE');

-- ====================================================
-- 2. Majors
-- ====================================================
INSERT INTO major_managedby(majorname,departmentname,electivecount)
  VALUES ('CS',  'CS',  5);
INSERT INTO major_managedby(majorname,departmentname,electivecount)
  VALUES ('ECE', 'ECE', 5);

-- ====================================================
-- 3. Students
-- ====================================================
INSERT INTO student(perm_num,name,address,pin,majorname,departmentname)
  VALUES ('12345','Alfred Hitchcock','6667 El Colegio #40','12345','CS','CS');
INSERT INTO student(perm_num,name,address,pin,majorname,departmentname)
  VALUES ('14682','Billy Clinton','5777 Hollister','14682','ECE','ECE');
INSERT INTO student(perm_num,name,address,pin,majorname,departmentname)
  VALUES ('37642','Cindy Laugher','7000 Hollister','37642','CS','CS');
INSERT INTO student(perm_num,name,address,pin,majorname,departmentname)
  VALUES ('85821','David Copperfill','1357 State St','85821','CS','CS');
INSERT INTO student(perm_num,name,address,pin,majorname,departmentname)
  VALUES ('38567','Elizabeth Sailor','4321 State St','38567','ECE','ECE');
INSERT INTO student(perm_num,name,address,pin,majorname,departmentname)
  VALUES ('81934','Fatal Castro','3756 La Cumbre Plaza','81934','CS','CS');
INSERT INTO student(perm_num,name,address,pin,majorname,departmentname)
  VALUES ('98246','George Brush','5346 Foothill Av','98246','CS','CS');
INSERT INTO student(perm_num,name,address,pin,majorname,departmentname)
  VALUES ('35328','Hurryson Ford','678 State St','35328','ECE','ECE');
INSERT INTO student(perm_num,name,address,pin,majorname,departmentname)
  VALUES ('84713','Ivan Lendme','1235 Johnson Dr','84713','ECE','ECE');
INSERT INTO student(perm_num,name,address,pin,majorname,departmentname)
  VALUES ('36912','Joe Pepsi','3210 State St','36912','CS','CS');
INSERT INTO student(perm_num,name,address,pin,majorname,departmentname)
  VALUES ('46590','Kelvin Coster','Santa Cruz #3579','46590','CS','CS');
INSERT INTO student(perm_num,name,address,pin,majorname,departmentname)
  VALUES ('91734','Li Kung','2 People''s Rd Beijing','91734','ECE','ECE');
INSERT INTO student(perm_num,name,address,pin,majorname,departmentname)
  VALUES ('73521','Magic Jordon','3852 Court Rd','73521','CS','CS');
INSERT INTO student(perm_num,name,address,pin,majorname,departmentname)
  VALUES ('53540','Nam-hoi Chung','1997 People''s St HK','53540','CS','CS');
INSERT INTO student(perm_num,name,address,pin,majorname,departmentname)
  VALUES ('82452','Olive Stoner','6689 El Colegio #151','82452','ECE','ECE');
INSERT INTO student(perm_num,name,address,pin,majorname,departmentname)
  VALUES ('18221','Pit Wilson','911 State St','18221','ECE','ECE');

-- ====================================================
-- 4. Courses
-- ====================================================
INSERT INTO course(cno,en_code) VALUES ('CS174','12345');
INSERT INTO course(cno,en_code) VALUES ('CS170','54321');
INSERT INTO course(cno,en_code) VALUES ('CS160','41725');
INSERT INTO course(cno,en_code) VALUES ('CS026','76543');
INSERT INTO course(cno,en_code) VALUES ('EC154','93156');
INSERT INTO course(cno,en_code) VALUES ('EC140','19023');
INSERT INTO course(cno,en_code) VALUES ('EC015','71631');
INSERT INTO course(cno,en_code) VALUES ('CS154','32165');
INSERT INTO course(cno,en_code) VALUES ('CS130','56789');
INSERT INTO course(cno,en_code) VALUES ('EC152','91823');
INSERT INTO course(cno,en_code) VALUES ('CS010','81623');
INSERT INTO course(cno,en_code) VALUES ('EC010','82612');

-- ====================================================
-- 5. Prerequisites
-- ====================================================
INSERT INTO prerequisites(cid,pid) VALUES ('CS174','CS130');
INSERT INTO prerequisites(cid,pid) VALUES ('CS174','CS026');
INSERT INTO prerequisites(cid,pid) VALUES ('CS170','CS130');
INSERT INTO prerequisites(cid,pid) VALUES ('CS170','CS154');
INSERT INTO prerequisites(cid,pid) VALUES ('CS160','CS026');
INSERT INTO prerequisites(cid,pid) VALUES ('EC154','CS026');
INSERT INTO prerequisites(cid,pid) VALUES ('EC154','EC152');

-- ====================================================
-- 6. Course Offerings
--    Spring 2025 (25S)
-- ====================================================
INSERT INTO courseoffering_offeredin(enrollment_id,cno,yr_qtr,max_enrollment,professor_name,time_location)
  VALUES (12345,'CS174','25S',8, 'Venus',  'TR10-12 Psycho 1132');
INSERT INTO courseoffering_offeredin(enrollment_id,cno,yr_qtr,max_enrollment,professor_name,time_location)
  VALUES (54321,'CS170','25S',8, 'Jupiter','MWF10-11 English 1124');
INSERT INTO courseoffering_offeredin(enrollment_id,cno,yr_qtr,max_enrollment,professor_name,time_location)
  VALUES (41725,'CS160','25S',8, 'Mercury','MWF2-3 Engr 1132');
INSERT INTO courseoffering_offeredin(enrollment_id,cno,yr_qtr,max_enrollment,professor_name,time_location)
  VALUES (76543,'CS026','25S',8, 'Mars',   'MWF2-3 Bio 2222');
INSERT INTO courseoffering_offeredin(enrollment_id,cno,yr_qtr,max_enrollment,professor_name,time_location)
  VALUES (93156,'EC154','25S',7, 'Saturn', 'T3-5 Maths 3333');
INSERT INTO courseoffering_offeredin(enrollment_id,cno,yr_qtr,max_enrollment,professor_name,time_location)
  VALUES (19023,'EC140','25S',10,'Gold',   'TR1-3 Chem 1234');
INSERT INTO courseoffering_offeredin(enrollment_id,cno,yr_qtr,max_enrollment,professor_name,time_location)
  VALUES (71631,'EC015','25S',8, 'Silver', 'MW11-1 Engr 2116');

-- ====================================================
--    Winter 2025 (25W)
-- ====================================================
INSERT INTO courseoffering_offeredin(enrollment_id,cno,yr_qtr,max_enrollment,professor_name,time_location)
  VALUES (54321,'CS170','25W',18,'Copper','MWF10-11 English 1124');
INSERT INTO courseoffering_offeredin(enrollment_id,cno,yr_qtr,max_enrollment,professor_name,time_location)
  VALUES (41725,'CS160','25W',15,'Iron',  'MWF2-3 Engr 1132');
INSERT INTO courseoffering_offeredin(enrollment_id,cno,yr_qtr,max_enrollment,professor_name,time_location)
  VALUES (32165,'CS154','25W',10,'Tin',   'MF8-9 Engr 2116');
INSERT INTO courseoffering_offeredin(enrollment_id,cno,yr_qtr,max_enrollment,professor_name,time_location)
  VALUES (56789,'CS130','25W',15,'Star',  'TR2-4 Chem 1111');
INSERT INTO courseoffering_offeredin(enrollment_id,cno,yr_qtr,max_enrollment,professor_name,time_location)
  VALUES (76543,'CS026','25W',15,'Tin',   'MWF2-3 Bio 2222');
INSERT INTO courseoffering_offeredin(enrollment_id,cno,yr_qtr,max_enrollment,professor_name,time_location)
  VALUES (93156,'EC154','25W',18,'Saturn','T3-5 Maths 3333');
INSERT INTO courseoffering_offeredin(enrollment_id,cno,yr_qtr,max_enrollment,professor_name,time_location)
  VALUES (91823,'EC152','25W',10,'Gold',  'MW11-1 Engr 3163');

-- ====================================================
--    Fall 2024 (24F)
-- ====================================================
INSERT INTO courseoffering_offeredin(enrollment_id,cno,yr_qtr,max_enrollment,professor_name,time_location)
  VALUES (54321,'CS170','24F',15,'Copper','MWF10-11 English 1124');
INSERT INTO courseoffering_offeredin(enrollment_id,cno,yr_qtr,max_enrollment,professor_name,time_location)
  VALUES (41725,'CS160','24F',10,'Mercury','MWF2-3 Engr 1132');
INSERT INTO courseoffering_offeredin(enrollment_id,cno,yr_qtr,max_enrollment,professor_name,time_location)
  VALUES (32165,'CS154','24F',10,'Mars',   'MF8-9 Engr 2116');
INSERT INTO courseoffering_offeredin(enrollment_id,cno,yr_qtr,max_enrollment,professor_name,time_location)
  VALUES (56789,'CS130','24F',15,'Jupiter','TR2-4 Chem 1111');
INSERT INTO courseoffering_offeredin(enrollment_id,cno,yr_qtr,max_enrollment,professor_name,time_location)
  VALUES (76543,'CS026','24F',15,'Tin',    'MWF2-3 Bio 2222');
INSERT INTO courseoffering_offeredin(enrollment_id,cno,yr_qtr,max_enrollment,professor_name,time_location)
  VALUES (81623,'CS010','24F',10,'Gold',   'MWR3-4 Chem 3333');
INSERT INTO courseoffering_offeredin(enrollment_id,cno,yr_qtr,max_enrollment,professor_name,time_location)
  VALUES (93156,'EC154','24F',10,'Silver', 'T3-5 Maths 3333');
INSERT INTO courseoffering_offeredin(enrollment_id,cno,yr_qtr,max_enrollment,professor_name,time_location)
  VALUES (91823,'EC152','24F',10,'Sun',    'MW11-1 Engr 3163');
INSERT INTO courseoffering_offeredin(enrollment_id,cno,yr_qtr,max_enrollment,professor_name,time_location)
  VALUES (71631,'EC015','24F',15,'Moon',   'TR2-4 Engr 1124');
INSERT INTO courseoffering_offeredin(enrollment_id,cno,yr_qtr,max_enrollment,professor_name,time_location)
  VALUES (82612,'EC010','24F',15,'Earth',  'MWF8-9 Physics 4004');

-- ====================================================
--    Summer 2024 (24S)
-- ====================================================
INSERT INTO courseoffering_offeredin(enrollment_id,cno,yr_qtr,max_enrollment,professor_name,time_location)
  VALUES (56789,'CS130','24S',15,'Mercury','TR2-4 Chem 1111');
INSERT INTO courseoffering_offeredin(enrollment_id,cno,yr_qtr,max_enrollment,professor_name,time_location)
  VALUES (76543,'CS026','24S',15,'Mars',   'MWF2-3 Bio 2222');
INSERT INTO courseoffering_offeredin(enrollment_id,cno,yr_qtr,max_enrollment,professor_name,time_location)
  VALUES (81623,'CS010','24S',10,'Gold',   'MWR3-4 Chem 3333');
INSERT INTO courseoffering_offeredin(enrollment_id,cno,yr_qtr,max_enrollment,professor_name,time_location)
  VALUES (91823,'EC152','24S',12,'Iron',   'MW11-1 Engr 3163');
INSERT INTO courseoffering_offeredin(enrollment_id,cno,yr_qtr,max_enrollment,professor_name,time_location)
  VALUES (71631,'EC015','24S',15,'Moon',   'TR2-4 Engr 1124');
INSERT INTO courseoffering_offeredin(enrollment_id,cno,yr_qtr,max_enrollment,professor_name,time_location)
  VALUES (82612,'EC010','24S',15,'Star',   'MWF8-9 Physics 4004');

-- ====================================================
-- 7. Takes_Courses (NULL = current, NOT NULL = past)
-- ----------------------------------------------------
-- 7.1 Current (Spring 2025 → yr_qtr='25S')
-- ====================================================
INSERT INTO takes_courses(perm_num,enrollment_id,yr_qtr,grade)
  VALUES ('12345',54321,'25S',NULL);
INSERT INTO takes_courses(perm_num,enrollment_id,yr_qtr,grade)
  VALUES ('12345',41725,'25S',NULL);
INSERT INTO takes_courses(perm_num,enrollment_id,yr_qtr,grade)
  VALUES ('37642',93156,'25S',NULL);
INSERT INTO takes_courses(perm_num,enrollment_id,yr_qtr,grade)
  VALUES ('37642',41725,'25S',NULL);
INSERT INTO takes_courses(perm_num,enrollment_id,yr_qtr,grade)
  VALUES ('85821',12345,'25S',NULL);
INSERT INTO takes_courses(perm_num,enrollment_id,yr_qtr,grade)
  VALUES ('85821',41725,'25S',NULL);
INSERT INTO takes_courses(perm_num,enrollment_id,yr_qtr,grade)
  VALUES ('38567',12345,'25S',NULL);
INSERT INTO takes_courses(perm_num,enrollment_id,yr_qtr,grade)
  VALUES ('38567',54321,'25S',NULL);
INSERT INTO takes_courses(perm_num,enrollment_id,yr_qtr,grade)
  VALUES ('38567',41725,'25S',NULL);
INSERT INTO takes_courses(perm_num,enrollment_id,yr_qtr,grade)
  VALUES ('81934',93156,'25S',NULL);
INSERT INTO takes_courses(perm_num,enrollment_id,yr_qtr,grade)
  VALUES ('98246',41725,'25S',NULL);
INSERT INTO takes_courses(perm_num,enrollment_id,yr_qtr,grade)
  VALUES ('98246',12345,'25S',NULL);
INSERT INTO takes_courses(perm_num,enrollment_id,yr_qtr,grade)
  VALUES ('98246',54321,'25S',NULL);
INSERT INTO takes_courses(perm_num,enrollment_id,yr_qtr,grade)
  VALUES ('98246',93156,'25S',NULL);
INSERT INTO takes_courses(perm_num,enrollment_id,yr_qtr,grade)
  VALUES ('35328',12345,'25S',NULL);
INSERT INTO takes_courses(perm_num,enrollment_id,yr_qtr,grade)
  VALUES ('53540',54321,'25S',NULL);
INSERT INTO takes_courses(perm_num,enrollment_id,yr_qtr,grade)
  VALUES ('82452',93156,'25S',NULL);
INSERT INTO takes_courses(perm_num,enrollment_id,yr_qtr,grade)
  VALUES ('18221',12345,'25S',NULL);

-- ====================================================
-- 7.2 Past (Winter 2025 → '25W', Fall 2024 → '24F', Summer 2024 → '24S')
-- ====================================================
INSERT INTO takes_courses(perm_num,enrollment_id,yr_qtr,grade) VALUES ('12345',32165,'25W','A');
INSERT INTO takes_courses(perm_num,enrollment_id,yr_qtr,grade) VALUES ('12345',56789,'25W','B');
INSERT INTO takes_courses(perm_num,enrollment_id,yr_qtr,grade) VALUES ('12345',93156,'25W','C');
INSERT INTO takes_courses(perm_num,enrollment_id,yr_qtr,grade) VALUES ('12345',76543,'24F','A');
INSERT INTO takes_courses(perm_num,enrollment_id,yr_qtr,grade) VALUES ('12345',81623,'24F','A');


INSERT INTO takes_courses(perm_num,enrollment_id,yr_qtr,grade) VALUES ('14682',41725,'25W','B');
INSERT INTO takes_courses(perm_num,enrollment_id,yr_qtr,grade) VALUES ('14682',56789,'25W','B');
INSERT INTO takes_courses(perm_num,enrollment_id,yr_qtr,grade) VALUES ('14682',76543,'24F','B');
INSERT INTO takes_courses(perm_num,enrollment_id,yr_qtr,grade) VALUES ('14682',81623,'24F','A');

INSERT INTO takes_courses(perm_num,enrollment_id,yr_qtr,grade) VALUES ('37642',91823,'25W','C');
INSERT INTO takes_courses(perm_num,enrollment_id,yr_qtr,grade) VALUES ('37642',56789,'25W','B');
INSERT INTO takes_courses(perm_num,enrollment_id,yr_qtr,grade) VALUES ('37642',71631,'24F','B');
INSERT INTO takes_courses(perm_num,enrollment_id,yr_qtr,grade) VALUES ('37642',81623,'24F','A');

INSERT INTO takes_courses(perm_num,enrollment_id,yr_qtr,grade) VALUES ('85821',56789,'25W','C');
INSERT INTO takes_courses(perm_num,enrollment_id,yr_qtr,grade) VALUES ('85821',76543,'25W','A');
INSERT INTO takes_courses(perm_num,enrollment_id,yr_qtr,grade) VALUES ('85821',81623,'24F','A');
INSERT INTO takes_courses(perm_num,enrollment_id,yr_qtr,grade) VALUES ('85821',71631,'24F','B');

INSERT INTO takes_courses(perm_num,enrollment_id,yr_qtr,grade) VALUES ('38567',93156,'25W','C');
INSERT INTO takes_courses(perm_num,enrollment_id,yr_qtr,grade) VALUES ('38567',56789,'25W','A');
INSERT INTO takes_courses(perm_num,enrollment_id,yr_qtr,grade) VALUES ('38567',91823,'24F','B');
INSERT INTO takes_courses(perm_num,enrollment_id,yr_qtr,grade) VALUES ('38567',32165,'24F','B');

INSERT INTO takes_courses(perm_num,enrollment_id,yr_qtr,grade) VALUES ('81934',32165,'25W','C');
INSERT INTO takes_courses(perm_num,enrollment_id,yr_qtr,grade) VALUES ('81934',56789,'25W','A');
INSERT INTO takes_courses(perm_num,enrollment_id,yr_qtr,grade) VALUES ('81934',76543,'24F','A');
INSERT INTO takes_courses(perm_num,enrollment_id,yr_qtr,grade) VALUES ('81934',91823,'24F','B');

INSERT INTO takes_courses(perm_num,enrollment_id,yr_qtr,grade) VALUES ('98246',91823,'25W','B');
INSERT INTO takes_courses(perm_num,enrollment_id,yr_qtr,grade) VALUES ('98246',32165,'24F','A');
INSERT INTO takes_courses(perm_num,enrollment_id,yr_qtr,grade) VALUES ('98246',56789,'24F','B');
INSERT INTO takes_courses(perm_num,enrollment_id,yr_qtr,grade) VALUES ('98246',76543,'24F','A');

INSERT INTO takes_courses(perm_num,enrollment_id,yr_qtr,grade) VALUES ('35328',56789,'24F','B');
INSERT INTO takes_courses(perm_num,enrollment_id,yr_qtr,grade) VALUES ('35328',76543,'24F','A');

INSERT INTO takes_courses(perm_num,enrollment_id,yr_qtr,grade) VALUES ('84713',76543,'25W','D');
INSERT INTO takes_courses(perm_num,enrollment_id,yr_qtr,grade) VALUES ('84713',71631,'24F','F');
INSERT INTO takes_courses(perm_num,enrollment_id,yr_qtr,grade) VALUES ('84713',81623,'24F','C');

INSERT INTO takes_courses(perm_num,enrollment_id,yr_qtr,grade) VALUES ('46590',76543,'25W','A');
INSERT INTO takes_courses(perm_num,enrollment_id,yr_qtr,grade) VALUES ('91734',76543,'25W','A');
INSERT INTO takes_courses(perm_num,enrollment_id,yr_qtr,grade) VALUES ('73521',76543,'25W','B');
-- INSERT INTO takes_courses(perm_num,enrollment_id,yr_qtr,grade) VALUES ('53540',54321,'25S',NULL);  -- Joe Pepsi currently none
INSERT INTO takes_courses(perm_num,enrollment_id,yr_qtr,grade) VALUES ('53540',32165,'25W','C');
INSERT INTO takes_courses(perm_num,enrollment_id,yr_qtr,grade) VALUES ('53540',56789,'25W','C');
-- INSERT INTO takes_courses(perm_num,enrollment_id,yr_qtr,grade) VALUES ('82452',93156,'25S',NULL);
INSERT INTO takes_courses(perm_num,enrollment_id,yr_qtr,grade) VALUES ('82452',91823,'25W','C');
INSERT INTO takes_courses(perm_num,enrollment_id,yr_qtr,grade) VALUES ('82452',76543,'25W','C');
-- INSERT INTO takes_courses(perm_num,enrollment_id,yr_qtr,grade) VALUES ('18221',12345,'25S',NULL);
INSERT INTO takes_courses(perm_num,enrollment_id,yr_qtr,grade) VALUES ('18221',56789,'25W','B');
INSERT INTO takes_courses(perm_num,enrollment_id,yr_qtr,grade) VALUES ('18221',76543,'25W','B');

-- ====================================================
-- 8. Mandatory courses per major
-- ====================================================
INSERT INTO mandatory(majorname,cno) VALUES ('CS','CS026');
INSERT INTO mandatory(majorname,cno) VALUES ('CS','CS130');
INSERT INTO mandatory(majorname,cno) VALUES ('CS','CS154');
INSERT INTO mandatory(majorname,cno) VALUES ('CS','CS160');
INSERT INTO mandatory(majorname,cno) VALUES ('CS','CS170');
INSERT INTO mandatory(majorname,cno) VALUES ('ECE','CS026');
INSERT INTO mandatory(majorname,cno) VALUES ('ECE','CS130');
INSERT INTO mandatory(majorname,cno) VALUES ('ECE','CS154');
INSERT INTO mandatory(majorname,cno) VALUES ('ECE','CS160');
INSERT INTO mandatory(majorname,cno) VALUES ('ECE','CS170');

-- ====================================================
-- 9. Elective courses per major
-- ====================================================
INSERT INTO elective(majorname,cno) VALUES ('CS','CS010');
INSERT INTO elective(majorname,cno) VALUES ('CS','EC010');
INSERT INTO elective(majorname,cno) VALUES ('CS','EC015');
INSERT INTO elective(majorname,cno) VALUES ('CS','EC140');
INSERT INTO elective(majorname,cno) VALUES ('CS','EC152');
INSERT INTO elective(majorname,cno) VALUES ('CS','EC154');
INSERT INTO elective(majorname,cno) VALUES ('CS','CS174');
INSERT INTO elective(majorname,cno) VALUES ('ECE','CS010');
INSERT INTO elective(majorname,cno) VALUES ('ECE','EC010');
INSERT INTO elective(majorname,cno) VALUES ('ECE','EC015');
INSERT INTO elective(majorname,cno) VALUES ('ECE','EC140');
INSERT INTO elective(majorname,cno) VALUES ('ECE','EC152');
INSERT INTO elective(majorname,cno) VALUES ('ECE','EC154');
INSERT INTO elective(majorname,cno) VALUES ('ECE','CS174');

commit;
