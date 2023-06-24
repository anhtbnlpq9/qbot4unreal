/**
 * Class that defines the messages returned by the bot.
 */
public class Messages {

    public Messages() {

    }

    /* Generic */
    static String strErrCommandUnknown     = "Unknown command. Type SHOWCOMMANDS for a list of available commands.";
    static String strErrCommandSyntax      = "Command has invalid syntax. Type HELP <COMMAND> to get information on that command.";
    static String strErrUserNonReg         = "Can't find this user.";
    static String strErrChanNonExist       = "This channel does not exist.";
    static String strErrChanNonReg         = "Can't find this channel.";
    static String strErrChanSusOrNotFound  = "Channel %s is unknown or suspended.";
    static String strErrChanSuspended      = "Can't change this setting while the channel is suspended.";
    static String strErrChanNotJoined      = "Can't use that command on a -j channel.";
    static String strErrNickNotFound       = "Can't find this nick.";
    static String strErrNickNotAuthed      = "That nickname is not authed.";
    static String strErrNoAccess           = "You do not have sufficient rights on %s to use %s.";
    static String strErrModeNotSupported   = "This network does not support that mode.";
    static String strSuccess               = "Done.";
    static String strEndOfList             = "End of List.";
    static String strMsgNever              = "(never)";
    static String strMsgNone               = "(none)";
    static String strAutoBanReason         = "Banned.";

    /* AUTHHISTORY */
    static String strAuthHistoryHead       = "#:  User:                                             Authed:                         Disconnected:       Reason:";

    /* AUTOLIMIT */
    static String strAutoLimitErrUnknownCommand     = "Unknown command. Type SHOWCOMMANDS for a list of available commands.";
    static String strAutoLimitErrInvalidCommand     = "Invalid command. AUTOLIMIT <channel> [limit]].";
    static String strAutoLimitErrUnknown            = "Error setting autolimit.";
    static String strAutoLimitStrSuccessSummary     = " - Autolimit for %s : %s.";
    static String strAutoLimitCurConf               = "Current autolimit setting on %s: %s";

    /* AUTH */
    static String strAuthErrAlreadyAuth     = "You are already authed.";
    static String strAuthErrAccountCred     = "User account not found or suspended, or incorrect password.";
    static String strAuthErrCertFpNotAvail  = "Your are not eligible to CertFP authentication.";
    static String strAuthSuccess            = "Auth successful.";

    /* HELLO */
    static String strHelloErrTooEasy        = "HELLO: Password must contain at least %s (at most %s) characters with at least one of the following types: lowercase, uppercase, number, symbol.";
    static String strHelloErrEmailInvalid   = "HELLO: Invalid email address.";
    static String strHelloErrAlreadyAuth    = "HELLO is not available once you have authed.";
    static String strHelloErrAccountExists  = "An account with that name already exists.";
    static String strHelloSucNewAccount     = "Your account has been created with username \"%s\" but you are not authed. You can now auth using AUTH %s <password>";


    /* CERTFP* */
    static String strCertFpErrMalformed    = "Malformed certificate fingerprint. Fingerprint must contains only hexadecimal characters (a-f, 0-9) and be <= 128 bytes long.";
    static String strCertFpErrAdd          = "Could not add the fingerprint. Check that you have not reached the limit (%s) and delete some of them if necessary.";
    static String strCertFpErrNoCertFp     = "There is no CertFP to add. You must connect using a certificate in order to use this command.";
    static String strCertFpErrRemove       = "Could not remove the fingerprint.";
    static String strCertFpErrAddSyntax    = "Invalid command. CERTFPADD <certfp>.";
    static String strCertFpSucRemove       = "The requested certificate will be removed from your account if it matches one of your fingerprints.";
    static String strCertFpSucAdd          = "The following fingerprint has been added to your account: %s";

    /* CHANFLAGS */
    static String strChanFlagsErrUnknown   = "Error setting chanflags.";
    static String strChanFlagsErrNoMod     = "Nothing changed. Your requested flag combination change was either the same as the existing flags, impossible, or you don't have enough access.";
    static String strChanFlagsList         = "Channel flags for %s: %s";
    static String strChanFlagsSuccessSumm  = " - New chan flags for %s: %s.";

    /* CHANLEV */
    static String strChanlevErrUnknown      = "Error setting the chanlev.";
    static String strChanlevDropChanLEmpty  = "Channel has been dropped because its chanlev was left empty.";
    static String strChanlevErrNoMod        = "Nothing changed. Your requested flag combination change was either the same as the existing flags, impossible, or you don't have enough access.";
    static String strChanlevSuccessSummary  = "Chanlev set. Chanlev for user account %s is now +%s.";
    static String strChanlevListTitle       = "Displaying CHANLEV for channel %s:";
    static String strChanlevListHeader      = "Account             Chanlev";

    /* USERFLAGS */
    static String strUserFlagsErrNoMode  = "You may have specified an invalid flags combination. Consult HELP USERFLAGS for valid flags.";
    static String strUserFlagsErrUnknown = "Error setting userflags.";
    static String strUserFlagsList       = "User flags for %s: +%s";

    /* HELP */
    static String strHelpErrNoHelp = "Help not available for that command.";

