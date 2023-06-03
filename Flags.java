import java.util.Map;
import java.util.Set;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;

import static java.util.Map.entry;

/**
 * Flags class to host all methods regarding flags
 * (user flags, chan flags, chanlev flags).
 * @author me
 */
abstract class Flags {

    /*
     * User flags
     * ==========
     * 
     * The following is the description of the user flags. User flags do not take argument.
     * +a ADMIN         :: Gives the user the admin status on the bot.
     *                     The admin has the right to perform any possible action on the bot,
     *                     such as DIE, RESTART or changing USERFLAGS, CHANFLAGS and CHANLEV
     *                     status of anyone/channel.
     * +g GLINE         :: TBD
     * +l NOAUTHLIMIT   :: Inhibit the per-account login limit.
     * +o OPER          :: Gives the user the oper status on the bot.
     *                     The oper has the right to perform operator-related actions:
     *                     - access to full WHOIS information of any user,
     *                     - access to any CHANLEV/CHANFLAGS/USERFLAGS information of any user,
     *                     - access to CHANLIST/USERLIST/SERVERLIST commands,
     *                     - access to GLINE/GZLINE/KILL commands,
     *                     - access to SUSPEND/UNSUSPEND commands (except to +a uflag carriers),
     *                     - modify CHANFLAGS of any channel (except for suspended channels),
     *                     - modify USERFLAGS of any user (except +az uflag users), except personal flags,
     *                     - modify CHANLEV of any channel (except +az uflag users), except personal flags.
     * +p PROTECT       :: TBD
     * +q STAFF         :: Gives the user the staff status on the bot.
     *                     The staff has the right to perform operator-related actions:
     *                     - access to additional WHOIS information (user CHANLEV) of any user
     *                     - access to any CHANLEV/CHANFLAGS/USERFLAGS information, of any user
     * +v NOAUTOVHOST   :: Disable auto setting the user vhost when authing
     * +w WELCOME       :: Hide the welcome message when the user join any channel (same effect
     *                     as setting +w in all the channel CHANLEVs the user is in)
     * +z SUSPENDED     :: Marks the user as suspended. At that moment the user account is frozen:
     *                     - it is not possible to auth with the account,
     *                     - it is not possible to perform USERFLAGS/CHANLEV modifications,
     *                     - the user does not appear inside the CHANLEV of any channel,
     *                     - the user WHOIS and CHANLEV will return an account-not-found error (except if performed by a staff member).
     * 
     * Note: some flags cannot be set through USERFLAGS but with specific commands: +g, +z.
     */

    //private static final Integer   UFLAG_SPARE_A       = 0x80000000; // +A
    //private static final Integer   UFLAG_SPARE_B       = 0x40000000; // +B
    //private static final Integer   UFLAG_SPARE_C       = 0x20000000; // +C
    //private static final Integer   UFLAG_SPARE_D       = 0x10000000; // +D
    //private static final Integer   UFLAG_SPARE_E       = 0x08000000; // +E
    //private static final Integer   UFLAG_SPARE_F       = 0x04000000; // +F
    private static final Integer   UFLAG_ADMIN         = 0x02000000; // +a
    //private static final Integer   UFLAG_SPARE_b       = 0x01000000; // +b
    //private static final Integer   UFLAG_SPACE_c       = 0x00800000; // +c
    //private static final Integer   UFLAG_SPARE_d       = 0x00400000; // +d
    //private static final Integer   UFLAG_SPARE_e       = 0x00200000; // +e
    //private static final Integer   UFLAG_SPARE_f       = 0x00100000; // +f
    private static final Integer   UFLAG_GLINE         = 0x00080000; // +g
    //private static final Integer   UFLAG_SPARE_h       = 0x00040000; // +h
    //private static final Integer   UFLAG_SPARE_i       = 0x00020000; // +i
    //private static final Integer   UFLAG_SPARE_j       = 0x00010000; // +j
    //private static final Integer   UFLAG_SPARE_k       = 0x00008000; // +k
    private static final Integer   UFLAG_NOAUTHLIMIT   = 0x00004000; // +l
    //private static final Integer   UFLAG_SPARE_m       = 0x00002000; // +m
    //private static final Integer   UFLAG_SPARE_n       = 0x00001000; // +n
    private static final Integer   UFLAG_OPER          = 0x00000800; // +o
    private static final Integer   UFLAG_PROTECT       = 0x00000400; // +p
    private static final Integer   UFLAG_STAFF         = 0x00000200; // +q
    //private static final Integer   UFLAG_SPARE_r       = 0x00000100; // +r
    //private static final Integer   UFLAG_SPARE_s       = 0x00000080; // +s
    //private static final Integer   UFLAG_SPARE_t       = 0x00000040; // +t
    //private static final Integer   UFLAG_SPARE_u       = 0x00000020; // +u
    private static final Integer   UFLAG_AUTOVHOST     = 0x00000010; // +v
    private static final Integer   UFLAG_WELCOME       = 0x00000008; // +w
    //private static final Integer   UFLAG_SPARE_x       = 0x00000004; // +x
    //private static final Integer   UFLAG_SPARE_y       = 0x00000002; // +y
    private static final Integer   UFLAG_SUSPENDED     = 0x00000001; // +z

    private static final Integer   UFLAG_ALL           = 0xffffffff;

    // User control
    private static final Integer   UFLAGS_USERCON      = (UFLAG_WELCOME | UFLAG_AUTOVHOST);

    // Oper control
    private static final Integer   UFLAGS_OPERCON      = ( UFLAG_NOAUTHLIMIT | UFLAG_PROTECT | UFLAG_STAFF);

    // Admin control
    private static final Integer   UFLAGS_ADMINCON     = (UFLAG_ADMIN | UFLAG_OPER | UFLAGS_OPERCON | UFLAGS_USERCON);

    private static final Integer   UFLAGS_ALLOWED      = (UFLAGS_USERCON | UFLAGS_OPERCON | UFLAGS_ADMINCON);

    private static final Integer   UFLAG_STAFF_PRIV    = (UFLAG_STAFF | UFLAG_OPER | UFLAG_ADMIN );
    private static final Integer   UFLAG_OPER_PRIV     = (UFLAG_OPER | UFLAG_ADMIN );
    private static final Integer   UFLAG_ADMIN_PRIV    = (UFLAG_ADMIN );
    
    private static final Integer   UFLAGS_PUBLIC       = (UFLAG_AUTOVHOST | UFLAG_WELCOME);

    private static final Integer   UFLAGS_NEW_ACCOUNT  = (UFLAG_AUTOVHOST | UFLAG_WELCOME);

    private static final Integer   UFLAGS_READONLY     = ( UFLAG_SUSPENDED | UFLAG_GLINE ); /* flags non-settable through USERFLAGS */



    /**
     * Maps user flag char to constant
     */
    private static final Map<String, Integer> userFlagCharMap = Map.ofEntries(
        entry("a",   UFLAG_ADMIN),
        entry("g",   UFLAG_GLINE),
        entry("l",   UFLAG_NOAUTHLIMIT),
        entry("o",   UFLAG_OPER),
        entry("p",   UFLAG_PROTECT),
        entry("q",   UFLAG_STAFF),
        entry("v",   UFLAG_AUTOVHOST),
        entry("w",   UFLAG_WELCOME),
        entry("z",   UFLAG_SUSPENDED)
    );


    /**
     * Maps user flag constant to char
     */
    private static final Map<Integer, String> userFlagCharRevMap = Map.ofEntries(
        entry(UFLAG_ADMIN,          "a"),
        entry(UFLAG_GLINE,          "g"),
        entry(UFLAG_NOAUTHLIMIT,    "l"),
        entry(UFLAG_OPER,           "o"),
        entry(UFLAG_PROTECT,        "p"),
        entry(UFLAG_STAFF,          "q"),
        entry(UFLAG_AUTOVHOST,      "v"),
        entry(UFLAG_WELCOME,        "w"),
        entry(UFLAG_SUSPENDED,      "z")
    );

    /* 
     * Channel flags
     * =============
     * 
     * The following is the description of the channel flags. Channel flags do not take argument.
     * +b BITCH         :: The bot will enforce modes according to their chanlev. If an user is set modes for
     *                     owner/admin/op/halfop/voice, the bot will clear it.
     * +c AUTOLIMIT     :: Enables the autolimit in order to limit potential join flood. The bot will regulary
     *                     set a channel limit after users has joined or left the channel. The limit can be set 
     *                     using the AUTOLIMIT command.
     * +e ENFORCE       :: The bot will enforce bans and kick the users that match the channel ban list.
     * +f FORCETOPIC    :: The bot will enforce the topic set in the SETTOPIC setting. If this flag is enabled, it will
     *                     be possible to change the topic only through the SETTOPIC command.
     * +j JOINED        :: When set, the bot will stay on the channel.
     * +k KNOWNONLY     :: Only allows users with the Known privilege (+m +n +o +h +v +k) to enter the channel. The bot
     *                     will kick unknown users.
     * +p PROTECT       :: TBD
     * +t TOPICSAVE     :: Save the topic when it changes. The topic can be restored with the SETTOPIC command or when the bot
     *                     reconnects.
     * +v VOICEALL      :: Automatically voices all the users that join the channel whether they are in the channel CHANLEV or not.
     *                     Users with chanlev flag +u are not voiced.
     * +w WELCOME       :: Send the welcome notice to the users joining the channel.
     * +z SUSPENDED     :: Marks the channel as suspended and frozen. Also the +j flag will be cleared. Once set,
     *                     - the bot will leave the channel,
     *                     - it is not possible to perform modifications inside the channel parameters (CHANLEV/CHANFLAGS/SETTOPIC...),
     *                     - the channel will not appear inside an user WHOIS,
     *                     - the CHANFLAGS and CHANLEV will return a channel-not-found error (except if performing user is +a or +o or +q),
     *                     - it is not possible to perform a REQUESTBOT nor DROP on a suspended channel.
     * 
     * Note: some flags cannot be set through CHANFLAGS but with specific commands: +z.
     */

