UPDATE
student_registration, students
SET
student_registration.firstname = students.firstname,
student_registration.lastname = students.lastname
WHERE student_registration.student_pk = students.student_pk;