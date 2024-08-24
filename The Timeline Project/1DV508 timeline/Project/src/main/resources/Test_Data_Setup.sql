INSERT INTO `users`
(`UserID`, `UserName`, `UserEmail`, `Password`, `Salt`, `Admin`, `Theme`)
VALUES ('15', 'Ben', 'Ben@gmail.com',
        'FPUpkk14h2EWAX9J7q18Ue6QJ/VSrs5ulnaw/Tggo23smYvqcLKihIUARNQcxUpDSGXOGBsGo4gjKTikDfrpxw==',
        'hXEFj6Yy9hanXVOUyACANrUi1eZs4f', '1', 'Default'),
       ('16', 'Max', 'Max@gmail.com',
        'bXKyPFQD//MW1XtOlVrgEDvEXIm9xzT+z4wBrMKR7DTHeETUPFlYpcuvanM/I2dPZSa5fQEnKc4E2D6ZD7sOiA==',
        'Q48XUaFIG4LITasAYZzSNUHskubTw5', '1', 'Default'),
       ('3', 'Dillon', 'Dillon@gmail.com',
        'a62/l7UcPfNOvlPLDwhy1AYocxnvmFQImNFQDbb+encq/FoV7OOYykxivMleq95EZw88wkG0H46XPS3kW6u3ag==',
        'xerSaGrjebBz0IET7x7vJr1ra9w0RX', '0', 'Dark'),
       ('4', 'Firas', 'Firas@gmail.com',
        'QLwdPiI5hPNyRBUD0tmbNaIZTelEAunhYBEl6RDLvhI3YDpUn73IiE8tg2zo1jVoCw3QM46b2SGkLS3QabSexg==',
        'FVauWBLlMJidPWyN4wCn3tzC0YvQLv', '0', 'Dark'),
       ('5', 'Lasse', 'Lasse@gmail.com',
        'oqi8KO+E3QCUKYbjX0CECxRunPEdZIY0rgcTVeo91UA7QPNH3VM0rMxkRtzDHPQDDvUQXlEz/4zhDlg36vl8+A==',
        'psh4bByUWvaP3UacHHU9VLEcm53Zfg', '0', 'Default'),
       ('6', 'Haraldur', 'Haraldur@gmail.com',
        'uT6n+/VL6V5yN9BDM7JgBvca0MbO2t3E8LnBwPSDpknUFBkGt89yEHeS20QNTYPX+O7InMuft5mxg4KLkkLoKA==',
        'X9Upxcdkg0JSgzXbTuDqW0XBXXXdgS', '0', 'Default'),
       ('7', 'Vytautas', 'Vytautas@gmail.com',
        '08faHiezce1RlZ22ntPWdX/tQZ5214qiIntsmKWNZ73prBi2rR45Y1UPWnyhN7fRfLCL/2jiRjw+ZtUR6Mwu7Q==',
        'Z5bCam0vfNQF9HjZd1VGJeu6WTgL4l', '0', 'Default'),
       ('8', 'Timothy', 'Timothy@gmail.com',
        'FUPZbpwU9RaPyIyh4hbiJGJJIlaEtm/zJ4owm1hfZ1d4Q7/JwEP0M2bRLA3SgxTRo1i+Ci0VUiofLyJEqkItng==',
        'HqJsDL08iV2tRAJuRojFGJTY0bpvy4', '0', 'Default'),
       ('9', 'Matas', 'Matas@gmail.com',
        'GyESVgT0doFrIJmTXRRhjEzGl4MoUc/hbZsVLRhipE8AUGZy4kV7sFVej40bGnhmyZYv98S6jhjdIcy9RLhheQ==',
        '9yaA4kzAWPYVPRhKog9fpCLeEm43Br', '0', 'Default'),
       ('10', 'Lorenz', 'Lorenz@gmail.com',
        'PYHAmdNfo+zK/kjvaRq6fON76Iymyb5FPkt2vich++tkPxugd76WPIF+PXjh9iWYhZUiJLlW7O883vj1O6gaug==',
        'n15sLcjfgleZFTvBQDvi1MhFDJ3R7u', '0', 'Default'),
       ('11', 'Chris', 'Chris@gmail.com',
        'ErFKIFDX5jx7+rFJDpMUpZOTHgCJVUZl6fPsdRZt4m96HpMZknbot/1pr1ns/xChn0V0wrXFbvcs7vTHNZxu+A==',
        '1cQoymwLl05zLbl2q36uWviIB8ffcS', '1', 'Default'),
       ('12', 'Jan', 'Jan@gmail.com',
        'J4zKZyfXIRQinm4jw/i1+3WXaVrTX9Wl0YhJNUHprUBE0ogUfEojn3gfOo2jBxqb9gtUxEhnhIGxpOfwerI+fQ==',
        'CEc1AAkRdz7BguqKQL4e4wrw7A3j6L', '0', 'Default'),
       ('13', 'Hans Ove', 'Hans@math.biz',
        'tPmbHxe4qtzP8AaCpQJs/Hjr8RW3xDUGx+kk75AENDVY7Kkz85jJ/H1KICOH9TOsZPg4e/4ldTM9WzajCOJQiw==',
        '8IzHZXvKP9hwwIr5EflEvhLYdo2AVY', '0', 'Default'),
       ('14', 'Test User', 'User@test.com',
        '9++aUh7ltf/BUAYP2adyTl4DoFthc387ahLWGV58pzyQsMRcJNKIH6g8UhdAF400MSysbm30v0AAkBXy4EgQaQ==',
        'OsMpNbYBiPYkLAgmVmAFUt6faEW1Ot', '0', 'Default');


