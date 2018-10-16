package me.vitoremnoel.blocklimiter.listeners;

import me.vitoremnoel.blocklimiter.BlockLimiter;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.text.Text;

import java.util.Optional;

public class MainListeners{

    private BlockLimiter blockLimiter = BlockLimiter.getPlugin();

    @Listener
    public void placeBlockEvent(ChangeBlockEvent.Place e, @First Player player){
        BlockState block = e.getTransactions().get(0).getFinal().getState();
        String blockstring = "";
        if(block.supports(Keys.ITEM_BLOCKSTATE)){
            Optional<BlockState> blockstate = block.get(Keys.ITEM_BLOCKSTATE);
            if(blockstate.isPresent()){
                blockstring = blockstate.get().getId();
            }
        }else{
            blockstring = block.getId();
        }
        if(!blockLimiter.isLimited(blockstring)){
            return;
        }
        if(!blockLimiter.canPlace(player, blockstring)) {
            player.sendMessage(blockLimiter.getMessage("no_place"));
            e.setCancelled(true);
            return;
        }
        BlockSnapshot bck = e.getTransactions().get(0).getOriginal();
        String coords = bck.getWorldUniqueId() + ";" + bck.getPosition().getX() + ";" + bck.getPosition().getY() + ";" + bck.getPosition().getZ();
        String permission = blockLimiter.getBlockPermission(blockstring) + ".limited";
        String permission2 = permission.replace(".limited", "unlimited");
        player.sendMessage(Text.of("Eu tenho a permissão?" + player.hasPermission(permission)));
        if(player.hasPermission(permission2)){
            player.sendMessage(blockLimiter.getMessage("unlimited_place"));
        }
        if(player.hasPermission(permission)){
            blockLimiter.placed(player.getUniqueId(), blockstring, coords);
            Text text = Text.of(blockLimiter.getMessage("place_block").toPlain().replace("$hasBlock$", "" + (blockLimiter.getMaxPlaces(blockstring) - blockLimiter.getPlayersBlock(player.getUniqueId(), blockstring))));
            player.sendMessage(text);
        }

    }

    @Listener
    public void breakBlockEvent(ChangeBlockEvent.Break e, @First Player player){
        BlockState block = e.getTransactions().get(0).getOriginal().getState();
        String blockstring = "";
        if(block.supports(Keys.ITEM_BLOCKSTATE)){
            Optional<BlockState> blockstate = block.get(Keys.ITEM_BLOCKSTATE);
            if(blockstate.isPresent()){
                blockstring = blockstate.get().getId();
            }
        }else{
            blockstring = block.getId();
        }
        if(!blockLimiter.isLimited(blockstring)){
            return;
        }
        BlockSnapshot bck = e.getTransactions().get(0).getOriginal();
        String coords = bck.getWorldUniqueId() + ";" + bck.getPosition().getX() + ";" + bck.getPosition().getY() + ";" + bck.getPosition().getZ();
        boolean teste = blockLimiter.hasPlaced(player.getUniqueId(), blockstring, coords);
        if(teste) {
            Text text = blockLimiter.getMessage("break_limited_block");
            blockLimiter.breakBlock(player.getUniqueId(), blockstring, coords);
            player.sendMessage(text);
        }else{
            if(!player.hasPermission(blockLimiter.getPermission())){
                e.setCancelled(true);
            }
        }
    }
}
