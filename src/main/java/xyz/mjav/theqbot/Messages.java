package xyz.mjav.theqbot;

/**
 * Class that defines the messages returned by the bot.
 */
public class Messages {

    /* Generic */
    public static final String strErrCommandUnknown     = "Unknown command. Type SHOWCOMMANDS for a list of available commands.";
    public static final String strErrCommandSyntax      = "Command has invalid syntax. Type HELP <COMMAND> to get information on that command.";
    public static final String strErrCmdExec            = "Error while processing the command.";
    public static final String strErrUserNonReg         = "Can't find this user.";
    public static final String strErrChanNonExist       = "This channel does not exist.";
    public static final String strErrChanNonReg         = "This channel is not registered.";
    public static final String strErrChanSusOrNotFound  = "Channel %s is unknown or suspended.";
    public static final String strErrChanSuspended      = "Can't change this setting while the channel is suspended.";
    public static final String strErrChanNotJoined      = "Can't use that command on a -j channel.";
    public static final String strErrNickNotFound       = "Can't find this nick.";
    public static final String strErrUserNotFound       = "Can't find this user.";
    public static final String strErrServerNotFound     = "Can't find this server.";
    public static final String strErrNickNotAuthed      = "That nickname is not authed.";
    public static final String strErrNoAccess           = "You do not have sufficient rights on that channel to use this command.";
    public static final String strErrModeNotSupported   = "This mode is not supported on this network.";
    public static final String strSuccess               = "Done.";
    public static final String strEndOfList             = "End of List.";
    public static final String strMsgNever              = "(never)";
    public static final String strMsgNone               = "(none)";
    public static final String strAutoBanReason         = "Banned.";
    public static final String strAutoExceptReason      = "Excepted.";
    public static final String strAutoInviteReason      = "Invited.";

    /* AUTHHISTORY */
    public static final String strAuthHisHeadFormat         = "%" + Const.COLUMN_ID_WIDTH + "s %-" + Const.COLUMN_NICK_WIDTH + "s %-" + Const.COLUMN_DATE_WIDTH
                                                            + "s %-" + Const.COLUMN_DATE_WIDTH + "s %-" + Const.COLUMN_MESSAGE_WIDTH + "s";
    public static final String strAuthHisHeadColID          = "#";
    public static final String strAuthHisHeadColDate1       = "Auth";
    public static final String strAuthHisHeadColDate2       = "Disconnect";
    public static final String strAuthHisHeadColUser        = "User";
    public static final String strAuthHisHeadColReason      = "Reason";

    /* CHANBEILIST */
    public static final String strChBeiListHeadFormat         = "%-" + Const.COLUMN_MASK_WIDTH + "s %-" + Const.COLUMN_NICK_WIDTH + "s %-" + Const.COLUMN_DATE_WIDTH
       + "s %-" + Const.COLUMN_DATE_WIDTH + "s %-" + Const.COLUMN_MESSAGE_WIDTH + "s";
    public static final String strChBeiListHeadColID          = "#";
    public static final String strChBeiListHeadColMask        = "Mask";
    public static final String strChBeiListHeadColDate1       = "Set on";
    public static final String strChBeiListHeadColDate2       = "Expires";
    public static final String strChBeiListHeadColSetBy       = "Set by";
    public static final String strChBeiListHeadColReason      = "Reason";
    public static final String strChBeiListHeadBans           = "Bans list for channel %s";
    public static final String strChBeiListHeadExcepts        = "Exceptions list for channel %s";
    public static final String strChBeiListHeadInvex          = "Invite exceptions list for channel %s";

    /* AUTOLIMIT */
    public static final String strAutoLimitErrUnknownCommand     = "Unknown command. Type SHOWCOMMANDS for a list of available commands.";
    public static final String strAutoLimitErrInvalidCommand     = "Invalid command. AUTOLIMIT <channel> [limit]].";
    public static final String strAutoLimitErrUnknown            = "Error setting autolimit.";
    public static final String strAutoLimitStrSuccessSummary     = " - Autolimit for %s : %s.";
    public static final String strAutoLimitCurConf               = "Current autolimit setting on %s: %s";

