
# Changes 2/22/2012
# Allow file to be null, and upto 200 characters

ALTER TABLE  `code_review_thread` CHANGE  `file`  `file` VARCHAR( 200 ) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL;

CREATE TABLE `review_requests` (
`submission_pk` INT NOT NULL ,
`course_pk` INT NOT NULL ,
INDEX (  `course_pk` ) ,
UNIQUE (
`submission_pk`
)
) ENGINE = INNODB;

