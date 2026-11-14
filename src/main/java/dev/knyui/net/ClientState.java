package dev.knyui.net;

import dev.knyui.data.KnyData;

/** Копия данных игрока на клиенте (обновляется пакетом SyncPacket). */
public final class ClientState {
    private ClientState() {}
    public static volatile KnyData data = new KnyData();
}