    /* AUTH */
    public static final String strAuthErrAlreadyAuth     = "You are already authed.";
    public static final String strAuthErrAccountCred     = "User account not found or suspended, or incorrect password.";
    public static final String strAuthErrCertFpNotAvail  = "Your are not eligible to CertFP authentication.";
    public static final String strAuthErrUnknown         = "There has been an error during authentication process.";
    public static final String strAuthSuccess            = "Auth successful.";

    /* HELLO */
    public static final String strHelloErrTooEasy            = "Password must contain at least %s (at most %s) characters with at least one of the following types: lowercase, uppercase, number, symbol.";
    public static final String strHelloErrEmailInvalid       = "Invalid email address.";
    public static final String strHelloErrAlreadyAuth        = "HELLO is not available once you have authed.";
    public static final String strHelloErrAccountExists      = "An account with that name already exists.";
    public static final String strHelloNewAccountCreated     = "Your account has been created with username \"%s\" and password \"%s\" but you are not authed. You can now auth using AUTH <username> <password>";
    public static final String strHelloThrottle              = "You can now auth using AUTH <username> <password>";


    /* CERTFP* */
    public static final String strCertFpErrMalformed    = "Malformed certificate fingerprint. Fingerprint must contains only hexadecimal characters (a-f, 0-9) and be <= 128 bytes long.";
    public static final String strCertFpErrAddMax       = "Could not add the fingerprint. Check that you have not reached the limit (%s) and delete some of them if necessary.";
    public static final String strCertFpErrNoCertFp     = "There is no CertFP to add. You must connect using a certificate in order to use this command.";
    public static final String strCertFpErrRemove       = "Could not remove the fingerprint.";
    public static final String strCertFpErrAddSyntax    = "Invalid command. CERTFPADD <certfp>.";
    public static final String strCertFpSucRemove       = "The requested certificate will be removed from your account if it matches one of your fingerprints.";
    public static final String strCertFpSucAdd          = "The following fingerprints have been added to your account: %s";
    public static final String strCertFpSucDel          = "The following fingerprints have been removed to your account, if they were existing: %s";
    public static final String strCertFpDenyManualAdd   = "Note: adding arbitrary fingerprint is disabled. Your current fingerprint will be added.";

    /* CHANFLAGS */
    public static final String strChanFlagsErrUnknown   = "Error setting chanflags.";
    public static final String strChanFlagsErrNoMod     = "Nothing changed. Your requested flag combination change was either the same as the existing flags, impossible, or you don't have enough access.";
    public static final String strChanFlagsList         = "Channel flags for %s: %s [%s]";
    public static final String strChanFlagsSuccessSumm  = " - New chan flags for %s: %s.";

    /* CHANLEV */
    public static final String strChanlevErrUnknown      = "Error setting the chanlev.";
    public static final String strChanlevDropChanLEmpty  = "Channel has been dropped because its chanlev was left empty.";
    public static final String strChanlevErrNoMod        = "Nothing changed. Your requested flag combination change was either the same as the existing flags, impossible, or you don't have enough access.";
    public static final String strChanlevSuccessSummary  = "Chanlev set. Chanlev for user account %s on channel %s is now +%s.";
    public static final String strChanlevListTitle       = "Displaying CHANLEV for channel %s:";
    public static final String strChanlevListCol1        = "Account";
    public static final String strChanlevListCol2        = "Chanlev";
    public static final String strChanlevListHeadFormat  = " %-" + Const.COLUMN_CHAN_WIDTH + "s %-" + Const.COLUMN_MESSAGE_WIDTH + "s";
    public static final String strChanlevListRowFormat   = " o %-" + (Const.COLUMN_CHAN_WIDTH-2) + "s +%-" + Const.COLUMN_MESSAGE_WIDTH + "s";

    /* USERFLAGS */
    public static final String strUserFlagsErrNoMode  = "You may have specified an invalid flags combination. Consult HELP USERFLAGS for valid flags.";
    public static final String strUserFlagsErrUnknown = "Error setting userflags.";
    public static final String strUserFlagsList       = "User flags for %s: +%s [%s]";

    /* HELP */
    public static final String strHelpErrNoHelp = "Help not available for that command.";