    //private static final Integer   CHFLAG_SPARE_A       = 0x80000000; // +A
    //private static final Integer   CHFLAG_SPARE_B       = 0x40000000; // +B
    //private static final Integer   CHFLAG_SPARE_C       = 0x20000000; // +C
    //private static final Integer   CHFLAG_SPARE_D       = 0x10000000; // +D
    //private static final Integer   CHFLAG_SPARE_E       = 0x08000000; // +E
    //private static final Integer   CHFLAG_SPARE_F       = 0x04000000; // +F
    //private static final Integer   CHFLAG_SPARE_a       = 0x02000000; // +a
    private static final Integer   CHFLAG_BITCH         = 0x01000000; // +b
    private static final Integer   CHFLAG_AUTOLIMIT     = 0x00800000; // +c
    //private static final Integer   CHFLAG_SPARE_d       = 0x00400000; // +d
    private static final Integer   CHFLAG_ENFORCE       = 0x00200000; // +e
    private static final Integer   CHFLAG_FORCETOPIC    = 0x00100000; // +f
    //private static final Integer   CHFLAG_SPARE_g       = 0x00080000; // +g
    //private static final Integer   CHFLAG_SPARE_h       = 0x00040000; // +h
    //private static final Integer   CHFLAG_SPARE_i       = 0x00020000; // +i
    private static final Integer   CHFLAG_JOINED        = 0x00010000; // +j
    private static final Integer   CHFLAG_KNOWNONLY     = 0x00008000; // +k
    //private static final Integer   CHFLAG_SPARE_l       = 0x00004000; // +l
    //private static final Integer   CHFLAG_SPARE_m       = 0x00002000; // +m
    //private static final Integer   CHFLAG_SPARE_n       = 0x00001000; // +n
    //private static final Integer   CHFLAG_SPARE_o       = 0x00000800; // +o
    private static final Integer   CHFLAG_PROTECT       = 0x00000400; // +p
    //private static final Integer   CHFLAG_SPARE_q       = 0x00000200; // +q
    //private static final Integer   CHFLAG_SPARE_r       = 0x00000100; // +r
    //private static final Integer   CHFLAG_SPARE_s       = 0x00000080; // +s
    private static final Integer   CHFLAG_TOPICSAVE     = 0x00000040; // +t
    //private static final Integer   CHFLAG_SPARE_u       = 0x00000020; // +u
    private static final Integer   CHFLAG_VOICEALL      = 0x00000010; // +v
    private static final Integer   CHFLAG_WELCOME       = 0x00000008; // +w
    //private static final Integer   CHFLAG_SPARE_x       = 0x00000004; // +x
    //private static final Integer   CHFLAG_SPARE_y       = 0x00000002; // +y
    private static final Integer   CHFLAG_SUSPENDED     = 0x00000001; // +z

    private static final Integer   CHFLAG_ALL           = 0xfffeffff; // except +j

    private static final Integer   CHFLAG_MASTERCONTROL  = (CHFLAG_BITCH | CHFLAG_AUTOLIMIT | CHFLAG_PROTECT | CHFLAG_ENFORCE | CHFLAG_FORCETOPIC | CHFLAG_KNOWNONLY | CHFLAG_WELCOME |
                                                           CHFLAG_TOPICSAVE | CHFLAG_VOICEALL);

    private static final Integer   CHFLAG_OWNERCONTROL   = (CHFLAG_MASTERCONTROL ); 

    private static final Integer   CHFLAG_OPERCONTROL   = ( CHFLAG_OWNERCONTROL | CHFLAG_MASTERCONTROL );
    private static final Integer   CHFLAG_ADMINCONTROL  = ( CHFLAG_OPERCONTROL );
                                                           
    private static final Integer   CHFLAGS_READONLY     = ( CHFLAG_SUSPENDED | CHFLAG_JOINED ); /* flags non-settable through CHANFLAGS */

    private static final Integer   CHFLAGS_PUBLIC       = ( CHFLAG_BITCH | CHFLAG_AUTOLIMIT | CHFLAG_ENFORCE | CHFLAG_FORCETOPIC | CHFLAG_JOINED | CHFLAG_KNOWNONLY | 
                                                            CHFLAG_TOPICSAVE | CHFLAG_VOICEALL | CHFLAG_WELCOME);

    private static final Integer   CHFLAGS_ALLOWED      = ( CHFLAG_MASTERCONTROL | CHFLAG_OWNERCONTROL | CHFLAG_ADMINCONTROL | CHFLAG_OPERCONTROL );

    private static final Integer   CHFLAGS_NEW_CHAN     = (CHFLAG_WELCOME | CHFLAG_JOINED);


    /**
     * Maps chan flag char to constant
     */
    private static final Map<String, Integer> chanFlagCharMap = Map.ofEntries(
        entry("b",   CHFLAG_BITCH),
        entry("c",   CHFLAG_AUTOLIMIT),
        entry("e",   CHFLAG_ENFORCE),
        entry("f",   CHFLAG_FORCETOPIC),
        entry("j",   CHFLAG_JOINED),
        entry("k",   CHFLAG_KNOWNONLY),
        entry("p",   CHFLAG_PROTECT),
        entry("t",   CHFLAG_TOPICSAVE),
        entry("v",   CHFLAG_VOICEALL),
        entry("w",   CHFLAG_WELCOME),
        entry("z",   CHFLAG_SUSPENDED)
    );


    /**
     * Maps chan flag constant to char
     */
    private static final Map<Integer, String> chanFlagCharRevMap = Map.ofEntries(
        entry(CHFLAG_BITCH,         "b"),
        entry(CHFLAG_AUTOLIMIT,     "c"),
        entry(CHFLAG_ENFORCE,       "e"),
        entry(CHFLAG_FORCETOPIC,    "f"),
        entry(CHFLAG_JOINED,        "j"),
        entry(CHFLAG_KNOWNONLY,     "k"),
        entry(CHFLAG_PROTECT,       "p"),
        entry(CHFLAG_TOPICSAVE,     "t"),
        entry(CHFLAG_VOICEALL,      "v"),
        entry(CHFLAG_WELCOME,       "w"),
        entry(CHFLAG_SUSPENDED,     "z")
    );


    
    /* 
     * Channel user level ("chanlev") flags
     * ====================================
     * 
     * The following is the description of the chanlev flags. Chanlev flags do not take argument.
     * +a AUTO          :: Auto voices/halfops/ops/admins/owners the user upon authing/joining the chan.
     *                     The highest corresponding flag (among +hmnov) is applied. An user with +amo will
     *                     be set Admin channel mode and not Admin + Op.
     * +b BANNED        :: Auto bans the user upon authing/joining the channel.
     * +d DENYOP        :: Punishment flag. Prevents an user to be opped. If the user is opped, the bot will deop them.
     * +h HALFOP        :: Gives the user the HalfOp flag on the channel. This also gives the user the known privilege.
     *                     This is associated to the HalfOp channel mode +h.
     * +j AUTOINVITE    :: Personnal flag. If the user holds this flag, the bot will invite the user to the channel upon authing.
     * +k KNOWN         :: Gives the user the Known flag on the channel. This gives the user the known privilege.
     *                     The user may have the right to use the INVITE command.
     * +m MASTER        :: Gives the user the Master flag on the channel. This also gives the user the known privilege.
     *                     The user may have the right to modify the CHANLEV (except setting/clearing +m and +n flags),
     *                     and modifying the channel flags through CHANFLAGS. This is associated to the Admin channel mode +a.
     * +n OWNER         :: Gives the user the Owner flag on the channel. This also gives the user the known privilege.
     *                     The user may have the right to modify the CHANLEV, and modifying the channel flags through CHANFLAGS.
     *                     This is associated to the Owner channel mode +q.
     * +o OP            :: Gives the user the Op flag on the channel. This also gives the user the known privilege.
     *                     The user may have the right to list the CHANLEV and CHANFLAGS. This is associated to the
     *                     Op channel mode +o.
     * +p PROTECT       :: Protects the user holding the flag. Coupled with another flag (+m +n +o +h +v), it will prevent the user
     *                     to be deowner/deadmin/deopped/dehalfopped/devoiced. If so the bot will revert the mode.
     *                     The flag applies only to the highest mode (if the user with +mop is deopped, the bot will not act, but
     *                     if the user is deadmin, the bot will set the admin mode to the user, similar to flag +a).
     * +t TOPIC         :: Gives the user the right to use the SETTOPIC command.
     * +u DENYVOICE     :: Punishment flag. Prevents an user to be voiced. If the user is voiced, the bot will devoice them.
     * +v VOICE         :: Gives the user the Op flag on the channel. This also gives the user the known privilege.
     *                     This is associated to the Voice channel mode +v.
     * +w HIDEWELCOME   :: Personnal flag. If the user holds this flag, the bot will not send them the welcome notice
     *                     upon joining the channel.
     * 
     * Notes: - Personal flags can only be set/cleared by the user itself. They appear inside the CHANLEV only to the
     *          holding user.
     *        - Punishment flags are only listed to the users that hold +m or +n in the channel.
     *        - Users may always be able to clear any of their flags in the CHANLEV.
     *        - Effects are immediate upon CHANLEV set. E.g. setting +b on an user will ban them immediately.
     *        - The known privilege permits the user to use the INVITE command.
     *        - Users without CHANLEV privilege always may display their own chanlev listing their flags on a channel
     *          using the WHOIS command.
     */

