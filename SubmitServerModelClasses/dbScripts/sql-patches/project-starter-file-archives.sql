ALTER TABLE `projects` ADD `archive_pk` MEDIUMINT(8) UNSIGNED DEFAULT NULL ;

CREATE TABLE `project_starter_file_archives` (
  `archive_pk` mediumint(8) unsigned NOT NULL auto_increment,
  `archive` mediumblob NOT NULL,
  PRIMARY KEY  (`archive_pk`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
        