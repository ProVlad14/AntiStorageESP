import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
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
            new PacketListenerAbstract(PacketListenerPriority.NORMAL) {
                @Override
                public void onPacketSend(PacketSendEvent event) {
                    Player player = (Player) event.getPlayer();
                    if (player == null) return;

                    if (event.getPacketType() == PacketType.Play.Server.BLOCK_ENTITY_DATA) {
                        try {
                            // Correct wrapper instancing for PacketEvents 2.x
                            WrapperPlayServerBlockEntityData wrapper = new WrapperPlayServerBlockEntityData(event);
                            com.github.retrooper.packetevents.util.Vector3i vec = wrapper.getBlockPosition();
                            
                            if (vec != null) {
                                Location playerLoc = player.getLocation();
                                double distSq = playerLoc.distanceSquared(new Location(player.getWorld(), vec.getX(), vec.getY(), vec.getZ()));
                                
                                if (distSq > maxEspDistanceSquared) {
                                    event.setCancelled(true);
                                }
                            }
                        } catch (Exception ignored) {
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
