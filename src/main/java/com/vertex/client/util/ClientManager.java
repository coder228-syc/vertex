package com.vertex.client.util;

public class ClientManager implements IMinecraft {
    public static String getKey(int keyCode) {
        return KeyMappings.keyMappings(keyCode);
    }
}
