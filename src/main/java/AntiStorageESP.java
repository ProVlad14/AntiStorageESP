import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
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
                            // Read the block position directly from the packet's internal byte stream buffer
                            event.getByteBuf().markReaderIndex();
                            long encodedPos = event.getByteBuf().readLong();
                            event.getByteBuf().resetReaderIndex();

                            // Decode standard Minecraft packed block coordinates
                            int x = (int) (encodedPos >> 38);
                            int y = (int) ((encodedPos << 52) >> 52);
                            int z = (int) ((encodedPos << 26) >> 38);
                            
                            Location playerLoc = player.getLocation();
                            double distSq = playerLoc.distanceSquared(new Location(player.getWorld(), x, y, z));
                            
                            if (distSq > maxEspDistanceSquared) {
                                event.setCancelled(true);
                            }
                        } catch (Exception ignored) {
                            // Safe fallback if buffer state manipulation errors out
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
