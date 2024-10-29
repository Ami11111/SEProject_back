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
  `role` bit(1) default b'0' comment '0表示普通教职工，1表示管理员'
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

delete from `user`;
insert into `user` values
	(21808080, '8080', null, null, null, null, 1),
    (21808081, '8081', 'chen', 'chen@fudan.edu.cn', '13998899889', '逸夫楼701-2', 0),
    (21606060, '6060', 'xuan', 'xuan@m.fudan.edu.cn', null, null, 0),
    (21606061, '6061', 'xiang', null, null, null, 0),
    (21606062, '6062', 'feng', null, null, null, 0),
    (21606063, '6063', 'liang', null, '18188866886', null, 0);
  
# table paper
drop table if exists `paper`;
create table if not exists `paper` (
  `doi` varchar(255) primary key comment 'DOI',
  `title` varchar(255) not null comment '名称',
  `author_names` varchar(255) not null comment '作者列表',
  `first_author` varchar(255) not null comment '第一作者，所有',
  `corresponding_author` varchar(255) comment '通讯作者',
  `page_count` int comment '论文页数',
  `conference_or_periodical` varchar(255) comment '会议期刊全称',
  `acronym` varchar(50) comment '会议期刊简称',
  `publisher` varchar(255) comment '出版商',
  `ccf` enum('A','B','C') comment 'CCF分区',
  `fund` varchar(255) comment '基金',
  `submit_time` date comment '提交时间',
  `receipt_time` date comment '接收时间',
  `publish_time` date comment '发表时间',
  `type` enum('regular','short','demo','poster') comment '类型',
  `file_data` mediumblob comment 'PDF文件',
  `status` enum('not_submit','review','approve','reject') not null comment '未提交，审核中，通过，驳回',
  `recommend` text comment '选填，驳回意见'
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

delete from `paper`;
insert into `paper` values
('10.1016/j.artint.2023.104057','Evolving interpretable decision trees for reinforcement learning',
'chen,xiang;feng;liang;','chen,xiang;',null,26,'Artificial Intelligence','AI','MDPI AG','A',
null,'2023-12-09','2023-12-10','2023-12-16','regular',null,'not_submit',null),
('10.1145/3626238','Design and Validation of a Virtual Reality Mental Rotation Test',
'feng;chen;liang;','feng',null,22,'ACM Transactions on Applied Perception','TAP','Association for Computing Machinery','B',
null,null,null,'2024-01-01','regular',null,'review',null),
('10.1016/j.cviu.2018.10.002','Visual tracking in video sequencesbasedonbiologically inspired mechanisms',
'xiang;feng;liang;','xiang',null,22,'Computer Vision and Image Understanding','CVIU','Academic Press Inc.','B',
null,'2018-10-05','2018-10-14','2018-10-26','regular',null,'approve',null),
('10.1016/j.ijar.2024.109266','On the enumeration of non-dominated matroids with imprecise weights',
'chen,liang;','chen,liang',null,23,'International Journal of Approximate Reasoning','IJAR','Elsevier','B',
null,'2024-06-24','2024-07-31','2024-08-08','regular',null,'reject','内容不实');

# table author_paper
drop table if exists `author_paper`;
create table if not exists `author_paper` (
 `a_id` int unsigned,
 `p_id` varchar(255),
 `seq` enum('first','second','third') not null comment '第一二三作者',
 primary key (`a_id`,`p_id`),
 constraint `author_paper1` foreign key(`a_id`) references `user`(`id`), 
 constraint `author_paper2` foreign key(`p_id`) references `paper`(`doi`), 
 index `a_id` (`a_id`) USING BTREE
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
(21606063,'10.1016/j.ijar.2024.109266','first');


/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
