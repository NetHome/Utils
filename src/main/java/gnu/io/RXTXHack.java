package gnu.io;

/**
 * This class goes around a nasty bug in RXTX 2.1.7 which sometimes makes calls to the close()-method hang forever.
 */
public final class RXTXHack {

    private RXTXHack() {
    }

    public static void closeRxtxPort(RXTXPort port) {
        port.IOLocked = 0;
        port.close();
    }
}