    private static final String    CLFLAGS_LIST         = "abdhjkmnoptuvw";

    //private static final Integer   CLFLAG_SPARE_A       = 0x80000000; // +A
    //private static final Integer   CLFLAG_SPARE_B       = 0x40000000; // +B
    //private static final Integer   CLFLAG_SPARE_C       = 0x20000000; // +C
    //private static final Integer   CLFLAG_SPARE_D       = 0x10000000; // +D
    //private static final Integer   CLFLAG_SPARE_E       = 0x08000000; // +E
    //private static final Integer   CLFLAG_SPARE_F       = 0x04000000; // +F
    private static final Integer   CLFLAG_AUTO          = 0x02000000; // +a
    private static final Integer   CLFLAG_BANNED        = 0x01000000; // +b
    //private static final Integer   CLFLAG_SPARE_c       = 0x00800000; // +c
    private static final Integer   CLFLAG_DENYOP        = 0x00400000; // +d
    //private static final Integer   CLFLAG_SPARE_e       = 0x00200000; // +e
    //private static final Integer   CLFLAG_SPARE_f       = 0x00100000; // +f
    //private static final Integer   CLFLAG_SPARE_g       = 0x00080000; // +g
    private static final Integer   CLFLAG_HALFOP        = 0x00040000; // +h
    //private static final Integer   CLFLAG_SPARE_i       = 0x00020000; // +i
    private static final Integer   CLFLAG_AUTOINVITE    = 0x00010000; // +j
    private static final Integer   CLFLAG_KNOWN         = 0x00008000; // +k
    //private static final Integer   CLFLAG_SPARE_l       = 0x00004000; // +l
    private static final Integer   CLFLAG_MASTER        = 0x00002000; // +m
    private static final Integer   CLFLAG_OWNER         = 0x00001000; // +n
    private static final Integer   CLFLAG_OP            = 0x00000800; // +o
    private static final Integer   CLFLAG_PROTECT       = 0x00000400; // +p
    //private static final Integer   CLFLAG_SPARE_q       = 0x00000200; // +q
    //private static final Integer   CLFLAG_SPARE_r       = 0x00000100; // +r
    //private static final Integer   CLFLAG_SPARE_s       = 0x00000080; // +s
    private static final Integer   CLFLAG_TOPIC         = 0x00000040; // +t
    private static final Integer   CLFLAG_DENYVOICE     = 0x00000020; // +u
    private static final Integer   CLFLAG_VOICE         = 0x00000010; // +v
    private static final Integer   CLFLAG_HIDEWELCOME   = 0x00000008; // +w
    //private static final Integer   CLFLAG_SPARE_x       = 0x00000004; // +x
    //private static final Integer   CLFLAG_SPARE_y       = 0x00000002; // +y
    //private static final Integer   CLFLAG_SPARE_z       = 0x00000001; // +z

    private static final Integer   CLFLAG_ALL           = 0xffffffff;

    private static final Integer   CLFLAG_VOICE_PRIV    = (CLFLAG_VOICE | CLFLAG_OP | CLFLAG_HALFOP | CLFLAG_MASTER | CLFLAG_OWNER);
    private static final Integer   CLFLAG_HALFOP_PRIV   = (CLFLAG_HALFOP | CLFLAG_OP | CLFLAG_MASTER | CLFLAG_OWNER);
    private static final Integer   CLFLAG_OP_PRIV       = (CLFLAG_OP | CLFLAG_MASTER | CLFLAG_OWNER);
    private static final Integer   CLFLAG_MASTER_PRIV   = (CLFLAG_MASTER | CLFLAG_OWNER);
    private static final Integer   CLFLAG_OWNER_PRIV    = (CLFLAG_OWNER);

    private static final Integer   CLFLAGS_KNOWNONCHAN  = (CLFLAG_KNOWN | CLFLAG_VOICE_PRIV | CLFLAG_HALFOP_PRIV | CLFLAG_OP_PRIV | CLFLAG_MASTER_PRIV | CLFLAG_OWNER_PRIV);
    private static final Integer   CLFLAG_SIGNIFICANT   = (CLFLAG_MASTER | CLFLAG_OWNER | CLFLAG_OP);

    private static final Integer   CLFLAG_MASTERCON     = (CLFLAG_AUTO | CLFLAG_BANNED | CLFLAG_DENYOP | CLFLAG_OP | CLFLAG_HALFOP | 
                                                           CLFLAG_DENYVOICE | CLFLAG_TOPIC | CLFLAG_VOICE | CLFLAG_PROTECT);

    private static final Integer   CLFLAG_OWNERCON     = (CLFLAG_MASTERCON | CLFLAG_OWNER | CLFLAG_MASTER);

    private static final Integer   CLFLAG_SELFCON       = (CLFLAG_OP | CLFLAG_VOICE | CLFLAG_AUTO | CLFLAG_TOPIC);

    private static final Integer   CLFLAGS_PUBLIC       = (CLFLAG_OWNER | CLFLAG_MASTER | CLFLAG_OP | CLFLAG_VOICE | 
                                                           CLFLAG_KNOWN | CLFLAG_AUTO | CLFLAG_TOPIC | CLFLAG_PROTECT);

    private static final Integer   CLFLAGS_PUNISH       = (CLFLAG_BANNED | CLFLAG_DENYVOICE | CLFLAG_DENYOP);

    private static final Integer   CLFLAGS_PERSONAL     = (CLFLAG_HIDEWELCOME | CLFLAG_AUTOINVITE);

    private static final Integer   CHANLEV_OWNER_DEF    = ( CLFLAG_OWNER | CLFLAG_AUTO | CLFLAG_OP );

    private static final Integer   CLFLAGS_ALLOWED      = ( CLFLAGS_PUBLIC | CLFLAGS_PUNISH | CLFLAGS_PERSONAL );


    /**
     * Maps chanlev flag char to constant
     */
    private static final Map<String, Integer> chanlevFlagCharMap = Map.ofEntries(
        entry("a",   CLFLAG_AUTO),
        entry("b",   CLFLAG_BANNED),
        entry("d",   CLFLAG_DENYOP),
        entry("h",   CLFLAG_HALFOP),
        entry("j",   CLFLAG_AUTOINVITE),
        entry("k",   CLFLAG_KNOWN),
        entry("m",   CLFLAG_MASTER),
        entry("n",   CLFLAG_OWNER),
        entry("o",   CLFLAG_OP),
        entry("p",   CLFLAG_PROTECT),
        entry("t",   CLFLAG_TOPIC),
        entry("u",   CLFLAG_DENYVOICE),
        entry("v",   CLFLAG_VOICE),
        entry("w",   CLFLAG_HIDEWELCOME)
    );


    /**
     * Maps chanlev flag constant to char
     */
    private static final Map<Integer, String> chanlevFlagCharRevMap = Map.ofEntries(
        entry(CLFLAG_AUTO,           "a"),
        entry(CLFLAG_BANNED,         "b"),
        entry(CLFLAG_DENYOP,         "d"),
        entry(CLFLAG_HALFOP,         "h"),
        entry(CLFLAG_AUTOINVITE,     "j"),
        entry(CLFLAG_KNOWN,          "k"),
        entry(CLFLAG_MASTER,         "m"),
        entry(CLFLAG_OWNER,          "n"),
        entry(CLFLAG_OP,             "o"),
        entry(CLFLAG_PROTECT,        "p"),
        entry(CLFLAG_TOPIC,          "t"),
        entry(CLFLAG_DENYVOICE,      "u"),
        entry(CLFLAG_VOICE,          "v"),
        entry(CLFLAG_HIDEWELCOME,    "w")
    );

    /* Other static attributes for the class */
    static String flagText  = "";
    static Integer flagInt  =  0;

    /*
     * Chanlev-related methods
     */

    /**
     * Returns the allowed chanlev flags list TODO: to reimplement with numeric
     * @return Allowed chanlev flags list
     */
    public static String getAllowedChanLFlags() {
        return CLFLAGS_LIST;
    }

    public static Integer stripUnknownChanlevFlags(Integer chanlev) {
        return (chanlev & CLFLAGS_ALLOWED);
    }

    public static Integer keepChanlevOwnerConFlags(Integer chanlev) {
        return (chanlev & CLFLAG_OWNERCON);
    }

