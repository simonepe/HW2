package Common;

/**
 * Defines all messages that can be sent between client and server
 */
public enum Command {

    /**
     * A new game. A client sends such a message to make the server to start a new game.
     */
    NEWGAME,

    /**
     * Client is about to close, all server recourses related to the sending client shod be
     * released.
     */
    DISCONNECT,


    /**
     * No command was specified. This means the entire command line is interpreted as a guess in the game (single letter or entire word).
     */
    NO_COMMAND
}
