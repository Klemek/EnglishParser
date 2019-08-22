drop table if exists `wn_antonym`;
CREATE TABLE `wn_antonym` (
  `synset_id_1` decimal(10,0) default NULL,
  `wnum_1` decimal(10,0) default NULL,
  `synset_id_2` decimal(10,0) default NULL,
  `wnum_2` decimal(10,0) default NULL,
  KEY `synset_id_1` (`synset_id_1`),
  KEY `synset_id_2` (`synset_id_2`),
  KEY `wnum_1` (`wnum_1`),
  KEY `wnum_2` (`wnum_2`)
) ENGINE=MyISAM;

drop table if exists `wn_attr_adj_noun`;
CREATE TABLE `wn_attr_adj_noun` (
  `synset_id_1` decimal(10,0) default NULL,
  `synset_id_2` decimal(10,0) default NULL,
  KEY `synset_id_1` (`synset_id_1`),
  KEY `synset_id_2` (`synset_id_2`)
) ENGINE=MyISAM;

drop table if exists `wn_cause`;
CREATE TABLE `wn_cause` (
  `synset_id_1` decimal(10,0) default NULL,
  `synset_id_2` decimal(10,0) default NULL,
  KEY `synset_id_1` (`synset_id_1`),
  KEY `synset_id_2` (`synset_id_2`)
) ENGINE=MyISAM;

drop table if exists `wn_class_member`;
CREATE TABLE `wn_class_member` (
  `synset_id_1` decimal(10,0) default NULL,
  `synset_id_2` decimal(10,0) default NULL,
  `class_type` char(2) default NULL,
  KEY `synset_id_1` (`synset_id_1`),
  KEY `synset_id_2` (`synset_id_2`)
) ENGINE=MyISAM;

drop table if exists `wn_derived`;
CREATE TABLE `wn_derived` (
  `synset_id_1` decimal(10,0) default NULL,
  `wnum_1` decimal(10,0) default NULL,
  `synset_id_2` decimal(10,0) default NULL,
  `wnum_2` decimal(10,0) default NULL,
  KEY `synset_id_1` (`synset_id_1`),
  KEY `synset_id_2` (`synset_id_2`),
  KEY `wnum_1` (`wnum_1`),
  KEY `wnum_2` (`wnum_2`)
) ENGINE=MyISAM;

drop table if exists `wn_entails`;
CREATE TABLE `wn_entails` (
  `synset_id_1` decimal(10,0) default NULL,
  `synset_id_2` decimal(10,0) default NULL,
  KEY `synset_id_1` (`synset_id_1`),
  KEY `synset_id_2` (`synset_id_2`)
) ENGINE=MyISAM;

drop table if exists `wn_gloss`;
CREATE TABLE `wn_gloss` (
  `synset_id` decimal(10,0) NOT NULL default '0',
  `gloss` varchar(255) default NULL,
  PRIMARY KEY  (`synset_id`),
  FULLTEXT KEY `gloss` (`gloss`)
) ENGINE=MyISAM;

drop table if exists `wn_hypernym`;
CREATE TABLE `wn_hypernym` (
  `synset_id_1` decimal(10,0) default NULL,
  `synset_id_2` decimal(10,0) default NULL,
  KEY `synset_id_1` (`synset_id_1`),
  KEY `synset_id_2` (`synset_id_2`)
) ENGINE=MyISAM;

drop table if exists `wn_hyponym`;
CREATE TABLE `wn_hyponym` (
  `synset_id_1` decimal(10,0) default NULL,
  `synset_id_2` decimal(10,0) default NULL,
  KEY `synset_id_1` (`synset_id_1`),
  KEY `synset_id_2` (`synset_id_2`)
) ENGINE=MyISAM;

drop table if exists `wn_mbr_meronym`;
CREATE TABLE `wn_mbr_meronym` (
  `synset_id_1` decimal(10,0) default NULL,
  `synset_id_2` decimal(10,0) default NULL,
  KEY `synset_id_1` (`synset_id_1`),
  KEY `synset_id_2` (`synset_id_2`)
) ENGINE=MyISAM;

