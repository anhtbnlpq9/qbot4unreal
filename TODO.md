# To do list

## The basic stuff

### General
- [ ] conf REHASH
- [ ] logging
- [ ] i18n

### Accounts
- [X] account registration
- [X] account auth with plain user/pass
- [X] user flags
- [X] account deletion
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
- [ ] account edit history (email, password)
- [ ] account auth with challenge auth => maybe not necessary and can be replaced with SASL/TLS or CertFP, or implement SCRAM-SHA256
- [X] SASL PLAIN
- [X] SASL EXTERNAL
- [X] user auto vhost on auth
- [X] certfp authentication


### Channels
- [X] adapt to IRCd implementing +q/+a/+o/+h/+v or not

## Oper stuff
- [ ] account suspension
- [ ] channel suspension
- [ ] user gline


## Original Q bot commands
- [ ] ACCOUNTHISTORY
- [X] ADDCHAN
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
- [ ] CLEARCHAN
- [X] CLEARTOPIC
- [X] DELCHAN => DROPCHAN
- [ ] DEOPALL
- [ ] DEVOICEALL
- [X] DROPUSER
- [ ] EMAIL
- [ ] GIVEOWNER
- [X] HELLO
- [X] HELP
- [ ] INVITE
- [ ] JUPE  => operservice?
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
- [ ] GHOST
- [X] LOGOUT
- [ ] REHASH
- [X] REQUESTBOT
- [ ] RESTART
- [X] SERVERLIST
- [X] USERLIST
- [ ] HALFOP
- [ ] MODEADMIN => rename to something better
- [ ] MODEOWNER => rename to something better
- [ ] SETUSERFLAGS => maybe rename to SAFLAGS or SETFLAGS
- [ ] SETUSERPASSWORD => maybe rename to SAPASSWORD or SETPASSWORD
- [X] CERTFPADD
- [X] CERTFPDEL

## Interesting commands/features
- [ ] user modes setting on auth
- [ ] user/channel account privacy
- [ ] channel banning
- [ ] DEFCON (operserv, anope)  => operservice?
- [ ] Qline manipulation => operservice?
- [ ] searching data (for users, for opers)

# Coding
- [ ] rework SJOIN handling
- [ ] rework MODE handling
- [X] rework CHANLEV
- [X] rework DROP commands to include some "safety integrity"
- [ ] rework channel user count again