    public static Integer keepChanlevMasterConFlags(Integer chanlev) {
        return (chanlev & CLFLAG_MASTERCON);
    }

    public static Integer keepChanlevPersonalConFlags(Integer chanlev) {
        return (chanlev & CLFLAGS_PERSONAL);
    }

    public static Integer keepChanlevSelfConFlags(Integer chanlev) {
        return (chanlev & CLFLAG_SELFCON);
    }

    public static Integer keepChanlevPublicFlags(Integer chanlev) {
        return (chanlev & CLFLAGS_PUBLIC);
    }

    public static Integer stripChanlevPersonalFlags(Integer chanlev) {
        return (chanlev & ~CLFLAGS_PERSONAL);
    }

    public static Integer stripChanlevPunishFlags(Integer chanlev) {
        return (chanlev & ~CLFLAGS_PUNISH);
    }



    /**
     * Returns whether the user is channel owner
     * @param chanlev Chanlev
     * @return True of False
     */
    public static Boolean isChanLOwner(Integer chanlev) {
        if ( (chanlev & CLFLAG_OWNER) == 0 ) {
            return false;

        }
        else return true;
    }

    /**
     * Returns whether the user is channel master
     * @param chanlev Chanlev
     * @return True or False
     */
    public static Boolean isChanLMaster(Integer chanlev) {
        if ( (chanlev & CLFLAG_MASTER) == 0 ) {
            return false;

        }
        else return true;
    }

    /**
     * Returns if the user has chanlev OP
     * @param chanlev Chanlev
     * @return True or False
     */
    public static Boolean isChanLOp(Integer chanlev) {
        if ( (chanlev & CLFLAG_OP) == 0 ) {
            return false;

        }
        else return true;
    }

    /**
     * Returns if the user has chanlev VOICE
     * @param chanlev Chanlev
     * @return True or False
     */
    public static Boolean isChanLVoice(Integer chanlev) {
        if ( (chanlev & CLFLAG_VOICE) == 0 ) {
            return false;

        }
        else return true;
    }

    /**
     * Returns if the user has chanlev AUTO
     * @param chanlev Chanlev
     * @return True or False
     */
    public static Boolean isChanLAuto(Integer chanlev) {
        if ( (chanlev & CLFLAG_AUTO) == 0 ) {
            return false;

        }
        else return true;
    }

    /**
     * Returns if the user has chanlev BANNED
     * @param chanlev Chanlev
     * @return True or False
     */
    public static Boolean isChanLBanned(Integer chanlev) {
        if ( (chanlev & CLFLAG_BANNED) == 0 ) {
            return false;

        }
        else return true;
    }

    /**
     * Returns if the user has chanlev DENYOP
     * @param chanlev Chanlev
     * @return True or False
     */
    public static Boolean isChanLDenyOp(Integer chanlev) {
        if ( (chanlev & CLFLAG_DENYOP) == 0 ) {
            return false;

        }
        else return true;
    }

    /**
     * Returns if the user has chanlev DENYVOICE
     * @param chanlev Chanlev
     * @return True or False
     */
    public static Boolean isChanLDenyVoice(Integer chanlev) {
        if ( (chanlev & CLFLAG_DENYVOICE) == 0 ) {
            return false;

        }
        else return true;
    }

    /**
     * Returns if the user has chanlev TOPIC
     * @param chanlev Chanlev
     * @return True or False
     */
    public static Boolean isChanLTopic(Integer chanlev) {
        if ( (chanlev & CLFLAG_TOPIC) == 0 ) {
            return false;

        }
        else return true;
    }

    /**
     * Returns if the user has chanlev HIDEWELCOME
     * @param chanlev Chanlev
     * @return True or False
     */
    public static Boolean isChanLHideWelcome(Integer chanlev) {
        if ( (chanlev & CLFLAG_HIDEWELCOME) == 0 ) {
            return false;

        }
        else return true;
    }

    /**
     * Returns if the user has chanlev PROTECT
     * @param chanlev Chanlev
     * @return True or False
     */
    public static Boolean isChanLProtect(Integer chanlev) {
        if ( (chanlev & CLFLAG_PROTECT) == 0 ) {
            return false;

        }
        else return true;
    }

    /**
     * Returns if the user has chanlev KNOWN
     * @param chanlev Chanlev
     * @return True or False
     */
    public static Boolean isChanLKnown(Integer chanlev) {
        if ( (chanlev & CLFLAG_KNOWN) == 0 ) {
            return false;

        }
        else return true;
    }

    /**
     * Returns if the user has chanlev AUTOINVITE
     * @param chanlev Chanlev
     * @return True or False
     */
    public static Boolean isChanLAutoInvite(Integer chanlev) {
        if ( (chanlev & CLFLAG_AUTOINVITE) == 0 ) {
            return false;

        }
        else return true;
    }

    /**
     * Returns if the user has chanlev HALFOP
     * @param chanlev Chanlev
     * @return True or False
     */
    public static Boolean isChanLHalfOp(Integer chanlev) {
        if ( (chanlev & CLFLAG_HALFOP) == 0 ) {
            return false;

        }
        else return true;
    }

    /**
     * Returns if the user has voice privilege
     * @param chanlev Chanlev
     * @return True or False
     */
    public static Boolean hasChanLVoicePriv(Integer chanlev) {
        if ( (chanlev & CLFLAG_VOICE_PRIV) == 0 ) {
            return false;

        }
        else return true;
    }

    /**
     * Returns if the user has op privilege
     * @param chanlev Chanlev
     * @return True or False
     */
    public static Boolean hasChanLOpPriv(Integer chanlev) {
        if ( (chanlev & CLFLAG_OP_PRIV) == 0 ) {
            return false;

        }
        else return true;
    }

    /**
     * Returns if the user has halfop privilege
     * @param chanlev Chanlev
     * @return True or False
     */
    public static Boolean hasChanLHalfOpPriv(Integer chanlev) {
        if ( (chanlev & CLFLAG_HALFOP_PRIV) == 0 ) {
            return false;

        }
        else return true;
    }

    /**
     * Returns if the user has admin/master privilege
     * @param chanlev Chanlev
     * @return True or False
     */
    public static Boolean hasChanLMasterPriv(Integer chanlev) {
        if ( (chanlev & CLFLAG_MASTER_PRIV) == 0 ) {
            return false;

        }
        else return true;
    }
    
    /**
     * Returns if the user has owner privilege
     * @param chanlev Chanlev
     * @return True or False
     */
    public static Boolean hasChanLOwnerPriv(Integer chanlev) {
        if ( (chanlev & CLFLAG_OWNER_PRIV) == 0 ) {
            return false;

        }
        else return true;
    }

    /**
     * Returns if the user has known privilege
     * @param chanlev Chanlev
     * @return True or False
     */
    public static Boolean hasChanLKnown(Integer chanlev) {
        if ( (chanlev & CLFLAGS_KNOWNONCHAN) == 0 ) {
            return false;

        }
        else return true;
    }

    /**
     * Returns if the user has significant privilege
     * @param chanlev Chanlev
     * @return True or False
     */
    public static Boolean hasChanLSignificant(Integer chanlev) {
        if ( (chanlev & (CLFLAG_SIGNIFICANT)) == 0 ) {
            return false;

        }
        else return true;
    }

    /**
     * Returns resulting chanlev for Set Owner
     * @param chanlev Chanlev
     * @return Reulting chanlev
     */
    public static Integer setChanLOwner(Integer chanlev) {
        return (chanlev | CLFLAG_OWNER);
    }

    /**
     * Returns resulting chanlev for Set Master
     * @param chanlev Chanlev
     * @return Reulting chanlev
     */
    public static Integer setChanLMaster(Integer chanlev) {
        return (chanlev | CLFLAG_MASTER);
    }

    /**
     * Returns resulting chanlev for Set Op
     * @param chanlev Chanlev
     * @return Reulting chanlev
     */
    public static Integer setChanLOp(Integer chanlev) {
        return (chanlev | CLFLAG_OP);
    }

    /**
     * Returns resulting chanlev for Set HalfOp
     * @param chanlev Chanlev
     * @return Reulting chanlev
     */
    public static Integer setChanLHalfOp(Integer chanlev) {
        return (chanlev | CLFLAG_HALFOP);
    }

    /**
     * Returns resulting chanlev for Set Voice
     * @param chanlev Chanlev
     * @return Reulting chanlev
     */
    public static Integer setChanLVoice(Integer chanlev) {
        return (chanlev | CLFLAG_VOICE);
    }

    /**
     * Returns resulting chanlev for Set Auto
     * @param chanlev Chanlev
     * @return Reulting chanlev
     */
    public static Integer setChanLAuto(Integer chanlev) {
        return (chanlev | CLFLAG_AUTO);
    }

    /**
     * Returns resulting chanlev for Set Banned
     * @param chanlev Chanlev
     * @return Reulting chanlev
     */
    public static Integer setChanLBanned(Integer chanlev) {
        return (chanlev | CLFLAG_BANNED);
    }

    /**
     * Returns resulting chanlev for Set DenyOp
     * @param chanlev Chanlev
     * @return Reulting chanlev
     */
    public static Integer setChanLDenyOp(Integer chanlev) {
        return (chanlev | CLFLAG_DENYOP);
    }

