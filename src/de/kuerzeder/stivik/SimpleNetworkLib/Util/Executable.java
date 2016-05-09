package de.kuerzeder.stivik.SimpleNetworkLib.Util;

/**
 * SimpleNetworkLib:
 * @author Stefan Kürzeder
 * created on 09.05.2016 in BY, Germany
 */
public interface Executable {

    /**
     * Put the lines to be executed inside the run.<br>
     * This method is called by the Client or Server if the identifier<br>
     * of an Callback fits to the identifier this Executable is registered with.
     * (From: https://github.com/DeBukkIt/SimpleServerClient/blob/master/com/blogspot/debukkitsblog/Util/Executable.java)
     * @param o the Object to work with
     */
    public abstract void run(Object o);
}
