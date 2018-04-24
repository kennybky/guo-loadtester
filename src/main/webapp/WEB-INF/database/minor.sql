 delimiter ;;
    drop procedure if exists stats.editAvg;;
    create procedure stats.editAvg()
    begin
    DECLARE n INT DEFAULT 0;
     DECLARE i INT DEFAULT 0;
     select min(id), max(id) from stats.projects into i, n;
     set n = n +1;

     WHILE i < n do
     update stats.projects
		set avgResponseTime = (select avg(roundTripTime) from stats.deltas
		where projectid = i), requestCount = (select count(id) from stats.deltas
		where projectid = i) , failedRequests = (select count(id) from stats.deltas
		where projectid = i and (statusCode < 200 or statusCode > 300))  where id = i;
        set i = i +1;
     end while;
    end
    ;;

    call stats.editAvg();


ALTER TABLE stats.project;
ADD method varchar(20) DEFAULT 'GET'

alter table stats.webstats
ADD CONSTRAINT FK_id
FOREIGN KEY (projectid) REFERENCES stats.webprojects(id) on delete cascade;



alter table stats.deltas
DROP foreign key deltas_ibfk_1;

delete from stats.deltas where projectid not in (select id from stats.projects) and id > -1;

alter table stats.deltas
ADD CONSTRAINT deltas_ibfk_1
FOREIGN KEY (projectid) REFERENCES stats.projects(id) on delete cascade;



