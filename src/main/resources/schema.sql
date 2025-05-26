-- ====================================================
-- CS174A Project Schema  (2025-05-24 final)
-- ====================================================

/* ---------- 0. Drop tables in reverse dependency order ---------- */
drop table scheduled_at cascade constraints;
drop table took_courses cascade constraints;
drop table enrolled cascade constraints;
drop table courseoffering_offeredin cascade constraints;
drop table prerequisites cascade constraints;
drop table mandatory cascade constraints;
drop table elective cascade constraints;
drop table student cascade constraints;
drop table major_managedby cascade constraints;
drop table department cascade constraints;
drop table course cascade constraints;
drop table slot cascade constraints;
drop table quarter cascade constraints;

/* ---------- 1. Department ---------- */
create table department (
   departmentname varchar2(100) primary key
);

/* ---------- 2. Major_managedby ---------- */
create table major_managedby (
   majorname      varchar2(100) primary key,
   departmentname varchar2(100) not null,
   electivecount  number not null check ( electivecount >= 0 ),
   foreign key ( departmentname )
      references department ( departmentname )
);

/* ---------- 3. Student ---------- */
create table student (
   perm           char(7) primary key,
   name           varchar2(80) not null,
   address        varchar2(200),
   pin            char(4) default '0000' not null check ( regexp_like ( pin,
                                                             '^\d{4}$' ) ),
   majorname      varchar2(100) not null,
   departmentname varchar2(100) not null,
   foreign key ( majorname )
      references major_managedby ( majorname )
         on delete cascade,
   foreign key ( departmentname )
      references department ( departmentname )
         on delete cascade
);

/* ---------- 4. Quarter ---------- */
create table quarter (
   quarterid number primary key,
   year      number(4) not null,
   term      varchar2(6) not null check ( term in ( 'Winter',
                                               'Spring',
                                               'Fall' ) ),
   startdate date not null,
   enddate   date not null
);

/* ---------- 5. Slot (meeting times) ---------- */
create table slot (
   slotid    number primary key,
   dayofweek varchar2(9) not null check ( dayofweek in ( 'Monday',
                                                         'Tuesday',
                                                         'Wednesday',
                                                         'Thursday',
                                                         'Friday',
                                                         'Saturday',
                                                         'Sunday' ) ),
   starttime varchar2(5) not null,
   endtime   varchar2(5) not null,
   check ( starttime < endtime )
);

/* ---------- 6. Course ---------- */
create table course (
   cno   varchar2(7) primary key check ( regexp_like ( cno,
                                                     '^[A-Za-z]{2,4}[0-9]{1,3}$' ) ),
   title varchar2(100) not null
);

/* ---------- 7. Prerequisites ---------- */
create table prerequisites (
   cid varchar2(7),
   pid varchar2(7),
   primary key ( cid,
                 pid ),
   foreign key ( cid )
      references course ( cno ),
   foreign key ( pid )
      references course ( cno )
);

/* ---------- 8. Course Offering (per quarter) ---------- */
create table courseoffering_offeredin (
   enrollment_id  number primary key,
   cno            varchar2(7) not null,
   quarter_id     number not null,
   act_enrolled   number default 0,
   max_enrollment number not null,
   plast_name     varchar2(30),
   pfirst_name    varchar2(30),
   building_code  varchar2(5),
   room_num       varchar2(4),
   time_slot      varchar2(30),
   foreign key ( cno )
      references course ( cno ),
   foreign key ( quarter_id )
      references quarter ( quarterid )
);

/* ---------- 9. Enrolled (current-quarter) ---------- */
create table enrolled (
   perm          char(7) not null,
   enrollment_id number not null,
   primary key ( perm,
                 enrollment_id ),
   foreign key ( perm )
      references student ( perm ),
   foreign key ( enrollment_id )
      references courseoffering_offeredin ( enrollment_id )
);

/* ---------- 10. Took_Courses (past grades) ---------- */
create table took_courses (
   perm          char(7) not null,
   enrollment_id number not null,
   grade         varchar2(2) check ( grade in ( 'A+',
                                        'A',
                                        'A-',
                                        'B+',
                                        'B',
                                        'B-',
                                        'C+',
                                        'C',
                                        'C-',
                                        'D',
                                        'F' ) ),
   primary key ( perm,
                 enrollment_id ),
   foreign key ( perm )
      references student ( perm ),
   foreign key ( enrollment_id )
      references courseoffering_offeredin ( enrollment_id )
);

/* ---------- 11. Mandatory courses per major ---------- */
create table mandatory (
   majorname varchar2(100) not null,
   cno       varchar2(7) not null,
   primary key ( majorname,
                 cno ),
   foreign key ( majorname )
      references major_managedby ( majorname ),
   foreign key ( cno )
      references course ( cno )
);

/* ---------- 12. Elective courses per major ---------- */
create table elective (
   majorname varchar2(100) not null,
   cno       varchar2(7) not null,
   primary key ( majorname,
                 cno ),
   foreign key ( majorname )
      references major_managedby ( majorname ),
   foreign key ( cno )
      references course ( cno )
);

/* ---------- 13. scheduled_at (offering ↔ slot) ---------- */
create table scheduled_at (
   enrollment_id number not null,
   slot_id       number not null,
   primary key ( enrollment_id,
                 slot_id ),
   foreign key ( enrollment_id )
      references courseoffering_offeredin ( enrollment_id )
         on delete cascade,
   foreign key ( slot_id )
      references slot ( slotid )
);