    /**
     * Returns resulting chanlev for Set DenyVoice
     * @param chanlev Chanlev
     * @return Reulting chanlev
     */
    public static Integer setChanLDenyVoice(Integer chanlev) {
        return (chanlev | CLFLAG_DENYVOICE);
    }

    /**
     * Returns resulting chanlev for Set Topic
     * @param chanlev Chanlev
     * @return Reulting chanlev
     */
    public static Integer setChanLTopic(Integer chanlev) {
        return (chanlev | CLFLAG_TOPIC);
    }

    /**
     * Returns resulting chanlev for Set HideWelcome
     * @param chanlev Chanlev
     * @return Reulting chanlev
     */
    public static Integer setChanLHideWelcome(Integer chanlev) {
        return (chanlev | CLFLAG_HIDEWELCOME);
    }

    /**
     * Returns resulting chanlev for Set Known
     * @param chanlev Chanlev
     * @return Reulting chanlev
     */
    public static Integer setChanLKnown(Integer chanlev) {
        return (chanlev | CLFLAG_KNOWN);
    }

    /**
     * Returns resulting chanlev for Set Auto Invite
     * @param chanlev Chanlev
     * @return Reulting chanlev
     */
    public static Integer setChanLAutoInvite(Integer chanlev) {
        return (chanlev | CLFLAG_AUTOINVITE);
    }

    /**
     * Returns resulting chanlev for Clear Owner
     * @param chanlev Chanlev
     * @return Reulting chanlev
     */
    public static Integer clearChanLOwner(Integer chanlev) {
        return (chanlev & ~CLFLAG_OWNER);
    }

    /**
     * Returns resulting chanlev for Clear Master
     * @param chanlev Chanlev
     * @return Reulting chanlev
     */
    public static Integer clearChanLMaster(Integer chanlev) {
        return (chanlev & ~CLFLAG_MASTER);
    }

    /**
     * Returns resulting chanlev for Clear HalfOp
     * @param chanlev Chanlev
     * @return Reulting chanlev
     */
    public static Integer clearChanLHalfOp(Integer chanlev) {
        return (chanlev & ~CLFLAG_HALFOP);
    }

    /**
     * Returns resulting chanlev for Clear Op
     * @param chanlev Chanlev
     * @return Reulting chanlev
     */
    public static Integer clearChanLOp(Integer chanlev) {
        return (chanlev & ~CLFLAG_OP);
    }

    /**
     * Returns resulting chanlev for Clear Voice
     * @param chanlev Chanlev
     * @return Reulting chanlev
     */
    public static Integer clearChanLVoice(Integer chanlev) {
        return (chanlev & ~CLFLAG_VOICE);
    }

    /**
     * Returns resulting chanlev for Clear Auto
     * @param chanlev Chanlev
     * @return Reulting chanlev
     */
    public static Integer clearChanLAuto(Integer chanlev) {
        return (chanlev & ~CLFLAG_AUTO);
    }

    /**
     * Returns resulting chanlev for Clear Banned
     * @param chanlev Chanlev
     * @return Reulting chanlev
     */
    public static Integer clearChanLBanned(Integer chanlev) {
        return (chanlev & ~CLFLAG_BANNED);
    }

    /**
     * Returns resulting chanlev for Clear DenyOp
     * @param chanlev Chanlev
     * @return Reulting chanlev
     */
    public static Integer clearChanLDenyOp(Integer chanlev) {
        return (chanlev & ~CLFLAG_DENYOP);
    }

    /**
     * Returns resulting chanlev for Clear DenyVoice
     * @param chanlev Chanlev
     * @return Reulting chanlev
     */
    public static Integer clearChanLDenyVoice(Integer chanlev) {
        return (chanlev & ~CLFLAG_DENYVOICE);
    }

    /**
     * Returns resulting chanlev for Clear Topic
     * @param chanlev Chanlev
     * @return Reulting chanlev
     */
    public static Integer clearChanLTopic(Integer chanlev) {
        return (chanlev & ~CLFLAG_TOPIC);
    }

    /**
     * Returns resulting chanlev for Clear HideWelcome
     * @param chanlev Chanlev
     * @return Reulting chanlev
     */
    public static Integer clearChanLHideWelcome(Integer chanlev) {
        return (chanlev & ~CLFLAG_HIDEWELCOME);
    }

    /**
     * Returns resulting chanlev for Clear Known
     * @param chanlev Chanlev
     * @return Reulting chanlev
     */
    public static Integer clearChanLKnown(Integer chanlev) {
        return (chanlev & ~CLFLAG_KNOWN);
    }

    /**
     * Returns resulting chanlev for Clear AutoInvite
     * @param chanlev Chanlev
     * @return Reulting chanlev
     */
    public static Integer clearChanLAutoInvite(Integer chanlev) {
        return (chanlev & ~CLFLAG_AUTOINVITE);
    }

    /**
     * Returns if the requested chanlev contains personal flags
     * @param chanlev
     * @return
     */
    public static Boolean containsPersonalFlags(Integer chanlev) {
        if ( (chanlev & CLFLAGS_PERSONAL) == 0) {
            return false;
        }
        else return true;
    }

    /**
     * Returns if the requested chanlev contains owner-control flags
     * @param chanlev
     * @return True or False
     */
    public static Boolean containsChanLOwnerConFlags(Integer chanlev) {
        if ( (chanlev & CLFLAG_OWNERCON) == 0) {
            return false;
        }
        else return true;
    }

    /**
     * Returns if the requested chanlev contains master-control flags
     * @param chanlev
     * @return True or False
     */
    public static Boolean containsChanLMasterConFlags(Integer chanlev) {
        if ( (chanlev & CLFLAG_MASTERCON) == 0) {
            return false;
        }
        else return true;
    }

    /**
     * Returns if the requested chanlev contains self-control flags
     * @param chanlev
     * @return True or False
     */
    public static Boolean containsChanLSelfConFlags(Integer chanlev) {
        if ( (chanlev & CLFLAG_SELFCON) == 0) {
            return false;
        }
        else return true;
    }

    /**
     * Returns if the requested chanlev contains personal flags
     * @param chanlev
     * @return True or False
     */
    public static Boolean containsChanLPunishFlags(Integer chanlev) {
        if ( (chanlev & CLFLAGS_PUNISH) == 0) {
            return false;
        }
        else return true;
    }

    /**
     * Returns the requested chanlev filtered out of punish flags
     * @param chanlev
     * @return Filtered chanlev
     */
    public static Boolean noChanLPunishFlags(Integer chanlev) {
        if ( (chanlev & ~CLFLAGS_PUNISH) == 0) {
            return false;
        }
        else return true;
    }

    /**
     * Returns the requested chanlev filtered out of non-public flags
     * @param chanlev
     * @return Filtered chanlev
     */
    public static Integer onlyChanLPublicFlags(Integer chanlev) {
        return ( chanlev & CLFLAGS_PUBLIC );
    }

    /**
     * Returns a cleared chanlev
     * @param chanlev
     * @return Resulting chanlev
     */
    public static Integer clearChanlev(Integer chanlev) {
        return (chanlev & ~CLFLAG_ALL);
    }

    /**
     * Fetches the chanlev flag int
     * @param character Chanlev flag character
     * @return Chanlev flag int
     */
    public static Integer getChanLFlagInt(String character) {
        try {
            return chanlevFlagCharMap.get(character);
        }
        catch (Exception e) { return 0; }
    }
    
    /**
     * Fetches the chanlev flag character
     * @param chanlev Chanlev flag int
     * @return Chanlev flag character
     */
    public static String getChanLFlagChar(Integer chanlev) {
        try {
            return chanlevFlagCharRevMap.get(chanlev);
        }
        catch (Exception e) { return ""; }
    }

    /**
     * Returns the owner default chanlev for new registered chans
     * @return
     */
    public static Integer getChanLFlagOwnerDefault() {
        return CHANLEV_OWNER_DEF;
    }

    /*
     * Chanflags-related methods
     */

    /**
     * Returns the public channel flags list
     * @return Public channel flags list
     */
    public static Integer getPublicChanFlags() {
        return CHFLAGS_PUBLIC;
    }

    /**
     * Returns the default flags for new user accounts
     * @return default user flags
     */
    public static Integer getDefaultChanFlags() {
        return CHFLAGS_NEW_CHAN;
    }

    /**
     * Returns if the channel has flag BITCH
     * @param userFlags Channel flags
     * @return True or False
     */
    public static Boolean isChanBitch(Integer chanFlags) {
        if ( (chanFlags & CHFLAG_BITCH) == 0) {
            return false;
        }
        else return true;
    }

    /**
     * Returns if the channel has flag ENFORCE
     * @param userFlags Channel flags
     * @return True or False
     */
    public static Boolean isChanEnforce(Integer chanFlags) {
        if ( (chanFlags & CHFLAG_ENFORCE) == 0) {
            return false;
        }
        else return true;
    }

    /**
     * Returns if the channel has flag AUTOLIMIT
     * @param userFlags Channel flags
     * @return True or False
     */
    public static Boolean isChanAutolimit(Integer chanFlags) {
        if ( (chanFlags & CHFLAG_AUTOLIMIT) == 0) {
            return false;
        }
        else return true;
    }

