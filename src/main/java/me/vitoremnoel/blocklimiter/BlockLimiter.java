package me.vitoremnoel.blocklimiter;

import com.google.inject.Inject;
import me.vitoremnoel.blocklimiter.commands.BlockLimiterCommand;
import me.vitoremnoel.blocklimiter.commands.RemoveLimiterCommand;
import me.vitoremnoel.blocklimiter.configuration.MainConfig;
import me.vitoremnoel.blocklimiter.listeners.MainListeners;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.*;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.sql.SqlService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Plugin(id="blocklimiter",
        name="BlockLimiter",
        version="1.0",
        description = "Este plugin e usado para limitar a quantidades de bloco por pessoa.",
        authors = "VitorEmanoel")
public class BlockLimiter {

    @Inject
    private Logger logger;

    @Inject
    @DefaultConfig(sharedRoot = false)
    private Path defaultConfig;

    @Inject
    @DefaultConfig(sharedRoot = false)
    private ConfigurationLoader<CommentedConfigurationNode> loader;

    private MainConfig mainConfig;
    private ConfigurationNode config;
    private SqlService sqlService;
    private Connection connection;
    private H2 h2;

    private static BlockLimiter blocklimiter;

    @Listener
    public void preInitEvent(GamePreInitializationEvent e){
        logger.info("Iniciando plugin!");
        blocklimiter = this;
        this.mainConfig = new MainConfig(defaultConfig, loader);
        mainConfig.setup();
        this.config = mainConfig.getConfig();
        h2 = new H2(
                config.getNode("BlockLimiter", "H2", "database").getString(),
                config.getNode("BlockLimiter", "H2", "user").getString(),
                config.getNode("BlockLimiter", "H2", "password").getString());
        h2.open();
        connection = h2.getConnection();
        registers();
        setupDatabase();
    }

    @Listener
    public void onEnable(GameStartedServerEvent e){
        logger.info("Plugin carregado com sucesso!");
    }

    @Listener
    public void onDisable(GameStoppingEvent e){
        h2.close();
    }

    private void registers(){

        CommandSpec limite = CommandSpec.builder()
                .description(Text.of("Esse comando é usado para limitar os blocos por pessoa"))
                .arguments(GenericArguments.string(Text.of("permission")),
                        GenericArguments.integer(Text.of("uses")))
                .executor(new BlockLimiterCommand())
                .permission(getPermission())
                .build();
        CommandSpec removeLimite = CommandSpec.builder()
                .description(Text.of("Esse comando remove o limite e um certo bloco"))
                .executor(new RemoveLimiterCommand())
                .permission(getPermission())
                .build();

        Sponge.getCommandManager().register(this, removeLimite, "removerlimite");
        Sponge.getCommandManager().register(this, limite, "limitarbloco");
        Sponge.getEventManager().registerListeners(getPlugin(), new MainListeners());
    }

    public static BlockLimiter getPlugin(){
        return blocklimiter;
    }

    public Logger getLogger(){
        return this.logger;
    }

    public String getPermission(){
        return config.getNode("BlockLimiter", "Geral", "permission").getString();
    }

    public Text getMessage(String key){
        Text text = TextSerializers.FORMATTING_CODE.deserialize(config.getNode("BlockLimiter", "Messages", key).getString());
        return text;
    }

