------------------------------------------------------
-- Export file for user TYYW                        --
-- Created by Administrator on 2019-11-09, 22:28:55 --
------------------------------------------------------

create table SJFH_FILE_NEWEST
(
    ID       VARCHAR2(32) default sys_guid(),
    FILEPATH VARCHAR2(500),
    TYPE     CHAR(4),
    FILETYPE CHAR(1),
    ZHXGSJ   DATE,
    CJSJ     DATE
)
;
comment on table SJFH_FILE_NEWEST
    is '用于存放有更改或者添加的电子卷宗和文书';
comment on column SJFH_FILE_NEWEST.ID
    is '文件序号';
comment on column SJFH_FILE_NEWEST.FILEPATH
    is '文件路径';
comment on column SJFH_FILE_NEWEST.TYPE
    is '文件类型d：电子卷宗；w：文书';
comment on column SJFH_FILE_NEWEST.FILETYPE
    is '案件类别';
comment on column SJFH_FILE_NEWEST.ZHXGSJ
    is '文件最后修改时间';
comment on column SJFH_FILE_NEWEST.CJSJ
    is '文件创建时间';
create index IDX_SJFH_FILE_NEWEST on SJFH_FILE_NEWEST (ID);


create table SJFH_FILE_NOFILE
(
    ID       VARCHAR2(32) default sys_guid() not null,
    FILEPATH VARCHAR2(500),
    TYPE     CHAR(4),
    FILETYPE CHAR(1),
    ZHXGSJ   DATE,
    CJSJ     DATE,
    FINDTIME NUMBER default 0
)
;
comment on table SJFH_FILE_NOFILE
    is '用于存放未找到的电子卷宗和文书';
comment on column SJFH_FILE_NOFILE.ID
    is '文件序号';
comment on column SJFH_FILE_NOFILE.FILEPATH
    is '文件路径';
comment on column SJFH_FILE_NOFILE.TYPE
    is '文件类型d：电子卷宗；w：文书';
comment on column SJFH_FILE_NOFILE.FILETYPE
    is '案件类别';
comment on column SJFH_FILE_NOFILE.ZHXGSJ
    is '文件最后修改时间';
comment on column SJFH_FILE_NOFILE.CJSJ
    is '文件创建时间';
comment on column SJFH_FILE_NOFILE.FINDTIME
    is '查找次数';
create index IDX_SJFH_FILE_NOFILE on SJFH_FILE_NOFILE (ID);

create table SJFH_FILE_DOWNLOADED
(
    BMSAH    VARCHAR2(100),
    FILEPATH VARCHAR2(500),
    FILETYPE CHAR(1)
);
-- Add comments to the table
comment on table SJFH_FILE_DOWNLOADED
    is '本地已下载文件';
-- Add comments to the columns
comment on column SJFH_FILE_DOWNLOADED.BMSAH
    is '部门受案号';
comment on column SJFH_FILE_DOWNLOADED.FILEPATH
    is '文件路径';
comment on column SJFH_FILE_DOWNLOADED.FILETYPE
    is '文件类型d：电子卷宗；w：文书';

CREATE OR REPLACE TRIGGER file_dzjz_update
    AFTER INSERT OR UPDATE ON yx_dzjz_jzmlwj
    FOR EACH ROW
    /**电子卷宗触发器（当yx_dzjz_jzmlwj表中添加或者修改电子卷宗后，将会添加到sjfh_file_newest表，然后软件会处理表中数据）*/
DECLARE
    MYTYPE varchar2(100);
BEGIN
    select ajlb_bm into MYTYPE from tyyw_gg_ajjbxx where bmsah = :NEW.BMSAH;
    INSERT INTO sjfh_file_newest(filePath,TYPE,fileType,ZHXGSJ,CJSJ) VALUES (:NEW.wjlj||'\'||:NEW.wjmc||:NEW.wjhz||'.encry',MYTYPE,'d',:NEW.ZHXGSJ,:NEW.CJSJ);
END;
/

CREATE OR REPLACE TRIGGER file_ws_update
    AFTER INSERT OR UPDATE ON YX_WS_AJJZWJ
    FOR EACH ROW
    /**文书触发器（当YX_WS_AJJZWJ 表中添加或者修改文书后，将会添加到sjfh_file_newest表，然后软件会处理表中数据）*/
DECLARE
    MYTYPE varchar2(100);
BEGIN
    select ajlb_bm into MYTYPE from tyyw_gg_ajjbxx where bmsah = :NEW.BMSAH;
    INSERT INTO sjfh_file_newest(filePath,TYPE,fileType,ZHXGSJ,CJSJ) VALUES (:NEW.WSCFLJ,MYTYPE,'w',:NEW.ZHXGSJ,:NEW.CJSJ);
END;
/