    /**
     * Returns if the channel has flag FORCETOPIC
     * @param userFlags Channel flags
     * @return True or False
     */
    public static Boolean isChanForceTopic(Integer chanFlags) {
        if ( (chanFlags & CHFLAG_FORCETOPIC) == 0) {
            return false;
        }
        else return true;
    }

    /**
     * Returns if the channel has flag JOINED
     * @param userFlags Channel flags
     * @return True or False
     */
    public static Boolean isChanJoined(Integer chanFlags) {
        if ( (chanFlags & CHFLAG_JOINED) == 0) {
            return false;
        }
        else return true;
    }

    /**
     * Returns if the channel has flag KNOWNONLY
     * @param userFlags Channel flags
     * @return True or False
     */
    public static Boolean isChanKnownOnly(Integer chanFlags) {
        if ( (chanFlags & CHFLAG_KNOWNONLY) == 0) {
            return false;
        }
        else return true;
    }

    /**
     * Returns if the channel has flag PROTECT
     * @param userFlags Channel flags
     * @return True or False
     */
    public static Boolean isChanProtect(Integer chanFlags) {
        if ( (chanFlags & CHFLAG_PROTECT) == 0) {
            return false;
        }
        else return true;
    }

    /**
     * Returns if the channel has flag TOPICSAVE
     * @param userFlags Channel flags
     * @return True or False
     */
    public static Boolean isChanTopicSave(Integer chanFlags) {
        if ( (chanFlags & CHFLAG_TOPICSAVE) == 0) {
            return false;
        }
        else return true;
    }

    /**
     * Returns if the channel has flag VOICEALL
     * @param userFlags Channel flags
     * @return True or False
     */
    public static Boolean isChanVoiceAll(Integer chanFlags) {
        if ( (chanFlags & CHFLAG_VOICEALL) == 0) {
            return false;
        }
        else return true;
    }

    /**
     * Returns if the channel has flag WELCOME
     * @param userFlags Channel flags
     * @return True or False
     */
    public static Boolean isChanWelcome(Integer chanFlags) {
        if ( (chanFlags & CHFLAG_WELCOME) == 0) {
            return false;
        }
        else return true;
    }

    /**
     * Returns if the channel has flag SUSPENDED
     * @param userFlags Channel flags
     * @return True or False
     */
    public static Boolean isChanSuspended(Integer chanFlags) {
        if ( (chanFlags & CHFLAG_SUSPENDED) == 0) {
            return false;
        }
        else return true;
    }

    /**
     * Sets the chan flag BITCH
     * @param chanFlags Chan flags
     * @return Resulting chanflag
     */
    public static Integer setChanBitch(Integer chanFlags) {
        return (chanFlags | CHFLAG_BITCH);
    }

    /**
     * Sets the chan flag AUTOLIMIT
     * @param chanFlags Chan flags
     * @return Resulting chanflag
     */
    public static Integer setChanAutoLimit(Integer chanFlags) {
        return (chanFlags | CHFLAG_AUTOLIMIT);
    }

    /**
     * Sets the chan flag ENFORCE
     * @param chanFlags Chan flags
     * @return Resulting chanflag
     */
    public static Integer setChanEnforce(Integer chanFlags) {
        return (chanFlags | CHFLAG_ENFORCE);
    }

    /**
     * Sets the chan flag FORCETOPIC
     * @param chanFlags Chan flags
     * @return Resulting chanflag
     */
    public static Integer setChanForceTopic(Integer chanFlags) {
        return (chanFlags | CHFLAG_FORCETOPIC);
    }

    /**
     * Sets the chan flag JOINED
     * @param chanFlags Chan flags
     * @return Resulting chanflag
     */
    public static Integer setChanJoined(Integer chanFlags) {
        return (chanFlags | CHFLAG_JOINED);
    }

    /**
     * Sets the chan flag KNOWNONLY
     * @param chanFlags Chan flags
     * @return Resulting chanflag
     */
    public static Integer setChanKnownOnly(Integer chanFlags) {
        return (chanFlags | CHFLAG_KNOWNONLY);
    }

    /**
     * Sets the chan flag PROTECT
     * @param chanFlags Chan flags
     * @return Resulting chanflag
     */
    public static Integer setChanProtect(Integer chanFlags) {
        return (chanFlags | CHFLAG_PROTECT);
    }

    /**
     * Sets the chan flag TOPICSAVE
     * @param chanFlags Chan flags
     * @return Resulting chanflag
     */
    public static Integer setChanTopicSave(Integer chanFlags) {
        return (chanFlags | CHFLAG_TOPICSAVE);
    }

    /**
     * Sets the chan flag VOICEALL
     * @param chanFlags Chan flags
     * @return Resulting chanflag
     */
    public static Integer setChanVoiceAll(Integer chanFlags) {
        return (chanFlags | CHFLAG_VOICEALL);
    }

    /**
     * Sets the chan flag WELCOME
     * @param chanFlags Chan flags
     * @return Resulting chanflag
     */
    public static Integer setChanWelcome(Integer chanFlags) {
        return (chanFlags | CHFLAG_WELCOME);
    }

    /**
     * Sets the chan flag SUSPENDED
     * @param chanFlags Chan flags
     * @return Resulting chanflag
     */
    public static Integer setChanSuspended(Integer chanFlags) {
        return (chanFlags | CHFLAG_SUSPENDED);
    }

    /**
     * Clears the chan flag BITCH
     * @param chanFlags Chan flags
     * @return Resulting chanflag
     */
    public static Integer clearChanBitch(Integer chanFlags) {
        return (chanFlags & ~CHFLAG_BITCH);
    }

    /**
     * Clears the chan flag AUTOLIMIT
     * @param chanFlags Chan flags
     * @return Resulting chanflag
     */
    public static Integer clearChanAutoLimit(Integer chanFlags) {
        return (chanFlags & ~CHFLAG_AUTOLIMIT);
    }

    /**
     * Clears the chan flag ENFORCE
     * @param chanFlags Chan flags
     * @return Resulting chanflag
     */
    public static Integer clearChanEnforce(Integer chanFlags) {
        return (chanFlags & ~CHFLAG_ENFORCE);
    }

    /**
     * Clears the chan flag FORCETOPIC
     * @param chanFlags Chan flags
     * @return Resulting chanflag
     */
    public static Integer clearChanForceTopic(Integer chanFlags) {
        return (chanFlags & ~CHFLAG_FORCETOPIC);
    }

    /**
     * Clears the chan flag JOINED
     * @param chanFlags Chan flags
     * @return Resulting chanflag
     */
    public static Integer clearChanJoined(Integer chanFlags) {
        return (chanFlags & ~CHFLAG_JOINED);
    }

    /**
     * Clears the chan flag KNOWNONLY
     * @param chanFlags Chan flags
     * @return Resulting chanflag
     */
    public static Integer clearChanKnownOnly(Integer chanFlags) {
        return (chanFlags & ~CHFLAG_KNOWNONLY);
    }

    /**
     * Clears the chan flag PROTECT
     * @param chanFlags Chan flags
     * @return Resulting chanflag
     */
    public static Integer clearChanProtect(Integer chanFlags) {
        return (chanFlags & ~CHFLAG_PROTECT);
    }

    /**
     * Clears the chan flag TOPICSAVE
     * @param chanFlags Chan flags
     * @return Resulting chanflag
     */
    public static Integer clearChanTopicSave(Integer chanFlags) {
        return (chanFlags & ~CHFLAG_TOPICSAVE);
    }

    /**
     * Clears the chan flag VOICEALL
     * @param chanFlags Chan flags
     * @return Resulting chanflag
     */
    public static Integer clearChanVoiceAll(Integer chanFlags) {
        return (chanFlags & ~CHFLAG_VOICEALL);
    }

    /**
     * Clears the chan flag WELCOME
     * @param chanFlags Chan flags
     * @return Resulting chanflag
     */
    public static Integer clearChanWelcome(Integer chanFlags) {
        return (chanFlags & ~CHFLAG_WELCOME);
    }

    /**
     * Clears the chan flag SUSPENDED
     * @param chanFlags Chan flags
     * @return Resulting chanflag
     */
    public static Integer clearChanSuspended(Integer chanFlags) {
        return (chanFlags & ~CHFLAG_SUSPENDED);
    }

    /**
     * Clears the channel flags of input
     * @param chanFlags Channel flags input
     * @return Resulting chanflags
     */
    public static Integer clearChanFlags(Integer chanFlags) {
        return (chanFlags & ~CHFLAG_ALL);
    }

    /**
     * Fetches the channel flag int
     * @param character Channel flag character
     * @return Channel flag int
     */
    public static Integer getChanFlagInt(String character) throws Exception {
        try {
            return chanFlagCharMap.get(character);
        }
        catch (Exception e) { throw new Exception("The channel flag does not exists."); }
    }
    
    /**
     * Fetches the chanlev flag character
     * @param chanFlag Chanlev flag int
     * @return Chanlev flag character
     */
    public static String getChanFlagChar(Integer chanFlag) throws Exception {
        try {
            return chanFlagCharRevMap.get(chanFlag);
        }
        catch (Exception e) { throw new Exception("The channel flag does not exists."); }
    }