    public void limite(String block, String permission, int uses){
        try{
            String insert = "insert into limitedblocks(`blockstring`, `permission`, `uses`) VALUES ('" + block + "', '" + permission + "', '" + uses + "');";
            PreparedStatement statement = connection.prepareStatement(insert);
            statement.execute();
            statement.close();
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    public void removeLimite(String block){
        try{
            String delete = "delete from limitedblocks where blockstring='" + block + "';";
            PreparedStatement statement = connection.prepareStatement(delete);
            connection.createStatement().executeUpdate("delete from usesblock where blockstring='" + block + "';");
            statement.executeUpdate();
            statement.close();
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    public List<String> getLimtedBlocks(){
        List<String> list = new ArrayList<>();
        try {
            ResultSet res = connection.createStatement().executeQuery("select * from limitedblocks;");
            while (res.next()) {
                list.add(res.getString("blockstring") + ";" + res.getString("permission"));
            }
            res.close();
        }catch(SQLException e){
            e.printStackTrace();
        }
        return list;
    }

    public boolean existsPermission(String permission){
        boolean retorno = false;
        try{
            PreparedStatement statement = connection.prepareStatement("select * from limitedblocks where permission='" + permission +"';");
            ResultSet res = statement.executeQuery();
            retorno = res.next();
            res.close();
        }catch(SQLException e){
            e.printStackTrace();
        }
        return retorno;
    }

    public int getPlayersBlock(UUID uuid, String blockstring){
        int blocos = 0;
        try{
            ResultSet res = connection.createStatement().executeQuery("select * from usesblock where Uniqueid='" + uuid + "' and blockstring='" + blockstring +"';");
            while (res.next()) {
                blocos += 1;
            }
            res.close();
        }catch(SQLException e){
            e.printStackTrace();
        }
        return blocos;
    }

    public int getMaxPlaces(String blockstring){
        int max = 0;
        try{
            ResultSet res = connection.createStatement().executeQuery("select uses from limitedblocks where blockstring='" + blockstring + "';");
            res.next();
            max = res.getInt("uses");
            res.close();
        }catch (SQLException e){
            e.printStackTrace();
        }
        return max;
    }

    public boolean existsBlock(String block){
        boolean retorno = false;
        try{
            PreparedStatement stat = connection.prepareStatement("select * from limitedblocks where blockstring='" + block + "'");
            ResultSet res = stat.executeQuery();
            retorno = res.next();
            res.close();
        }catch(SQLException e){
            e.printStackTrace();
        }
        return retorno;
    }

    private void setupDatabase(){
        try {
            if (config.getNode("BlockLimiter", "H2", "database").getString().equals("")) {
                logger.error("Você não definiu o database, vou criar o meu proprio!");
                connection.createStatement().execute("create database if not exists blocklimiter;");
                connection.createStatement().execute("use blocklimiter;");
            }
            connection.createStatement().execute("create table if not exists limitedblocks(ID INT auto_increment," +
                    "blockstring TEXT NOT NULL," +
                    "permission varchar(50) NOT NULL," +
                    "uses INT NOT NULL ,PRIMARY KEY(ID));");
            connection.createStatement().execute("CREATE TABLE if not exists usesblock(UniqueID varchar(50)," +
                    "blockstring text not null," +
                    "block_id INT," +
                    "coords text," +
                    "FOREIGN KEY (block_id) REFERENCES limitedblocks(ID));");
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    public boolean isLimited(String blockstring){
        boolean retorno = true;
        try{
            ResultSet res = connection.createStatement().executeQuery("select * from limitedblocks where blockstring='" + blockstring + "';");
            retorno = res.next();
            res.close();
        }catch(SQLException e){
            e.printStackTrace();
        }
        return retorno;
    }

    public String getBlockPermission(String blockstring){
        String permission = "";
        try{
            ResultSet res = connection.createStatement().executeQuery("select permission from limitedblocks where blockstring='" + blockstring + "';");
            res.next();
            permission = res.getString("permission");
            res.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        return permission;
    }

    public boolean canPlace(Player player, String blockstring) {
        boolean retorno = false;
        String permission = getBlockPermission(blockstring) + ".limited";
        String permission2 = permission.replace(".limited", ".unlimited");
        if(!player.hasPermission(permission) && !player.hasPermission(permission2)){
            return false;
        }
        UUID uuid = player.getUniqueId();
        int placeds = getPlayersBlock(player.getUniqueId(), blockstring);
        int canPlaced = getMaxPlaces(blockstring);
        if (placeds < canPlaced) {
            retorno = true;
        }
        return retorno;

    }

    public boolean hasPlaced(UUID uuid, String blockstring, String coords){
        boolean retorno = false;
        try{
            ResultSet res = connection.createStatement().executeQuery("select * from usesblock where UniqueID='" + uuid + "' and blockstring='" + blockstring + "' and coords='" + coords + "'");
            retorno = res.next();
            res.close();
        }catch(SQLException e){
            e.printStackTrace();
        }
        return retorno;
    }



    public void placed(UUID uuid, String blockstring, String coords){
        try{
            ResultSet res = connection.createStatement().executeQuery("select ID from limitedblocks where blockstring='" + blockstring + "';");
            res.next();
            int blockid = res.getInt("ID");
            connection.createStatement().executeUpdate("insert into usesblock(`UniqueID`, `blockstring`, `block_id`, `coords`) VALUES ('" + uuid + "', '" + blockstring + "', '" + blockid + "', '" + coords + "');");
            res.close();
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    public void breakBlock(UUID uuid, String blockstring, String coords){
        try{
            connection.createStatement().executeUpdate("delete from usesblock where UniqueID='" + uuid + "' and blockstring='" + blockstring + "' and coords='" + coords + "';");
        }catch(SQLException e){
            e.printStackTrace();
        }

    }
}
