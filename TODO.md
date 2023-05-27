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
- [X] auth history
- [ ] account auto expiry

### Channels
- [X] channel registration
- [X] chanlev
- [X] chanflags
- [ ] channel editing
- [ ] MLOCK
- [ ] bans, excepts, invites
- [ ] chanlev history
- [ ] channel auto expiry

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
- [X] AUTHHISTORY
- [X] AUTOLIMIT
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
- [X] CLEARTOPIC
- [X] DELCHAN => DROP?
- [ ] DELUSER
- [ ] DEOPALL
- [ ] DEVOICEALL
- [ ] EMAIL
- [ ] GIVEOWNER
- [X] HELLO
- [X] HELP
- [ ] INVITE
- [ ] JUPE
- [ ] LISTFLAGS
- [ ] NEWPASS
- [ ] OP
- [ ] PERMBAN
- [ ] RECOVER
- [X] REJOIN
- [ ] RENCHAN
- [ ] REMOVEUSER
- [ ] REQUESTOWNER
- [X] SETTOPIC
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
- [X] WELCOME
- [X] WHOAMI
- [X] WHOIS

## Added commands
- [ ] CHANINFO
- [ ] CHANORPHANS
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
- [ ] SETUSERFLAGS => maybe rename to SAFLAGS or SETFLAGS
- [ ] SETUSERPASSWORD => maybe rename to SAPASSWORD or SETPASSWORD
- [ ] CERTFP

## Interesting commands/features
- [ ] user modes setting on auth
- [ ] user/channel account privacy
- [ ] channel banning
- [ ] DEFCON (operserv, anope)
- [ ] Qline manipulation
- [ ] searching data (for users, for opers)

# Coding
- [ ] rework SJOIN handling
- [ ] rework MODE handling
- [ ] rework CHANLEV
- [ ] rework DROP command to include some "safety integrity"
