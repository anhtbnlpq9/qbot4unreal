# To do list

## The basic stuff

### General
- [ ] conf REHASH

### Accounts
- [X] account registration
- [X] account auth with plain user/pass
- [X] user flags
- [ ] account deletion
- [ ] account editing
- [ ] nick ownership
- [ ] auth history
- [ ] account expiry

### Channels
- [X] channel registration
- [X] chanlev
- [X] chanflags
- [ ] channel editing
- [ ] MLOCK
- [ ] bans, excepts, invites
- [ ] chanlev history
- [ ] channel expiry

## Advanced stuff

### Protocol
- [ ] IRCv3

### Accounts
- [X] multi user auth
- [ ] edit history (email, password)
- [ ] account auth with challenge auth
- [ ] SASL PLAIN
- [ ] SASL EXTERNAL
- [X] user auto vhost on auth


### Channels
- [X] adapt to IRCd implementing +q/+a/+o/+h/+v or not

## Oper stuff
- [ ] account suspension
- [ ] channel suspension
- [ ] user gline


## Original Q bot commands
- [ ] ACCOUNTHISTORY
- [ ] ADDCHAN
- [ ] ADDUSER
- [X] AUTH
- [ ] AUTHHISTORY
- [ ] AUTOLIMIT
- [ ] BANCLEAR
- [ ] BANDEL
- [ ] BANLIST
- [ ] BANTIMER
- [X] CHANFLAGS
- [X] CHANLEV
- [ ] CHANLEVHISTORY
- [ ] CHANMODE
- [ ] CHANNELCOMMENT
- [ ] CHANOPHISTORY
- [ ] CHANSTAT
- [ ] CHANTYPE
- [ ] CLEARCHAN
- [ ] CLEARTOPIC
- [ ] DELCHAN
- [ ] DELUSER
- [ ] DEOPALL
- [ ] DEVOICEALL
- [ ] EMAIL
- [ ] GIVEOWNER
- [ ] HELLO
- [X] HELP
- [ ] INVITE
- [ ] LISTFLAGS
- [ ] NEWPASS
- [ ] OP
- [ ] PERMBAN
- [ ] RECOVER
- [ ] REJOIN
- [ ] RENCHAN
- [ ] REMOVEUSER
- [ ] REQUESTOWNER
- [ ] SETTOPIC
- [X] SHOWCOMMANDS
- [ ] SUSPENDCHAN
- [ ] SUSPENDCHANLIST
- [ ] SUSPENDUSER
- [ ] SUSPENDUSERLIST
- [ ] TEMPBAN
- [ ] UNBANALL
- [ ] UNBANMASK
- [ ] UNBANME
- [ ] UNSUSPENDCHAN
- [ ] UNSUSPENDUSER
- [ ] USERCOMMENT
- [X] USERFLAGS
- [ ] USERS
- [X] VERSION
- [ ] VOICE
- [ ] WELCOME
- [X] WHOAMI
- [X] WHOIS

## Added commands
- [ ] CHANINFO
- [X] CHANLIST
- [ ] DIE
- [X] DROP
- [X] LOGOUT
- [ ] REHASH
- [X] REQUESTBOT
- [ ] RESTART
- [X] SERVERLIST
- [X] USERLIST
- [ ] HALFOP
- [ ] MODEADMIN
- [ ] MODEOWNER
- [ ] SETUSERFLAGS => maybe rename to SAFLAGS
- [ ] SETUSERPASSWORD => maybe rename to SAPASSWORD

### Coding
- [ ] rework SJOIN handling
- [ ] rework MODE handling
- [ ] rework CHANLEV