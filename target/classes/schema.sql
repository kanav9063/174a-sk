-- ====================================================
-- CS174A Project Schema  (2025-05-24 final)
-- ====================================================

drop table elective cascade constraints;
drop table mandatory cascade constraints;
drop table takes_courses cascade constraints;
drop table courseoffering_offeredin cascade constraints;
drop table prerequisites cascade constraints;
drop table course cascade constraints;
drop table student cascade constraints;
drop table major_managedby cascade constraints;
drop table department cascade constraints;

/* ---------- 1. Department ---------- */
create table department (
   departmentname varchar2(100),
   primary key ( departmentname )
);

/* ---------- 2. Major_managedby ---------- */
create table major_managedby (
   majorname      varchar2(100),
   departmentname varchar2(100),
   electivecount  number,
   foreign key ( departmentname )
      references department ( departmentname ),
   primary key ( majorname )
);

/* ---------- 3. Student ---------- */
create table student (
   perm_num       char(5),
   name           varchar2(80),
   address        varchar2(400),
   pin            varchar2(64) default '00000',
   majorname      varchar2(100),
   departmentname varchar2(100),
   foreign key ( majorname )
      references major_managedby ( majorname ),
   primary key ( perm_num )
);

/* ---------- 4. Course ---------- */
create table course (
   cno     varchar2(10) not null,
   en_code varchar2(10),
   primary key ( cno )
);

/* ---------- 5. Prerequisites ---------- */
create table prerequisites (
   cid varchar2(10),
   pid varchar2(10),
   foreign key ( cid )
      references course ( cno ),
   foreign key ( pid )
      references course ( cno ),
   primary key ( cid,
                 pid )
);

/* ---------- 6. Course Offering (per quarter) ---------- */
create table courseoffering_offeredin (
   enrollment_id  number,
   cno            varchar2(10),
   yr_qtr         varchar2(4),
   max_enrollment integer,
   professor_name varchar2(30),
   time_location  varchar2(30),
   foreign key ( cno )
      references course ( cno ),
   primary key ( enrollment_id,
                 yr_qtr )
);

/* ---------- 7. Takes_Courses (past and current grades(checkas past and current based on grade being null or not)) ---------- */
create table takes_courses (
   perm_num      char(5),
   enrollment_id number,
   yr_qtr        varchar2(4),
   grade         varchar2(2),
   foreign key ( perm_num )
      references student ( perm_num ),
      foreign key ( enrollment_id,
                    yr_qtr )
         references courseoffering_offeredin ( enrollment_id,
                                               yr_qtr ),
   primary key ( perm_num,
                 enrollment_id,
                 yr_qtr )
);

/* ---------- 8. Mandatory courses per major ---------- */
create table mandatory (
   majorname varchar2(100),
   cno       varchar2(10),
   foreign key ( majorname )
      references major_managedby ( majorname ),
   foreign key ( cno )
      references course ( cno ),
   primary key ( majorname,
                 cno )
);

/* ---------- 9. Elective courses per major ---------- */
create table elective (
   majorname varchar2(100),
   cno       varchar2(10),
   foreign key ( majorname )
      references major_managedby ( majorname ),
   foreign key ( cno )
      references course ( cno ),
   primary key ( majorname,
                 cno )
);