    /**
     * Strips the unknown flags from the provided list
     * @param chanFlags chan flags
     * @return stripped chan flags
     */
    public static Integer stripUnknownChanFlags(Integer chanFlags) {
        return (chanFlags & CHFLAGS_ALLOWED);
    }

    /**
     * Keeps requested chanflags of chanmaster control flags
     * @param chanFlags
     * @return stripped flags
     */
    public static Integer keepChanMasterConFlags(Integer chanFlags) {
        return (chanFlags & CHFLAG_MASTERCONTROL);
    }

    /**
     * Keeps requested chanflags of chanowner-control flags
     * @param chanFlags
     * @return stripped flags
     */
    public static Integer keepChanOwnerConFlags(Integer chanFlags) {
        return (chanFlags & CHFLAG_OWNERCONTROL);
    }    

    /**
     * Keeps requested chanflags of oper-control flags
     * @param chanFlags
     * @return stripped flags
     */
    public static Integer keepChanOperConFlags(Integer chanFlags) {
        return (chanFlags & CHFLAG_OPERCONTROL);
    }

    /**
     * Keeps requested chanflags of admin-control flags
     * @param chanFlags
     * @return stripped flags
     */
    public static Integer keepChanAdminConFlags(Integer chanFlags) {
        return (chanFlags & CHFLAG_ADMINCONTROL);
    }

    /**
     * Strips non-public flags
     * @param chanFlags
     * @return stripped flags
     */
    public static Integer stripChanNonPublicFlags(Integer chanFlags) {
        return (chanFlags & CHFLAGS_PUBLIC);
    }


    /*
     * Userflags-related methods
     * =========================
     */

    /**
     * Returns the public user flags list
     * @return Public user flags list
     */
    public static Integer getPublicUserFlags() {
        return UFLAGS_PUBLIC;
    }

    /**
     * Returns the default flags for new user accounts
     * @return default user flags
     */
    public static Integer getDefaultUserFlags() {
        return UFLAGS_NEW_ACCOUNT;
    }

    /**
     * Strips the unknown flags from the provided list
     * @param userFlags user flags
     * @return stripped user flags
     */
    public static Integer stripUnknownUserFlags(Integer userFlags) {
        return (userFlags & UFLAGS_ALLOWED);
    }

    /**
     * Returns if the requested userflags contains user-control flags
     * @param userFlags
     * @return True or False
     */
    public static Boolean containsUserUserConFlags(Integer userFlags) {
        if ( (userFlags & UFLAGS_USERCON) == 0) {
            return false;
        }
        else return true;
    }

    /**
     * Returns if the requested userflags contains oper-control flags
     * @param userFlags
     * @return True or False
     */
    public static Boolean containsUserOperConFlags(Integer userFlags) {
        if ( (userFlags & UFLAGS_OPERCON) == 0) {
            return false;
        }
        else return true;
    }

    /**
     * Returns if the requested userflags contains admin-control flags
     * @param userFlags
     * @return True or False
     */
    public static Boolean containsUserAdminConFlags(Integer userFlags) {
        if ( (userFlags & UFLAGS_ADMINCON) == 0) {
            return false;
        }
        else return true;
    }

    /**
     * Strips requested userflags of non user-control flags
     * @param userFlags
     * @return stripped flags
     */
    public static Integer stripUserUserConFlags(Integer userFlags) {
        return (userFlags & UFLAGS_USERCON);
    }

    /**
     * Strips requested userflags of non oper-control flags
     * @param userFlags
     * @return stripped flags
     */
    public static Integer stripUserOperConFlags(Integer userFlags) {
        return (userFlags & UFLAGS_OPERCON);
    }

    /**
     * Strips requested userflags of non admin-control flags
     * @param userFlags
     * @return stripped flags
     */
    public static Integer stripUserAdminConFlags(Integer userFlags) {
        return (userFlags & UFLAGS_ADMINCON);
    }

    /**
     * Returns if the user has user flag ADMIN
     * @param userFlags User flags
     * @return True or False
     */
    public static Boolean isUserAdmin(Integer userFlags) {
        if ( (userFlags & UFLAG_ADMIN) == 0) {
            return false;
        }
        else return true;
    }

    /**
     * Returns if the user has user flag GLINE
     * @param userFlags User flags
     * @return True or False
     */
    public static Boolean isUserGline(Integer userFlags) {
        if ( (userFlags & UFLAG_GLINE) == 0) {
            return false;
        }
        else return true;
    }

    /**
     * Returns if the user has user flag NOAUTHLIMIT
     * @param userFlags User flags
     * @return True or False
     */
    public static Boolean isUserNoAuthLimit(Integer userFlags) {
        if ( (userFlags & UFLAG_NOAUTHLIMIT) == 0) {
            return false;
        }
        else return true;
    }

    /**
     * Returns if the user has user flag OPER
     * @param userFlags User flags
     * @return True or False
     */
    public static Boolean isUserOper(Integer userFlags) {
        if ( (userFlags & UFLAG_OPER) == 0) {
            return false;
        }
        else return true;
    }

    /**
     * Returns if the user has user flag STAFF
     * @param userFlags User flags
     * @return True or False
     */
    public static Boolean isUserStaff(Integer userFlags) {
        if ( (userFlags & UFLAG_STAFF) == 0) {
            return false;
        }
        else return true;
    }

    /**
     * Returns if the user has user flag PROTECT
     * @param userFlags User flags
     * @return True or False
     */
    public static Boolean isUserProtect(Integer userFlags) {
        if ( (userFlags & UFLAG_PROTECT) == 0) {
            return false;
        }
        else return true;
    }

    /**
     * Returns if the user has user flag WELCOME
     * @param userFlags User flags
     * @return True or False
     */
    public static Boolean isUserWelcome(Integer userFlags) {
        if ( (userFlags & UFLAG_WELCOME) == 0) {
            return false;
        }
        else return true;
    }

    /**
     * Returns if the user has user flag SUSPENDED
     * @param userFlags User flags
     * @return True or False
     */
    public static Boolean isUserSuspended(Integer userFlags) {
        if ( (userFlags & UFLAG_SUSPENDED) == 0) {
            return false;
        }
        else return true;
    }

    /**
     * Returns if the user has NOAUTOVHOST flag
     * @param userFlags use flags
     * @return true or false
     */
    public static Boolean isUserAutoVhost(Integer userFlags) {
        if ( (userFlags & UFLAG_AUTOVHOST) == 0) {
            return false;
        }
        else return true; 
    }

    /**
     * Returns if the user has staff privilege
     * @param userFlags User flags
     * @return True or False
     */
    public static Boolean hasUserStaffPriv(Integer userFlags) {
        if ((userFlags & UFLAG_STAFF_PRIV) == 0) {
            return false;
        }
        else return true;
    }

    /**
     * Returns if the user has oper privilege
     * @param userFlags User flags
     * @return True or False
     */
    public static Boolean hasUserOperPriv(Integer userFlags) {
        if ((userFlags & UFLAG_OPER_PRIV) == 0) {
            return false;
        }
        else return true;
    }

    /**
     * Returns if the user has admin privilege
     * @param userFlags User flags
     * @return True or False
     */
    public static Boolean hasUserAdminPriv(Integer userFlags) {
        if ((userFlags & UFLAG_ADMIN_PRIV) == 0) {
            return false;
        }
        else return true;
    }

    /**
     * Sets the user flag GLINE
     * @param userFlags User flags
     * @return Resulting user flags
     */
    public static Integer setUserGline(Integer userFlags) {
        return (userFlags | UFLAG_GLINE);
    }

    /**
     * Sets the user flag SUSPENDED
     * @param userFlags User flags
     * @return Resulting user flags
     */
    public static Integer setUserSuspended(Integer userFlags) {
        return (userFlags | UFLAG_SUSPENDED);
    }

    /**
     * Sets the user flag OPER
     * @param userFlags User flags
     * @return Resulting user flags
     */
    public static Integer setUserOper(Integer userFlags) {
        return (userFlags | UFLAG_OPER);
    }

    /**
     * Sets the user flag PROTECT
     * @param userFlags User flags
     * @return Resulting user flags
     */
    public static Integer setUserProtect(Integer userFlags) {
        return (userFlags | UFLAG_PROTECT);
    }

    /**
     * Sets the user flag ADMIN
     * @param userFlags User flags
     * @return Resulting user flags
     */
    public static Integer setUserAdmin(Integer userFlags) {
        return (userFlags | UFLAG_ADMIN);
    }

    /**
     * Sets the user flag NOAUTHLIMIT
     * @param userFlags User flags
     * @return Resulting user flags
     */
    public static Integer setUserNoAuthLimit(Integer userFlags) {
        return (userFlags | UFLAG_NOAUTHLIMIT);
    }

    /**
     * Sets the user flag NOAUTOVHOST
     * @param userFlags user flags
     * @return Resulting user flags
     */
    public static Integer setUserAutoVhost(Integer userFlags) {
        return (userFlags | UFLAG_AUTOVHOST); 
    }

    /**
     * Clears the user flag GLINE
     * @param userFlags User flags
     * @return Resulting user flags
     */
    public static Integer clearUserGline(Integer userFlags) {
        return (userFlags & ~UFLAG_GLINE);
    }

