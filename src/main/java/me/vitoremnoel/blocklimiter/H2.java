package me.vitoremnoel.blocklimiter;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class H2 {

    private String database;
    private String user;
    private String password;
    private Logger logger;
    private BlockLimiter blockLimiter;

    private Connection connection;

    public H2(String database, String user, String password){
        this.database = database;
        this.user = user;
        this.password = password;
        this.blockLimiter = BlockLimiter.getPlugin();
        this.logger = blockLimiter.getLogger();
    }


    private void loadDriver(){
        try{
            Class.forName("org.h2.Driver");
        }catch(ClassNotFoundException e){
            logger.error("==================================");
            logger.error("Não foi possivel carregar o driver");
            logger.error("==================================");
        }
    }

    public void open(){
        logger.info("Abrindo conexão com o banco de dados.");
        String url = "jdbc:h2:" + "~/" + this.database;
        try {
            loadDriver();
            connection = DriverManager.getConnection(url, user, password);
            if(connection == null){
                logger.error("===================================================");
                logger.error("Não foi possivel se conectar com o banco de dados.");
                logger.error("Desativando servidor");
                logger.error("===================================================");
                Sponge.getServer().shutdown();
            }else{
                logger.info("Conexão realizada com sucesso!");
            }
        }catch(SQLException e2){
            logger.error("===============================================");
            logger.error("Ocorreu erro ao conectar com o banco de dados");
            logger.error("verifique a configuração e tente novamente.");
            logger.error("===============================================");
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void close(){
        try{
            logger.info("Fechando conexão com o banco de dados");
            connection.close();
            if(connection.isClosed()){
                logger.info("Conexão fechada com sucesso!");
            }else{
                logger.warn("A conexão com bancos de dados não foi fechada");
            }
        }catch(SQLException e){
            e.printStackTrace();
        }

    }
}
