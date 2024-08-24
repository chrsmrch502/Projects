CREATE TABLE `events`
(
    `EventID`            int NOT NULL AUTO_INCREMENT,
    `EventOwner`         int               DEFAULT 0,
    `EventPriority`      int NOT NULL,
    `ImagePath`          nvarchar(5000)    DEFAULT NULL,
    `EventName`          nvarchar(100)     DEFAULT NULL,
    `EventDescription`   nvarchar(5000)    DEFAULT NULL,
    `StartYear`          bigint            DEFAULT NULL,
    `StartMonth`         tinyint unsigned  DEFAULT NULL,
    `StartDay`           tinyint unsigned  DEFAULT NULL,
    `StartHour`          tinyint unsigned  DEFAULT NULL,
    `StartMinute`        tinyint unsigned  DEFAULT NULL,
    `StartSecond`        tinyint unsigned  DEFAULT NULL,
    `StartMillisecond`   smallint unsigned DEFAULT NULL,
    `EndYear`            bigint            DEFAULT NULL,
    `EndMonth`           tinyint unsigned  DEFAULT NULL,
    `EndDay`             tinyint unsigned  DEFAULT NULL,
    `EndHour`            tinyint unsigned  DEFAULT NULL,
    `EndMinute`          tinyint unsigned  DEFAULT NULL,
    `EndSecond`          tinyint unsigned  DEFAULT NULL,
    `EndMillisecond`     smallint unsigned DEFAULT NULL,
    `CreatedYear`        bigint            DEFAULT NULL,
    `CreatedMonth`       tinyint unsigned  DEFAULT NULL,
    `CreatedDay`         tinyint unsigned  DEFAULT NULL,
    `CreatedHour`        tinyint unsigned  DEFAULT NULL,
    `CreatedMinute`      tinyint unsigned  DEFAULT NULL,
    `CreatedSecond`      tinyint unsigned  DEFAULT NULL,
    `CreatedMillisecond` smallint unsigned DEFAULT NULL,
    PRIMARY KEY (`EventID`),
    UNIQUE KEY `EventID_UNIQUE` (`EventID`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;


CREATE TRIGGER `CreatedDateTimeEvents`
    BEFORE INSERT
    ON `events`
    FOR EACH ROW
BEGIN
    if (isnull(new.`CreatedYear`)) then
        set new.`CreatedYear` = YEAR(NOW());
        set new.`CreatedMonth` = MONTH(NOW());
        set new.`CreatedDay` = DAY(NOW());
        set new.`CreatedHour` = HOUR(NOW());
        set new.`CreatedMinute` = MINUTE(NOW());
        set new.`CreatedSecond` = SECOND(NOW());
        set new.`CreatedMillisecond` = CAST(UNIX_TIMESTAMP(CURTIME(3)) % 1 * 1000 AS unsigned);
    end if;
END;


CREATE TRIGGER `EndDate`
    BEFORE INSERT
    ON `events`
    FOR EACH ROW
BEGIN
    if (isnull(new.`EndYear`)) then
        set new.`EndYear` = new.StartYear;
        set new.`EndMonth` = new.StartMonth;
        set new.`EndDay` = new.StartDay;
        set new.`EndHour` = new.StartHour;
        set new.`EndMinute` = new.StartMinute;
        set new.`EndSecond` = new.StartSecond;
        set new.`EndMillisecond` = new.StartMillisecond;
    end if;
END;


-- Lookup table for the scale column of timeline table
CREATE TABLE `scales`
(
    `ID`   int          NOT NULL AUTO_INCREMENT,
    `Unit` nvarchar(20) NOT NULL,
    PRIMARY KEY (`ID`)
)
    ENGINE = InnoDB
    AUTO_INCREMENT = 9
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci;


INSERT INTO `scales`
(`ID`,
 `Unit`)
VALUES (1, 'Milliseconds'),
       (2, 'Seconds'),
       (3, 'Minutes'),
       (4, 'Hours'),
       (5, 'Days'),
       (6, 'Weeks'),
       (7, 'Months'),
       (8, 'Years'),
       (9, 'Decades'),
       (10, 'Centuries'),
       (11, 'Millennia');


CREATE TABLE `users`
(
    `UserID`    int           NOT NULL AUTO_INCREMENT,
    `UserName`  nvarchar(100) DEFAULT NULL,
    `UserEmail` nvarchar(100) NOT NULL,
    `Password`  nvarchar(90)  NOT NULL,
    `Salt`      nvarchar(30)  NOT NULL,
    `Admin`     tinyint       DEFAULT '0',
    `Theme`     nvarchar(20)  DEFAULT 'Default',
    PRIMARY KEY (`UserID`),
    UNIQUE KEY `UserID_UNIQUE` (`UserID`),
    UNIQUE KEY `UserEmail_UNIQUE` (`UserEmail`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;


CREATE TABLE `timelines`
(
    `TimelineID`          int               NOT NULL AUTO_INCREMENT,
    `Scale`               int               DEFAULT 8,
    `TimelineName`        nvarchar(100)     DEFAULT NULL,
    `TimelineDescription` nvarchar(5000)    DEFAULT NULL,
    `ImagePath`           nvarchar(5000)    DEFAULT NULL,
    `StartYear`           bigint            NOT NULL,
    `StartMonth`          tinyint unsigned  NOT NULL,
    `StartDay`            tinyint unsigned  NOT NULL,
    `StartHour`           tinyint unsigned  NOT NULL,
    `StartMinute`         tinyint unsigned  NOT NULL,
    `StartSecond`         tinyint unsigned  NOT NULL,
    `StartMillisecond`    smallint unsigned NOT NULL,
    `EndYear`             bigint            DEFAULT NULL,
    `EndMonth`            tinyint unsigned  DEFAULT NULL,
    `EndDay`              tinyint unsigned  DEFAULT NULL,
    `EndHour`             tinyint unsigned  DEFAULT NULL,
    `EndMinute`           tinyint unsigned  DEFAULT NULL,
    `EndSecond`           tinyint unsigned  DEFAULT NULL,
    `EndMillisecond`      smallint unsigned DEFAULT NULL,
    `CreatedYear`         bigint            DEFAULT NULL,
    `CreatedMonth`        tinyint unsigned  DEFAULT NULL,
    `CreatedDay`          tinyint unsigned  DEFAULT NULL,
    `CreatedHour`         tinyint unsigned  DEFAULT NULL,
    `CreatedMinute`       tinyint unsigned  DEFAULT NULL,
    `CreatedSecond`       tinyint unsigned  DEFAULT NULL,
    `CreatedMillisecond`  smallint unsigned DEFAULT NULL,
    `TimelineOwner`       int               NOT NULL,
    `Keywords`            varchar(1000)     DEFAULT NULL,
    PRIMARY KEY (`TimelineID`),
    UNIQUE KEY `TimelineID_UNIQUE` (`TimelineID`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;


CREATE TABLE `timelineevents`
(
    TimelineID int NOT NULL,
    EventID    int NOT NULL,
    CONSTRAINT pK_timelinesevent PRIMARY KEY (eventID, timelineID),
    CONSTRAINT fk_timelineevents_events1
        FOREIGN KEY (EventID)
            REFERENCES events (EventID)
            ON DELETE CASCADE,
    CONSTRAINT fk_timelineevents_timelines
        FOREIGN KEY (TimelineID)
            REFERENCES timelines (TimelineID)
            ON DELETE CASCADE
);


CREATE TRIGGER `CreatedDateTime`
    BEFORE INSERT
    ON `timelines`
    FOR EACH ROW
BEGIN
    if (isnull(new.`CreatedYear`)) then
        set new.`CreatedYear` = YEAR(NOW());
        set new.`CreatedMonth` = MONTH(NOW());
        set new.`CreatedDay` = DAY(NOW());
        set new.`CreatedHour` = HOUR(NOW());
        set new.`CreatedMinute` = MINUTE(NOW());
        set new.`CreatedSecond` = SECOND(NOW());
        set new.`CreatedMillisecond` = CAST(UNIX_TIMESTAMP(CURTIME(3)) % 1 * 1000 AS unsigned);
    end if;
END;


CREATE TABLE `ratings`
(
    `UserID`     int NOT NULL,
    `TimeLineID` int NOT NULL,
    `Rating`     int NOT NULL,
    KEY `UserID_idx` (`UserID`),
    KEY `TimeLineID_idx` (`TimeLineID`),
    CONSTRAINT `TimeLineID` FOREIGN KEY (`TimeLineID`) REFERENCES `timelines` (`TimelineID`) ON DELETE CASCADE,
    CONSTRAINT `UserID` FOREIGN KEY (`UserID`) REFERENCES `users` (`UserID`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;


INSERT INTO `users`
(`UserID`, `UserName`, `UserEmail`, `Password`, `Salt`, `Admin`, `Theme`)
VALUES ('1', 'Admin', 'Admin@gmail.com',
        'FPUpkk14h2EWAX9J7q18Ue6QJ/VSrs5ulnaw/Tggo23smYvqcLKihIUARNQcxUpDSGXOGBsGo4gjKTikDfrpxw==',
        'hXEFj6Yy9hanXVOUyACANrUi1eZs4f', '1', 'Default'),
       ('2', 'NonAdmin', 'NonAdmin@gmail.com',
        'bXKyPFQD//MW1XtOlVrgEDvEXIm9xzT+z4wBrMKR7DTHeETUPFlYpcuvanM/I2dPZSa5fQEnKc4E2D6ZD7sOiA==',
        'Q48XUaFIG4LITasAYZzSNUHskubTw5', '0', 'Default');