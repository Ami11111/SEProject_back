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
  `username` varchar(50) default null comment '姓名，论文作者必须维护姓名信息',
  `email` varchar(50) default null,
  `phone` varchar(25) default null comment '电话，限制一个',
  `address` varchar(100) default null,
  `role` bit(1) default b'0' comment '0表示普通教职工，1表示管理员'
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

delete from `user`;
insert into `user` values
	(21808080, '8080', null, null, null, null, 1),
    (21808081, '8081', 'chen', 'chen@fudan.edu.cn', '13998899889', '逸夫楼701-2', 1),
    (21606060, '6060', 'xuan', 'xuan@m.fudan.edu.cn', null, null, 0),
    (21606061, '6061', 'xiang', null, null, null, 0),
    (21606062, '6062', 'feng', null, null, null, 0),
    (21606063, '6063', 'liang', null, '18188866886', null, 0);
  
# table paper
drop table if exists `paper`;
create table if not exists `paper` (
  `id` varchar(50) primary key,
  `title` varchar(255) not null,
  `Abstract` text comment '摘要',
  `keywords` varchar(255) comment '关键词',
  `publication_data` date comment '发表时间',
  `page_count` int comment '论文页数',
  `meeting` varchar(255) comment '会议名称',
  `periodical` varchar(255) comment '期刊名称',
  `publisher` varchar(255) comment '出版商',
  `status` bit(2) default b'00' comment '未提交00，待审,01，通过10，驳回11',
  `recommend` text comment '11选填，驳回意见'
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

delete from `paper`;
insert into `paper` values
('F592;G353.1','基于山茶产业分析的旅游营销策略研究','balabala','山茶；旅游；营销策略','2023-01-10',9,null,null,null,b'00',null),
('F274;F426.82','次优石榴干预营销的有效性：来自石榴村的证据','shiliushiliu','石榴；干预','2020-09-11',13,null,null,null,b'01',null),
('G899;I207.41','从石猴到黑神话：悟空的形象变迁与跨媒介叙事','sunwukongsunwukong','黑神话；悟空','2024-08-23',99,null,'哈哈游月刊','哈哈游出版社',b'10',null),
('F124.3','论生产力：特征与发力点','chanchanchan','生产力；特征；发力点','2020-12-27',10,'附中年会',null,null,b'11','内容不实');

# table author_paper
drop table if exists `author_paper`;
create table if not exists `author_paper` (
 `a_id` int unsigned,
 `p_id` varchar(50),
 `seq` bit(2) not null comment '第一二三作者00,01,10',
 primary key (`a_id`,`p_id`),
 constraint `author_paper1` foreign key(`a_id`) references `user`(`id`), 
 constraint `author_paper2` foreign key(`p_id`) references `paper`(`id`), 
 index `a_id` (`a_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

delete from `author_paper`;
insert into `author_paper` values
(21808081,'F592;G353.1',b'00'),
(21606061,'F592;G353.1',b'00'),
(21606062,'F274;F426.82',b'00'),
(21808081,'F274;F426.82',b'01'),
(21606063,'F274;F426.82',b'10'),
(21606061,'G899;I207.41',b'00'),
(21606062,'G899;I207.41',b'01'),
(21606063,'G899;I207.41',b'10'),
(21808081,'F124.3',b'00'),
(21606063,'F124.3',b'00');


/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
