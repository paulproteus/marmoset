
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


ALTER TABLE  `review_requests` ADD  `when` DATETIME NOT NULL DEFAULT  '2012/02/01',
ADD  `accepted` TINYINT( 1 ) NOT NULL DEFAULT  '0';

RENAME TABLE  `submitserver`.`review_requests` TO  `submitserver`.`help_requests` ;

ALTER TABLE  `code_review_thread` CHANGE  `file`  `file` VARCHAR( 120 ) CHARACTER SET utf8 COLLATE utf8_general_ci NULL;
