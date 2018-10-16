package me.vitoremnoel.blocklimiter.commands;

import me.vitoremnoel.blocklimiter.BlockLimiter;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;

import java.util.Optional;

public class RemoveLimiterCommand implements CommandExecutor {


    private BlockLimiter blockLimiter = BlockLimiter.getPlugin();

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Text text;
        if(!(src instanceof Player)){
            text = blockLimiter.getMessage("only_player");
            src.sendMessage(text);
            return CommandResult.success();
        }
        Player player = (Player)src;
        Optional<ItemStack> opItem = player.getItemInHand(HandTypes.MAIN_HAND);
        if(!player.hasPermission(blockLimiter.getPermission())){
            player.sendMessage(blockLimiter.getMessage("no_has_permission"));
            return CommandResult.success();
        }
        if(!opItem.isPresent()){
            text = blockLimiter.getMessage("empty_hand");
            player.sendMessage(text);
            return CommandResult.success();
        }
        if (!opItem.get().getItem().getBlock().isPresent()) {
            text = blockLimiter.getMessage("its_not_block");
            player.sendMessage(text);
            return CommandResult.success();
        }
        String block = "";
        if (opItem.get().supports(Keys.ITEM_BLOCKSTATE)) {
            Optional<BlockState> opBlock = opItem.get().get(Keys.ITEM_BLOCKSTATE);
            if(opBlock.isPresent()){
                block = opBlock.get().getId();
            }

        }else{
            block = opItem.get().getItem().getId();
        }

        if(!blockLimiter.existsBlock(block)){
            text = blockLimiter.getMessage("no_limited");
            player.sendMessage(text);
            return CommandResult.success();
        }
        text = Text.of(blockLimiter.getMessage("remove_limite").toPlain().replace("$bloco$", block));
        player.sendMessage(text);
        blockLimiter.removeLimite(block);
        return CommandResult.success();
    }
}