    /* WHOIS */
    static String strWhoisHeaderAccount            = "-Information for account %s:";
    static String strWhoisHeaderNick               = "-Information for user %s (using account %s):";
    static String strWhoisContentUserId            = "User ID        : %s";
    static String strWhoisContentUserLevel         = "Staff          : %s %s";
    static String strWhoisContentUserFlags         = "User flags     : %s";
    static String strWhoisContentUserLinkedNicks   = "Account users  : %s";
    static String strWhoisContentUserCreated       = "User created   : %s";
    static String strWhoisContentUserLastAuth      = "Last auth      : %s";
    static String strWhoisContentUserEmail         = "Email address  : %s";
    static String strWhoisContentUserEmailLast     = "Email last set : %s";
    static String strWhoisContentUserPassLast      = "Pass last set  : %s";
    static String strWhoisContentUserSuspensions   = "Account suspensions: %s, suspended: %s (Since/Last: %s)";
    static String strWhoisContentUserSuspended     = "Account suspended: %s (Since: %s), %s time(s)";
    static String strWhoisContentUserCertFpTitle   = "List of registered certificate fingerprints:";
    static String strWhoisContentUserChanlevTitle  = "Known on the following channels:";
    static String strWhoisContentUserChanlevHead   = "Channel                        Flags:";
    static String strWhoisContentUserLevIrcop      = "IRC Operator";
    static String strWhoisContentUserLevStaff      = "Staff member";

    /* WELCOME */
    static String strWelcomeDispMess     = "Welcome message for %s: %s";
    static String strWelcomeErrUnknown   = "Error setting welcome for %s.";

    /* SUSPENDCHAN */
    static String strSuspendChanErrHistory    = "Cannot add suspend to history for channel: %s";
    static String strSuspendChanErrSuspended  = "This channel is already suspended.";

    /* UNSUSPENDCHAN */
    static String strUnSuspendChanErrSuspended    = "This channel is not suspended.";
    static String strUnSuspendChanErrHistory      = "Cannot update suspend to history for channel: %s";

    /* SUSPENDUSER */
    static String strSuspendUserErrSuspended    = "This user is already suspended.";
    static String strSuspendUserErrHistory      = "Cannot add suspend to history for user: %s";
    static String strSuspendUserErrUnknown      = "Error suspending the user.";

    static String strSuspendUserDeAuth          = "You have been deauthed because your account has been suspended (reason: %s).";

    /* UNSUSPENDUSER */
    static String strUnSuspendUserErrSuspended    = "This user is not suspended.";
    static String strUnSuspendUserErrHistory      = "Cannot update suspend to history for user: %s";
    

    /* LOGOUT */
    static String strLogoutErrUnknown   = "Error while logging out.";

    /* SETTOPIC */
    static String strSetTopicErrUnknown  = "Error setting topic for %s.";

    /* CLEARTOPIC */
    static String strClearTopicErrUnknown = "Error clearing topic for %s.";

    /* REQUESTBOT */
    static String strRequestBotErrChanNotPresentOrOp = "You must be present on the channel and be opped.";
    static String strRequestBotErrUnknown            = "Error while registering the channel.";
    static String strRequestBotErrChanlevFull        = "There are too many channels in user's chanlev. Remove some and try again.";
    static String strRequestBotErrCmdIncomplete      = "You must specify the target nick/#account for the channel.";
    static String strRequestBotErrNickOrAccNotFound  = "This nick is not online or this #account does not exist.";
    static String strRequestBotSuccess               = "Channel successfully registered.";

    /* DROPCHAN */
    static String strDropChanErrChanNotReg      = "The channel %s is not registered.";
    static String strDropChanErrUserNotOwner    = "You must have the flag +n in the channel's chanlev to be able to drop it.";
    static String strDropChanErrChanSuspended   = "You cannot drop a suspended channel.";
    static String strDropChanErrUnknown         = "Error dropping the channel.";
    static String strDropChanErrWrongConfirm    = "Incorrect confirmation code. Confirmation code reset.";
    static String strDropChanConfirmMessage1    = "Destructive operation: dropping of channel %s requested. Please note that all the channel settings, chanlev, history... will be deleted. This action cannot be undone, even by the staff.";
    static String strDropChanConfirmMessage2    = "To confirm, please send the command: DROPCHAN %s %s";
    static String strDropChanConfirmMessage3    = "Please enter the confirmation code as: DROPCHAN %s %s";
    static String strDropChanSuccess            = "Channel successfully dropped.";


    /* DROPUSER */
    static String strDropUserErrUnknown         = "Error dropping the user.";
    static String strDropUserErrWrongConfirm    = "Incorrect confirmation code. Confirmation code reset.";
    static String strDropUserErrUserSuspended   = "You cannot drop a suspended user account.";
    static String strDropUserErrTargetUser      = "You cannot request that on another user.";
    static String strDropUserErrSyntax          = "Syntax: DROPUSER <nick|#user> [confirmationcode]";
    static String strDropUserConfirmMessage1    = "Destructive operation: dropping of account %s requested. Please note that all the data, history... will be deleted. This action cannot be undone, even by the staff.";
    static String strDropUserConfirmMessage2    = "To confirm, please send the command: DROPUSER #%s %s";
    static String strDropUserConfirmMessage3    = "Please enter the confirmation code as: DROPUSER #%s %s";

    static String strDropUserSuccess            = "User successfully dropped.";

    static String strDropUserDeAuth             = "You have been deauthed because your account is being dropped.";


    
}
