# To do list


## General
### Accounts
- [ ] account editing
- [ ] nick ownership
- [ ] account auto expiry
- [ ] (opt-in) certfp autologin without SASL (cons: imposes that certfp can be used in only one account)
- [ ] account profiles (in order to assign specific privileges)
- [ ] services-managed o:lines
- [ ] account edit history (email, password)
- [ ] account auth with challenge auth => maybe not necessary and can be replaced with SASL/TLS or CertFP, or implement SCRAM-SHA256
- [ ] vhosts

### Channels
- [ ] channel editing
- [ ] chanlev history
- [ ] channel auto expiry
- [ ] handle setting multiple (user) chan modes at the same time
- [ ] chaninfo: channel idle time

### Protocol
- [ ] security groups

### Database
- [ ] Implement Elastic
- [ ] Implement MariaDB/MySQL


### Flags
#### User flags
- [x] +D deleted
- [X] +a admin
- [X] +d devgod
- [ ] +g glined
- [ ] +l no auth limit
- [X] +m privmsg
- [X] +o oper
- [ ] +p protected
- [X] +q staff
- [X] +v auto vhost
- [X] +w welcome
- [X] +z suspended

#### Chan flags
- [ ] +b bitch
- [X] +c autolimit
- [ ] +e enforce
- [ ] +f force topic
- [X] +j joined
- [ ] +k known only
- [ ] +p protected
- [ ] +t topic save
- [X] +v voice all
- [X] +w welcome
- [ ] +y glined
- [X] +z suspended

#### Chanlev
- [X] +a auto
- [X] +b banned
- [ ] +d deny op
- [X] +h half op
- [ ] +j auto invite
- [X] +k known
- [X] +m master
- [X] +n owner
- [X] +o op
- [ ] +p protect
- [ ] +t topic
- [ ] +u deny voice
- [X] +v voice
- [ ] +w hide welcome

### Other
- [ ] service master/slave redundancy
- [ ] conf REHASH
- [X] logging to a database
- [ ] i18n
- [ ] actions log

## Oper stuff
- [ ] user gline
- [ ] channel gline => sqline

## Commands

### Chan related
- [X] ADDUSER => add user(s) in chanlev given flags ADDUSER #channel chanlev-flags user1 user2 user3...
- [ ] (BAN|EXCEPT|INVEX)CLEAR => clear all bans on a channel
- [ ] BANTIMER
- [ ] CHANLEVHISTORY
- [ ] CHANNELCOMMENT => manage comments about registered channels
- [ ] CHANOPHISTORY
- [ ] CHANSTAT
- [ ] CLEARCHAN
- [ ] INVITE
- [ ] REQUESTOWNER
- [ ] SUSPENDCHANLIST
- [ ] UNBANALL
- [ ] UNBANMASK
- [ ] UNBANME
- [ ] CHANORPHANS => channels with no +n
- [ ] CHANBAN => implement channel "gline"

### User related
- [ ] ACCOUNTHISTORY => list action history for user account
- [ ] EMAIL => change self email
- [ ] RECOVER => see GHOST
- [ ] SUSPENDUSERLIST => list of suspended users
- [ ] USERCOMMENT => manage comments about registered users
- [ ] USERS
- [ ] GHOST => kills a nick (if nick is owned and taken)
- [ ] SETUSERFLAGS -> maybe rename to SAFLAGS or SETFLAGS => set user flags for a third party

### Misc
- [ ] JUPE  => operservice?
- [ ] REHASH
- [ ] RESTART
- [ ] NETINFO => network/protocol information


## Interesting commands/features
- [ ] user modes setting on auth
- [ ] user/channel account privacy
- [ ] channel banning
- [ ] DEFCON (operserv, anope)  => operservice?
- [ ] Q:Line manipulation => operservice?
- [ ] searching for data (for users, for opers)

# Coding
- [X] rework the flags/levels help/showcommands system a bit

# Issues
- [ ] issues during auth in certain conditions
- [ ] issues during reauth (after netjoin or service restart)
- [ ] auth may not be managed the same way if it is made from SASL or AUTH command
- [ ] mlock isn't sent when starting qbot
- [ ] mlock isn't sent when a new server connects