INSERT INTO `timelines`
(`TimelineID`,
 `Scale`, `TimelineName`, `TimelineDescription`, `StartYear`, `StartMonth`, `StartDay`, `StartHour`,
 `StartMinute`, `StartSecond`, `StartMillisecond`, `EndYear`, `EndMonth`, `EndDay`, `EndHour`, `EndMinute`, `EndSecond`,
 `EndMillisecond`, `CreatedYear`, `CreatedMonth`, `CreatedDay`, `CreatedHour`, `CreatedMinute`, `CreatedSecond`,
 `CreatedMillisecond`, `TimelineOwner`, `Keywords`, `ImagePath`)
VALUES (01, 8, 'Fall of Rome', 'Out with a whimper, not a bang', 45, 5, 20, 4, 43, 32, 213, 201, 5, 20, 4,
        43, 32, 213, 2000, 5, 20, 4, 43,
        32, 213, 1, 'Caesar,Rome,', 'src/main/resources/images/timeline/fall_of_rome.jpg'),
       (02, 2, 'New Timeline', '', 2000, 5, 20, 4, 43, 20, 213, 2000, 5, 20, 4, 43, 32, 213, 2003, 5, 20, 4,
        43, 32, 213, 1, 'stuff,things,test,test1,test3,test4,test5,test6,',
        null),
       (03, 4, 'Hound of Baskervilles', 'Investigation of an attempted murder', 2006, 5, 19, 4, 43, 32, 213,
        2006, 5, 20, 4, 43, 32, 213, 2003, 5, 20, 3,
        43, 32, 213, 2, 'murder,death,', 'src/main/resources/images/timeline/hound.jpg'),
       (04, 5, 'Dr. Strangelove', 'A dark comedy on nuclear war', 2007, 5, 18, 4, 43, 32, 213, 2007, 5, 20, 4,
        43, 32, 213, 2007, 5, 20, 4, 43,
        32, 213, 2, 'war,nuclear,', 'src/main/resources/images/timeline/strangelove.jpg'),
       (05, 6, 'Incredibly, Wastefully Long Timeline Name', '', 2009, 2, 20, 4, 43, 32, 213, 2009, 5, 20, 4,
        43, 32, 213, 2008, 5, 20, 4,
        43, 32, 213, 3, 'testing,123,', 'src/main/resources/images/timeline/rainbow_waves.jpg'),
       (06, 11, 'Bronze Age Collapse', 'When civilization reset', -13000, 5, 20, 4, 43, 32, 213, 2020, 5, 20,
        4,
        43, 32, 213, 2009, 5, 20, 4,
        43, 32, 213, 4, 'bronze,collapse,', 'src/main/resources/images/timeline/BronzeAge_Collapse.jpg'),
       (07, 8, 'Life of Bacillus', 'Life and times of a bacterium', 1450, 5, 20, 4, 43, 32, 213, 1505, 5, 20, 4,
        43, 32, 213, 2000, 5, 20, 4, 46,
        32, 213, 5, 'basillus,life,', 'src/main/resources/images/timeline/transistor.jpg'),
       (08, 5, 'Decay of Ununoctium', 'Radioactive decay - a study', 2000, 4, 20, 4, 43, 32, 213, 2000, 5, 20,
        4, 43, 32, 213, 1550, 5, 20, 4, 43,
        32, 213, 6, 'decay,long,', 'src/main/resources/images/timeline/ununoctium.jpg'),
       (09, 8, 'Owner: Max - Dummy timeline 9', 'A timeline meant for testing years', 50, 4, 20, 4, 43, 32, 213,
        100, 5, 20,
        4, 43, 32, 213, 1550, 5, 20, 4, 43,
        32, 213, 2, 'testing, 2, fifty', 'src/main/resources/images/timeline/art_andre_kutscherauer.jpg'),
       (10, 2, 'Owner: Max - Dummy timeline 10', 'A timeline meant for testing seconds', 0, 1, 1, 0, 0, 1, 0, 0,
        1, 1,
        0, 0, 59, 0, 1550, 5, 20, 4, 43,
        32, 213, 2, 'testing, 2, sixty', 'src/main/resources/images/timeline/watchman.jpg'),
       (11, 2, 'Owner: Max - Dummy timeline 11',
        'A timeline meant for testing the upper bounds of seconds (1 min 10 sec)', 0, 1, 1, 0, 0, 1, 0, 0, 1, 1,
        0, 1, 10, 0, 1550, 5, 20, 4, 43,
        32, 213, 2, 'testing, 2, sixty', null),
       (12, 6, 'Owner: Max - Dummy timeline 12', 'A timeline meant for testing weeks', 50, 4, 20, 4, 43, 32,
        213, 50, 8, 20,
        4, 43, 32, 213, 1550, 5, 20, 4, 43,
        32, 213, 2, 'testing, 2, fifty', 'src/main/resources/images/timeline/neverknowsbest.png');