    /* WHOIS */
    public static final String strWhoisHeaderAccount                 = "-Information for account %s:";
    public static final String strWhoisHeaderNick                    = "-Information for user %s (using account %s):";
    public static final String strWhoisContentUserId                 = " o User ID                 :: %s";
    public static final String strWhoisContentUserLevel              = " o Staff                   :: %s %s";
    public static final String strWhoisContentUserFlags              = " o User flags              :: %s [%s]";
    public static final String strWhoisContentUserLinkedNicks        = " o Account users           :: %s";
    public static final String strWhoisContentUserCreated            = " o User created            :: %s";
    public static final String strWhoisContentUserLastAuth           = " o Last auth               :: %s";
    public static final String strWhoisContentUserEmail              = " o Email address           :: %s";
    public static final String strWhoisContentUserEmailLast          = " o Email last set          :: %s";
    public static final String strWhoisContentUserPassLast           = " o Pass last set           :: %s";
    public static final String strWhoisContentUserAuthSasl           = " o SASL-authed             :: %s %s";
    public static final String strWhoisContentUserSuspended          = " o Suspended               :: %s (Since/Last: %s, %s times): %s";
    public static final String strWhoisContentUserOperLogin          = " o Oper login (oper class) :: %s (%s)";
    public static final String strWhoisContentUserNickAliases        = " o Associated nicknames    :: %s, use NICKALIAS for more details.";
    public static final String strWhoisContentUserCertFpTitle        = " o Associated certfp       :: %s, use CERTFP for more details.";
    //public static final String strWhoisContentUserCertfpRowFormat    = " o %-" + Const.COLUMN_ID_WIDTH + "s %-" + Const.COLUMN_MESSAGE_WIDTH + "s";
    public static final String strWhoisContentUserCertfpRowFormat    = " o %s";
    public static final String strWhoisContentUserChanlevTitle       = "Known on the following channels:";
    public static final String strWhoisContentUserChanlevHeadFormat  = " %-" + Const.COLUMN_CHAN_WIDTH + "s %-" + Const.COLUMN_MESSAGE_WIDTH + "s";
    public static final String strWhoisContentUserChanlevRowFormat   = " o %-" + (Const.COLUMN_CHAN_WIDTH-2) + "s +%-" + Const.COLUMN_MESSAGE_WIDTH + "s";
    public static final String strWhoisContentUserChanlevCol1        = "Channel";
    public static final String strWhoisContentUserChanlevCol2        = "Flags:";
    public static final String strWhoisContentUserLevIrcop           = "IRC Operator";
    public static final String strWhoisContentUserLevStaff           = "Staff member";
    public static final String strWhoisContentSublistItem            = "   > %s";

    /* CERTFP */
    public static final String strCertFpTitle        = "Associated certfp: %s";

    /* CONFIGREPORT */
    public static final String strConfigInfoHeader                  = "-Configuration information";
    public static final String strConfigInfoNetworkHeader           = "Network section";
    public static final String strConfigInfoNetwork                 = " o network name: '%s', protocol: '%s'";
    public static final String strConfigInfoServerHeader            = "Server section";
    public static final String strConfigInfoServerInfo              = " o name: '%s', SID: '%s', description: '%s'";
    public static final String strConfigInfoServerProtoVersion      = " o protocol version: '%s', version flags: '%s', full version text: '%s'";
    public static final String strConfigInfoServerVersionString     = " o server version string: '%s'";
    public static final String strConfigInfoLinkHeader              = "Link section";
    public static final String strConfigInfoLinkPeer                = " o peer: '%s', host: '%s', port: '%s'";
    public static final String strConfigInfoCSHeader                = "CService section";
    public static final String strConfigInfoCSBotNick               = " o nick: '%s', UID: '%s', ident: '%s', host: '%s', realname: '%s', modes: '%s'";
    public static final String strConfigInfoCSVHost                 = " o VHost prefix: '%s', suffix: '%s'";
    public static final String strConfigInfoCSChannel               = " o channel default modes: '%s', ban time: '%s', autolimit: '%s', autolimit freq: '%s', max chanlev: '%s'";
    public static final String strConfigInfoCSAccount               = " o account max certfp: '%s', min pass: '%s', max pass: '%s', wrong cred wait: '%s', max chans: '%s', max auth histo: '%s'";
    public static final String strConfigInfoFeaturesHeader          = "Features section";
    public static final String strConfigInfoFeaturesNetwork         = " o network sasl: '%s', svslogin: '%s', chghost: '%s', deny auth on plain text: '%s'";
    public static final String strConfigInfoFeaturesCS              = " o CS random account name: '%s' (%s chars), temporary account pass: '%s' (%s chars)";
    public static final String strConfigInfoLoggingHeader           = "Logging section";
    public static final String strConfigInfoLogging                 = " o debug logging: '%s', logging to ES: '%s'";
    public static final String strConfigInfoDatabaseHeader          = "Database section";
    public static final String strConfigInfoDatabase                = " o type: '%s'";
    public static final String strConfigInfoSchedulerHeader         = "Scheduler section";
    public static final String strConfigInfoScheduler               = " o database freq: '%s'";

