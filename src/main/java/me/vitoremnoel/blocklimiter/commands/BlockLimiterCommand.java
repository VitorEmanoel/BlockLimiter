package me.vitoremnoel.blocklimiter.commands;

import me.vitoremnoel.blocklimiter.BlockLimiter;
import ninja.leaping.configurate.ConfigurationNode;
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
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.Optional;

public class BlockLimiterCommand implements CommandExecutor {

    private BlockLimiter blockLimiter = BlockLimiter.getPlugin();

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Text text;
        if (!(src instanceof Player)) {
            text = blockLimiter.getMessage("only_player");
            src.sendMessage(text);
            return CommandResult.success();
        }
        Player player = (Player) src;
        Optional<ItemStack> opItem = player.getItemInHand(HandTypes.MAIN_HAND);
        if(!player.hasPermission(blockLimiter.getPermission())){
            text = blockLimiter.getMessage("no_has_permission");
            player.sendMessage(text);
            return CommandResult.success();
        }
        if (!opItem.isPresent()) {
            text = blockLimiter.getMessage("empty_mainhand");
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

        } else{
            block = opItem.get().getItem().getId();
        }
        int uses = args.<Integer>getOne("uses").get();
        String permission = args.<String>getOne("permission").get();
        if(blockLimiter.existsBlock(block)){
            text = blockLimiter.getMessage("has_limited");
            player.sendMessage(text);
            return CommandResult.success();
        }
        if(blockLimiter.existsPermission(permission)){
            text = blockLimiter.getMessage("has_permission");
            player.sendMessage(text);
            return CommandResult.success();
        }

        text = TextSerializers.FORMATTING_CODE.deserialize(blockLimiter.getMessage("limite_sucess").toPlain()
                .replace("$bloco$", block));
        player.sendMessage(text);
        player.sendMessage(Text.of("Permissão para o bloco: " + permission));
        blockLimiter.limite(block, permission, Integer.parseInt(Integer.toString(uses)));

        return CommandResult.success();
    }

}