drop table if exists `wn_part_meronym`;
CREATE TABLE `wn_part_meronym` (
  `synset_id_1` decimal(10,0) default NULL,
  `synset_id_2` decimal(10,0) default NULL,
  KEY `synset_id_1` (`synset_id_1`),
  KEY `synset_id_2` (`synset_id_2`)
) ENGINE=MyISAM;

drop table if exists `wn_participle`;
CREATE TABLE `wn_participle` (
  `synset_id_1` decimal(10,0) default NULL,
  `wnum_1` decimal(10,0) default NULL,
  `synset_id_2` decimal(10,0) default NULL,
  `wnum_2` decimal(10,0) default NULL,
  KEY `synset_id_1` (`synset_id_1`),
  KEY `synset_id_2` (`synset_id_2`),
  KEY `wnum_1` (`wnum_1`),
  KEY `wnum_2` (`wnum_2`)
) ENGINE=MyISAM;

drop table if exists `wn_pertainym`;
CREATE TABLE `wn_pertainym` (
  `synset_id_1` decimal(10,0) default NULL,
  `wnum_1` decimal(10,0) default NULL,
  `synset_id_2` decimal(10,0) default NULL,
  `wnum_2` decimal(10,0) default NULL,
  KEY `synset_id_1` (`synset_id_1`),
  KEY `synset_id_2` (`synset_id_2`),
  KEY `wnum_1` (`wnum_1`),
  KEY `wnum_2` (`wnum_2`)
) ENGINE=MyISAM;

drop table if exists `wn_see_also`;
CREATE TABLE `wn_see_also` (
  `synset_id_1` decimal(10,0) default NULL,
  `wnum_1` decimal(10,0) default NULL,
  `synset_id_2` decimal(10,0) default NULL,
  `wnum_2` decimal(10,0) default NULL,
  KEY `synset_id_1` (`synset_id_1`),
  KEY `synset_id_2` (`synset_id_2`),
  KEY `wnum_1` (`wnum_1`),
  KEY `wnum_2` (`wnum_2`)
) ENGINE=MyISAM;

drop table if exists `wn_similar`;
CREATE TABLE `wn_similar` (
  `synset_id_1` decimal(10,0) default NULL,
  `synset_id_2` decimal(10,0) default NULL,
  KEY `synset_id_1` (`synset_id_1`),
  KEY `synset_id_2` (`synset_id_2`)
) ENGINE=MyISAM;

drop table if exists `wn_subst_meronym`;
CREATE TABLE `wn_subst_meronym` (
  `synset_id_1` decimal(10,0) default NULL,
  `synset_id_2` decimal(10,0) default NULL,
  KEY `synset_id_1` (`synset_id_1`),
  KEY `synset_id_2` (`synset_id_2`)
) ENGINE=MyISAM;

drop table if exists `wn_synset`;
CREATE TABLE `wn_synset` (
  `synset_id` decimal(10,0) NOT NULL default '0',
  `w_num` decimal(10,0) NOT NULL default '0',
  `word` varchar(50) default NULL,
  `ss_type` char(2) default NULL,
  `sense_number` decimal(10,0) NOT NULL default '0',
  `tag_count` decimal(10,0) default NULL,
  PRIMARY KEY  (`synset_id`,`w_num`),
  KEY `synset_id` (`synset_id`),
  KEY `w_num` (`w_num`),
  KEY `word` (`word`)
) ENGINE=MyISAM;

drop table if exists `wn_verb_frame`;
CREATE TABLE `wn_verb_frame` (
  `synset_id_1` decimal(10,0) default NULL,
  `f_num` decimal(10,0) default NULL,
  `w_num` decimal(10,0) default NULL,
  KEY `synset_id_1` (`synset_id_1`),
  KEY `f_num` (`f_num`),
  KEY `w_num` (`w_num`)
) ENGINE=MyISAM;

drop table if exists `wn_verb_group`;
CREATE TABLE `wn_verb_group` (
  `synset_id_1` decimal(10,0) default NULL,
  `synset_id_2` decimal(10,0) default NULL,
  KEY `synset_id_1` (`synset_id_1`),
  KEY `synset_id_2` (`synset_id_2`)
) ENGINE=MyISAM;