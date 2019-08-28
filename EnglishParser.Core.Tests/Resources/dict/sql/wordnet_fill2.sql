SET FOREIGN_KEY_CHECKS=0;
SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET AUTOCOMMIT = 0;
START TRANSACTION;
SET time_zone = "+00:00";

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

INSERT INTO `wn_synset` (`synset_id`, `w_num`, `word`, `ss_type`, `sense_number`, `tag_count`) VALUES
/* normal nouns */
(100001740,1,'entity','n',1,11),
(101561368,1,'frog','n',1,2),
(101561368,2,'toad','n',1,4),
(103274132,1,'frog','n',3,0),
(103274132,2,'frogs','n',1,0),
/* gender nouns */
(109095182,1,'Frenchman','n',1,5),
(109095182,2,'Frenchwoman','n',1,0),
/* irregular plural noun */
(101570679,1,'spadefoot','n',1,0),
/* noun with space */
(109095182,3,'French_person','n',1,0),
/* adjectives */
(300014951,1,'easy','s',15,0),
(300805869,1,'economic','s',5,19),
(300003777,1,'dying(a)','a',1,2),
/* irregular adverb adjective */
(301798961,1,'nonpublic','s',1,0),
/* adverbs */
(400012225,2,'easily','r',3,0),
(400125362,1,'economically','r',1,3),
/* normal verbs */
(200006000,1,'hack','v',8,0),
/* irragular verbs */
(200010141,3,'do','v',9,3),
(200206385,1,'can','v',1,2);
/* definitions */

INSERT INTO `wn_gloss` (`synset_id`, `gloss`) VALUES
(100001740,'that which is perceived or known or inferred to have its own distinct existence (living or nonliving)'),
(101561368,'any of various tailless stout-bodied amphibians with long hind limbs for leaping; semiaquatic and terrestrial species'),
(103274132,'a decorative loop of braid or cord'),
(109095182,'a person of French nationality'),
(200015400,'sleep longer than intended');

SET FOREIGN_KEY_CHECKS=1;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;