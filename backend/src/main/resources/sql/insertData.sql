-- insert initial test data
-- the IDs are hardcoded to enable references between further test data
-- negative IDs are used to not interfere with user-entered data and allow clean deletion of test data

UPDATE HORSE
SET MOTHER_ID=NULL
WHERE MOTHER_ID < 0;
UPDATE HORSE
SET FATHER_ID=NULL
WHERE FATHER_ID < 0;

DELETE
FROM horse
where id < 0;

DELETE
FROM owner
WHERE id < 0;

INSERT INTO owner (id, first_name, last_name, email)
VALUES (-1, 'Max', 'Mustermann', 'max.must@gmail.com'),
       (-2, 'Anakin', 'Skywalker', 'anakin.skywalker@gmail.com'),
       (-3, 'Darth', 'Vader', 'dath.vader@gmail.com'),
       (-4, 'Frodo', 'Baggins', null),
       (-5, 'Gandalf', 'The Grey', null),
       (-6, 'Gandalf', 'The White', null),
       (-7, 'Saruman', 'The Wise', null),
       (-8, 'Ben', 'Obi-Wan Kenobi', 'ben.kenobi@starwars.com'),
       (-9, 'Yoda', '(Baby Yoda)', null),
       (-10, 'Padme', 'Amidala', 'padme99@icloud.com')
;

INSERT INTO horse (id, name, description, date_of_birth, sex, owner_id, mother_id, father_id)
VALUES (-1, 'Wendy', 'The famous one!', '2014-10-10', 'FEMALE', -9, null, null),
       (-2, '1-MOTHER-M-M', 'The famous one!', '2015-12-12', 'FEMALE', -8, null, null),
       (-3, '1-FATHER-F', 'The famous one!', '2016-12-12', 'MALE', null, null, null),
       (-4, '1-FATHER-M', 'The famous one!', '2017-12-12', 'FEMALE', -7, null, null),
       (-5, '1-MOTHER-F', 'The famous one!', '2018-12-12', 'MALE', null, null, null),
       (-6, '1-MOTHER-M', 'The famous one!', '2019-12-12', 'FEMALE', -10, -2, null),
       (-7, '1-FATHER', 'The famous one!', '2020-12-12', 'MALE', null, -4, -3),
       (-8, '1-MOTHER', 'The famous one!', '2021-12-12', 'FEMALE', null, -6, -5),
       (-9, '1', '----STAMMBAUM-WURZEL----', '2022-10-11', 'FEMALE', null, -8, -7),
       (-10, 'Brandy', null, '2022-10-11', 'FEMALE', -1, null, null)
;
