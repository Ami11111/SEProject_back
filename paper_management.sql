/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

# database paper_management
drop database if exists `paper_management`;
create database if not exists `paper_management`;
use `paper_management`;

# table user
drop table if exists `user`;
create table if not exists `user` (
  `id` int unsigned primary key comment '工号，主键',
  `password` varchar(25) not null,
  `name` varchar(50) default null comment '姓名，论文作者必须维护姓名信息',
  `email` varchar(50) default null,
  `phone` varchar(25) default null comment '电话，限制一个',
  `address` varchar(100) default null,
  index `id` (`id`) using btree
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

delete from `user`;
insert into `user` values
    (21808081, '8081', 'chen', 'chen@fudan.edu.cn', '15185199778', '逸夫楼701-2'),
    (21606060, '6060', 'xuan', 'xuan@m.fudan.edu.cn', '13199420064', '上海市虹口区松花江路2500号'),
    (21606061, '6061', 'xiang', 'xiang@m.fudan.edu.cn', '14819747682', '上海市虹口区松花江路2500号'),
    (21606062, '6062', 'feng', 'feng@m.fudan.edu.cn', '17852587345', '上海市虹口区松花江路2500号'),
    (21606063, '6063', 'liang', 'liang@m.fudan.edu.cn', '18188866886', '上海市虹口区松花江路2500号');

# table admin 
drop table if exists `admin`;
create table if not exists `admin` (
  `id` int unsigned primary key comment '工号，主键',
  `password` varchar(25) not null,
  index `id` (`id`) using btree
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

delete from `admin`;
insert into `admin` values
	(21808080, '8080');

# table paper
drop table if exists `paper`;
create table if not exists `paper` (
  `doi` varchar(255) primary key comment 'DOI',
  `title` varchar(255) comment '名称',
  `first_author` varchar(255) comment '第一作者，所有',
  `second_author` varchar(255) comment '第二作者，所有',
  `third_author` varchar(255) comment '第三作者，所有',
  `ccf` enum('A','B','C') comment 'CCF分区',
  `file_data` mediumblob comment 'PDF文件',
  `url` varchar(255) comment '下载链接',
  `status` enum('notSubmit','review','approve','reject') not null comment '未提交，审核中，通过，驳回',
  `recommend` text comment '选填，驳回意见',
  index `doi` (`doi`) using btree
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

delete from `paper`;
insert into `paper` values
('10.1016/j.artint.2023.104057','Evolving interpretable decision trees for reinforcement learning',
'chen,xiang','feng','liang','A',null,null,'notSubmit',null),
('10.1145/3626238','Design and Validation of a Virtual Reality Mental Rotation Test',
'feng','chen','liang','B',null,null,'review',null),
('10.1016/j.cviu.2018.10.002','Visual tracking in video sequencesbasedonbiologically inspired mechanisms',
'xiang','feng','liang','B',null,null,'approve',null),
('10.1016/j.ijar.2024.109266','On the enumeration of non-dominated matroids with imprecise weights',
'chen,liang',null,null,'B',null,null,'reject','内容不实'),
('10.1234/j.kmjb.2024.11.02','kmjb project research based on the evaluation from members',
'liang','xiang,feng','chen','B',null,null,'approve',null),
('10.1234/j.group.2024.11.11','Teamwork: group cooperation and division of labor',
'liang','feng','chen','C',null,null,'approve',null);

# table author_paper
drop table if exists `author_paper`;
create table if not exists `author_paper` (
 `a_id` int unsigned,
 `p_id` varchar(255),
 `seq` enum('first','second','third') not null comment '第一二三作者',
 primary key (`a_id`,`p_id`),
 constraint `author_paper1` foreign key(`a_id`) references `user`(`id`) on delete cascade on update cascade, 
 constraint `author_paper2` foreign key(`p_id`) references `paper`(`doi`) on delete cascade on update cascade, 
 index `a_id` (`a_id`) using btree,
 index `p_id` (`p_id`) using btree
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

delete from `author_paper`;
insert into `author_paper` values
(21808081,'10.1016/j.artint.2023.104057','first'),
(21606061,'10.1016/j.artint.2023.104057','first'),
(21606062,'10.1016/j.artint.2023.104057','second'),
(21606063,'10.1016/j.artint.2023.104057','third'),
(21606062,'10.1145/3626238','first'),
(21606061,'10.1145/3626238','second'),
(21606063,'10.1145/3626238','third'),
(21606061,'10.1016/j.cviu.2018.10.002','first'),
(21606062,'10.1016/j.cviu.2018.10.002','second'),
(21606063,'10.1016/j.cviu.2018.10.002','third'),
(21808081,'10.1016/j.ijar.2024.109266','first'),
(21606063,'10.1016/j.ijar.2024.109266','first'),
(21606063,'10.1234/j.kmjb.2024.11.02','first'),
(21606063,'10.1234/j.group.2024.11.11','first');


# table paper_additional
# correspondingAuthor：通讯作者
# pageCount：论文页数
# conferenceOrPeriodical：会议期刊全称
# acronym：会议期刊简称
# publisher：出版商
# fund：基金
# submitTime：提交时间
# receiptTime：接收时间
# publishTime：发表时间
# type：论文类型
drop table if exists `paper_additional`;
create table if not exists `paper_additional` (
  `doi` varchar(255) comment 'DOI',
  `key` enum('correspondingAuthor','pageCount','conferenceOrPeriodical','acronym',
  'publisher','fund','submitTime','receiptTime','publishTime','type') not null,
  `value` varchar(255) not null,
  primary key (`doi`,`key`),
  constraint `paper_additional1` foreign key(`doi`) references `paper`(`doi`) on delete cascade on update cascade, 
  index `doi` (`doi`) using btree
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

delete from `paper_additional`;
insert into `paper_additional` values
('10.1016/j.artint.2023.104057','pageCount','26'),
('10.1016/j.artint.2023.104057','conferenceOrPeriodical','Artificial Intelligence'),
('10.1016/j.artint.2023.104057','acronym','AI'),
('10.1016/j.artint.2023.104057','publisher','MDPI AG'),
('10.1016/j.artint.2023.104057','submitTime','2023-12-09'),
('10.1016/j.artint.2023.104057','receiptTime','2023-12-10'),
('10.1016/j.artint.2023.104057','publishTime','2023-12-16'),
('10.1016/j.artint.2023.104057','type','regular'),
('10.1145/3626238','pageCount','22'),
('10.1145/3626238','conferenceOrPeriodical','ACM Transactions on Applied Perception'),
('10.1145/3626238','acronym','TAP'),
('10.1145/3626238','publisher','Association for Computing Machinery'),
('10.1145/3626238','publishTime','2024-01-01'),
('10.1145/3626238','type','regular'),
('10.1016/j.cviu.2018.10.002','pageCount','22'),
('10.1016/j.cviu.2018.10.002','conferenceOrPeriodical','Computer Vision and Image Understanding'),
('10.1016/j.cviu.2018.10.002','acronym','CVIU'),
('10.1016/j.cviu.2018.10.002','publisher','Academic Press Inc.'),
('10.1016/j.cviu.2018.10.002','submitTime','2018-10-05'),
('10.1016/j.cviu.2018.10.002','receiptTime','2018-10-14'),
('10.1016/j.cviu.2018.10.002','publishTime','2018-10-26'),
('10.1016/j.cviu.2018.10.002','type','regular'),
('10.1016/j.ijar.2024.109266','pageCount','23'),
('10.1016/j.ijar.2024.109266','conferenceOrPeriodical','International Journal of Approximate Reasoning'),
('10.1016/j.ijar.2024.109266','acronym','IJAR'),
('10.1016/j.ijar.2024.109266','publisher','Elsevier'),
('10.1016/j.ijar.2024.109266','submitTime','2024-06-24'),
('10.1016/j.ijar.2024.109266','receiptTime','2024-07-31'),
('10.1016/j.ijar.2024.109266','publishTime','2024-08-08'),
('10.1016/j.ijar.2024.109266','type','regular');


# table delete_requests
drop table if exists `delete_requests`;
create table if not exists `delete_requests` (
    `request_id` INT AUTO_INCREMENT PRIMARY KEY comment '请求ID，自动递增的主键',
    `user_id` INT unsigned NULL comment '用户ID，用于标识哪个用户发起了请求',
    `doi` VARCHAR(255) NOT NULL comment '论文的DOI，唯一标识论文',
	FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
	FOREIGN KEY (`doi`) REFERENCES `paper` (`doi`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

delete from `delete_requests`;
insert into `delete_requests` (`user_id`, `doi`) values
(21606063,'10.1234/j.group.2024.11.11');


# table author_paper_claim
drop table if exists `author_paper_claim`;
CREATE TABLE IF NOT EXISTS `author_paper_claim` (
`author_id` INT UNSIGNED NOT NULL COMMENT '用户ID，对应user表中的id',
`paper_doi` VARCHAR(255) NOT NULL COMMENT '论文DOI，对应paper表中的doi',
PRIMARY KEY (`author_id`, `paper_doi`),
FOREIGN KEY (`author_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
FOREIGN KEY (`paper_doi`) REFERENCES `paper` (`doi`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

delete from `author_paper_claim`;
insert into `author_paper_claim` values
(21606061,'10.1234/j.kmjb.2024.11.02'),
(21606062,'10.1234/j.kmjb.2024.11.02'),
(21808080,'10.1234/j.kmjb.2024.11.02');


/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;