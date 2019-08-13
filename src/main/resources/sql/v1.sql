CREATE TABLE `db_info` (
	`version` SMALLINT NOT NULL DEFAULT 0,
	`update_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP(),
	`dict_init` ENUM('0','1') DEFAULT '0'
);

CREATE TABLE `dict_word` (
                             `word`       VARCHAR(255) NOT NULL,
                             `type`       SMALLINT     NOT NULL,
                             `syn_set_id` INTEGER      NOT NULL,
                             `word_num`   SMALLINT     NOT NULL,
                             PRIMARY KEY (`syn_set_id`, `word_num`)
);

CREATE TABLE `dict_def` (
    `syn_set_id` INTEGER NOT NULL,
    `definition` TEXT NOT NULL,
    PRIMARY KEY (`syn_set_id`),
    CONSTRAINT `FK_DefWord` FOREIGN KEY (`syn_set_id`) REFERENCES `dict_word`(`syn_set_id`) ON DELETE CASCADE
);

CREATE TABLE `dict_noun` (
                             `base`          VARCHAR(255) NOT NULL,
                             `plural`        VARCHAR(255) NOT NULL,
                             `male`          BIT          NOT NULL,
                             `female`        VARCHAR(255),
                             `female_plural` VARCHAR(255),
                             PRIMARY KEY (`base`, `plural`)
);

CREATE INDEX `IDX_Noun1` ON `dict_noun`(`base`,`plural`);
CREATE INDEX `IDX_Noun2` ON `dict_noun`(`female`);

CREATE TABLE `dict_verb` (
                             `base`       VARCHAR(255) NOT NULL,
                             `past_tense` VARCHAR(255) NOT NULL,
                             `past_part`  VARCHAR(255) NOT NULL,
                             `pres_part`  VARCHAR(255) NOT NULL,
                             `third_pers` VARCHAR(255) NOT NULL,
                             PRIMARY KEY (`base`, `past_tense`, `past_part`)
);

CREATE TABLE `dict_adj` (
                            `base`   VARCHAR(255) NOT NULL,
                            `adverb` VARCHAR(255) NOT NULL,
                            PRIMARY KEY (`base`, `adverb`)
);

INSERT INTO `db_info` () VALUES ();