# Backend Database Structure

We use MongoDB for the website backend to store every data.

## Collections / Tables

Table: `matches`

| duration | teamRank | replay |
| --- | --- | --- |
| int64 | teams.ObjectId[] | any |

Table: `teams`

| name | members | primarySubmission |
| --- | --- | --- |
| string | members.ObjectId[] | submissions.ObjectId | null |

Table: `members`

| name | email | team |
| --- | --- | --- |
| string | string | teams.ObjectId |

Table: `submissions` 

| team | user | file | time |
| --- | --- | --- | --- |
| teams.ObjectId | user.ObjectID | string | DateTime |