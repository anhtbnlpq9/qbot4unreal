/**
 * Class that defines the messages returned by the bot.
 */
public class Messages {

    public Messages() {

    }

    /* Generic */
    static String strErrCommandUnknown   = "Unknown command. Type SHOWCOMMANDS for a list of available commands.";
    static String strErrCommandSyntax    = "Command has syntax. Type HELP <COMMAND> to get information on that command.";

    static String strErrUserNonReg     = "Can't find this user.";

    static String strErrChanNonReg         = "Can't find this channel.";
    static String strErrChanSusOrNotFound  = "Channel %s is unknown or suspended.";

    static String strErrNickNotFound   = "Can't find this nick.";
    static String strErrNickNotAuthed  = "That nickname is not authed.";

    static String strErrNoAccess       = "You do not have sufficient rights on %s to use %s.";



    static String strSuccess          = "Done.";
    
    static String strEndOfList        = "End of List.";

    static String strMsgNever         = "(never)";
    static String strMsgNone          = "(none)";




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

    static String strAuthSuccess = "Auth successful.";

    /* CERTFP* */
    static String strCertFpErrMalformed    = "Malformed certificate fingerprint. Fingerprint must contains only hexadecimal characters (a-f, 0-9) and be <= 128 bytes long.";
    static String strCertFpErrUnknown      = "Could not add the fingerprint. Check that you have not reached the limit (%s) and delete some of them if necessary.";
    static String strCertFpErrNoCertFp     = "There is no CertFP to add. You must connect using a certificate in order to use this command.";
    
}
