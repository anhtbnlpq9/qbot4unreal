# To do list

## The basic stuff

### General
- [ ] conf REHASH
- [X] logging
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
- [ ] handle setting multiple (user) chan modes at the same time

## Advanced stuff

### Protocol
- [X] decode IRCv3 tags
- [ ] do IRCv3 stuff?

### Accounts
- [X] multi user auth
- [ ] account edit history (email, password)
- [ ] account auth with challenge auth => maybe not necessary and can be replaced with SASL/TLS or CertFP, or implement SCRAM-SHA256
- [X] SASL PLAIN
- [X] SASL EXTERNAL
- [X] user auto vhost on auth
- [X] certfp authentication
- [X] replace bcrypt with argon2 for password hashing


### Channels
- [X] adapt to IRCd implementing +q/+a/+o/+h/+v or not

## Oper stuff
- [X] account suspension
- [X] channel suspension
- [ ] user gline
- [ ] channel gline

## Commands

### Chan related
- [X] ADDCHAN => register new chan for a third party
- [ ] ADDUSER => add user(s) in chanlev given flags
- [X] AUTOLIMIT => sets autolimit delta
- [ ] BANCLEAR => 
- [ ] BANDEL =>
- [ ] BANLIST => list effective bans to be set by the bot
- [ ] EXCEPTLIST =>
- [ ] INVEXLIST => 
- [ ] BANTIMER 
- [X] CHANFLAGS
- [X] CHANLEV
- [ ] CHANLEVHISTORY
- [ ] CHANMODE
- [ ] CHANNELCOMMENT => manage comments about registered channels
- [ ] CHANOPHISTORY 
- [ ] CHANSTAT
- [ ] CLEARCHAN
- [X] CLEARTOPIC
- [X] DELCHAN => DROPCHAN
- [ ] DEOPALL
- [ ] DEVOICEALL
- [ ] GIVEOWNER
- [ ] INVITE
- [X] OP
- [ ] PERMBAN
- [X] REJOIN
- [ ] RENCHAN
- [ ] REQUESTOWNER
- [X] SETTOPIC
- [X] SUSPENDCHAN
- [ ] SUSPENDCHANLIST
- [ ] SUSPENDHISTORY
- [ ] TEMPBAN
- [ ] UNBANALL
- [ ] UNBANMASK
- [ ] UNBANME
- [X] UNSUSPENDCHAN
- [X] VOICE
- [X] WELCOME
- [ ] CHANINFO
- [ ] CHANORPHANS
- [ ] CHANLIST => lookup registered chans and not all chans
- [X] REQUESTBOT
- [X] HALFOP
- [X] ADMIN
- [X] OWNER
- [ ] MLOCK
- [ ] CHANBAN => implement channel "gline"

### User related
- [ ] ACCOUNTHISTORY => list action history for user account
- [X] AUTH => auth on the bots
- [X] AUTHHISTORY => list account auth history
- [X] DROPUSER => drop an user account
- [ ] EMAIL => change self email
- [X] HELLO => create a new account
- [X] NEWPASS => change self or one's password
- [ ] RECOVER => see GHOST
- [ ] SUSPENDHISTORY => list of global/targeted suspension list
- [X] SUSPENDUSER => add account suspension
- [ ] SUSPENDUSERLIST => list if suspended users
- [X] UNSUSPENDUSER => remove account suspension
- [ ] USERCOMMENT => manage comments about registered users
- [X] USERFLAGS => manage self user flags
- [ ] USERS
- [X] WHOAMI => = WHOIS me
- [X] WHOIS => information about an user
- [ ] GHOST => kills a nick (if nick is owned and taken)
- [X] LOGOUT => logout of user account
- [ ] LOGOUTALL => logout all sessions linked to an user account
- [ ] USERLIST => lookup registered users and not all users
- [ ] SETUSERFLAGS -> maybe rename to SAFLAGS or SETFLAGS => set user flags for a third party
- [X] SETUSERPASSWORD -> maybe rename to SAPASSWORD or SETPASSWORD => set user password for a third party
- [X] CERTFPADD => add a certfp to self account
- [X] CERTFPDEL => remove a certfp from self account

### Misc
- [X] HELP
- [ ] JUPE  => operservice?
- [ ] LISTFLAGS
- [X] SHOWCOMMANDS
- [X] VERSION
- [X] DIE
- [ ] REHASH
- [ ] RESTART
- [X] SERVERLIST


## Interesting commands/features
- [ ] user modes setting on auth
- [ ] user/channel account privacy
- [ ] channel banning
- [ ] DEFCON (operserv, anope)  => operservice?
- [ ] Qline manipulation => operservice?
- [ ] searching data (for users, for opers)

# Coding
- [X] rework SJOIN handling
- [X] rework MODE handling
- [X] rework CHANLEV
- [X] rework DROP commands to include some "safety integrity"
- [ ] rework channel user count again
- [ ] implement restartable threads in case of crash
