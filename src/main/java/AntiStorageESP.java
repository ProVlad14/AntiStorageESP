import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.impl.packetsend.PacketPlaySendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockEntityData;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class AntiStorageESP extends JavaPlugin {
    private final double maxEspDistanceSquared = 16.0 * 16.0; 

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        PacketEvents.getAPI().init();
        PacketEvents.getAPI().getEventManager().registerListener(
            new SimplePacketListenerAbstract(PacketListenerPriority.NORMAL) {
                @Override
                public void onPacketPlaySend(PacketPlaySendEvent event) {
                    Player player = (Player) event.getPlayer();
                    if (player == null) return;

                    if (event.getPacketType() == PacketType.Play.Server.BLOCK_ENTITY_DATA) {
                        WrapperPlayServerBlockEntityData packet = new WrapperPlayServerBlockEntityData(event);
                        com.github.retrooper.packetevents.util.Vector3i vec = packet.getBlockPosition();
                        
                        Location playerLoc = player.getLocation();
                        double distSq = playerLoc.distanceSquared(new Location(player.getWorld(), vec.getX(), vec.getY(), vec.getZ()));
                        
                        if (distSq > maxEspDistanceSquared) {
                            event.setCancelled(true);
                        }
                    }
                }
            }
        );
    }

    @Override
    public void onDisable() {
        PacketEvents.getAPI().terminate();
    }
}