    /* CHANINFO */
    public static final String strChanInfoHeader                    = "-Information for channel %s:"; /* only known */
    public static final String strChanInfoCServeNotRegistered       = "Channel is not registered.";
    public static final String strChanInfoCServeHeader              = " CService:"; /* only known */
    public static final String strChanInfoContentRegistered         = " o Registered             :: %s";
    public static final String strChanInfoContentChanId             = " o Channel ID             :: %s";
    public static final String strChanInfoContentChanFlags          = " o Channel flags          :: +%s [%s]";
    public static final String strChanInfoContentChanCreated        = " o Channel registered     :: %s";
    public static final String strChanInfoContentChanWelcome        = " o Welcome message        :: %s";
    public static final String strChanInfoContentChanTopic          = " o Saved topic            :: %s";
    public static final String strChanInfoContentChanBanTime        = " o Ban time               :: %s";
    public static final String strChanInfoContentLockedModes        = " o Locked modes           :: %s";
    public static final String strChanInfoContentChanAutoLimit      = " o Auto imit              :: %s";
    public static final String strChanInfoContentChanOrphan         = " o Orphaned               :: %s %s"; /* only staff+ */
    public static final String strChanInfoContentChanSuspended      = " o Suspended              :: %s (Since/Last: %s, %s times): %s"; /* only staff+ */
    public static final String strChanInfoContentChanBanTitle       = " o Bans                   :: %s excl. channel bans, use BANLIST for more details"; /* only staff+ */
    public static final String strChanInfoContentChanExcTitle       = " o Excepts                :: %s excl. channel excepts, use EXCEPTLIST for more details"; /* only staff+ */
    public static final String strChanInfoContentChanInvTitle       = " o Invites                :: %s excl. channel invex, use INVITELIST for more details"; /* only staff+ */
    public static final String strChanInfoNetworkHeader             = " Network:"; /* only known */
    public static final String strChanInfoContentCurTimestamp       = " o Timestamp              :: %s"; /* only staff+ */
    public static final String strChanInfoContentCurTopic           = " o Topic                  :: %s"; /* only staff+ */
    public static final String strChanInfoContentCurModes           = " o Modes                  :: +%s"; /* only staff+ */
    public static final String strChanInfoContentCurModesLong       = " o Modes (long)           :: %s"; /* only staff+ */
    public static final String strChanInfoContentCurMlockPolicy     = " o MLOCK policy           :: +%s"; /* only staff+ */
    //public static final String strChanInfoContentCounter            = " o Counters              :: users:%s, +q:%s +a:%s +o:%s +h:%s +v:%s"; /* only staff+ */
    public static final String strChanInfoContentCounter            = " o Users                  :: %s (q:%s a:%s o:%s h:%s v:%s), use /NAMES for the list"; /* only staff+ */
    public static final String strChanInfoContentIdleTime           = " o Idle for               :: %s (since %s)"; /* only opers */
    public static final String strChanInfoContentIdleTimeStr        = "%s days, %s hours, %s minutes, %s seconds";
    public static final String strChanInfoContentCurKey             = " o Key                    :: %s"; /* only staff+ */
    public static final String strChanInfoContentCurUserLimit       = " o Users limit            :: %s"; /* only staff+ */
    public static final String strChanInfoContentCurFloodProf       = " o Flood profile          :: %s"; /* only staff+ */
    public static final String strChanInfoContentCurFloodParam      = " o Flood params           :: %s"; /* only staff+ */
    public static final String strChanInfoContentCurHistoParam      = " o History params         :: %s"; /* only staff+ */
    public static final String strChanInfoContentCurChanLink        = " o Linked channel         :: %s"; /* only staff+ */
    public static final String strChanInfoContentCurBEI             = " o Bans / excepts / invex :: %s / %s / %s"; /* only staff+ */
    public static final String strChanInfoContentCurBanTitle        = " o Bans (current)         :: %s"; /* only staff+ */
    public static final String strChanInfoContentCurExcTitle        = " o Excepts (current)      :: %s"; /* only staff+ */
    public static final String strChanInfoContentCurInvTitle        = " o Invites (current)      :: %s"; /* only staff+ */
    //public static final String strChanInfoContentCurBanTitle        = " o Bans (%s):"; /* only staff+ */
    //public static final String strChanInfoContentCurExcTitle        = " o Excepts (%s):"; /* only staff+ */
    //public static final String strChanInfoContentCurInvTitle        = " o Invites (%s):"; /* only staff+ */
    public static final String strChanInfoContentSublistItem        = "   > %s";

