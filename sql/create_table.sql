CREATE TABLE IF NOT EXISTS Member (
    id         INTEGER,
    nickname   NVARCHAR (255),
    guid       VARCHAR (255),
    puid       VARCHAR (255),
    udid       TEXT,
    code       TEXT,
    slot_item1 INTEGER,
    slot_item2 INTEGER,
    slot_item3 INTEGER,
    slot_item4 INTEGER
);

CREATE TABLE IF NOT EXISTS Play (
    pattern_id    INTEGER,
    user_id       INTEGER,
    score         INTEGER,
    grade         CHAR (1),
    isAllCombo    CHAR (1),
    isPerfectPlay CHAR (1),
    judgement     INTEGER
);