INSERT INTO `events` (`EventOwner`, `EventPriority`, `EventName`, `EventDescription`, `StartYear`, `StartMonth`,
                      `StartDay`, `StartHour`, `StartMinute`, `StartSecond`, `StartMillisecond`, `EndYear`, `EndMonth`,
                      `EndDay`,
                      `EndHour`, `EndMinute`, `EndSecond`, `EndMillisecond`)
VALUES ( '1', '1', 'Crossing the Rubicon', 'Julius Caesar''s crossing the Rubicon river in January 49 BC precipitated
        the Roman Civil War, which ultimately led to Caesar becoming dictator and the rise of the imperial era of Rome.
        Caesar had been appointed to a governorship over a region that ranged from southern Gaul to Illyricum (but not Italy).
        As his term of governorship ended, the Roman Senate ordered Caesar to disband his army and return to Rome. He was
        explicitly ordered not to bring his army across the Rubicon river, which was at that time a northern boundary of Italy.
        In January of 49 BC, Caesar brought the 13th legion across the river, which the Roman government considered
        insurrection, treason, and a declaration of war on the Roman Senate. According to some authors, he is said to have
        uttered the phrase "alea iacta est"—the die is cast—as his army marched through the shallow river.'
       , '49', '1', '13', '17', '25', '40', '20', '52', '10', '25', '22', '50', '45', '40'),
       ( '1', '2', 'TestEvent', '.'
       , '49', '1', '13', '17', '25', '40', '20', '56', '10', '25', '22', '50', '45', '40'),
       ('1', '2', 'Great Roman Civil War', 'The Great Roman Civil War (49–45 BC), also known as Caesar''s Civil War, was
        one of the last politico-military conflicts in the Roman Republic before the establishment of the Roman Empire.
        It began as a series of political and military confrontations, between Julius Caesar (100–44 BC), his political supporters
        (broadly known as Populares), and his legions, against the Optimates (or Boni), the politically conservative and socially
        traditionalist faction of the Roman Senate, who were supported by Pompey (106–48 BC) and his legions.[1]',
        '48', '5', '5', '5', '10', '10', '10', '51', '10', '25', '22', '50', '45', '40'),
       ('1', '2', 'Marcus Tullius Cicero', 'Marcus Tullius Cicero[a] (/ˈsɪsəroʊ/ SISS-ə-roh, Latin:
        ; 3 January 106 BC – 7 December 43 BC) was a Roman statesman, lawyer and Academic
        Skeptic philosopher[3] who wrote extensively on rhetoric, orations, philosophy, and politics, and is considered one of
        Rome''s greatest orators and prose stylists.[4][5] A leading political figure in the final years of the Roman Republic,
        Cicero vainly tried to uphold the republican system''s integrity during the instability that led to the establishment of
        the Roman Empire.[6] He came from a wealthy municipal family of the Roman equestrian order, and served as consul in the
        year 63 BC.', '56', '8', '8', '9', '20', '20', '25', '59', '10', '30', '22', '50', '45', '40'),
       ('2', '0', 'dummyEvent5', 'This event breaks the upper bound of dummy timeline 9',
        '52', '2', '14', '18', '45', '30', '28', '115', '9', '28', '21', '48', '46', '11'),
       ('2', '1', 'dummyEvent6', 'This event breaks the lower bound of dummy timeline 9',
        '45', '2', '14', '18', '45', '30', '28', '61', '9', '28', '21', '48', '46', '11'),
       ('2', '3', 'dummyEvent7', 'This event breaks the lower and upper bounds of dummy timeline 9',
        '49', '2', '14', '18', '45', '30', '28', '110', '9', '28', '21', '48', '46', '11'),
       ('2', '2', 'dummyEvent8', 'Owner: Max - Testing overlapping events on dummy timeline 9',
        '55', '2', '14', '18', '45', '30', '28', '63', '9', '28', '21', '48', '46', '11'),
       ('2', '0', 'dummyEvent9', 'Owner: Max - Testing overlapping events on dummy timeline 9',
        '56', '2', '14', '18', '45', '30', '28', '64', '9', '28', '21', '48', '46', '11'),
       ('2', '0', 'dummyEvent10', 'Owner: Max - Testing overlapping events on dummy timeline 9',
        '57', '2', '14', '18', '45', '30', '28', '65', '9', '28', '21', '48', '46', '11'),
       ('2', '0', 'dummyEvent11', 'Owner: Max - Testing overlapping events on dummy timeline 9',
        '58', '2', '14', '18', '45', '30', '28', '60', '9', '28', '21', '48', '46', '11'),
       ('2', '2', 'dummyEvent12', 'Owner: Max - Testing overlapping events on dummy timeline 9',
        '55', '2', '14', '18', '45', '30', '28', '63', '9', '28', '21', '48', '46', '11'),
       ('2', '2', 'dummyEvent13', 'Owner: Max - Testing overlapping events on dummy timeline12',
        '50', '4', '20', '0', '0', '0', '0', '50', '5', '12', '0', '0', '0', '0'),
       ('2', '2', 'dummyEvent14', 'Owner: Max - Testing overlapping events on dummy timeline12',
        '50', '4', '20', '0', '0', '0', '0', '50', '6', '12', '0', '0', '0', '0'),
       ('2', '2', 'dummyEvent15', 'Owner: Max - Testing overlapping events on dummy timeline12',
        '50', '4', '20', '0', '0', '0', '0', '50', '7', '12', '0', '0', '0', '0'),
       ('2', '2', 'dummyEvent16', 'Owner: Max - Testing overlapping events on dummy timeline12',
        '50', '4', '20', '0', '0', '0', '0', '50', '6', '20', '0', '0', '0', '0'),
       ('2', '2', 'dummyEvent17', 'Owner: Max - Testing overlapping events on dummy timeline12',
        '50', '6', '20', '0', '0', '0', '0', '50', '7', '12', '0', '0', '0', '0'),
       ('2', '2', 'dummyEvent18', 'Owner: Max - Testing overlapping events on dummy timeline12',
        '50', '7', '20', '0', '0', '0', '0', '50', '7', '30', '0', '0', '0', '0'),
       ('2', '2', 'dummyEvent19', 'Owner: Max - Testing overlapping events on dummy timeline12',
        '50', '5', '2', '0', '0', '0', '0', '50', '5', '12', '0', '0', '0', '0'),
       ('2', '2', 'dummyEvent20', 'Owner: Max - Testing overlapping events on dummy timeline12',
        '50', '4', '30', '0', '0', '0', '0', '50', '6', '30', '0', '0', '0', '0'),
       ('2', '2', 'dummyEvent21', 'Owner: Max - Testing overlapping events on dummy timeline12',
        '50', '5', '25', '0', '0', '0', '0', '50', '5', '30', '0', '0', '0', '0'),
       ('2', '2', 'dummyEvent22', 'Owner: Max - Testing overlapping events on dummy timeline12',
        '50', '4', '1', '0', '0', '0', '0', '50', '7', '15', '0', '0', '0', '0'),
       ('2', '2', 'dummyEvent23', 'Owner: Max - Testing overlapping events on dummy timeline12',
        '50', '6', '5', '0', '0', '0', '0', '50', '6', '15', '0', '0', '0', '0'),
       ('2', '2', 'dummyEvent24', 'Owner: Max - Testing upper bounds of dummy timeline12',
        '50', '4', '20', '0', '0', '0', '0', '50', '9', '1', '0', '0', '0', '0'),
       ('2', '2', 'dummyEvent25', 'Owner: Max - Testing lower bounds of dummy timeline12',
        '50', '3', '20', '0', '0', '0', '0', '50', '5', '12', '0', '0', '0', '0'),
       ('2', '2', 'dummyEvent26', 'Owner: Max - Testing upper and lower bounds of dummy timeline12',
        '50', '3', '15', '0', '0', '0', '0', '50', '9', '1', '0', '0', '0', '0'),
    /*
         dummyEvent(ID)27-37

     Events varying in Seconds / Minutes / Milliseconds

     Note: Not linked in the junction table.

     Owner:              Max(ID2)
     Priority:           0

     StartYear:          10        ==        EndYear:            10
     StartMonth:          2        ==        EndMonth:            2
     StartDay:           14        ==        EndDay:             14
     StartHour:          18        ==        EndHour:            18
     StartMinute:        45      ------>     EndMinute:          45+
     StartSecond:        30      ------>     EndSecond:          30+
     StartMillisecond:   28      ------>     EndMillisecond:     28+

     */

    /* Events varying in Seconds */

       ('2', '0', 'dummyEvent27', '1 Second',
        '10', '2', '14', '18', '45', '30', '28', '10', '2', '14', '18', '45', '31', '28'),
       ('2', '0', 'dummyEvent28', '10 Seconds',
        '10', '2', '14', '18', '45', '30', '28', '10', '2', '14', '18', '45', '40', '28'),
       ('2', '0', 'dummyEvent29', '29 Seconds',
        '10', '2', '14', '18', '45', '30', '28', '10', '2', '14', '18', '45', '59', '28'),

    /* Events varying in Minutes */

       ('2', '0', 'dummyEvent30', '1 Minute',
        '10', '2', '14', '18', '45', '30', '28', '10', '2', '14', '18', '46', '30', '28'),
       ('2', '2', 'dummyEvent31', '2 Minutes',
        '10', '2', '14', '18', '45', '30', '28', '10', '2', '14', '18', '47', '30', '28'),
       ('2', '2', 'dummyEvent32', '5 Minutes',
        '10', '2', '14', '18', '45', '30', '28', '10', '2', '14', '18', '50', '30', '28'),

    /* Events varying in Minutes + Seconds (+) Milliseconds */

       ('2', '2', 'dummyEvent33', '1 Minute 1 Second',
        '10', '2', '14', '18', '45', '30', '28', '10', '2', '14', '18', '46', '31', '28'),
       ('2', '0', 'dummyEvent34', '1 Minute 2 Seconds',
        '10', '2', '14', '18', '45', '30', '28', '10', '2', '14', '18', '46', '32', '28'),
       ('2', '0', 'dummyEvent35', '1 Minute 1 Second 1 Millisecond',
        '10', '2', '14', '18', '45', '30', '28', '10', '2', '14', '18', '46', '31', '29'),

    /* Events that break the upper limit of minutes and seconds */

       ('2', '0', 'dummyEvent36', '200 seconds (breaks the 59 second barrier)',
        '10', '2', '14', '18', '45', '30', '28', '10', '2', '14', '18', '45', '23', '28'),
       ('2', '0', 'dummyEvent37', '200 Minutes (breaks the 59 Minute barrier)',
        '10', '2', '14', '18', '45', '30', '28', '10', '2', '14', '18', '45', '23', '28');