    /**
     * Clears the user flag SUSPEND
     * @param userFlags User flags
     * @return Resulting user flags
     */
    public static Integer clearUserSuspended(Integer userFlags) {
        return (userFlags & ~UFLAG_SUSPENDED);
    }

    /**
     * Clears the user flag OPER
     * @param userFlags User flags
     * @return Resulting user flags
     */
    public static Integer clearUserOper(Integer userFlags) {
        return (userFlags & ~UFLAG_OPER);
    }

    /**
     * Clears the user flag PROTECT
     * @param userFlags User flags
     * @return Resulting user flags
     */
    public static Integer clearUserProtect(Integer userFlags) {
        return (userFlags & ~UFLAG_ADMIN);
    }

    /**
     * Clears the user flag ADMIN
     * @param userFlags User flags
     * @return Resulting user flags
     */
    public static Integer clearUserAdmin(Integer userFlags) {
        return (userFlags & ~UFLAG_ADMIN);
    }

    /**
     * Clears the user flag NOAUTHLIMIT
     * @param userFlags User flags
     * @return Resulting user flags
     */
    public static Integer clearUserNoAuthLimit(Integer userFlags) {
        return (userFlags & ~UFLAG_NOAUTHLIMIT);
    }

    /**
     * Clears the user flag NOAUTOVHOST
     * @param userFlags user flags
     * @return Resulting user flags
     */
    public static Integer clearUserAutoVhost(Integer userFlags) {
        return (userFlags & ~UFLAG_AUTOVHOST); 
    }

    /**
     * Clears the user flags
     * @param userFlags User flags
     * @return Cleared user flags
     */
    public static Integer clearUserFlags(Integer userFlags) {
        return (userFlags & ~UFLAG_ALL);
    }    

    /**
     * Fetches the user flag int
     * @param character User flag character
     * @return User flag int
     */
    public static Integer getUserFlagInt(String character) {
        try {
            return userFlagCharMap.get(character);
        }
        catch (Exception e) { return 0; }
    }
    

    /*
     * General methods
     * ===============
     */

    /**
     * Fetches the user flag character
     * @param userFlag User flag int
     * @return User flag character
     */
    public static String getUserFlagChar(Integer userFlag) {
        try {
            return userFlagCharRevMap.get(userFlag);
        }
        catch (Exception e) { return ""; }
    }

    /**
     * Converts a flags integer value to their textual format
     * @param type Type of flags
     * @param flagInt Flags in integer format
     * @return Flags in textual format
     */
    public static String flagsIntToChars(String type, Integer flagInt) {
        flagText = "";
        Map<Integer, String> map;

        switch (type) {
            case "chanlev":
                map = chanlevFlagCharRevMap;
                break;
            case "chanflags":
                map = chanFlagCharRevMap;
                break;
            case "userflags":
                map = userFlagCharRevMap;
                break;
            default:
                return "";
        }

        map.forEach( (flagConst, flagChar) -> {
            if ((flagConst & flagInt) != 0) {
                flagText += flagChar;
            }
        });
        return sortString(flagText);
    }

    /**
     * Converts a flags text to their integer equivalent
     * @param type Type of flags
     * @param flagChars Flags in textual format
     * @return Flags in integer format
     */
    public static Integer flagsCharsToInt(String type, String flagChars) {
        flagInt = 0;
        Map<String, Integer> map;

        switch (type) {
            case "chanlev":
                map = chanlevFlagCharMap;
                break;
            case "chanflags":
                map = chanFlagCharMap;
                break;
            case "userflags":
                map = userFlagCharMap;
                break;
            default:
                return 0;
        }
        try {
            for (int i=0; i < flagChars.length(); i++) {
                flagInt += map.get(String.valueOf(flagChars.charAt(i)));
            }
            return flagInt;
        }
        catch (Exception e) { return 0; }
    }

    /**
     * Sort a string alphabetically
     * @param str Input string
     * @return Sorted string
     */
    private static String sortString(String str) {
        char charArray[] = str.toCharArray();
        Arrays.sort(charArray);
        return new String(charArray);
    }
        
    /**
     * Removes duplicate chars in a string
     * @param s Input string
     * @return String with unique chars
     */
    private static String deDupString(String s) {
        char[] chars = s.toCharArray();
        Set<Character> charSet = new LinkedHashSet<Character>();
        for (char c : chars) {
            charSet.add(c);
        }
        StringBuilder sb = new StringBuilder();
        for (Character character : charSet) {
            sb.append(character);
        }
        return sb.toString();
    }

    /**
     * Applies the modification flags (+/-) to the input flags depending on flags type.
     * @param type Flags type (chanlev, chanflags, userflags)
     * @param flagsInput Input flags (numeric)
     * @param flagsMod Input change Integer flags (+xyz / -xyz) has a HashMap with a "+" key containing +flags and a "-" key containing -flags
     * @return
     */
    public static Integer applyFlagsFromInt(String type, Integer flagsInput, HashMap<String, Integer> flagsMod) {
        Integer flagsModP = flagsMod.get("+");
        Integer flagsModM = flagsMod.get("-");

        Integer flagsNew = flagsInput;
        
        // Remove common values between + and - (+amno-antv = +m-tv) 
        flagsModP ^= (flagsModP & flagsModM);
        flagsModM ^= (flagsModP & flagsModM);

        switch (type) {
            case "chanlev":
                // in flagsP, +od => +d; +vu => +u
                if ( (flagsModP & CLFLAG_OP) > 0 &&  (flagsModP & CLFLAG_DENYOP) > 0) flagsModP &= ~CLFLAG_OP;
                if ( (flagsModP & CLFLAG_VOICE) > 0 &&  (flagsModP & CLFLAG_DENYVOICE) > 0) flagsModP &= ~CLFLAG_VOICE;

                // if userFlags contains +ov and flagsP contains +du, then -ov+du is applied 
                if ( (flagsModP & CLFLAG_DENYOP) > 0 &&  (flagsNew & CLFLAG_OP) > 0) flagsNew &= ~CLFLAG_DENYOP;
                if ( (flagsModP & CLFLAG_DENYVOICE) > 0 &&  (flagsNew & CLFLAG_VOICE) > 0) flagsNew &= ~CLFLAG_DENYVOICE;

                // if userFlags contains +du and flagsP contains +ov, then -du+ov is applied
                if ( (flagsModP & CLFLAG_OP) > 0 &&  (flagsNew & CLFLAG_DENYOP) > 0) flagsNew &= ~CLFLAG_OP;
                if ( (flagsModP & CLFLAG_VOICE) > 0 &&  (flagsNew & CLFLAG_DENYVOICE) > 0) flagsNew &= ~CLFLAG_VOICE;

                break;

            case "chanflags":
                // Prevent readonly flags to being set/cleared
                flagsModP &= ~CHFLAGS_READONLY;
                flagsModM &= ~CHFLAGS_READONLY;
                break;

            case "userflags":
                // Prevent readonly flags to being set/cleared
                flagsModP &= ~UFLAGS_READONLY;
                flagsModM &= ~UFLAGS_READONLY;
                break;
        }

        flagsNew &=  ~flagsModM;
        flagsNew |=   flagsModP;


        return flagsNew;
    }

    /**
     * Applies the modification flags (+/-) to the input flags depending on flags type.
     * @param type Flags type (chanlev, chanflags, userflags)
     * @param flagsInput Input flags (textual)
     * @param flagsMod Input change flags (+xyz / -xyz)
     * @return
     */
    public static Integer applyFlagsFromStr(String type, Integer flagsInput, HashMap<String, String> flagsMod) {
        Integer flagsModP = flagsCharsToInt(type, flagsMod.get("+"));
        Integer flagsModM = flagsCharsToInt(type, flagsMod.get("-"));

        HashMap<String, Integer> flagsMP = new HashMap<String, Integer>();
        flagsMP.put("+", flagsModP);
        flagsMP.put("-", flagsModM);

        return applyFlagsFromInt(type, flagsInput, flagsMP);
    }

    /**
     * Parse textual flags (in the format +abc-de+f-gh+i...), removes dupes between + and - and
     * returns a HM<String, String> containing the list of sorted/deduplicated plus and minus flags.
     * @param flagsIn Input flags modes
     * @return Separated flags modes
     */
    public static HashMap<String, String> parseFlags(String flagsIn) { 
        HashMap<String, String> flagsTemp = new HashMap<String, String>();
        flagsTemp.put("+", "");
        flagsTemp.put("-", "");
        
        boolean plusMode = false;
        for(int i=0; i < flagsIn.length(); i++) {
            if (flagsIn.charAt(i) == '+') {
                plusMode = true;
            }
            else if (flagsIn.charAt(i) == '-') {
                plusMode = false;
            }
            else {
                if (plusMode == true) {
                    flagsTemp.replace("+", flagsTemp.get("+") + String.valueOf(flagsIn.charAt(i)));
                }
                else {
                    flagsTemp.replace("-", flagsTemp.get("-") + String.valueOf(flagsIn.charAt(i)));
                }
            }
        }

        flagsTemp.replace("+", sortString(deDupString(flagsTemp.get("+"))));
        flagsTemp.replace("-", sortString(deDupString(flagsTemp.get("-"))));

        return flagsTemp;
    }
}

