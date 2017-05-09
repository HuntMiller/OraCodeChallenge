# OraCodeChallenge
Fixed repo, fixed app

This is a chat / messenger style app, developed for an Ora Interactive code challenge. Note that it does not interact with the provided apiary's API, but instead a legitimate database (Firebase). This choice was made to hopefully show initiative to improve upon given standards. I do not want to devalue the concept of following rules and guidelines, as in a company setting these values are essential.

APP SCREENSHOTS:  https://www.dropbox.com/sh/d59n1vm2svflsyr/AAB8JQl7aRnhSPBJCA49mKQNa?dl=0

Deployment: I actually messed up the structure of the repo on my initial commit. The original repo can be found at https://github.com/HuntMiller/OraChat (contains commits).

1. Open Android Studio
2. File > New > Project From Version Control > Github  (URL:  https://github.com/HuntMiller/OraCodeChallenge.git)
3. Press Clone
4. Build & Run project.

Testing: I did all testing on my Google Pixel XL.

Features:

-Register: A user is registered into the database upon logging in with their Google Account.

-Login: A user logs in using their Google Account.

-View Profile: A user can view their profile partially in the Navigation View, or fully in the Profile Fragment.

-Edit Profile: A user can edit their profile in the Profile Fragment.

-Create Chat: A user can create a chat by pressing the Floating Action Button.

-Edit Chat: Not entirely clear on what this requirement wants.

-List Chats: A user can view all their chats on the Message Fragment.

-View Chat Messages: A user can view their message history by selecting it from the Message Fragment or selecting a user through the create chat feature.

-Create Message: A user can create a message while viewing the chat message history (see View Chat Messages).
