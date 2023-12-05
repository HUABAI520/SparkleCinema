create table team
(
    id          bigint auto_increment comment '队伍id'
        primary key,
    teamName    varchar(256)                       null comment '队伍名',
    description varchar(1024)                      null comment '队伍描述',
    maxNum      int      default 1                 not null comment '队伍最大人数',
    expireTime  datetime                           null comment '队伍过期时间',
    userId      bigint comment '用户id',
    status      int      default 0                 not null comment '队伍状态 0-正常 1-异常',
    password    varchar(512)                       null comment '队伍密码',
    createTime  datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint  default 0                 not null comment '是否删除  0-未删除 1-删除'
)
    comment '队伍';

-- 队伍成员表

create table teamMember
(
    id         bigint auto_increment comment 'id'
        primary key,
    teamId     bigint comment '队伍id',
    userId     bigint comment '用户id',
    joinTime   datetime                           null comment '加入时间',
    createTime datetime default CURRENT_TIMESTAMP null comment '队伍创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除  0-未删除 1-删除'
)
    comment '队伍成员关系表';