    /* SERVERINFO */
    public static final String strServerInfoHeader                    = "-Information for server %s:";
    public static final String strServerInfoContentSid                = " o SID                      :: %s";
    public static final String strServerInfoContentName               = " o Name                     :: %s";
    public static final String strServerInfoContentDescription        = " o Description              :: %s";
    public static final String strServerInfoContentCertfp             = " o Certificate fingerprint  :: %s";
    public static final String strServerInfoContentCountry            = " o Country                  :: %s (%s)";
    public static final String strServerInfoContentTimestamp          = " o Timestamp                :: %s";
    public static final String strServerInfoContentUserCount          = " o Users count              :: %s";
    public static final String strServerInfoContentParent             = " o Parent server            :: %s (%s)";
    public static final String strServerInfoContentChild              = " o Child servers            :: %s";
    public static final String strServerInfoContentSublistItem        = "   > %s - %s";

    /* NICKINFO */
    public static final String strNickInfoContentNickSummary        = "-Information for nick %s";
    public static final String strNickInfoContentUid                = " o UID                       :: %s";
    public static final String strNickInfoContentIdent              = " o Ident                     :: %s";
    public static final String strNickInfoContentRealname           = " o Realname                  :: %s";
    public static final String strNickInfoContentHostname           = " o Hostname                  :: %s";
    public static final String strNickInfoContentConnectFrom        = " o Realhost (IP)             :: %s (%s)";
    public static final String strNickInfoContentCloakedHost        = " o Cloaked host              :: %s";
    public static final String strNickInfoContentUserMasks          = " o User masks                :: %s";
    public static final String strNickInfoContentUserModes          = " o Modes                     :: +%s";
    public static final String strNickInfoContentUserModesLong      = " o Modes (long)              :: %s";
    public static final String strNickInfoContentSecureConnection   = " o Secure connection         :: %s";
    public static final String strNickInfoContentServerName         = " o Server                    :: %s (%s)";
    public static final String strNickInfoContentSignOnTS           = " o Signed on                 :: %s";
    public static final String strNickInfoContentAuthAccount        = " o User account              :: %s";
    public static final String strNickInfoContentUserOperLogin      = " o Oper login (oper class)   :: %s (%s)";
    public static final String strNickInfoContentUserSecGroup       = " o Security group            :: %s";
    public static final String strNickInfoContentUserCertFP         = " o Certificate fingerprint   :: %s";
    public static final String strNickInfoContentUserCountry        = " o Country                   :: %s (%s)";
    public static final String strNickInfoContentChanListTitle      = "Present on the following channels (modes):";
    public static final String strNickInfoContentChanListLine       = " o %s (+%s%s)";

    /* NICKHISTORY */
    public static final String strNickHistoryHeader      = "-Nick history for nick %s";
    public static final String strNickHistoryLine        = " %s :: %s";

    /* WELCOME */
    public static final String strWelcomeDispMess     = "Welcome message for %s: %s";
    public static final String strWelcomeErrUnknown   = "Error setting welcome for %s.";

