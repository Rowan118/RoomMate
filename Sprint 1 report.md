# Sprint x Report 
https://www.youtube.com/watch?v=cdvoIMdfADM
## What's New (User Facing)
 * New account creation
 * Ability to login to account 
 * Ability to select preferences

## Work Summary (Developer Facing)
Over the past month, we planned out the general structure of your project, including use cases, UML diagrams, and what we expect our end product to be able to do. In the last couple weeks, we have created a front-facing website for students to interact with. This includes a login screen, a new account creation screen, a homepage, and a preference screen. We also created a backend to recieve and store the user input from the front end, so that created accounts can be saved for future logins. This represents the first iteration of our project, which allows students to add their preferences to their accounts.  

## Unfinished Work
We did not end up adding the ability to actually store preferences added by users. Due to constraints introduced by our overall course work (existing projects in other classes, midterms, the first few weeks of the sprint being taken up by our initial project report, user story creation, and UML case diagrams), we did not have enough time to actually implement the merging and storage of account preferences in the backend. 

## Code Files for Review
Please review the following code files, which were actively developed during this sprint, for quality:
 * new_account.html - https://github.com/Rowan118/RoomMate/blob/main/code/frontend/new_account.html
 * check_password.js - https://github.com/Rowan118/RoomMate/blob/main/code/frontend/new_account/check_password.js
 * index.html - https://github.com/Rowan118/RoomMate/blob/main/code/frontend/index.html
 * fetch_password.js - https://github.com/Rowan118/RoomMate/blob/main/code/frontend/login/fetch_password.js
 * homepage.html - https://github.com/Rowan118/RoomMate/blob/main/code/frontend/homepage.html
 * preferences.html - https://github.com/Rowan118/RoomMate/blob/main/code/frontend/preferences.html
 * get_preferences.js - https://github.com/Rowan118/RoomMate/blob/main/code/frontend/preferences/get_preferences.js
 
## Retrospective Summary
Here's what went well:
  * Most of the development of the front end (html and js files)
  * Setting up pages on github to host the website
  * Use of VSCode for testing front end before commiting to github
 
Here's what we'd like to improve:
   * When we start working on the code
   * Getting clearer assignment guidelines and expected deadlines from instructor
   * Getting other CPT_S 322 work done earlier so we can actually focus on the project 
  
Here are changes we plan to implement in the next sprint:
   * Creation and verification of RA account
   * Addition of a hard-coded admin account that cannot be accessed by anyone else
   * Ability to store and modify existing preferences, which can only be accessed by a given account
   * Ability to send emails to students from website to verify emails and give students their assignments
   * Ability to calculate assignments once started