INSERT INTO `timelineevents` (`TimelineID`, `EventID`)
VALUES ('1', '1'),
       ('1', '2'),
       ('1', '3'),
       ('1', '4'),
       ('2', '2'),
       ('3', '2'),
       ('5', '1'),
       ('6', '1'),
       ('7', '1'),
       ('8', '1'),
       ('9', '5'),
       ('9', '6'),
       ('9', '7'),
       ('9', '8'),
       ('9', '9'),
       ('9', '10'),
       ('9', '11'),
       ('9', '12'),
       ('12', '13'),
       ('12', '14'),
       ('12', '15'),
       ('12', '16'),
       ('12', '17'),
       ('12', '18'),
       ('12', '19'),
       ('12', '20'),
       ('12', '21'),
       ('12', '22'),
       ('12', '23'),
       ('12', '24'),
       ('12', '25'),
       ('12', '26');


INSERT INTO `ratings` (`UserID`, `TimelineID`, `Rating`)
VALUES ('1', '1', '3'),
       ('2', '6', '1'),
       ('2', '5', '1'),
       ('2', '7', '3'),
       ('2', '8', '1'),
       ('1', '6', '2'),
       ('1', '4', '5'),
       ('1', '3', '2'),
       ('1', '5', '1'),
       ('1', '7', '4'),
       ('1', '10', '3'),
       ('1', '11', '2'),
       ('1', '12', '4'),
       ('1', '9', '3'),
       ('12', '6', '2'),
       ('12', '4', '5'),
       ('12', '1', '5'),
       ('12', '7', '2'),
       ('12', '8', '1'),
       ('12', '11', '4'),
       ('12', '9', '5'),
       ('3', '6', '2'),
       ('3', '4', '3'),
       ('3', '1', '3'),
       ('3', '3', '4'),
       ('3', '12', '2'),
       ('3', '9', '1'),
       ('10', '6', '2'),
       ('10', '3', '5'),
       ('10', '5', '1'),
       ('10', '12', '4'),
       ('1', '1', '3'),
       ('2', '1', '3'),
       ('4', '1', '3'),
       ('5', '1', '3'),
       ('6', '1', '3'),
       ('7', '1', '3'),
       ('8', '1', '3'),
       ('9', '1', '3'),
       ('10', '1', '3'),
       ('11', '1', '3');