    /* SUSPENDCHAN */
    public static final String strSuspendChanErrHistory    = "Cannot add suspend to history for channel: %s";
    public static final String strSuspendChanErrSuspended  = "This channel is already suspended.";

    /* UNSUSPENDCHAN */
    public static final String strUnSuspendChanErrSuspended    = "This channel is not suspended.";
    public static final String strUnSuspendChanErrHistory      = "Cannot update suspend to history for channel: %s";

    /* SUSPENDUSER */
    public static final String strSuspendUserErrSuspended    = "This user is already suspended.";
    public static final String strSuspendUserErrHistory      = "Cannot add suspend to history for user: %s";
    public static final String strSuspendUserErrUnknown      = "Error suspending the user.";

    public static final String strSuspendUserDeAuth          = "You have been deauthed because your account has been suspended (reason: %s).";

    /* UNSUSPENDUSER */
    public static final String strUnSuspendUserErrSuspended    = "This user is not suspended.";
    public static final String strUnSuspendUserErrHistory      = "Cannot update suspend to history for user: %s";

    /* SUSPENDHISTORY */
    public static final String strSuspendHisHeaderFormat     = "%" + Const.COLUMN_ID_WIDTH + "s %-" + Const.COLUMN_DATE_WIDTH + "s %-" + Const.COLUMN_DATE_WIDTH
                                                                  + "s %-" + Const.COLUMN_NICK_WIDTH + "s %-" + Const.COLUMN_NICK_WIDTH + "s %-" + Const.COLUMN_MESSAGE_WIDTH + "s";
    public static final String strSuspendHisHeadColID       = "#";
    public static final String strSuspendHisHeadColDate1    = "Suspend date";
    public static final String strSuspendHisHeadColDate2    = "Unsuspend date";
    public static final String strSuspendHisHeadColBy1      = "By (Id)";
    public static final String strSuspendHisHeadColBy2      = "By (Id)";
    public static final String strSuspendHisHeadColReason   = "Reason";


    /* DEAUTH */
    public static final String strDeAuthErrUnknown   = "Error while logging out.";

    /* DEAUTHALL */
    public static final String strDeAuthAllNotice     = "You have been DEAUTHed by the user %s using DEAUTHALL command on your account %s.";
    public static final String strDeAuthAllErrUnknown = "Error while logging out.";

    /* SETTOPIC */
    public static final String strSetTopicErrUnknown  = "Error setting topic for %s.";

    /* CLEARTOPIC */
    public static final String strClearTopicErrUnknown = "Error clearing topic for %s.";

    /* REQUESTBOT */
    public static final String strRequestBotErrChanNotPresentOrOp = "You must be present on the channel and be opped.";
    public static final String strRequestBotErrUnknown            = "Error while registering the channel.";
    public static final String strRequestBotErrChanlevFull        = "There are too many channels in user's chanlev. Remove some and try again.";
    public static final String strRequestBotErrCmdIncomplete      = "You must specify the target nick/#account for the channel.";
    public static final String strRequestBotErrNickOrAccNotFound  = "This nick is not online or this #account does not exist.";
    public static final String strRequestBotSuccess               = "Channel successfully registered.";

    /* DROPCHAN */
    public static final String strDropChanErrChanNotReg      = "The channel %s is not registered.";
    public static final String strDropChanErrUserNotOwner    = "You must have the flag +n in the channel's chanlev to be able to drop it.";
    public static final String strDropChanErrChanSuspended   = "You cannot drop a suspended channel.";
    public static final String strDropChanErrUnknown         = "Error dropping the channel.";
    public static final String strDropChanErrWrongConfirm    = "Incorrect confirmation code. Confirmation code reset.";
    public static final String strDropChanConfirmMessage1    = "Destructive operation: drop of channel %s requested. Please note that all the channel settings, chanlev, history... will be deleted. This action cannot be undone, even by the staff.";
    public static final String strDropChanConfirmMessage2    = "To confirm, please send the command: DROPCHAN %s %s";
    public static final String strDropChanConfirmMessage3    = "Please enter the confirmation code as: DROPCHAN %s %s";
    public static final String strDropChanSuccess            = "Channel successfully dropped.";


    /* DROPUSER */
    public static final String strDropUserErrUnknown         = "Error dropping the user.";
    public static final String strDropUserErrWrongConfirm    = "Incorrect confirmation code. Confirmation code reset.";
    public static final String strDropUserErrUserSuspended   = "You cannot drop a suspended user account.";
    public static final String strDropUserErrTargetUser      = "You cannot request that on another user.";
    public static final String strDropUserErrSyntax          = "Syntax: DROPUSER <nick|#user> [confirmationcode]";
    public static final String strDropUserConfirmMessage1    = "Destructive operation: drop of account %s requested. Please note that all the user data, history... will be deleted. This action cannot be undone, even by the staff.";
    public static final String strDropUserConfirmMessage2    = "To confirm, please send the command: DROPUSER #%s %s";
    public static final String strDropUserConfirmMessage3    = "Please enter the confirmation code as: DROPUSER #%s %s";
    public static final String strDropUserSuccess            = "User successfully dropped.";
    public static final String strDropUserDeAuth             = "You have been deauthed because your account is being dropped.";

    /* NEWPASS */
    public static final String strNewPassErrUpdate           = "Problem during password update.";
    public static final String strNewPassSucOtherUser        = "Changing password for user account %s";

    /* REGUSERLIST */
    public static final String strUserlistHeader      = "List of registered users:";
    public static final String strUserlistHeader2     = "Legend: B:Account banned, O:Orphan, P:Protected, S:Suspended, -:N/A";
    public static final String strUserlistEntry       = " o [] %s - %s";

    /* REGCHANLIST */
    public static final String strChanlistHeader      = "List of registered channels:";
    public static final String strChanlistHeader2     = "Legend: G:Chan banned, O:Orphan, P:Protected, S:Suspended, -:N/A";
    public static final String strChanlistEntry       = " o [%s] %s - %s";

    /* CHANMODE (MLOCK) */
    public static final String strMlockModeList         = "Current modes for channel %s: %s, locked modes: %s";
    public static final String strMlockModeSet          = "Locking modes for channel %s: %s";
    public static final String strMlockModeClear        = "Cleared locked modes for channel %s";

    /*
     * OperService
     */

    /* NETINFO */
    public static final String strOSNetInfoHeader           = "-Network and Protocol information";
    public static final String strOSNetInfoNetName          = " o Network name                      :: %s";
    public static final String strOSNetInfoLongLines        = " o Network long lines support        :: %s";
    public static final String strOSNetInfoUserModes        = " o User modes                        :: %s";
    public static final String strOSNetInfoChanModes        = " o Channel modes                     :: %s";
    public static final String strOSNetInfoChanOwner        = " o Channel OWNER prefix              :: %s";
    public static final String strOSNetInfoChanAdmin        = " o Channel ADMIN prefix              :: %s";
    public static final String strOSNetInfoChanOp           = " o Channel OP prefix                 :: %s";
    public static final String strOSNetInfoChanHalfOp       = " o Channel HALF-OP prefix            :: %s";
    public static final String strOSNetInfoChanVoice        = " o Channel VOICE prefix              :: %s";
    public static final String strOSNetInfoChanMlock        = " o Channel MLOCK support             :: %s";



    /*
     * Misc
     */

    /* Channel part messages */
    public static final String strChanPartRejoin        = "Be right back.";
    public static final String strChanPartRenchan       = "Leaving channel because it has been renamed.";
    public static final String strChanPartDropActivity  = "Leaving channel due to lack of activity.";
    public static final String strChanPartDropManual    = "Leaving channel because it has been dropped.";
    public static final String strChanPartSuspend       = "Leaving channel because it has been suspended.";

    /* Configuration output */
    public static final String strConfxxx               = "";

    /* Misc strings */
    public static final String strMiscTheCakeIsALie        = "The cake is a lie.";
    public static final String strUnsupported              = "Not supported";
    public static final String strSupported                = "Supported";
    public static final String strYes                      = "Yes";
    public static final String strNo                       = "No";
    public static final String strNA                       = "N/A";
    public static final String strNone                     = "None";
    public static final String strUnknown                  = "Unknown";

}
